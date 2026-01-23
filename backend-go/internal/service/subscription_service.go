package service

import (
	"time"

	"github.com/leavemarker/backend-go/internal/dto"
	"github.com/leavemarker/backend-go/internal/enums"
	"github.com/leavemarker/backend-go/internal/models"
	"github.com/leavemarker/backend-go/internal/repository"
	"github.com/leavemarker/backend-go/internal/utils"
	"github.com/shopspring/decimal"
)

// SubscriptionService handles subscription business logic
type SubscriptionService struct {
	subscriptionRepo *repository.SubscriptionRepository
	planRepo         *repository.PlanRepository
}

// NewSubscriptionService creates a new SubscriptionService
func NewSubscriptionService(
	subscriptionRepo *repository.SubscriptionRepository,
	planRepo *repository.PlanRepository,
) *SubscriptionService {
	return &SubscriptionService{
		subscriptionRepo: subscriptionRepo,
		planRepo:         planRepo,
	}
}

// GetActiveSubscription gets the active subscription for a company
func (s *SubscriptionService) GetActiveSubscription(companyID uint) (*dto.SubscriptionResponse, error) {
	subscription, err := s.subscriptionRepo.FindActiveByCompanyID(companyID)
	if err != nil {
		return nil, utils.NewNotFoundError("No active subscription found")
	}

	return s.toSubscriptionResponse(subscription), nil
}

// GetAllSubscriptions gets all subscriptions for a company
func (s *SubscriptionService) GetAllSubscriptions(companyID uint) ([]dto.SubscriptionResponse, error) {
	subscriptions, err := s.subscriptionRepo.FindByCompanyID(companyID)
	if err != nil {
		return nil, utils.NewInternalServerError("Failed to get subscriptions")
	}

	responses := make([]dto.SubscriptionResponse, len(subscriptions))
	for i, sub := range subscriptions {
		responses[i] = *s.toSubscriptionResponse(&sub)
	}

	return responses, nil
}

// CreateSubscription creates a new subscription
func (s *SubscriptionService) CreateSubscription(companyID uint, req dto.SubscriptionRequest) (*dto.SubscriptionResponse, error) {
	plan, err := s.planRepo.FindByID(req.PlanID)
	if err != nil {
		return nil, utils.NewNotFoundError("Plan not found")
	}

	// Cancel any existing active subscription
	existing, err := s.subscriptionRepo.FindActiveByCompanyID(companyID)
	if err == nil && existing != nil {
		existing.Status = enums.SubscriptionStatusCancelled
		now := time.Now()
		existing.CancelledAt = &now
		_ = s.subscriptionRepo.Update(existing)
	}

	// Calculate amount
	var amount decimal.Decimal
	if req.BillingCycle == enums.BillingCycleMonthly {
		amount = plan.MonthlyPrice
	} else {
		amount = plan.YearlyPrice
	}

	// Calculate period
	now := time.Now()
	var endDate time.Time
	if req.BillingCycle == enums.BillingCycleMonthly {
		endDate = now.AddDate(0, 1, 0)
	} else {
		endDate = now.AddDate(1, 0, 0)
	}

	subscription := &models.Subscription{
		CompanyID:              companyID,
		PlanID:                 req.PlanID,
		Status:                 enums.SubscriptionStatusActive,
		BillingCycle:           req.BillingCycle,
		StartDate:              now,
		EndDate:                endDate,
		CurrentPeriodStart:     now,
		CurrentPeriodEnd:       endDate,
		Amount:                 amount,
		AutoRenew:              req.AutoRenew,
		IsPaid:                 plan.PlanType == enums.PlanTypeFree,
		HasReportDownloadAddon: req.HasReportDownloadAddon,
	}

	if err := s.subscriptionRepo.Create(subscription); err != nil {
		return nil, utils.NewInternalServerError("Failed to create subscription")
	}

	// Reload to get plan
	subscription, _ = s.subscriptionRepo.FindByID(subscription.ID)

	return s.toSubscriptionResponse(subscription), nil
}

// UpdateSubscription updates a subscription
func (s *SubscriptionService) UpdateSubscription(id, companyID uint, req dto.SubscriptionRequest) (*dto.SubscriptionResponse, error) {
	subscription, err := s.subscriptionRepo.FindByID(id)
	if err != nil {
		return nil, utils.NewNotFoundError("Subscription not found")
	}

	if subscription.CompanyID != companyID {
		return nil, utils.NewForbiddenError("Access denied")
	}

	subscription.AutoRenew = req.AutoRenew
	subscription.HasReportDownloadAddon = req.HasReportDownloadAddon

	if err := s.subscriptionRepo.Update(subscription); err != nil {
		return nil, utils.NewInternalServerError("Failed to update subscription")
	}

	return s.toSubscriptionResponse(subscription), nil
}

// CancelSubscription cancels a subscription
func (s *SubscriptionService) CancelSubscription(id, companyID uint, req dto.SubscriptionCancelRequest) (*dto.SubscriptionResponse, error) {
	subscription, err := s.subscriptionRepo.FindByID(id)
	if err != nil {
		return nil, utils.NewNotFoundError("Subscription not found")
	}

	if subscription.CompanyID != companyID {
		return nil, utils.NewForbiddenError("Access denied")
	}

	now := time.Now()
	subscription.Status = enums.SubscriptionStatusCancelled
	subscription.CancelledAt = &now
	subscription.CancellationReason = &req.Reason

	if err := s.subscriptionRepo.Update(subscription); err != nil {
		return nil, utils.NewInternalServerError("Failed to cancel subscription")
	}

	return s.toSubscriptionResponse(subscription), nil
}

// GetSubscriptionFeatures gets the features of a subscription
func (s *SubscriptionService) GetSubscriptionFeatures(companyID uint) (*dto.SubscriptionFeatureResponse, error) {
	subscription, err := s.subscriptionRepo.FindActiveByCompanyID(companyID)
	if err != nil {
		return nil, utils.NewNotFoundError("No active subscription found")
	}

	return &dto.SubscriptionFeatureResponse{
		MaxEmployees:            subscription.Plan.MaxEmployees,
		MaxLeavePolicies:        subscription.Plan.MaxLeavePolicies,
		MaxHolidays:             subscription.Plan.MaxHolidays,
		AttendanceManagement:    subscription.Plan.AttendanceManagement,
		ReportsDownload:         subscription.Plan.ReportsDownload || subscription.HasReportDownloadAddon,
		MultipleLeavePolicies:   subscription.Plan.MultipleLeavePolicies,
		UnlimitedHolidays:       subscription.Plan.UnlimitedHolidays,
		AttendanceRateAnalytics: subscription.Plan.AttendanceRateAnalytics,
		AdvancedReports:         subscription.Plan.AdvancedReports,
		CustomLeaveTypes:        subscription.Plan.CustomLeaveTypes,
		APIAccess:               subscription.Plan.APIAccess,
		PrioritySupport:         subscription.Plan.PrioritySupport,
	}, nil
}

func (s *SubscriptionService) toSubscriptionResponse(sub *models.Subscription) *dto.SubscriptionResponse {
	response := &dto.SubscriptionResponse{
		ID:                       sub.ID,
		PlanID:                   sub.PlanID,
		Status:                   sub.Status,
		BillingCycle:             sub.BillingCycle,
		StartDate:                sub.StartDate,
		EndDate:                  sub.EndDate,
		CurrentPeriodStart:       sub.CurrentPeriodStart,
		CurrentPeriodEnd:         sub.CurrentPeriodEnd,
		Amount:                   sub.Amount,
		AutoRenew:                sub.AutoRenew,
		IsPaid:                   sub.IsPaid,
		HasReportDownloadAddon:   sub.HasReportDownloadAddon,
		ReportDownloadAddonPrice: sub.ReportDownloadAddonPrice,
		CancellationReason:       sub.CancellationReason,
		CancelledAt:              sub.CancelledAt,
		Notes:                    sub.Notes,
		CreatedAt:                sub.CreatedAt,
		UpdatedAt:                sub.UpdatedAt,
	}

	if sub.Plan.ID != 0 {
		response.PlanName = sub.Plan.Name
	}

	return response
}
