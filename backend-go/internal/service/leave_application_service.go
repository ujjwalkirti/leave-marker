package service

import (
	"time"

	"github.com/leavemarker/backend-go/internal/dto"
	"github.com/leavemarker/backend-go/internal/enums"
	"github.com/leavemarker/backend-go/internal/models"
	"github.com/leavemarker/backend-go/internal/repository"
	"github.com/leavemarker/backend-go/internal/utils"
)

// LeaveApplicationService handles leave application business logic
type LeaveApplicationService struct {
	leaveApplicationRepo *repository.LeaveApplicationRepository
	employeeRepo         *repository.EmployeeRepository
	leaveBalanceService  *LeaveBalanceService
	emailService         *EmailService
}

// NewLeaveApplicationService creates a new LeaveApplicationService
func NewLeaveApplicationService(
	leaveApplicationRepo *repository.LeaveApplicationRepository,
	employeeRepo *repository.EmployeeRepository,
	leaveBalanceService *LeaveBalanceService,
	emailService *EmailService,
) *LeaveApplicationService {
	return &LeaveApplicationService{
		leaveApplicationRepo: leaveApplicationRepo,
		employeeRepo:         employeeRepo,
		leaveBalanceService:  leaveBalanceService,
		emailService:         emailService,
	}
}

// ApplyLeave creates a new leave application
func (s *LeaveApplicationService) ApplyLeave(employeeID uint, req dto.LeaveApplicationRequest) (*dto.LeaveApplicationResponse, error) {
	// Check for overlapping leaves
	overlapping, err := s.leaveApplicationRepo.FindOverlapping(employeeID, req.StartDate, req.EndDate, nil)
	if err != nil {
		return nil, utils.NewInternalServerError("Failed to check for overlapping leaves")
	}
	if len(overlapping) > 0 {
		return nil, utils.NewBadRequestError("Leave dates overlap with existing application")
	}

	employee, err := s.employeeRepo.FindByID(employeeID)
	if err != nil {
		return nil, utils.NewNotFoundError("Employee not found")
	}

	application := &models.LeaveApplication{
		EmployeeID:         employeeID,
		LeaveType:          req.LeaveType,
		StartDate:          req.StartDate,
		EndDate:            req.EndDate,
		NumberOfDays:       req.NumberOfDays,
		IsHalfDay:          req.IsHalfDay,
		Reason:             req.Reason,
		AttachmentURL:      req.AttachmentURL,
		Status:             enums.LeaveStatusPending,
		RequiresHRApproval: false,
	}

	// If employee has no manager, require HR approval
	if employee.ManagerID == nil {
		application.RequiresHRApproval = true
	}

	if err := s.leaveApplicationRepo.Create(application); err != nil {
		return nil, utils.NewInternalServerError("Failed to create leave application")
	}

	// Update leave balance (add to pending)
	year := req.StartDate.Year()
	if err := s.leaveBalanceService.UpdateBalance(employeeID, req.LeaveType, year, req.NumberOfDays, false); err != nil {
		// Log error but don't fail the request
	}

	return s.toLeaveApplicationResponse(application, employee), nil
}

// GetLeaveApplication gets a leave application by ID
func (s *LeaveApplicationService) GetLeaveApplication(id, companyID uint) (*dto.LeaveApplicationResponse, error) {
	application, err := s.leaveApplicationRepo.FindByID(id)
	if err != nil {
		return nil, utils.NewNotFoundError("Leave application not found")
	}

	if application.Employee.CompanyID != companyID {
		return nil, utils.NewForbiddenError("Access denied")
	}

	return s.toLeaveApplicationResponse(application, &application.Employee), nil
}

// GetMyLeaveApplications gets leave applications for an employee
func (s *LeaveApplicationService) GetMyLeaveApplications(employeeID uint) ([]dto.LeaveApplicationResponse, error) {
	applications, err := s.leaveApplicationRepo.FindByEmployeeID(employeeID)
	if err != nil {
		return nil, utils.NewInternalServerError("Failed to get leave applications")
	}

	employee, _ := s.employeeRepo.FindByID(employeeID)

	responses := make([]dto.LeaveApplicationResponse, len(applications))
	for i, app := range applications {
		responses[i] = *s.toLeaveApplicationResponse(&app, employee)
	}

	return responses, nil
}

// CountPendingApplications counts pending leave applications for an employee
func (s *LeaveApplicationService) CountPendingApplications(employeeID uint) (int64, error) {
	return s.leaveApplicationRepo.CountPendingByEmployeeID(employeeID)
}

// GetPendingApprovalsForManager gets pending leave applications for a manager
func (s *LeaveApplicationService) GetPendingApprovalsForManager(managerID uint) ([]dto.LeaveApplicationResponse, error) {
	applications, err := s.leaveApplicationRepo.FindPendingByManagerID(managerID)
	if err != nil {
		return nil, utils.NewInternalServerError("Failed to get pending approvals")
	}

	responses := make([]dto.LeaveApplicationResponse, len(applications))
	for i, app := range applications {
		responses[i] = *s.toLeaveApplicationResponse(&app, &app.Employee)
	}

	return responses, nil
}

// GetPendingApprovalsForHR gets pending leave applications for HR
func (s *LeaveApplicationService) GetPendingApprovalsForHR(companyID uint) ([]dto.LeaveApplicationResponse, error) {
	applications, err := s.leaveApplicationRepo.FindPendingForHR(companyID)
	if err != nil {
		return nil, utils.NewInternalServerError("Failed to get pending approvals")
	}

	responses := make([]dto.LeaveApplicationResponse, len(applications))
	for i, app := range applications {
		responses[i] = *s.toLeaveApplicationResponse(&app, &app.Employee)
	}

	return responses, nil
}

// GetLeaveApplicationsByDateRange gets leave applications by date range
func (s *LeaveApplicationService) GetLeaveApplicationsByDateRange(companyID uint, startDate, endDate time.Time) ([]dto.LeaveApplicationResponse, error) {
	applications, err := s.leaveApplicationRepo.FindByCompanyIDAndDateRange(companyID, startDate, endDate)
	if err != nil {
		return nil, utils.NewInternalServerError("Failed to get leave applications")
	}

	responses := make([]dto.LeaveApplicationResponse, len(applications))
	for i, app := range applications {
		responses[i] = *s.toLeaveApplicationResponse(&app, &app.Employee)
	}

	return responses, nil
}

// ApproveByManager approves or rejects a leave application by manager
func (s *LeaveApplicationService) ApproveByManager(applicationID, managerID uint, req dto.LeaveApprovalRequest) (*dto.LeaveApplicationResponse, error) {
	application, err := s.leaveApplicationRepo.FindByID(applicationID)
	if err != nil {
		return nil, utils.NewNotFoundError("Leave application not found")
	}

	if application.Status != enums.LeaveStatusPending {
		return nil, utils.NewBadRequestError("Leave application is not pending")
	}

	// Verify manager has authority
	if application.Employee.ManagerID == nil || *application.Employee.ManagerID != managerID {
		return nil, utils.NewForbiddenError("You are not the manager of this employee")
	}

	now := time.Now()
	if req.Approved {
		application.ApprovedByManagerID = &managerID
		application.ManagerApprovalDate = &now

		// If no HR approval required, mark as approved
		if !application.RequiresHRApproval {
			application.Status = enums.LeaveStatusApproved
			// Update balance
			year := application.StartDate.Year()
			_ = s.leaveBalanceService.UpdateBalance(application.EmployeeID, application.LeaveType, year, application.NumberOfDays, true)
		}
	} else {
		application.Status = enums.LeaveStatusRejected
		application.RejectionReason = &req.RejectionReason
		application.RejectionDate = &now

		// Restore balance
		year := application.StartDate.Year()
		_ = s.leaveBalanceService.CancelPendingLeave(application.EmployeeID, application.LeaveType, year, application.NumberOfDays)
	}

	if err := s.leaveApplicationRepo.Update(application); err != nil {
		return nil, utils.NewInternalServerError("Failed to update leave application")
	}

	// Send email notification
	statusStr := "approved"
	if !req.Approved {
		statusStr = "rejected"
	}
	_ = s.emailService.SendLeaveApprovalEmail(
		application.Employee.Email,
		application.Employee.FullName,
		string(application.LeaveType),
		application.StartDate.Format("2006-01-02"),
		application.EndDate.Format("2006-01-02"),
		statusStr,
	)

	return s.toLeaveApplicationResponse(application, &application.Employee), nil
}

// ApproveByHR approves or rejects a leave application by HR
func (s *LeaveApplicationService) ApproveByHR(applicationID, hrID uint, req dto.LeaveApprovalRequest) (*dto.LeaveApplicationResponse, error) {
	application, err := s.leaveApplicationRepo.FindByID(applicationID)
	if err != nil {
		return nil, utils.NewNotFoundError("Leave application not found")
	}

	if application.Status != enums.LeaveStatusPending {
		return nil, utils.NewBadRequestError("Leave application is not pending")
	}

	// For HR approval, manager should have already approved (or no manager)
	if !application.RequiresHRApproval && application.ApprovedByManagerID == nil {
		return nil, utils.NewBadRequestError("Manager approval required first")
	}

	now := time.Now()
	if req.Approved {
		application.ApprovedByHRID = &hrID
		application.HRApprovalDate = &now
		application.Status = enums.LeaveStatusApproved

		// Update balance
		year := application.StartDate.Year()
		_ = s.leaveBalanceService.UpdateBalance(application.EmployeeID, application.LeaveType, year, application.NumberOfDays, true)
	} else {
		application.Status = enums.LeaveStatusRejected
		application.RejectionReason = &req.RejectionReason
		application.RejectionDate = &now

		// Restore balance
		year := application.StartDate.Year()
		_ = s.leaveBalanceService.CancelPendingLeave(application.EmployeeID, application.LeaveType, year, application.NumberOfDays)
	}

	if err := s.leaveApplicationRepo.Update(application); err != nil {
		return nil, utils.NewInternalServerError("Failed to update leave application")
	}

	// Send email notification
	statusStr := "approved"
	if !req.Approved {
		statusStr = "rejected"
	}
	_ = s.emailService.SendLeaveApprovalEmail(
		application.Employee.Email,
		application.Employee.FullName,
		string(application.LeaveType),
		application.StartDate.Format("2006-01-02"),
		application.EndDate.Format("2006-01-02"),
		statusStr,
	)

	return s.toLeaveApplicationResponse(application, &application.Employee), nil
}

// CancelLeave cancels a leave application
func (s *LeaveApplicationService) CancelLeave(applicationID, employeeID uint) (*dto.LeaveApplicationResponse, error) {
	application, err := s.leaveApplicationRepo.FindByID(applicationID)
	if err != nil {
		return nil, utils.NewNotFoundError("Leave application not found")
	}

	if application.EmployeeID != employeeID {
		return nil, utils.NewForbiddenError("You can only cancel your own leave applications")
	}

	if application.Status != enums.LeaveStatusPending {
		return nil, utils.NewBadRequestError("Only pending applications can be cancelled")
	}

	application.Status = enums.LeaveStatusCancelled

	if err := s.leaveApplicationRepo.Update(application); err != nil {
		return nil, utils.NewInternalServerError("Failed to cancel leave application")
	}

	// Restore balance
	year := application.StartDate.Year()
	_ = s.leaveBalanceService.CancelPendingLeave(application.EmployeeID, application.LeaveType, year, application.NumberOfDays)

	employee, _ := s.employeeRepo.FindByID(employeeID)
	return s.toLeaveApplicationResponse(application, employee), nil
}

func (s *LeaveApplicationService) toLeaveApplicationResponse(app *models.LeaveApplication, employee *models.Employee) *dto.LeaveApplicationResponse {
	response := &dto.LeaveApplicationResponse{
		ID:                  app.ID,
		EmployeeID:          app.EmployeeID,
		LeaveType:           app.LeaveType,
		StartDate:           app.StartDate,
		EndDate:             app.EndDate,
		NumberOfDays:        app.NumberOfDays,
		IsHalfDay:           app.IsHalfDay,
		Reason:              app.Reason,
		AttachmentURL:       app.AttachmentURL,
		Status:              app.Status,
		ApprovedByManagerID: app.ApprovedByManagerID,
		ManagerApprovalDate: app.ManagerApprovalDate,
		ApprovedByHRID:      app.ApprovedByHRID,
		HRApprovalDate:      app.HRApprovalDate,
		RejectionReason:     app.RejectionReason,
		RejectionDate:       app.RejectionDate,
		RequiresHRApproval:  app.RequiresHRApproval,
		CreatedAt:           app.CreatedAt,
		UpdatedAt:           app.UpdatedAt,
	}

	if employee != nil {
		response.EmployeeName = employee.FullName
		response.EmployeeEmail = employee.Email
	}

	if app.ApprovedByManager != nil {
		response.ApprovedByManagerName = app.ApprovedByManager.FullName
	}

	if app.ApprovedByHR != nil {
		response.ApprovedByHRName = app.ApprovedByHR.FullName
	}

	return response
}
