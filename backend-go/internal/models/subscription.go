package models

import (
	"time"

	"github.com/leavemarker/backend-go/internal/enums"
	"github.com/shopspring/decimal"
)

// Subscription represents a subscription entity
type Subscription struct {
	BaseModel
	CompanyID              uint                     `gorm:"not null;index" json:"companyId"`
	PlanID                 uint                     `gorm:"not null;index" json:"planId"`
	Status                 enums.SubscriptionStatus `gorm:"size:20;default:ACTIVE" json:"status"`
	BillingCycle           enums.BillingCycle       `gorm:"size:20" json:"billingCycle"`
	StartDate              time.Time                `json:"startDate"`
	EndDate                time.Time                `json:"endDate"`
	CurrentPeriodStart     time.Time                `json:"currentPeriodStart"`
	CurrentPeriodEnd       time.Time                `json:"currentPeriodEnd"`
	Amount                 decimal.Decimal          `gorm:"type:decimal(10,2);default:0.00" json:"amount"`
	AutoRenew              bool                     `gorm:"default:true" json:"autoRenew"`
	IsPaid                 bool                     `gorm:"default:false" json:"isPaid"`
	HasReportDownloadAddon bool                     `gorm:"default:false" json:"hasReportDownloadAddon"`
	ReportDownloadAddonPrice decimal.Decimal        `gorm:"type:decimal(10,2);default:0.00" json:"reportDownloadAddonPrice"`
	CancellationReason     *string                  `gorm:"size:500" json:"cancellationReason"`
	CancelledAt            *time.Time               `json:"cancelledAt"`
	Notes                  *string                  `gorm:"size:500" json:"notes"`

	// Relationships
	Company  Company   `gorm:"foreignKey:CompanyID" json:"-"`
	Plan     Plan      `gorm:"foreignKey:PlanID" json:"-"`
	Payments []Payment `gorm:"foreignKey:SubscriptionID" json:"-"`
}

func (Subscription) TableName() string {
	return "subscriptions"
}
