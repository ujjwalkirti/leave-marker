package service

import (
	"github.com/leavemarker/backend-go/internal/dto"
	"github.com/leavemarker/backend-go/internal/models"
	"github.com/leavemarker/backend-go/internal/repository"
	"github.com/leavemarker/backend-go/internal/utils"
)

// PlanService handles plan business logic
type PlanService struct {
	planRepo *repository.PlanRepository
}

// NewPlanService creates a new PlanService
func NewPlanService(planRepo *repository.PlanRepository) *PlanService {
	return &PlanService{planRepo: planRepo}
}

// GetAllPlans gets all plans
func (s *PlanService) GetAllPlans() ([]dto.PlanResponse, error) {
	plans, err := s.planRepo.FindAll()
	if err != nil {
		return nil, utils.NewInternalServerError("Failed to get plans")
	}

	responses := make([]dto.PlanResponse, len(plans))
	for i, plan := range plans {
		responses[i] = *s.toPlanResponse(&plan)
	}

	return responses, nil
}

// GetActivePlans gets all active plans
func (s *PlanService) GetActivePlans() ([]dto.PlanResponse, error) {
	plans, err := s.planRepo.FindActive()
	if err != nil {
		return nil, utils.NewInternalServerError("Failed to get plans")
	}

	responses := make([]dto.PlanResponse, len(plans))
	for i, plan := range plans {
		responses[i] = *s.toPlanResponse(&plan)
	}

	return responses, nil
}

// GetPlan gets a plan by ID
func (s *PlanService) GetPlan(id uint) (*dto.PlanResponse, error) {
	plan, err := s.planRepo.FindByID(id)
	if err != nil {
		return nil, utils.NewNotFoundError("Plan not found")
	}

	return s.toPlanResponse(plan), nil
}

// CreatePlan creates a new plan
func (s *PlanService) CreatePlan(req dto.PlanRequest) (*dto.PlanResponse, error) {
	plan := &models.Plan{
		Name:                       req.Name,
		Description:                req.Description,
		Tier:                       req.Tier,
		PlanType:                   req.PlanType,
		BillingCycle:               req.BillingCycle,
		MonthlyPrice:               req.MonthlyPrice,
		YearlyPrice:                req.YearlyPrice,
		MinEmployees:               req.MinEmployees,
		MaxEmployees:               req.MaxEmployees,
		MaxLeavePolicies:           req.MaxLeavePolicies,
		MaxHolidays:                req.MaxHolidays,
		Active:                     req.Active,
		AttendanceManagement:       req.AttendanceManagement,
		ReportsDownload:            req.ReportsDownload,
		MultipleLeavePolicies:      req.MultipleLeavePolicies,
		UnlimitedHolidays:          req.UnlimitedHolidays,
		AttendanceRateAnalytics:    req.AttendanceRateAnalytics,
		AdvancedReports:            req.AdvancedReports,
		CustomLeaveTypes:           req.CustomLeaveTypes,
		APIAccess:                  req.APIAccess,
		PrioritySupport:            req.PrioritySupport,
		PricePerEmployee:           req.PricePerEmployee,
		ReportDownloadPriceUnder50: req.ReportDownloadPriceUnder50,
		ReportDownloadPrice50Plus:  req.ReportDownloadPrice50Plus,
	}

	if err := s.planRepo.Create(plan); err != nil {
		return nil, utils.NewInternalServerError("Failed to create plan")
	}

	return s.toPlanResponse(plan), nil
}

// UpdatePlan updates a plan
func (s *PlanService) UpdatePlan(id uint, req dto.PlanRequest) (*dto.PlanResponse, error) {
	plan, err := s.planRepo.FindByID(id)
	if err != nil {
		return nil, utils.NewNotFoundError("Plan not found")
	}

	plan.Name = req.Name
	plan.Description = req.Description
	plan.Tier = req.Tier
	plan.PlanType = req.PlanType
	plan.BillingCycle = req.BillingCycle
	plan.MonthlyPrice = req.MonthlyPrice
	plan.YearlyPrice = req.YearlyPrice
	plan.MinEmployees = req.MinEmployees
	plan.MaxEmployees = req.MaxEmployees
	plan.MaxLeavePolicies = req.MaxLeavePolicies
	plan.MaxHolidays = req.MaxHolidays
	plan.Active = req.Active
	plan.AttendanceManagement = req.AttendanceManagement
	plan.ReportsDownload = req.ReportsDownload
	plan.MultipleLeavePolicies = req.MultipleLeavePolicies
	plan.UnlimitedHolidays = req.UnlimitedHolidays
	plan.AttendanceRateAnalytics = req.AttendanceRateAnalytics
	plan.AdvancedReports = req.AdvancedReports
	plan.CustomLeaveTypes = req.CustomLeaveTypes
	plan.APIAccess = req.APIAccess
	plan.PrioritySupport = req.PrioritySupport
	plan.PricePerEmployee = req.PricePerEmployee
	plan.ReportDownloadPriceUnder50 = req.ReportDownloadPriceUnder50
	plan.ReportDownloadPrice50Plus = req.ReportDownloadPrice50Plus

	if err := s.planRepo.Update(plan); err != nil {
		return nil, utils.NewInternalServerError("Failed to update plan")
	}

	return s.toPlanResponse(plan), nil
}

// DeletePlan soft deletes a plan
func (s *PlanService) DeletePlan(id uint) error {
	_, err := s.planRepo.FindByID(id)
	if err != nil {
		return utils.NewNotFoundError("Plan not found")
	}

	return s.planRepo.Delete(id)
}

func (s *PlanService) toPlanResponse(plan *models.Plan) *dto.PlanResponse {
	return &dto.PlanResponse{
		ID:                         plan.ID,
		Name:                       plan.Name,
		Description:                plan.Description,
		Tier:                       plan.Tier,
		PlanType:                   plan.PlanType,
		BillingCycle:               plan.BillingCycle,
		MonthlyPrice:               plan.MonthlyPrice,
		YearlyPrice:                plan.YearlyPrice,
		MinEmployees:               plan.MinEmployees,
		MaxEmployees:               plan.MaxEmployees,
		MaxLeavePolicies:           plan.MaxLeavePolicies,
		MaxHolidays:                plan.MaxHolidays,
		Active:                     plan.Active,
		AttendanceManagement:       plan.AttendanceManagement,
		ReportsDownload:            plan.ReportsDownload,
		MultipleLeavePolicies:      plan.MultipleLeavePolicies,
		UnlimitedHolidays:          plan.UnlimitedHolidays,
		AttendanceRateAnalytics:    plan.AttendanceRateAnalytics,
		AdvancedReports:            plan.AdvancedReports,
		CustomLeaveTypes:           plan.CustomLeaveTypes,
		APIAccess:                  plan.APIAccess,
		PrioritySupport:            plan.PrioritySupport,
		PricePerEmployee:           plan.PricePerEmployee,
		ReportDownloadPriceUnder50: plan.ReportDownloadPriceUnder50,
		ReportDownloadPrice50Plus:  plan.ReportDownloadPrice50Plus,
		CreatedAt:                  plan.CreatedAt,
		UpdatedAt:                  plan.UpdatedAt,
	}
}
