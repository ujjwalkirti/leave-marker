package dto

import (
	"time"

	"github.com/leavemarker/backend-go/internal/enums"
)

// AttendancePunchRequest for punch in/out
type AttendancePunchRequest struct {
	Date         time.Time        `json:"date" binding:"required"`
	PunchInTime  *time.Time       `json:"punchInTime"`
	PunchOutTime *time.Time       `json:"punchOutTime"`
	WorkType     *enums.WorkType  `json:"workType"`
}

// AttendanceCorrectionRequest for requesting correction
type AttendanceCorrectionRequest struct {
	CorrectedPunchInTime  *time.Time `json:"correctedPunchInTime"`
	CorrectedPunchOutTime *time.Time `json:"correctedPunchOutTime"`
	Reason                string     `json:"reason" binding:"required,max=500"`
}

// AttendanceMarkRequest for HR marking attendance
type AttendanceMarkRequest struct {
	EmployeeID   uint                   `json:"employeeId" binding:"required"`
	Date         time.Time              `json:"date" binding:"required"`
	PunchInTime  *time.Time             `json:"punchInTime"`
	PunchOutTime *time.Time             `json:"punchOutTime"`
	WorkType     *enums.WorkType        `json:"workType"`
	Status       enums.AttendanceStatus `json:"status" binding:"required"`
	Remarks      *string                `json:"remarks"`
}

// AttendanceResponse for attendance response
type AttendanceResponse struct {
	ID                  uint                   `json:"id"`
	EmployeeID          uint                   `json:"employeeId"`
	EmployeeName        string                 `json:"employeeName,omitempty"`
	Date                time.Time              `json:"date"`
	PunchInTime         *time.Time             `json:"punchInTime"`
	PunchOutTime        *time.Time             `json:"punchOutTime"`
	WorkType            *enums.WorkType        `json:"workType"`
	Status              enums.AttendanceStatus `json:"status"`
	Remarks             *string                `json:"remarks"`
	CorrectionRequested bool                   `json:"correctionRequested"`
	CorrectionApproved  bool                   `json:"correctionApproved"`
	CreatedAt           time.Time              `json:"createdAt"`
	UpdatedAt           time.Time              `json:"updatedAt"`
}

// AttendanceRateResponse for attendance rate
type AttendanceRateResponse struct {
	TotalDays      int     `json:"totalDays"`
	PresentDays    int     `json:"presentDays"`
	AbsentDays     int     `json:"absentDays"`
	LeaveDays      int     `json:"leaveDays"`
	AttendanceRate float64 `json:"attendanceRate"`
}
