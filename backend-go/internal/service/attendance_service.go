package service

import (
	"time"

	"github.com/leavemarker/backend-go/internal/dto"
	"github.com/leavemarker/backend-go/internal/enums"
	"github.com/leavemarker/backend-go/internal/models"
	"github.com/leavemarker/backend-go/internal/repository"
	"github.com/leavemarker/backend-go/internal/utils"
)

// AttendanceService handles attendance business logic
type AttendanceService struct {
	attendanceRepo        *repository.AttendanceRepository
	employeeRepo          *repository.EmployeeRepository
	planValidationService *PlanValidationService
}

// NewAttendanceService creates a new AttendanceService
func NewAttendanceService(
	attendanceRepo *repository.AttendanceRepository,
	employeeRepo *repository.EmployeeRepository,
	planValidationService *PlanValidationService,
) *AttendanceService {
	return &AttendanceService{
		attendanceRepo:        attendanceRepo,
		employeeRepo:          employeeRepo,
		planValidationService: planValidationService,
	}
}

// PunchInOut handles punch in/out
func (s *AttendanceService) PunchInOut(employeeID, companyID uint, req dto.AttendancePunchRequest) (*dto.AttendanceResponse, error) {
	// Validate plan access
	if err := s.planValidationService.ValidateAttendanceManagementAccess(companyID); err != nil {
		return nil, err
	}

	// Check if attendance record exists for today
	existing, err := s.attendanceRepo.FindByEmployeeIDAndDate(employeeID, req.Date)
	if err == nil {
		// Update existing record
		if req.PunchInTime != nil {
			existing.PunchInTime = req.PunchInTime
		}
		if req.PunchOutTime != nil {
			existing.PunchOutTime = req.PunchOutTime
		}
		if req.WorkType != nil {
			existing.WorkType = req.WorkType
		}

		if err := s.attendanceRepo.Update(existing); err != nil {
			return nil, utils.NewInternalServerError("Failed to update attendance")
		}

		return s.toAttendanceResponse(existing, nil), nil
	}

	// Create new attendance record
	attendance := &models.Attendance{
		EmployeeID:   employeeID,
		Date:         req.Date,
		PunchInTime:  req.PunchInTime,
		PunchOutTime: req.PunchOutTime,
		WorkType:     req.WorkType,
		Status:       enums.AttendanceStatusPresent,
	}

	if err := s.attendanceRepo.Create(attendance); err != nil {
		return nil, utils.NewInternalServerError("Failed to create attendance")
	}

	return s.toAttendanceResponse(attendance, nil), nil
}

// GetTodayAttendance gets today's attendance for an employee
func (s *AttendanceService) GetTodayAttendance(employeeID uint) (*dto.AttendanceResponse, error) {
	today := time.Now().Truncate(24 * time.Hour)
	attendance, err := s.attendanceRepo.FindByEmployeeIDAndDate(employeeID, today)
	if err != nil {
		return nil, utils.NewNotFoundError("No attendance record for today")
	}

	return s.toAttendanceResponse(attendance, nil), nil
}

// GetAttendance gets an attendance record by ID
func (s *AttendanceService) GetAttendance(id, companyID uint) (*dto.AttendanceResponse, error) {
	attendance, err := s.attendanceRepo.FindByID(id)
	if err != nil {
		return nil, utils.NewNotFoundError("Attendance not found")
	}

	if attendance.Employee.CompanyID != companyID {
		return nil, utils.NewForbiddenError("Access denied")
	}

	return s.toAttendanceResponse(attendance, &attendance.Employee), nil
}

// GetMyAttendance gets all attendance records for an employee
func (s *AttendanceService) GetMyAttendance(employeeID uint) ([]dto.AttendanceResponse, error) {
	attendances, err := s.attendanceRepo.FindByEmployeeID(employeeID)
	if err != nil {
		return nil, utils.NewInternalServerError("Failed to get attendance")
	}

	responses := make([]dto.AttendanceResponse, len(attendances))
	for i, att := range attendances {
		responses[i] = *s.toAttendanceResponse(&att, nil)
	}

	return responses, nil
}

// GetMyAttendanceByDateRange gets attendance records by date range for an employee
func (s *AttendanceService) GetMyAttendanceByDateRange(employeeID uint, startDate, endDate time.Time) ([]dto.AttendanceResponse, error) {
	attendances, err := s.attendanceRepo.FindByEmployeeIDAndDateRange(employeeID, startDate, endDate)
	if err != nil {
		return nil, utils.NewInternalServerError("Failed to get attendance")
	}

	responses := make([]dto.AttendanceResponse, len(attendances))
	for i, att := range attendances {
		responses[i] = *s.toAttendanceResponse(&att, nil)
	}

	return responses, nil
}

// GetAttendanceRate calculates attendance rate for an employee
func (s *AttendanceService) GetAttendanceRate(employeeID, companyID uint, startDate, endDate time.Time) (*dto.AttendanceRateResponse, error) {
	// Validate plan access
	if err := s.planValidationService.ValidateAttendanceRateAnalyticsAccess(companyID); err != nil {
		return nil, err
	}

	attendances, err := s.attendanceRepo.FindByEmployeeIDAndDateRange(employeeID, startDate, endDate)
	if err != nil {
		return nil, utils.NewInternalServerError("Failed to get attendance")
	}

	totalDays := int(endDate.Sub(startDate).Hours()/24) + 1
	presentDays := 0
	absentDays := 0
	leaveDays := 0

	for _, att := range attendances {
		switch att.Status {
		case enums.AttendanceStatusPresent, enums.AttendanceStatusWorkFromHome:
			presentDays++
		case enums.AttendanceStatusAbsent:
			absentDays++
		case enums.AttendanceStatusOnLeave:
			leaveDays++
		}
	}

	rate := 0.0
	if totalDays > 0 {
		rate = float64(presentDays) / float64(totalDays) * 100
	}

	return &dto.AttendanceRateResponse{
		TotalDays:      totalDays,
		PresentDays:    presentDays,
		AbsentDays:     absentDays,
		LeaveDays:      leaveDays,
		AttendanceRate: rate,
	}, nil
}

// GetAttendanceByDateRange gets attendance records by date range for a company
func (s *AttendanceService) GetAttendanceByDateRange(companyID uint, startDate, endDate time.Time) ([]dto.AttendanceResponse, error) {
	attendances, err := s.attendanceRepo.FindByCompanyIDAndDateRange(companyID, startDate, endDate)
	if err != nil {
		return nil, utils.NewInternalServerError("Failed to get attendance")
	}

	responses := make([]dto.AttendanceResponse, len(attendances))
	for i, att := range attendances {
		responses[i] = *s.toAttendanceResponse(&att, &att.Employee)
	}

	return responses, nil
}

// RequestCorrection requests attendance correction
func (s *AttendanceService) RequestCorrection(id, employeeID uint, req dto.AttendanceCorrectionRequest) (*dto.AttendanceResponse, error) {
	attendance, err := s.attendanceRepo.FindByID(id)
	if err != nil {
		return nil, utils.NewNotFoundError("Attendance not found")
	}

	if attendance.EmployeeID != employeeID {
		return nil, utils.NewForbiddenError("You can only request correction for your own attendance")
	}

	attendance.CorrectionRequested = true
	attendance.Remarks = &req.Reason

	if err := s.attendanceRepo.Update(attendance); err != nil {
		return nil, utils.NewInternalServerError("Failed to request correction")
	}

	return s.toAttendanceResponse(attendance, nil), nil
}

// ApproveCorrection approves an attendance correction
func (s *AttendanceService) ApproveCorrection(id, companyID uint) (*dto.AttendanceResponse, error) {
	attendance, err := s.attendanceRepo.FindByID(id)
	if err != nil {
		return nil, utils.NewNotFoundError("Attendance not found")
	}

	if attendance.Employee.CompanyID != companyID {
		return nil, utils.NewForbiddenError("Access denied")
	}

	if !attendance.CorrectionRequested {
		return nil, utils.NewBadRequestError("No correction requested")
	}

	attendance.CorrectionApproved = true
	attendance.CorrectionRequested = false

	if err := s.attendanceRepo.Update(attendance); err != nil {
		return nil, utils.NewInternalServerError("Failed to approve correction")
	}

	return s.toAttendanceResponse(attendance, &attendance.Employee), nil
}

// RejectCorrection rejects an attendance correction
func (s *AttendanceService) RejectCorrection(id, companyID uint) (*dto.AttendanceResponse, error) {
	attendance, err := s.attendanceRepo.FindByID(id)
	if err != nil {
		return nil, utils.NewNotFoundError("Attendance not found")
	}

	if attendance.Employee.CompanyID != companyID {
		return nil, utils.NewForbiddenError("Access denied")
	}

	if !attendance.CorrectionRequested {
		return nil, utils.NewBadRequestError("No correction requested")
	}

	attendance.CorrectionRequested = false

	if err := s.attendanceRepo.Update(attendance); err != nil {
		return nil, utils.NewInternalServerError("Failed to reject correction")
	}

	return s.toAttendanceResponse(attendance, &attendance.Employee), nil
}

// GetPendingCorrections gets pending corrections for a company
func (s *AttendanceService) GetPendingCorrections(companyID uint) ([]dto.AttendanceResponse, error) {
	attendances, err := s.attendanceRepo.FindPendingCorrections(companyID)
	if err != nil {
		return nil, utils.NewInternalServerError("Failed to get pending corrections")
	}

	responses := make([]dto.AttendanceResponse, len(attendances))
	for i, att := range attendances {
		responses[i] = *s.toAttendanceResponse(&att, &att.Employee)
	}

	return responses, nil
}

// MarkAttendance marks attendance for an employee (HR only)
func (s *AttendanceService) MarkAttendance(companyID uint, req dto.AttendanceMarkRequest) (*dto.AttendanceResponse, error) {
	// Validate plan access
	if err := s.planValidationService.ValidateAttendanceManagementAccess(companyID); err != nil {
		return nil, err
	}

	// Verify employee belongs to company
	employee, err := s.employeeRepo.FindByID(req.EmployeeID)
	if err != nil {
		return nil, utils.NewNotFoundError("Employee not found")
	}

	if employee.CompanyID != companyID {
		return nil, utils.NewForbiddenError("Access denied")
	}

	attendance := &models.Attendance{
		EmployeeID:   req.EmployeeID,
		Date:         req.Date,
		PunchInTime:  req.PunchInTime,
		PunchOutTime: req.PunchOutTime,
		WorkType:     req.WorkType,
		Status:       req.Status,
		Remarks:      req.Remarks,
	}

	if err := s.attendanceRepo.Upsert(attendance); err != nil {
		return nil, utils.NewInternalServerError("Failed to mark attendance")
	}

	return s.toAttendanceResponse(attendance, employee), nil
}

func (s *AttendanceService) toAttendanceResponse(att *models.Attendance, employee *models.Employee) *dto.AttendanceResponse {
	response := &dto.AttendanceResponse{
		ID:                  att.ID,
		EmployeeID:          att.EmployeeID,
		Date:                att.Date,
		PunchInTime:         att.PunchInTime,
		PunchOutTime:        att.PunchOutTime,
		WorkType:            att.WorkType,
		Status:              att.Status,
		Remarks:             att.Remarks,
		CorrectionRequested: att.CorrectionRequested,
		CorrectionApproved:  att.CorrectionApproved,
		CreatedAt:           att.CreatedAt,
		UpdatedAt:           att.UpdatedAt,
	}

	if employee != nil {
		response.EmployeeName = employee.FullName
	}

	return response
}
