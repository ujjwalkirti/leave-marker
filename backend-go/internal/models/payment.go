package models

import (
	"time"

	"github.com/leavemarker/backend-go/internal/enums"
	"github.com/shopspring/decimal"
)

// Payment represents a payment entity
type Payment struct {
	BaseModel
	SubscriptionID     *uint               `gorm:"index" json:"subscriptionId"`
	PlanID             uint                `gorm:"not null;index" json:"planId"`
	CompanyID          uint                `gorm:"not null;index" json:"companyId"`
	TransactionID      string              `gorm:"size:100;uniqueIndex;not null" json:"transactionId"`
	RazorpayOrderID    *string             `gorm:"size:100" json:"razorpayOrderId"`
	RazorpayPaymentID  *string             `gorm:"size:100" json:"razorpayPaymentId"`
	RazorpaySignature  *string             `gorm:"size:256" json:"razorpaySignature"`
	PaymentType        enums.PaymentType   `gorm:"size:20;not null" json:"paymentType"`
	BillingCycle       enums.BillingCycle  `gorm:"size:20" json:"billingCycle"`
	Amount             decimal.Decimal     `gorm:"type:decimal(10,2);not null" json:"amount"`
	TaxAmount          decimal.Decimal     `gorm:"type:decimal(10,2);default:0.00" json:"taxAmount"`
	DiscountAmount     decimal.Decimal     `gorm:"type:decimal(10,2);default:0.00" json:"discountAmount"`
	TotalAmount        decimal.Decimal     `gorm:"type:decimal(10,2);not null" json:"totalAmount"`
	Currency           string              `gorm:"size:10;default:INR" json:"currency"`
	Status             enums.PaymentStatus `gorm:"size:20;default:PENDING" json:"status"`
	PaymentMethod      *string             `gorm:"size:100" json:"paymentMethod"`

	// Timestamps
	InitiatedAt *time.Time `json:"initiatedAt"`
	PaidAt      *time.Time `json:"paidAt"`
	FailedAt    *time.Time `json:"failedAt"`
	RefundedAt  *time.Time `json:"refundedAt"`

	// Period Coverage
	PeriodStart time.Time `json:"periodStart"`
	PeriodEnd   time.Time `json:"periodEnd"`

	// Webhook Tracking
	WebhookStatus     *string    `gorm:"size:50" json:"webhookStatus"`
	WebhookReceivedAt *time.Time `json:"webhookReceivedAt"`
	WebhookPayload    *string    `gorm:"type:text" json:"webhookPayload"`

	// Audit
	IPAddress     *string `gorm:"size:50" json:"ipAddress"`
	UserAgent     *string `gorm:"size:500" json:"userAgent"`
	Metadata      *string `gorm:"type:text" json:"metadata"`
	FailureReason *string `gorm:"size:500" json:"failureReason"`
	RefundReason  *string `gorm:"size:500" json:"refundReason"`

	// Retry
	IdempotencyKey *string    `gorm:"size:100;uniqueIndex" json:"idempotencyKey"`
	RetryCount     int        `gorm:"default:0" json:"retryCount"`
	LastRetryAt    *time.Time `json:"lastRetryAt"`

	// Relationships
	Subscription *Subscription `gorm:"foreignKey:SubscriptionID" json:"-"`
	Plan         Plan          `gorm:"foreignKey:PlanID" json:"-"`
	Company      Company       `gorm:"foreignKey:CompanyID" json:"-"`
}

func (Payment) TableName() string {
	return "payments"
}
