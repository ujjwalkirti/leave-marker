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

// LeavePolicyHandler handles leave policy endpoints
type LeavePolicyHandler struct {
	leavePolicyService *service.LeavePolicyService
}

// NewLeavePolicyHandler creates a new LeavePolicyHandler
func NewLeavePolicyHandler(leavePolicyService *service.LeavePolicyService) *LeavePolicyHandler {
	return &LeavePolicyHandler{leavePolicyService: leavePolicyService}
}

// CreateLeavePolicy creates a new leave policy
func (h *LeavePolicyHandler) CreateLeavePolicy(c *gin.Context) {
	companyID := middleware.GetCompanyID(c)

	var req dto.LeavePolicyRequest
	if err := c.ShouldBindJSON(&req); err != nil {
		utils.RespondWithError(c, http.StatusBadRequest, "Invalid request body")
		return
	}

	response, err := h.leavePolicyService.CreateLeavePolicy(companyID, req)
	if err != nil {
		utils.HandleError(c, err)
		return
	}

	utils.RespondWithCreated(c, "Leave policy created successfully", response)
}

// GetLeavePolicy gets a leave policy by ID
func (h *LeavePolicyHandler) GetLeavePolicy(c *gin.Context) {
	companyID := middleware.GetCompanyID(c)
	id, err := strconv.ParseUint(c.Param("id"), 10, 32)
	if err != nil {
		utils.RespondWithError(c, http.StatusBadRequest, "Invalid policy ID")
		return
	}

	response, err := h.leavePolicyService.GetLeavePolicy(uint(id), companyID)
	if err != nil {
		utils.HandleError(c, err)
		return
	}

	utils.RespondWithSuccess(c, "Leave policy retrieved successfully", response)
}

// GetAllLeavePolicies gets all leave policies
func (h *LeavePolicyHandler) GetAllLeavePolicies(c *gin.Context) {
	companyID := middleware.GetCompanyID(c)

	response, err := h.leavePolicyService.GetAllLeavePolicies(companyID)
	if err != nil {
		utils.HandleError(c, err)
		return
	}

	utils.RespondWithSuccess(c, "Leave policies retrieved successfully", response)
}

// GetActiveLeavePolicies gets all active leave policies
func (h *LeavePolicyHandler) GetActiveLeavePolicies(c *gin.Context) {
	companyID := middleware.GetCompanyID(c)

	response, err := h.leavePolicyService.GetActiveLeavePolicies(companyID)
	if err != nil {
		utils.HandleError(c, err)
		return
	}

	utils.RespondWithSuccess(c, "Active leave policies retrieved successfully", response)
}

// UpdateLeavePolicy updates a leave policy
func (h *LeavePolicyHandler) UpdateLeavePolicy(c *gin.Context) {
	companyID := middleware.GetCompanyID(c)
	id, err := strconv.ParseUint(c.Param("id"), 10, 32)
	if err != nil {
		utils.RespondWithError(c, http.StatusBadRequest, "Invalid policy ID")
		return
	}

	var req dto.LeavePolicyRequest
	if err := c.ShouldBindJSON(&req); err != nil {
		utils.RespondWithError(c, http.StatusBadRequest, "Invalid request body")
		return
	}

	response, err := h.leavePolicyService.UpdateLeavePolicy(uint(id), companyID, req)
	if err != nil {
		utils.HandleError(c, err)
		return
	}

	utils.RespondWithSuccess(c, "Leave policy updated successfully", response)
}

// DeleteLeavePolicy deletes a leave policy
func (h *LeavePolicyHandler) DeleteLeavePolicy(c *gin.Context) {
	companyID := middleware.GetCompanyID(c)
	id, err := strconv.ParseUint(c.Param("id"), 10, 32)
	if err != nil {
		utils.RespondWithError(c, http.StatusBadRequest, "Invalid policy ID")
		return
	}

	if err := h.leavePolicyService.DeleteLeavePolicy(uint(id), companyID); err != nil {
		utils.HandleError(c, err)
		return
	}

	utils.RespondWithSuccess(c, "Leave policy deleted successfully", nil)
}
