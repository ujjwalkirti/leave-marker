package handler

import (
	"net/http"
	"strconv"

	"github.com/gin-gonic/gin"
	"github.com/leavemarker/backend-go/internal/dto"
	"github.com/leavemarker/backend-go/internal/middleware"
	"github.com/leavemarker/backend-go/internal/service"
	"github.com/leavemarker/backend-go/internal/utils"
)

// SubscriptionHandler handles subscription endpoints
type SubscriptionHandler struct {
	subscriptionService *service.SubscriptionService
}

// NewSubscriptionHandler creates a new SubscriptionHandler
func NewSubscriptionHandler(subscriptionService *service.SubscriptionService) *SubscriptionHandler {
	return &SubscriptionHandler{subscriptionService: subscriptionService}
}

// GetActiveSubscription gets the active subscription
func (h *SubscriptionHandler) GetActiveSubscription(c *gin.Context) {
	companyID := middleware.GetCompanyID(c)

	response, err := h.subscriptionService.GetActiveSubscription(companyID)
	if err != nil {
		utils.HandleError(c, err)
		return
	}

	utils.RespondWithSuccess(c, "Active subscription retrieved successfully", response)
}

// GetAllSubscriptions gets all subscriptions
func (h *SubscriptionHandler) GetAllSubscriptions(c *gin.Context) {
	companyID := middleware.GetCompanyID(c)

	response, err := h.subscriptionService.GetAllSubscriptions(companyID)
	if err != nil {
		utils.HandleError(c, err)
		return
	}

	utils.RespondWithSuccess(c, "Subscriptions retrieved successfully", response)
}

// CreateSubscription creates a new subscription
func (h *SubscriptionHandler) CreateSubscription(c *gin.Context) {
	companyID := middleware.GetCompanyID(c)

	var req dto.SubscriptionRequest
	if err := c.ShouldBindJSON(&req); err != nil {
		utils.RespondWithError(c, http.StatusBadRequest, "Invalid request body")
		return
	}

	response, err := h.subscriptionService.CreateSubscription(companyID, req)
	if err != nil {
		utils.HandleError(c, err)
		return
	}

	utils.RespondWithCreated(c, "Subscription created successfully", response)
}

// UpdateSubscription updates a subscription
func (h *SubscriptionHandler) UpdateSubscription(c *gin.Context) {
	companyID := middleware.GetCompanyID(c)
	id, err := strconv.ParseUint(c.Param("id"), 10, 32)
	if err != nil {
		utils.RespondWithError(c, http.StatusBadRequest, "Invalid subscription ID")
		return
	}

	var req dto.SubscriptionRequest
	if err := c.ShouldBindJSON(&req); err != nil {
		utils.RespondWithError(c, http.StatusBadRequest, "Invalid request body")
		return
	}

	response, err := h.subscriptionService.UpdateSubscription(uint(id), companyID, req)
	if err != nil {
		utils.HandleError(c, err)
		return
	}

	utils.RespondWithSuccess(c, "Subscription updated successfully", response)
}

// CancelSubscription cancels a subscription
func (h *SubscriptionHandler) CancelSubscription(c *gin.Context) {
	companyID := middleware.GetCompanyID(c)
	id, err := strconv.ParseUint(c.Param("id"), 10, 32)
	if err != nil {
		utils.RespondWithError(c, http.StatusBadRequest, "Invalid subscription ID")
		return
	}

	var req dto.SubscriptionCancelRequest
	if err := c.ShouldBindJSON(&req); err != nil {
		req = dto.SubscriptionCancelRequest{}
	}

	response, err := h.subscriptionService.CancelSubscription(uint(id), companyID, req)
	if err != nil {
		utils.HandleError(c, err)
		return
	}

	utils.RespondWithSuccess(c, "Subscription cancelled successfully", response)
}

// GetSubscriptionFeatures gets subscription features
func (h *SubscriptionHandler) GetSubscriptionFeatures(c *gin.Context) {
	companyID := middleware.GetCompanyID(c)

	response, err := h.subscriptionService.GetSubscriptionFeatures(companyID)
	if err != nil {
		utils.HandleError(c, err)
		return
	}

	utils.RespondWithSuccess(c, "Subscription features retrieved successfully", response)
}
