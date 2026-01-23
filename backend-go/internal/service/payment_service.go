package service

import (
	"crypto/hmac"
	"crypto/sha256"
	"encoding/hex"
	"fmt"
	"time"

	"github.com/google/uuid"
	"github.com/leavemarker/backend-go/internal/config"
	"github.com/leavemarker/backend-go/internal/dto"
	"github.com/leavemarker/backend-go/internal/enums"
	"github.com/leavemarker/backend-go/internal/models"
	"github.com/leavemarker/backend-go/internal/repository"
	"github.com/leavemarker/backend-go/internal/utils"
	razorpay "github.com/razorpay/razorpay-go"
	"github.com/shopspring/decimal"
)

// PaymentService handles payment business logic
type PaymentService struct {
	paymentRepo      *repository.PaymentRepository
	subscriptionRepo *repository.SubscriptionRepository
	planRepo         *repository.PlanRepository
	razorpayClient   *razorpay.Client
	cfg              *config.Config
}

// NewPaymentService creates a new PaymentService
func NewPaymentService(
	paymentRepo *repository.PaymentRepository,
	subscriptionRepo *repository.SubscriptionRepository,
	planRepo *repository.PlanRepository,
	cfg *config.Config,
) *PaymentService {
	var client *razorpay.Client
	if cfg.Razorpay.KeyID != "" && cfg.Razorpay.KeySecret != "" {
		client = razorpay.NewClient(cfg.Razorpay.KeyID, cfg.Razorpay.KeySecret)
	}
	return &PaymentService{
		paymentRepo:      paymentRepo,
		subscriptionRepo: subscriptionRepo,
		planRepo:         planRepo,
		razorpayClient:   client,
		cfg:              cfg,
	}
}

// GetPayments gets all payments for a company
func (s *PaymentService) GetPayments(companyID uint) ([]dto.PaymentResponse, error) {
	payments, err := s.paymentRepo.FindByCompanyID(companyID)
	if err != nil {
		return nil, utils.NewInternalServerError("Failed to get payments")
	}

	responses := make([]dto.PaymentResponse, len(payments))
	for i, payment := range payments {
		responses[i] = *s.toPaymentResponse(&payment)
	}

	return responses, nil
}

// GetPayment gets a payment by ID
func (s *PaymentService) GetPayment(id, companyID uint) (*dto.PaymentResponse, error) {
	payment, err := s.paymentRepo.FindByID(id)
	if err != nil {
		return nil, utils.NewNotFoundError("Payment not found")
	}

	if payment.CompanyID != companyID {
		return nil, utils.NewForbiddenError("Access denied")
	}

	return s.toPaymentResponse(payment), nil
}

// InitiatePayment initiates a new payment
func (s *PaymentService) InitiatePayment(companyID uint, req dto.PaymentInitiateRequest, ipAddress, userAgent string) (*dto.PaymentInitiateResponse, error) {
	if s.razorpayClient == nil {
		return nil, utils.NewInternalServerError("Payment gateway not configured")
	}

	plan, err := s.planRepo.FindByID(req.PlanID)
	if err != nil {
		return nil, utils.NewNotFoundError("Plan not found")
	}

	// Calculate amount
	var amount decimal.Decimal
	if req.BillingCycle == enums.BillingCycleMonthly {
		amount = plan.MonthlyPrice
	} else {
		amount = plan.YearlyPrice
	}

	// Convert to paise (Razorpay uses smallest currency unit)
	amountInPaise := amount.Mul(decimal.NewFromInt(100)).IntPart()

	// Create Razorpay order
	orderData := map[string]interface{}{
		"amount":   amountInPaise,
		"currency": "INR",
		"receipt":  fmt.Sprintf("rcpt_%s", uuid.New().String()[:8]),
	}

	order, err := s.razorpayClient.Order.Create(orderData, nil)
	if err != nil {
		return nil, utils.NewInternalServerError("Failed to create payment order")
	}

	orderID := order["id"].(string)

	// Calculate period
	now := time.Now()
	var periodEnd time.Time
	if req.BillingCycle == enums.BillingCycleMonthly {
		periodEnd = now.AddDate(0, 1, 0)
	} else {
		periodEnd = now.AddDate(1, 0, 0)
	}

	// Create payment record
	transactionID := uuid.New().String()
	payment := &models.Payment{
		PlanID:          req.PlanID,
		CompanyID:       companyID,
		TransactionID:   transactionID,
		RazorpayOrderID: &orderID,
		PaymentType:     enums.PaymentTypeSubscription,
		BillingCycle:    req.BillingCycle,
		Amount:          amount,
		TotalAmount:     amount,
		Currency:        "INR",
		Status:          enums.PaymentStatusPending,
		InitiatedAt:     &now,
		PeriodStart:     now,
		PeriodEnd:       periodEnd,
		IPAddress:       &ipAddress,
		UserAgent:       &userAgent,
	}

	if err := s.paymentRepo.Create(payment); err != nil {
		return nil, utils.NewInternalServerError("Failed to create payment record")
	}

	return &dto.PaymentInitiateResponse{
		OrderID:  orderID,
		Amount:   amount,
		Currency: "INR",
		KeyID:    s.cfg.Razorpay.KeyID,
	}, nil
}

// VerifyPayment verifies and completes a payment
func (s *PaymentService) VerifyPayment(companyID uint, req dto.PaymentVerifyRequest) (*dto.PaymentResponse, error) {
	// Verify signature
	message := req.RazorpayOrderID + "|" + req.RazorpayPaymentID
	expectedSignature := s.generateSignature(message, s.cfg.Razorpay.KeySecret)

	if expectedSignature != req.RazorpaySignature {
		return nil, utils.NewBadRequestError("Invalid payment signature")
	}

	// Find payment by order ID
	payment, err := s.paymentRepo.FindByRazorpayOrderID(req.RazorpayOrderID)
	if err != nil {
		return nil, utils.NewNotFoundError("Payment not found")
	}

	if payment.CompanyID != companyID {
		return nil, utils.NewForbiddenError("Access denied")
	}

	// Update payment
	now := time.Now()
	payment.RazorpayPaymentID = &req.RazorpayPaymentID
	payment.RazorpaySignature = &req.RazorpaySignature
	payment.Status = enums.PaymentStatusSuccess
	payment.PaidAt = &now

	if err := s.paymentRepo.Update(payment); err != nil {
		return nil, utils.NewInternalServerError("Failed to update payment")
	}

	// Activate subscription
	subscription, err := s.subscriptionRepo.FindActiveByCompanyID(companyID)
	if err == nil && subscription != nil {
		subscription.IsPaid = true
		subscription.PlanID = payment.PlanID
		subscription.Amount = payment.Amount
		subscription.StartDate = payment.PeriodStart
		subscription.EndDate = payment.PeriodEnd
		subscription.CurrentPeriodStart = payment.PeriodStart
		subscription.CurrentPeriodEnd = payment.PeriodEnd
		_ = s.subscriptionRepo.Update(subscription)
	}

	return s.toPaymentResponse(payment), nil
}

// HandleWebhook handles Razorpay webhook
func (s *PaymentService) HandleWebhook(payload []byte, signature string) error {
	// Verify webhook signature
	expectedSignature := s.generateSignature(string(payload), s.cfg.Razorpay.WebhookSecret)
	if expectedSignature != signature {
		return utils.NewBadRequestError("Invalid webhook signature")
	}

	// Process webhook (simplified - in production, parse JSON and handle different events)
	return nil
}

// RetryPayment retries a failed payment
func (s *PaymentService) RetryPayment(id, companyID uint) (*dto.PaymentInitiateResponse, error) {
	payment, err := s.paymentRepo.FindByID(id)
	if err != nil {
		return nil, utils.NewNotFoundError("Payment not found")
	}

	if payment.CompanyID != companyID {
		return nil, utils.NewForbiddenError("Access denied")
	}

	if payment.Status != enums.PaymentStatusFailed {
		return nil, utils.NewBadRequestError("Only failed payments can be retried")
	}

	// Create new Razorpay order
	amountInPaise := payment.Amount.Mul(decimal.NewFromInt(100)).IntPart()
	orderData := map[string]interface{}{
		"amount":   amountInPaise,
		"currency": "INR",
		"receipt":  fmt.Sprintf("retry_%s", uuid.New().String()[:8]),
	}

	order, err := s.razorpayClient.Order.Create(orderData, nil)
	if err != nil {
		return nil, utils.NewInternalServerError("Failed to create payment order")
	}

	orderID := order["id"].(string)

	// Update payment record
	now := time.Now()
	payment.RazorpayOrderID = &orderID
	payment.Status = enums.PaymentStatusPending
	payment.RetryCount++
	payment.LastRetryAt = &now

	if err := s.paymentRepo.Update(payment); err != nil {
		return nil, utils.NewInternalServerError("Failed to update payment record")
	}

	return &dto.PaymentInitiateResponse{
		OrderID:  orderID,
		Amount:   payment.Amount,
		Currency: "INR",
		KeyID:    s.cfg.Razorpay.KeyID,
	}, nil
}

func (s *PaymentService) generateSignature(message, secret string) string {
	h := hmac.New(sha256.New, []byte(secret))
	h.Write([]byte(message))
	return hex.EncodeToString(h.Sum(nil))
}

func (s *PaymentService) toPaymentResponse(payment *models.Payment) *dto.PaymentResponse {
	response := &dto.PaymentResponse{
		ID:                payment.ID,
		TransactionID:     payment.TransactionID,
		PlanID:            payment.PlanID,
		SubscriptionID:    payment.SubscriptionID,
		PaymentType:       payment.PaymentType,
		BillingCycle:      payment.BillingCycle,
		Amount:            payment.Amount,
		TaxAmount:         payment.TaxAmount,
		DiscountAmount:    payment.DiscountAmount,
		TotalAmount:       payment.TotalAmount,
		Currency:          payment.Currency,
		Status:            payment.Status,
		PaymentMethod:     payment.PaymentMethod,
		RazorpayOrderID:   payment.RazorpayOrderID,
		RazorpayPaymentID: payment.RazorpayPaymentID,
		PaidAt:            payment.PaidAt,
		PeriodStart:       payment.PeriodStart,
		PeriodEnd:         payment.PeriodEnd,
		CreatedAt:         payment.CreatedAt,
		UpdatedAt:         payment.UpdatedAt,
	}

	if payment.Plan.ID != 0 {
		response.PlanName = payment.Plan.Name
	}

	return response
}
