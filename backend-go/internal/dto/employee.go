package dto

import (
	"time"

	"github.com/leavemarker/backend-go/internal/enums"
)

// EmployeeRequest for creating/updating employee
type EmployeeRequest struct {
	EmployeeID     string               `json:"employeeId" binding:"required,max=50"`
	FullName       string               `json:"fullName" binding:"required,max=100"`
	Email          string               `json:"email" binding:"required,email,max=100"`
	Password       string               `json:"password" binding:"omitempty,min=6"`
	Role           enums.Role           `json:"role" binding:"required"`
	Department     string               `json:"department" binding:"max=100"`
	JobTitle       string               `json:"jobTitle" binding:"max=100"`
	DateOfJoining  *time.Time           `json:"dateOfJoining"`
	EmploymentType enums.EmploymentType `json:"employmentType"`
	WorkLocation   enums.IndianState    `json:"workLocation"`
	ManagerID      *uint                `json:"managerId"`
}

// EmployeeUpdateRequest for updating employee
type EmployeeUpdateRequest struct {
	EmployeeID     string               `json:"employeeId" binding:"max=50"`
	FullName       string               `json:"fullName" binding:"max=100"`
	Email          string               `json:"email" binding:"omitempty,email,max=100"`
	Role           enums.Role           `json:"role"`
	Department     string               `json:"department" binding:"max=100"`
	JobTitle       string               `json:"jobTitle" binding:"max=100"`
	DateOfJoining  *time.Time           `json:"dateOfJoining"`
	EmploymentType enums.EmploymentType `json:"employmentType"`
	WorkLocation   enums.IndianState    `json:"workLocation"`
	ManagerID      *uint                `json:"managerId"`
}

// EmployeeResponse for employee response
type EmployeeResponse struct {
	ID             uint                 `json:"id"`
	EmployeeID     string               `json:"employeeId"`
	FullName       string               `json:"fullName"`
	Email          string               `json:"email"`
	Role           enums.Role           `json:"role"`
	Department     string               `json:"department"`
	JobTitle       string               `json:"jobTitle"`
	DateOfJoining  *time.Time           `json:"dateOfJoining"`
	EmploymentType enums.EmploymentType `json:"employmentType"`
	WorkLocation   enums.IndianState    `json:"workLocation"`
	Status         enums.EmployeeStatus `json:"status"`
	ManagerID      *uint                `json:"managerId"`
	ManagerName    string               `json:"managerName,omitempty"`
	CompanyID      uint                 `json:"companyId"`
	CompanyName    string               `json:"companyName,omitempty"`
	CreatedAt      time.Time            `json:"createdAt"`
	UpdatedAt      time.Time            `json:"updatedAt"`
}
