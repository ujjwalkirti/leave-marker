package dto

import (
	"time"

	"github.com/leavemarker/backend-go/internal/enums"
)

// LeaveApplicationRequest for creating leave application
type LeaveApplicationRequest struct {
	LeaveType     enums.LeaveType `json:"leaveType" binding:"required"`
	StartDate     time.Time       `json:"startDate" binding:"required"`
	EndDate       time.Time       `json:"endDate" binding:"required"`
	NumberOfDays  float64         `json:"numberOfDays" binding:"required"`
	IsHalfDay     bool            `json:"isHalfDay"`
	Reason        string          `json:"reason" binding:"max=1000"`
	AttachmentURL *string         `json:"attachmentUrl"`
}

// LeaveApplicationResponse for leave application response
type LeaveApplicationResponse struct {
	ID                    uint              `json:"id"`
	EmployeeID            uint              `json:"employeeId"`
	EmployeeName          string            `json:"employeeName"`
	EmployeeEmail         string            `json:"employeeEmail"`
	LeaveType             enums.LeaveType   `json:"leaveType"`
	StartDate             time.Time         `json:"startDate"`
	EndDate               time.Time         `json:"endDate"`
	NumberOfDays          float64           `json:"numberOfDays"`
	IsHalfDay             bool              `json:"isHalfDay"`
	Reason                string            `json:"reason"`
	AttachmentURL         *string           `json:"attachmentUrl"`
	Status                enums.LeaveStatus `json:"status"`
	ApprovedByManagerID   *uint             `json:"approvedByManagerId"`
	ApprovedByManagerName string            `json:"approvedByManagerName,omitempty"`
	ManagerApprovalDate   *time.Time        `json:"managerApprovalDate"`
	ApprovedByHRID        *uint             `json:"approvedByHrId"`
	ApprovedByHRName      string            `json:"approvedByHrName,omitempty"`
	HRApprovalDate        *time.Time        `json:"hrApprovalDate"`
	RejectionReason       *string           `json:"rejectionReason"`
	RejectionDate         *time.Time        `json:"rejectionDate"`
	RequiresHRApproval    bool              `json:"requiresHrApproval"`
	CreatedAt             time.Time         `json:"createdAt"`
	UpdatedAt             time.Time         `json:"updatedAt"`
}

// LeaveApprovalRequest for approving/rejecting leave
type LeaveApprovalRequest struct {
	Approved        bool   `json:"approved"`
	RejectionReason string `json:"rejectionReason"`
}

// LeavePolicyRequest for creating/updating leave policy
type LeavePolicyRequest struct {
	LeaveType         enums.LeaveType `json:"leaveType" binding:"required"`
	AnnualQuota       int             `json:"annualQuota" binding:"required"`
	MonthlyAccrual    float64         `json:"monthlyAccrual"`
	CarryForward      bool            `json:"carryForward"`
	MaxCarryForward   int             `json:"maxCarryForward"`
	EncashmentAllowed bool            `json:"encashmentAllowed"`
	HalfDayAllowed    bool            `json:"halfDayAllowed"`
	Active            bool            `json:"active"`
}

// LeavePolicyResponse for leave policy response
type LeavePolicyResponse struct {
	ID                uint            `json:"id"`
	LeaveType         enums.LeaveType `json:"leaveType"`
	AnnualQuota       int             `json:"annualQuota"`
	MonthlyAccrual    float64         `json:"monthlyAccrual"`
	CarryForward      bool            `json:"carryForward"`
	MaxCarryForward   int             `json:"maxCarryForward"`
	EncashmentAllowed bool            `json:"encashmentAllowed"`
	HalfDayAllowed    bool            `json:"halfDayAllowed"`
	Active            bool            `json:"active"`
	CreatedAt         time.Time       `json:"createdAt"`
	UpdatedAt         time.Time       `json:"updatedAt"`
}

// LeaveBalanceResponse for leave balance response
type LeaveBalanceResponse struct {
	ID             uint            `json:"id"`
	EmployeeID     uint            `json:"employeeId"`
	LeaveType      enums.LeaveType `json:"leaveType"`
	Year           int             `json:"year"`
	TotalQuota     float64         `json:"totalQuota"`
	Used           float64         `json:"used"`
	Pending        float64         `json:"pending"`
	Available      float64         `json:"available"`
	CarriedForward float64         `json:"carriedForward"`
}

// DateRangeRequest for date range queries
type DateRangeRequest struct {
	StartDate time.Time `form:"startDate" binding:"required"`
	EndDate   time.Time `form:"endDate" binding:"required"`
}
