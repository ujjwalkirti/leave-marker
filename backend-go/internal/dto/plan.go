package dto

import (
	"time"

	"github.com/leavemarker/backend-go/internal/enums"
	"github.com/shopspring/decimal"
)

// PlanRequest for creating/updating plan
type PlanRequest struct {
	Name                       string             `json:"name" binding:"required,max=100"`
	Description                string             `json:"description" binding:"max=500"`
	Tier                       enums.PlanTier     `json:"tier" binding:"required"`
	PlanType                   enums.PlanType     `json:"planType"`
	BillingCycle               enums.BillingCycle `json:"billingCycle"`
	MonthlyPrice               decimal.Decimal    `json:"monthlyPrice"`
	YearlyPrice                decimal.Decimal    `json:"yearlyPrice"`
	MinEmployees               int                `json:"minEmployees"`
	MaxEmployees               int                `json:"maxEmployees"`
	MaxLeavePolicies           int                `json:"maxLeavePolicies"`
	MaxHolidays                int                `json:"maxHolidays"`
	Active                     bool               `json:"active"`
	AttendanceManagement       bool               `json:"attendanceManagement"`
	ReportsDownload            bool               `json:"reportsDownload"`
	MultipleLeavePolicies      bool               `json:"multipleLeavePolicies"`
	UnlimitedHolidays          bool               `json:"unlimitedHolidays"`
	AttendanceRateAnalytics    bool               `json:"attendanceRateAnalytics"`
	AdvancedReports            bool               `json:"advancedReports"`
	CustomLeaveTypes           bool               `json:"customLeaveTypes"`
	APIAccess                  bool               `json:"apiAccess"`
	PrioritySupport            bool               `json:"prioritySupport"`
	PricePerEmployee           decimal.Decimal    `json:"pricePerEmployee"`
	ReportDownloadPriceUnder50 decimal.Decimal    `json:"reportDownloadPriceUnder50"`
	ReportDownloadPrice50Plus  decimal.Decimal    `json:"reportDownloadPrice50Plus"`
}

// PlanResponse for plan response
type PlanResponse struct {
	ID                         uint               `json:"id"`
	Name                       string             `json:"name"`
	Description                string             `json:"description"`
	Tier                       enums.PlanTier     `json:"tier"`
	PlanType                   enums.PlanType     `json:"planType"`
	BillingCycle               enums.BillingCycle `json:"billingCycle"`
	MonthlyPrice               decimal.Decimal    `json:"monthlyPrice"`
	YearlyPrice                decimal.Decimal    `json:"yearlyPrice"`
	MinEmployees               int                `json:"minEmployees"`
	MaxEmployees               int                `json:"maxEmployees"`
	MaxLeavePolicies           int                `json:"maxLeavePolicies"`
	MaxHolidays                int                `json:"maxHolidays"`
	Active                     bool               `json:"active"`
	AttendanceManagement       bool               `json:"attendanceManagement"`
	ReportsDownload            bool               `json:"reportsDownload"`
	MultipleLeavePolicies      bool               `json:"multipleLeavePolicies"`
	UnlimitedHolidays          bool               `json:"unlimitedHolidays"`
	AttendanceRateAnalytics    bool               `json:"attendanceRateAnalytics"`
	AdvancedReports            bool               `json:"advancedReports"`
	CustomLeaveTypes           bool               `json:"customLeaveTypes"`
	APIAccess                  bool               `json:"apiAccess"`
	PrioritySupport            bool               `json:"prioritySupport"`
	PricePerEmployee           decimal.Decimal    `json:"pricePerEmployee"`
	ReportDownloadPriceUnder50 decimal.Decimal    `json:"reportDownloadPriceUnder50"`
	ReportDownloadPrice50Plus  decimal.Decimal    `json:"reportDownloadPrice50Plus"`
	CreatedAt                  time.Time          `json:"createdAt"`
	UpdatedAt                  time.Time          `json:"updatedAt"`
}
