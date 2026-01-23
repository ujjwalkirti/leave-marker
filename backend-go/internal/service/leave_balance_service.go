package service

import (
	"time"

	"github.com/leavemarker/backend-go/internal/dto"
	"github.com/leavemarker/backend-go/internal/enums"
	"github.com/leavemarker/backend-go/internal/models"
	"github.com/leavemarker/backend-go/internal/repository"
	"github.com/leavemarker/backend-go/internal/utils"
)

// LeaveBalanceService handles leave balance business logic
type LeaveBalanceService struct {
	leaveBalanceRepo *repository.LeaveBalanceRepository
	leavePolicyRepo  *repository.LeavePolicyRepository
	employeeRepo     *repository.EmployeeRepository
}

// NewLeaveBalanceService creates a new LeaveBalanceService
func NewLeaveBalanceService(
	leaveBalanceRepo *repository.LeaveBalanceRepository,
	leavePolicyRepo *repository.LeavePolicyRepository,
	employeeRepo *repository.EmployeeRepository,
) *LeaveBalanceService {
	return &LeaveBalanceService{
		leaveBalanceRepo: leaveBalanceRepo,
		leavePolicyRepo:  leavePolicyRepo,
		employeeRepo:     employeeRepo,
	}
}

// InitializeLeaveBalance initializes leave balances for a new employee
func (s *LeaveBalanceService) InitializeLeaveBalance(employeeID, companyID uint) error {
	year := time.Now().Year()

	policies, err := s.leavePolicyRepo.FindActiveByCompanyID(companyID)
	if err != nil {
		return utils.NewInternalServerError("Failed to get leave policies")
	}

	for _, policy := range policies {
		balance := &models.LeaveBalance{
			EmployeeID: employeeID,
			LeaveType:  policy.LeaveType,
			Year:       year,
			TotalQuota: float64(policy.AnnualQuota),
			Used:       0,
			Pending:    0,
			Available:  float64(policy.AnnualQuota),
		}

		if err := s.leaveBalanceRepo.Create(balance); err != nil {
			return utils.NewInternalServerError("Failed to create leave balance")
		}
	}

	return nil
}

// GetLeaveBalances gets all leave balances for an employee
func (s *LeaveBalanceService) GetLeaveBalances(employeeID, companyID uint) ([]dto.LeaveBalanceResponse, error) {
	employee, err := s.employeeRepo.FindByID(employeeID)
	if err != nil {
		return nil, utils.NewNotFoundError("Employee not found")
	}

	if employee.CompanyID != companyID {
		return nil, utils.NewForbiddenError("Access denied")
	}

	balances, err := s.leaveBalanceRepo.FindByEmployeeID(employeeID)
	if err != nil {
		return nil, utils.NewInternalServerError("Failed to get leave balances")
	}

	responses := make([]dto.LeaveBalanceResponse, len(balances))
	for i, balance := range balances {
		responses[i] = *s.toLeaveBalanceResponse(&balance)
	}

	return responses, nil
}

// GetLeaveBalancesByYear gets leave balances for an employee by year
func (s *LeaveBalanceService) GetLeaveBalancesByYear(employeeID, companyID uint, year int) ([]dto.LeaveBalanceResponse, error) {
	employee, err := s.employeeRepo.FindByID(employeeID)
	if err != nil {
		return nil, utils.NewNotFoundError("Employee not found")
	}

	if employee.CompanyID != companyID {
		return nil, utils.NewForbiddenError("Access denied")
	}

	balances, err := s.leaveBalanceRepo.FindByEmployeeIDAndYear(employeeID, year)
	if err != nil {
		return nil, utils.NewInternalServerError("Failed to get leave balances")
	}

	responses := make([]dto.LeaveBalanceResponse, len(balances))
	for i, balance := range balances {
		responses[i] = *s.toLeaveBalanceResponse(&balance)
	}

	return responses, nil
}

// GetLeaveBalance gets a specific leave balance
func (s *LeaveBalanceService) GetLeaveBalance(employeeID uint, leaveType enums.LeaveType, year int) (*dto.LeaveBalanceResponse, error) {
	balance, err := s.leaveBalanceRepo.FindByEmployeeIDAndLeaveTypeAndYear(employeeID, leaveType, year)
	if err != nil {
		return nil, utils.NewNotFoundError("Leave balance not found")
	}

	return s.toLeaveBalanceResponse(balance), nil
}

// UpdateBalance updates leave balance after leave application status change
func (s *LeaveBalanceService) UpdateBalance(employeeID uint, leaveType enums.LeaveType, year int, days float64, isApproval bool) error {
	balance, err := s.leaveBalanceRepo.FindByEmployeeIDAndLeaveTypeAndYear(employeeID, leaveType, year)
	if err != nil {
		// Create balance if not exists
		balance = &models.LeaveBalance{
			EmployeeID: employeeID,
			LeaveType:  leaveType,
			Year:       year,
			TotalQuota: 0,
			Used:       0,
			Pending:    0,
			Available:  0,
		}
	}

	if isApproval {
		// Approved - move from pending to used
		balance.Pending -= days
		balance.Used += days
	} else {
		// Pending - add to pending
		balance.Pending += days
	}

	balance.Available = balance.TotalQuota + balance.CarriedForward - balance.Used - balance.Pending

	return s.leaveBalanceRepo.Upsert(balance)
}

// CancelPendingLeave cancels pending leave and restores balance
func (s *LeaveBalanceService) CancelPendingLeave(employeeID uint, leaveType enums.LeaveType, year int, days float64) error {
	balance, err := s.leaveBalanceRepo.FindByEmployeeIDAndLeaveTypeAndYear(employeeID, leaveType, year)
	if err != nil {
		return utils.NewNotFoundError("Leave balance not found")
	}

	balance.Pending -= days
	balance.Available = balance.TotalQuota + balance.CarriedForward - balance.Used - balance.Pending

	return s.leaveBalanceRepo.Update(balance)
}

func (s *LeaveBalanceService) toLeaveBalanceResponse(balance *models.LeaveBalance) *dto.LeaveBalanceResponse {
	return &dto.LeaveBalanceResponse{
		ID:             balance.ID,
		EmployeeID:     balance.EmployeeID,
		LeaveType:      balance.LeaveType,
		Year:           balance.Year,
		TotalQuota:     balance.TotalQuota,
		Used:           balance.Used,
		Pending:        balance.Pending,
		Available:      balance.Available,
		CarriedForward: balance.CarriedForward,
	}
}
