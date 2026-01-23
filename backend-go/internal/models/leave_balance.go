package models

import "github.com/leavemarker/backend-go/internal/enums"

// LeaveBalance represents a leave balance entity
type LeaveBalance struct {
	BaseModel
	EmployeeID     uint            `gorm:"not null;index;uniqueIndex:idx_emp_type_year" json:"employeeId"`
	LeaveType      enums.LeaveType `gorm:"size:30;not null;uniqueIndex:idx_emp_type_year" json:"leaveType"`
	Year           int             `gorm:"not null;uniqueIndex:idx_emp_type_year" json:"year"`
	TotalQuota     float64         `gorm:"default:0.0" json:"totalQuota"`
	Used           float64         `gorm:"default:0.0" json:"used"`
	Pending        float64         `gorm:"default:0.0" json:"pending"`
	Available      float64         `gorm:"default:0.0" json:"available"`
	CarriedForward float64         `gorm:"default:0.0" json:"carriedForward"`

	// Relationships
	Employee Employee `gorm:"foreignKey:EmployeeID" json:"-"`
}

func (LeaveBalance) TableName() string {
	return "leave_balances"
}
