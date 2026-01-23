package handler

import (
	"net/http"
	"strconv"

	"github.com/gin-gonic/gin"
	"github.com/leavemarker/backend-go/internal/dto"
	"github.com/leavemarker/backend-go/internal/service"
	"github.com/leavemarker/backend-go/internal/utils"
)

// PlanHandler handles plan endpoints
type PlanHandler struct {
	planService *service.PlanService
}

// NewPlanHandler creates a new PlanHandler
func NewPlanHandler(planService *service.PlanService) *PlanHandler {
	return &PlanHandler{planService: planService}
}

// GetAllPlans gets all plans
func (h *PlanHandler) GetAllPlans(c *gin.Context) {
	response, err := h.planService.GetAllPlans()
	if err != nil {
		utils.HandleError(c, err)
		return
	}

	utils.RespondWithSuccess(c, "Plans retrieved successfully", response)
}

// GetActivePlans gets all active plans
func (h *PlanHandler) GetActivePlans(c *gin.Context) {
	response, err := h.planService.GetActivePlans()
	if err != nil {
		utils.HandleError(c, err)
		return
	}

	utils.RespondWithSuccess(c, "Active plans retrieved successfully", response)
}

// GetPlan gets a plan by ID
func (h *PlanHandler) GetPlan(c *gin.Context) {
	id, err := strconv.ParseUint(c.Param("id"), 10, 32)
	if err != nil {
		utils.RespondWithError(c, http.StatusBadRequest, "Invalid plan ID")
		return
	}

	response, err := h.planService.GetPlan(uint(id))
	if err != nil {
		utils.HandleError(c, err)
		return
	}

	utils.RespondWithSuccess(c, "Plan retrieved successfully", response)
}

// CreatePlan creates a new plan
func (h *PlanHandler) CreatePlan(c *gin.Context) {
	var req dto.PlanRequest
	if err := c.ShouldBindJSON(&req); err != nil {
		utils.RespondWithError(c, http.StatusBadRequest, "Invalid request body")
		return
	}

	response, err := h.planService.CreatePlan(req)
	if err != nil {
		utils.HandleError(c, err)
		return
	}

	utils.RespondWithCreated(c, "Plan created successfully", response)
}

// UpdatePlan updates a plan
func (h *PlanHandler) UpdatePlan(c *gin.Context) {
	id, err := strconv.ParseUint(c.Param("id"), 10, 32)
	if err != nil {
		utils.RespondWithError(c, http.StatusBadRequest, "Invalid plan ID")
		return
	}

	var req dto.PlanRequest
	if err := c.ShouldBindJSON(&req); err != nil {
		utils.RespondWithError(c, http.StatusBadRequest, "Invalid request body")
		return
	}

	response, err := h.planService.UpdatePlan(uint(id), req)
	if err != nil {
		utils.HandleError(c, err)
		return
	}

	utils.RespondWithSuccess(c, "Plan updated successfully", response)
}

// DeletePlan deletes a plan
func (h *PlanHandler) DeletePlan(c *gin.Context) {
	id, err := strconv.ParseUint(c.Param("id"), 10, 32)
	if err != nil {
		utils.RespondWithError(c, http.StatusBadRequest, "Invalid plan ID")
		return
	}

	if err := h.planService.DeletePlan(uint(id)); err != nil {
		utils.HandleError(c, err)
		return
	}

	utils.RespondWithSuccess(c, "Plan deleted successfully", nil)
}
