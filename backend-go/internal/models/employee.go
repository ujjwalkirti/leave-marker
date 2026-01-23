package models

import (
	"time"

	"github.com/leavemarker/backend-go/internal/enums"
)

// Employee represents an employee entity
type Employee struct {
	BaseModel
	CompanyID                uint                  `gorm:"not null;index" json:"companyId"`
	EmployeeID               string                `gorm:"size:50;not null" json:"employeeId"`
	FullName                 string                `gorm:"size:100;not null" json:"fullName"`
	Email                    string                `gorm:"size:100;uniqueIndex;not null" json:"email"`
	Password                 string                `gorm:"size:255;not null" json:"-"`
	Role                     enums.Role            `gorm:"size:20;not null" json:"role"`
	Department               string                `gorm:"size:100" json:"department"`
	JobTitle                 string                `gorm:"size:100" json:"jobTitle"`
	DateOfJoining            *time.Time            `gorm:"type:date" json:"dateOfJoining"`
	EmploymentType           enums.EmploymentType  `gorm:"size:20;default:FULL_TIME" json:"employmentType"`
	WorkLocation             enums.IndianState     `gorm:"size:50" json:"workLocation"`
	ManagerID                *uint                 `gorm:"index" json:"managerId"`
	Status                   enums.EmployeeStatus  `gorm:"size:20;default:ACTIVE" json:"status"`
	PasswordResetToken       *string               `gorm:"size:500" json:"-"`
	PasswordResetTokenExpiry *time.Time            `gorm:"type:date" json:"-"`

	// Relationships
	Company           Company            `gorm:"foreignKey:CompanyID" json:"-"`
	Manager           *Employee          `gorm:"foreignKey:ManagerID" json:"-"`
	DirectReports     []Employee         `gorm:"foreignKey:ManagerID" json:"-"`
	LeaveApplications []LeaveApplication `gorm:"foreignKey:EmployeeID" json:"-"`
	LeaveBalances     []LeaveBalance     `gorm:"foreignKey:EmployeeID" json:"-"`
	Attendances       []Attendance       `gorm:"foreignKey:EmployeeID" json:"-"`
}

func (Employee) TableName() string {
	return "employees"
}
