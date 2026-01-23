package models

import (
	"time"

	"github.com/leavemarker/backend-go/internal/enums"
)

// LeaveApplication represents a leave application entity
type LeaveApplication struct {
	BaseModel
	EmployeeID          uint              `gorm:"not null;index" json:"employeeId"`
	LeaveType           enums.LeaveType   `gorm:"size:30;not null" json:"leaveType"`
	StartDate           time.Time         `gorm:"type:date;not null" json:"startDate"`
	EndDate             time.Time         `gorm:"type:date;not null" json:"endDate"`
	NumberOfDays        float64           `gorm:"not null" json:"numberOfDays"`
	IsHalfDay           bool              `gorm:"default:false" json:"isHalfDay"`
	Reason              string            `gorm:"size:1000" json:"reason"`
	AttachmentURL       *string           `gorm:"size:500" json:"attachmentUrl"`
	Status              enums.LeaveStatus `gorm:"size:20;default:PENDING" json:"status"`
	ApprovedByManagerID *uint             `gorm:"index" json:"approvedByManagerId"`
	ManagerApprovalDate *time.Time        `gorm:"type:date" json:"managerApprovalDate"`
	ApprovedByHRID      *uint             `gorm:"index" json:"approvedByHrId"`
	HRApprovalDate      *time.Time        `gorm:"type:date" json:"hrApprovalDate"`
	RejectionReason     *string           `gorm:"size:500" json:"rejectionReason"`
	RejectionDate       *time.Time        `gorm:"type:date" json:"rejectionDate"`
	RequiresHRApproval  bool              `gorm:"default:false" json:"requiresHrApproval"`

	// Relationships
	Employee          Employee  `gorm:"foreignKey:EmployeeID" json:"-"`
	ApprovedByManager *Employee `gorm:"foreignKey:ApprovedByManagerID" json:"-"`
	ApprovedByHR      *Employee `gorm:"foreignKey:ApprovedByHRID" json:"-"`
}

func (LeaveApplication) TableName() string {
	return "leave_applications"
}
