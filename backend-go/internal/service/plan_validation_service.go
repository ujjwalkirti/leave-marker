package service

import (
	"github.com/leavemarker/backend-go/internal/repository"
	"github.com/leavemarker/backend-go/internal/utils"
)

// PlanValidationService handles plan limit validation
type PlanValidationService struct {
	employeeRepo     *repository.EmployeeRepository
	leavePolicyRepo  *repository.LeavePolicyRepository
	holidayRepo      *repository.HolidayRepository
	subscriptionRepo *repository.SubscriptionRepository
}

// NewPlanValidationService creates a new PlanValidationService
func NewPlanValidationService(
	employeeRepo *repository.EmployeeRepository,
	leavePolicyRepo *repository.LeavePolicyRepository,
	holidayRepo *repository.HolidayRepository,
	subscriptionRepo *repository.SubscriptionRepository,
) *PlanValidationService {
	return &PlanValidationService{
		employeeRepo:     employeeRepo,
		leavePolicyRepo:  leavePolicyRepo,
		holidayRepo:      holidayRepo,
		subscriptionRepo: subscriptionRepo,
	}
}

// ValidateEmployeeLimit checks if company can add more employees
func (s *PlanValidationService) ValidateEmployeeLimit(companyID uint) error {
	subscription, err := s.subscriptionRepo.FindActiveByCompanyID(companyID)
	if err != nil {
		return utils.NewBadRequestError("No active subscription found")
	}

	currentCount, err := s.employeeRepo.CountActiveByCompanyID(companyID)
	if err != nil {
		return utils.NewInternalServerError("Failed to count employees")
	}

	if int(currentCount) >= subscription.Plan.MaxEmployees {
		return utils.NewBadRequestError("Employee limit reached for your plan")
	}

	return nil
}

// ValidateLeavePolicyLimit checks if company can add more leave policies
func (s *PlanValidationService) ValidateLeavePolicyLimit(companyID uint) error {
	subscription, err := s.subscriptionRepo.FindActiveByCompanyID(companyID)
	if err != nil {
		return utils.NewBadRequestError("No active subscription found")
	}

	if subscription.Plan.MultipleLeavePolicies {
		return nil // Unlimited policies
	}

	currentCount, err := s.leavePolicyRepo.CountActiveByCompanyID(companyID)
	if err != nil {
		return utils.NewInternalServerError("Failed to count leave policies")
	}

	if int(currentCount) >= subscription.Plan.MaxLeavePolicies {
		return utils.NewBadRequestError("Leave policy limit reached for your plan")
	}

	return nil
}

// ValidateHolidayLimit checks if company can add more holidays
func (s *PlanValidationService) ValidateHolidayLimit(companyID uint) error {
	subscription, err := s.subscriptionRepo.FindActiveByCompanyID(companyID)
	if err != nil {
		return utils.NewBadRequestError("No active subscription found")
	}

	if subscription.Plan.UnlimitedHolidays {
		return nil // Unlimited holidays
	}

	currentCount, err := s.holidayRepo.CountActiveByCompanyID(companyID)
	if err != nil {
		return utils.NewInternalServerError("Failed to count holidays")
	}

	if int(currentCount) >= subscription.Plan.MaxHolidays {
		return utils.NewBadRequestError("Holiday limit reached for your plan")
	}

	return nil
}

// ValidateAttendanceManagementAccess checks if company has access to attendance management
func (s *PlanValidationService) ValidateAttendanceManagementAccess(companyID uint) error {
	subscription, err := s.subscriptionRepo.FindActiveByCompanyID(companyID)
	if err != nil {
		return utils.NewBadRequestError("No active subscription found")
	}

	if !subscription.Plan.AttendanceManagement {
		return utils.NewForbiddenError("Attendance management is not available in your plan")
	}

	return nil
}

// ValidateReportsDownloadAccess checks if company has access to reports download
func (s *PlanValidationService) ValidateReportsDownloadAccess(companyID uint) error {
	subscription, err := s.subscriptionRepo.FindActiveByCompanyID(companyID)
	if err != nil {
		return utils.NewBadRequestError("No active subscription found")
	}

	if !subscription.Plan.ReportsDownload && !subscription.HasReportDownloadAddon {
		return utils.NewForbiddenError("Reports download is not available in your plan")
	}

	return nil
}

// ValidateAttendanceRateAnalyticsAccess checks if company has access to attendance rate analytics
func (s *PlanValidationService) ValidateAttendanceRateAnalyticsAccess(companyID uint) error {
	subscription, err := s.subscriptionRepo.FindActiveByCompanyID(companyID)
	if err != nil {
		return utils.NewBadRequestError("No active subscription found")
	}

	if !subscription.Plan.AttendanceRateAnalytics {
		return utils.NewForbiddenError("Attendance rate analytics is not available in your plan")
	}

	return nil
}

// GetSubscriptionFeatures returns the subscription features for a company
func (s *PlanValidationService) GetSubscriptionFeatures(companyID uint) (map[string]interface{}, error) {
	subscription, err := s.subscriptionRepo.FindActiveByCompanyID(companyID)
	if err != nil {
		return nil, utils.NewBadRequestError("No active subscription found")
	}

	features := map[string]interface{}{
		"maxEmployees":            subscription.Plan.MaxEmployees,
		"maxLeavePolicies":        subscription.Plan.MaxLeavePolicies,
		"maxHolidays":             subscription.Plan.MaxHolidays,
		"attendanceManagement":    subscription.Plan.AttendanceManagement,
		"reportsDownload":         subscription.Plan.ReportsDownload || subscription.HasReportDownloadAddon,
		"multipleLeavePolicies":   subscription.Plan.MultipleLeavePolicies,
		"unlimitedHolidays":       subscription.Plan.UnlimitedHolidays,
		"attendanceRateAnalytics": subscription.Plan.AttendanceRateAnalytics,
		"advancedReports":         subscription.Plan.AdvancedReports,
		"customLeaveTypes":        subscription.Plan.CustomLeaveTypes,
		"apiAccess":               subscription.Plan.APIAccess,
		"prioritySupport":         subscription.Plan.PrioritySupport,
	}

	return features, nil
}
