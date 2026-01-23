package dto

import (
	"time"

	"github.com/leavemarker/backend-go/internal/enums"
	"github.com/shopspring/decimal"
)

// SubscriptionRequest for creating/updating subscription
type SubscriptionRequest struct {
	PlanID                 uint               `json:"planId" binding:"required"`
	BillingCycle           enums.BillingCycle `json:"billingCycle" binding:"required"`
	AutoRenew              bool               `json:"autoRenew"`
	HasReportDownloadAddon bool               `json:"hasReportDownloadAddon"`
}

// SubscriptionResponse for subscription response
type SubscriptionResponse struct {
	ID                       uint                     `json:"id"`
	PlanID                   uint                     `json:"planId"`
	PlanName                 string                   `json:"planName,omitempty"`
	Status                   enums.SubscriptionStatus `json:"status"`
	BillingCycle             enums.BillingCycle       `json:"billingCycle"`
	StartDate                time.Time                `json:"startDate"`
	EndDate                  time.Time                `json:"endDate"`
	CurrentPeriodStart       time.Time                `json:"currentPeriodStart"`
	CurrentPeriodEnd         time.Time                `json:"currentPeriodEnd"`
	Amount                   decimal.Decimal          `json:"amount"`
	AutoRenew                bool                     `json:"autoRenew"`
	IsPaid                   bool                     `json:"isPaid"`
	HasReportDownloadAddon   bool                     `json:"hasReportDownloadAddon"`
	ReportDownloadAddonPrice decimal.Decimal          `json:"reportDownloadAddonPrice"`
	CancellationReason       *string                  `json:"cancellationReason"`
	CancelledAt              *time.Time               `json:"cancelledAt"`
	Notes                    *string                  `json:"notes"`
	CreatedAt                time.Time                `json:"createdAt"`
	UpdatedAt                time.Time                `json:"updatedAt"`
}

// SubscriptionCancelRequest for cancelling subscription
type SubscriptionCancelRequest struct {
	Reason string `json:"reason" binding:"max=500"`
}

// SubscriptionFeatureResponse for subscription features
type SubscriptionFeatureResponse struct {
	MaxEmployees            int  `json:"maxEmployees"`
	MaxLeavePolicies        int  `json:"maxLeavePolicies"`
	MaxHolidays             int  `json:"maxHolidays"`
	AttendanceManagement    bool `json:"attendanceManagement"`
	ReportsDownload         bool `json:"reportsDownload"`
	MultipleLeavePolicies   bool `json:"multipleLeavePolicies"`
	UnlimitedHolidays       bool `json:"unlimitedHolidays"`
	AttendanceRateAnalytics bool `json:"attendanceRateAnalytics"`
	AdvancedReports         bool `json:"advancedReports"`
	CustomLeaveTypes        bool `json:"customLeaveTypes"`
	APIAccess               bool `json:"apiAccess"`
	PrioritySupport         bool `json:"prioritySupport"`
}
