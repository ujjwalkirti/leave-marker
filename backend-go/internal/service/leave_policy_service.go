package service

import (
	"github.com/leavemarker/backend-go/internal/dto"
	"github.com/leavemarker/backend-go/internal/models"
	"github.com/leavemarker/backend-go/internal/repository"
	"github.com/leavemarker/backend-go/internal/utils"
)

// LeavePolicyService handles leave policy business logic
type LeavePolicyService struct {
	leavePolicyRepo       *repository.LeavePolicyRepository
	planValidationService *PlanValidationService
}

// NewLeavePolicyService creates a new LeavePolicyService
func NewLeavePolicyService(
	leavePolicyRepo *repository.LeavePolicyRepository,
	planValidationService *PlanValidationService,
) *LeavePolicyService {
	return &LeavePolicyService{
		leavePolicyRepo:       leavePolicyRepo,
		planValidationService: planValidationService,
	}
}

// CreateLeavePolicy creates a new leave policy
func (s *LeavePolicyService) CreateLeavePolicy(companyID uint, req dto.LeavePolicyRequest) (*dto.LeavePolicyResponse, error) {
	// Validate plan limit
	if err := s.planValidationService.ValidateLeavePolicyLimit(companyID); err != nil {
		return nil, err
	}

	// Check if policy for this leave type already exists
	exists, err := s.leavePolicyRepo.ExistsByCompanyIDAndLeaveType(companyID, req.LeaveType)
	if err != nil {
		return nil, utils.NewInternalServerError("Failed to check leave policy")
	}
	if exists {
		return nil, utils.NewBadRequestError("Leave policy for this type already exists")
	}

	policy := &models.LeavePolicy{
		CompanyID:         companyID,
		LeaveType:         req.LeaveType,
		AnnualQuota:       req.AnnualQuota,
		MonthlyAccrual:    req.MonthlyAccrual,
		CarryForward:      req.CarryForward,
		MaxCarryForward:   req.MaxCarryForward,
		EncashmentAllowed: req.EncashmentAllowed,
		HalfDayAllowed:    req.HalfDayAllowed,
		Active:            req.Active,
	}

	if err := s.leavePolicyRepo.Create(policy); err != nil {
		return nil, utils.NewInternalServerError("Failed to create leave policy")
	}

	return s.toLeavePolicyResponse(policy), nil
}

// GetLeavePolicy gets a leave policy by ID
func (s *LeavePolicyService) GetLeavePolicy(id, companyID uint) (*dto.LeavePolicyResponse, error) {
	policy, err := s.leavePolicyRepo.FindByID(id)
	if err != nil {
		return nil, utils.NewNotFoundError("Leave policy not found")
	}

	if policy.CompanyID != companyID {
		return nil, utils.NewForbiddenError("Access denied")
	}

	return s.toLeavePolicyResponse(policy), nil
}

// GetAllLeavePolicies gets all leave policies for a company
func (s *LeavePolicyService) GetAllLeavePolicies(companyID uint) ([]dto.LeavePolicyResponse, error) {
	policies, err := s.leavePolicyRepo.FindByCompanyID(companyID)
	if err != nil {
		return nil, utils.NewInternalServerError("Failed to get leave policies")
	}

	responses := make([]dto.LeavePolicyResponse, len(policies))
	for i, policy := range policies {
		responses[i] = *s.toLeavePolicyResponse(&policy)
	}

	return responses, nil
}

// GetActiveLeavePolicies gets all active leave policies for a company
func (s *LeavePolicyService) GetActiveLeavePolicies(companyID uint) ([]dto.LeavePolicyResponse, error) {
	policies, err := s.leavePolicyRepo.FindActiveByCompanyID(companyID)
	if err != nil {
		return nil, utils.NewInternalServerError("Failed to get leave policies")
	}

	responses := make([]dto.LeavePolicyResponse, len(policies))
	for i, policy := range policies {
		responses[i] = *s.toLeavePolicyResponse(&policy)
	}

	return responses, nil
}

// UpdateLeavePolicy updates a leave policy
func (s *LeavePolicyService) UpdateLeavePolicy(id, companyID uint, req dto.LeavePolicyRequest) (*dto.LeavePolicyResponse, error) {
	policy, err := s.leavePolicyRepo.FindByID(id)
	if err != nil {
		return nil, utils.NewNotFoundError("Leave policy not found")
	}

	if policy.CompanyID != companyID {
		return nil, utils.NewForbiddenError("Access denied")
	}

	// Check if changing leave type and if new type already exists
	if req.LeaveType != policy.LeaveType {
		exists, err := s.leavePolicyRepo.ExistsByCompanyIDAndLeaveType(companyID, req.LeaveType)
		if err != nil {
			return nil, utils.NewInternalServerError("Failed to check leave policy")
		}
		if exists {
			return nil, utils.NewBadRequestError("Leave policy for this type already exists")
		}
	}

	policy.LeaveType = req.LeaveType
	policy.AnnualQuota = req.AnnualQuota
	policy.MonthlyAccrual = req.MonthlyAccrual
	policy.CarryForward = req.CarryForward
	policy.MaxCarryForward = req.MaxCarryForward
	policy.EncashmentAllowed = req.EncashmentAllowed
	policy.HalfDayAllowed = req.HalfDayAllowed
	policy.Active = req.Active

	if err := s.leavePolicyRepo.Update(policy); err != nil {
		return nil, utils.NewInternalServerError("Failed to update leave policy")
	}

	return s.toLeavePolicyResponse(policy), nil
}

// DeleteLeavePolicy soft deletes a leave policy
func (s *LeavePolicyService) DeleteLeavePolicy(id, companyID uint) error {
	policy, err := s.leavePolicyRepo.FindByID(id)
	if err != nil {
		return utils.NewNotFoundError("Leave policy not found")
	}

	if policy.CompanyID != companyID {
		return utils.NewForbiddenError("Access denied")
	}

	return s.leavePolicyRepo.Delete(id)
}

func (s *LeavePolicyService) toLeavePolicyResponse(policy *models.LeavePolicy) *dto.LeavePolicyResponse {
	return &dto.LeavePolicyResponse{
		ID:                policy.ID,
		LeaveType:         policy.LeaveType,
		AnnualQuota:       policy.AnnualQuota,
		MonthlyAccrual:    policy.MonthlyAccrual,
		CarryForward:      policy.CarryForward,
		MaxCarryForward:   policy.MaxCarryForward,
		EncashmentAllowed: policy.EncashmentAllowed,
		HalfDayAllowed:    policy.HalfDayAllowed,
		Active:            policy.Active,
		CreatedAt:         policy.CreatedAt,
		UpdatedAt:         policy.UpdatedAt,
	}
}
