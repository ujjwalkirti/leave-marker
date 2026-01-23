package dto

import (
	"time"

	"github.com/leavemarker/backend-go/internal/enums"
	"github.com/shopspring/decimal"
)

// PaymentInitiateRequest for initiating payment
type PaymentInitiateRequest struct {
	PlanID            uint               `json:"planId" binding:"required"`
	BillingCycle      enums.BillingCycle `json:"billingCycle" binding:"required"`
	NumberOfEmployees int                `json:"numberOfEmployees"`
	CouponCode        string             `json:"couponCode"`
}

// PaymentInitiateResponse for payment initiation response
type PaymentInitiateResponse struct {
	OrderID  string          `json:"orderId"`
	Amount   decimal.Decimal `json:"amount"`
	Currency string          `json:"currency"`
	KeyID    string          `json:"keyId"`
}

// PaymentVerifyRequest for verifying payment
type PaymentVerifyRequest struct {
	RazorpayPaymentID string `json:"razorpayPaymentId" binding:"required"`
	RazorpayOrderID   string `json:"razorpayOrderId" binding:"required"`
	RazorpaySignature string `json:"razorpaySignature" binding:"required"`
}

// PaymentResponse for payment response
type PaymentResponse struct {
	ID                uint                `json:"id"`
	TransactionID     string              `json:"transactionId"`
	PlanID            uint                `json:"planId"`
	PlanName          string              `json:"planName,omitempty"`
	SubscriptionID    *uint               `json:"subscriptionId"`
	PaymentType       enums.PaymentType   `json:"paymentType"`
	BillingCycle      enums.BillingCycle  `json:"billingCycle"`
	Amount            decimal.Decimal     `json:"amount"`
	TaxAmount         decimal.Decimal     `json:"taxAmount"`
	DiscountAmount    decimal.Decimal     `json:"discountAmount"`
	TotalAmount       decimal.Decimal     `json:"totalAmount"`
	Currency          string              `json:"currency"`
	Status            enums.PaymentStatus `json:"status"`
	PaymentMethod     *string             `json:"paymentMethod"`
	RazorpayOrderID   *string             `json:"razorpayOrderId"`
	RazorpayPaymentID *string             `json:"razorpayPaymentId"`
	PaidAt            *time.Time          `json:"paidAt"`
	PeriodStart       time.Time           `json:"periodStart"`
	PeriodEnd         time.Time           `json:"periodEnd"`
	CreatedAt         time.Time           `json:"createdAt"`
	UpdatedAt         time.Time           `json:"updatedAt"`
}

// ContactRequest for contact form
type ContactRequest struct {
	Name    string `json:"name" binding:"required,max=100"`
	Email   string `json:"email" binding:"required,email,max=100"`
	Subject string `json:"subject" binding:"required,max=200"`
	Message string `json:"message" binding:"required,max=2000"`
}
