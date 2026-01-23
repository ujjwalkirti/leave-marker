package repository

import (
	"github.com/leavemarker/backend-go/internal/enums"
	"github.com/leavemarker/backend-go/internal/models"
	"gorm.io/gorm"
)

// PaymentRepository handles payment data access
type PaymentRepository struct {
	db *gorm.DB
}

// NewPaymentRepository creates a new PaymentRepository
func NewPaymentRepository(db *gorm.DB) *PaymentRepository {
	return &PaymentRepository{db: db}
}

// Create creates a new payment
func (r *PaymentRepository) Create(payment *models.Payment) error {
	return r.db.Create(payment).Error
}

// FindByID finds a payment by ID
func (r *PaymentRepository) FindByID(id uint) (*models.Payment, error) {
	var payment models.Payment
	err := r.db.Preload("Plan").Where("id = ? AND deleted = false", id).First(&payment).Error
	if err != nil {
		return nil, err
	}
	return &payment, nil
}

// FindByTransactionID finds a payment by transaction ID
func (r *PaymentRepository) FindByTransactionID(transactionID string) (*models.Payment, error) {
	var payment models.Payment
	err := r.db.Preload("Plan").Where("transaction_id = ? AND deleted = false", transactionID).First(&payment).Error
	if err != nil {
		return nil, err
	}
	return &payment, nil
}

// FindByRazorpayOrderID finds a payment by Razorpay order ID
func (r *PaymentRepository) FindByRazorpayOrderID(orderID string) (*models.Payment, error) {
	var payment models.Payment
	err := r.db.Preload("Plan").Where("razorpay_order_id = ? AND deleted = false", orderID).First(&payment).Error
	if err != nil {
		return nil, err
	}
	return &payment, nil
}

// FindByRazorpayPaymentID finds a payment by Razorpay payment ID
func (r *PaymentRepository) FindByRazorpayPaymentID(paymentID string) (*models.Payment, error) {
	var payment models.Payment
	err := r.db.Preload("Plan").Where("razorpay_payment_id = ? AND deleted = false", paymentID).First(&payment).Error
	if err != nil {
		return nil, err
	}
	return &payment, nil
}

// FindByCompanyID finds all payments by company ID
func (r *PaymentRepository) FindByCompanyID(companyID uint) ([]models.Payment, error) {
	var payments []models.Payment
	err := r.db.Preload("Plan").Where("company_id = ? AND deleted = false", companyID).Order("created_at DESC").Find(&payments).Error
	return payments, err
}

// FindByCompanyIDAndStatus finds payments by company ID and status
func (r *PaymentRepository) FindByCompanyIDAndStatus(companyID uint, status enums.PaymentStatus) ([]models.Payment, error) {
	var payments []models.Payment
	err := r.db.Preload("Plan").Where("company_id = ? AND status = ? AND deleted = false", companyID, status).Order("created_at DESC").Find(&payments).Error
	return payments, err
}

// FindByIdempotencyKey finds a payment by idempotency key
func (r *PaymentRepository) FindByIdempotencyKey(key string) (*models.Payment, error) {
	var payment models.Payment
	err := r.db.Preload("Plan").Where("idempotency_key = ? AND deleted = false", key).First(&payment).Error
	if err != nil {
		return nil, err
	}
	return &payment, nil
}

// Update updates a payment
func (r *PaymentRepository) Update(payment *models.Payment) error {
	return r.db.Save(payment).Error
}

// Delete soft deletes a payment
func (r *PaymentRepository) Delete(id uint) error {
	return r.db.Model(&models.Payment{}).Where("id = ?", id).Update("deleted", true).Error
}
