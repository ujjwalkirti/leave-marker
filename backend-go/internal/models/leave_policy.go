package models

import "github.com/leavemarker/backend-go/internal/enums"

// LeavePolicy represents a leave policy entity
type LeavePolicy struct {
	BaseModel
	CompanyID          uint            `gorm:"not null;index;uniqueIndex:idx_company_leave_type" json:"companyId"`
	LeaveType          enums.LeaveType `gorm:"size:30;not null;uniqueIndex:idx_company_leave_type" json:"leaveType"`
	AnnualQuota        int             `gorm:"not null" json:"annualQuota"`
	MonthlyAccrual     float64         `gorm:"default:0.0" json:"monthlyAccrual"`
	CarryForward       bool            `gorm:"default:false" json:"carryForward"`
	MaxCarryForward    int             `gorm:"default:0" json:"maxCarryForward"`
	EncashmentAllowed  bool            `gorm:"default:false" json:"encashmentAllowed"`
	HalfDayAllowed     bool            `gorm:"default:true" json:"halfDayAllowed"`
	Active             bool            `gorm:"default:true" json:"active"`

	// Relationships
	Company Company `gorm:"foreignKey:CompanyID" json:"-"`
}

func (LeavePolicy) TableName() string {
	return "leave_policies"
}
