package models

import (
	"github.com/leavemarker/backend-go/internal/enums"
	"github.com/shopspring/decimal"
)

// Plan represents a subscription plan entity
type Plan struct {
	BaseModel
	Name                        string              `gorm:"size:100;not null" json:"name"`
	Description                 string              `gorm:"size:500" json:"description"`
	Tier                        enums.PlanTier      `gorm:"size:20;not null" json:"tier"`
	PlanType                    enums.PlanType      `gorm:"size:20;default:FREE" json:"planType"`
	BillingCycle                enums.BillingCycle  `gorm:"size:20" json:"billingCycle"`
	MonthlyPrice                decimal.Decimal     `gorm:"type:decimal(10,2);default:0.00" json:"monthlyPrice"`
	YearlyPrice                 decimal.Decimal     `gorm:"type:decimal(10,2);default:0.00" json:"yearlyPrice"`
	MinEmployees                int                 `gorm:"default:1" json:"minEmployees"`
	MaxEmployees                int                 `gorm:"default:10" json:"maxEmployees"`
	MaxLeavePolicies            int                 `gorm:"default:1" json:"maxLeavePolicies"`
	MaxHolidays                 int                 `gorm:"default:6" json:"maxHolidays"`
	Active                      bool                `gorm:"default:true" json:"active"`

	// Feature flags
	AttendanceManagement        bool                `gorm:"default:false" json:"attendanceManagement"`
	ReportsDownload             bool                `gorm:"default:false" json:"reportsDownload"`
	MultipleLeavePolicies       bool                `gorm:"default:false" json:"multipleLeavePolicies"`
	UnlimitedHolidays           bool                `gorm:"default:false" json:"unlimitedHolidays"`
	AttendanceRateAnalytics     bool                `gorm:"default:false" json:"attendanceRateAnalytics"`
	AdvancedReports             bool                `gorm:"default:false" json:"advancedReports"`
	CustomLeaveTypes            bool                `gorm:"default:false" json:"customLeaveTypes"`
	APIAccess                   bool                `gorm:"default:false" json:"apiAccess"`
	PrioritySupport             bool                `gorm:"default:false" json:"prioritySupport"`

	// Pricing
	PricePerEmployee            decimal.Decimal     `gorm:"type:decimal(10,2);default:0.00" json:"pricePerEmployee"`
	ReportDownloadPriceUnder50  decimal.Decimal     `gorm:"type:decimal(10,2);default:0.00" json:"reportDownloadPriceUnder50"`
	ReportDownloadPrice50Plus   decimal.Decimal     `gorm:"type:decimal(10,2);default:0.00" json:"reportDownloadPrice50Plus"`

	// Relationships
	Subscriptions []Subscription `gorm:"foreignKey:PlanID" json:"-"`
	Payments      []Payment      `gorm:"foreignKey:PlanID" json:"-"`
}

func (Plan) TableName() string {
	return "plans"
}
