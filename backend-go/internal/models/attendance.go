package models

import (
	"time"

	"github.com/leavemarker/backend-go/internal/enums"
)

// Attendance represents an attendance entity
type Attendance struct {
	BaseModel
	EmployeeID          uint                   `gorm:"not null;index;uniqueIndex:idx_emp_date" json:"employeeId"`
	Date                time.Time              `gorm:"type:date;not null;uniqueIndex:idx_emp_date" json:"date"`
	PunchInTime         *time.Time             `gorm:"type:time" json:"punchInTime"`
	PunchOutTime        *time.Time             `gorm:"type:time" json:"punchOutTime"`
	WorkType            *enums.WorkType        `gorm:"size:20" json:"workType"`
	Status              enums.AttendanceStatus `gorm:"size:20;not null" json:"status"`
	Remarks             *string                `gorm:"size:500" json:"remarks"`
	CorrectionRequested bool                   `gorm:"default:false" json:"correctionRequested"`
	CorrectionApproved  bool                   `gorm:"default:false" json:"correctionApproved"`

	// Relationships
	Employee Employee `gorm:"foreignKey:EmployeeID" json:"-"`
}

func (Attendance) TableName() string {
	return "attendances"
}
