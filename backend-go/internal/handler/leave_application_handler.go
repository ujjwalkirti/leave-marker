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

// LeaveApplicationHandler handles leave application endpoints
type LeaveApplicationHandler struct {
	leaveApplicationService *service.LeaveApplicationService
}

// NewLeaveApplicationHandler creates a new LeaveApplicationHandler
func NewLeaveApplicationHandler(leaveApplicationService *service.LeaveApplicationService) *LeaveApplicationHandler {
	return &LeaveApplicationHandler{leaveApplicationService: leaveApplicationService}
}

// ApplyLeave creates a new leave application
func (h *LeaveApplicationHandler) ApplyLeave(c *gin.Context) {
	userID := middleware.GetUserID(c)

	var req dto.LeaveApplicationRequest
	if err := c.ShouldBindJSON(&req); err != nil {
		utils.RespondWithError(c, http.StatusBadRequest, "Invalid request body")
		return
	}

	response, err := h.leaveApplicationService.ApplyLeave(userID, req)
	if err != nil {
		utils.HandleError(c, err)
		return
	}

	utils.RespondWithCreated(c, "Leave application submitted successfully", response)
}

// GetLeaveApplication gets a leave application by ID
func (h *LeaveApplicationHandler) GetLeaveApplication(c *gin.Context) {
	companyID := middleware.GetCompanyID(c)
	id, err := strconv.ParseUint(c.Param("id"), 10, 32)
	if err != nil {
		utils.RespondWithError(c, http.StatusBadRequest, "Invalid application ID")
		return
	}

	response, err := h.leaveApplicationService.GetLeaveApplication(uint(id), companyID)
	if err != nil {
		utils.HandleError(c, err)
		return
	}

	utils.RespondWithSuccess(c, "Leave application retrieved successfully", response)
}

// GetMyLeaveApplications gets leave applications for the current user
func (h *LeaveApplicationHandler) GetMyLeaveApplications(c *gin.Context) {
	userID := middleware.GetUserID(c)

	response, err := h.leaveApplicationService.GetMyLeaveApplications(userID)
	if err != nil {
		utils.HandleError(c, err)
		return
	}

	utils.RespondWithSuccess(c, "Leave applications retrieved successfully", response)
}

// CountPendingApplications counts pending applications
func (h *LeaveApplicationHandler) CountPendingApplications(c *gin.Context) {
	userID := middleware.GetUserID(c)

	count, err := h.leaveApplicationService.CountPendingApplications(userID)
	if err != nil {
		utils.HandleError(c, err)
		return
	}

	utils.RespondWithSuccess(c, "Pending count retrieved", map[string]int64{"count": count})
}

// GetPendingApprovalsForManager gets pending approvals for a manager
func (h *LeaveApplicationHandler) GetPendingApprovalsForManager(c *gin.Context) {
	userID := middleware.GetUserID(c)

	response, err := h.leaveApplicationService.GetPendingApprovalsForManager(userID)
	if err != nil {
		utils.HandleError(c, err)
		return
	}

	utils.RespondWithSuccess(c, "Pending approvals retrieved successfully", response)
}

// GetPendingApprovalsForHR gets pending approvals for HR
func (h *LeaveApplicationHandler) GetPendingApprovalsForHR(c *gin.Context) {
	companyID := middleware.GetCompanyID(c)

	response, err := h.leaveApplicationService.GetPendingApprovalsForHR(companyID)
	if err != nil {
		utils.HandleError(c, err)
		return
	}

	utils.RespondWithSuccess(c, "Pending approvals retrieved successfully", response)
}

// GetLeaveApplicationsByDateRange gets leave applications by date range
func (h *LeaveApplicationHandler) GetLeaveApplicationsByDateRange(c *gin.Context) {
	companyID := middleware.GetCompanyID(c)

	var req dto.DateRangeRequest
	if err := c.ShouldBindQuery(&req); err != nil {
		utils.RespondWithError(c, http.StatusBadRequest, "Invalid date range")
		return
	}

	response, err := h.leaveApplicationService.GetLeaveApplicationsByDateRange(companyID, req.StartDate, req.EndDate)
	if err != nil {
		utils.HandleError(c, err)
		return
	}

	utils.RespondWithSuccess(c, "Leave applications retrieved successfully", response)
}

// ApproveByManager approves a leave application by manager
func (h *LeaveApplicationHandler) ApproveByManager(c *gin.Context) {
	userID := middleware.GetUserID(c)
	id, err := strconv.ParseUint(c.Param("id"), 10, 32)
	if err != nil {
		utils.RespondWithError(c, http.StatusBadRequest, "Invalid application ID")
		return
	}

	var req dto.LeaveApprovalRequest
	if err := c.ShouldBindJSON(&req); err != nil {
		utils.RespondWithError(c, http.StatusBadRequest, "Invalid request body")
		return
	}

	response, err := h.leaveApplicationService.ApproveByManager(uint(id), userID, req)
	if err != nil {
		utils.HandleError(c, err)
		return
	}

	utils.RespondWithSuccess(c, "Leave application processed successfully", response)
}

// ApproveByHR approves a leave application by HR
func (h *LeaveApplicationHandler) ApproveByHR(c *gin.Context) {
	userID := middleware.GetUserID(c)
	id, err := strconv.ParseUint(c.Param("id"), 10, 32)
	if err != nil {
		utils.RespondWithError(c, http.StatusBadRequest, "Invalid application ID")
		return
	}

	var req dto.LeaveApprovalRequest
	if err := c.ShouldBindJSON(&req); err != nil {
		utils.RespondWithError(c, http.StatusBadRequest, "Invalid request body")
		return
	}

	response, err := h.leaveApplicationService.ApproveByHR(uint(id), userID, req)
	if err != nil {
		utils.HandleError(c, err)
		return
	}

	utils.RespondWithSuccess(c, "Leave application processed successfully", response)
}

// CancelLeave cancels a leave application
func (h *LeaveApplicationHandler) CancelLeave(c *gin.Context) {
	userID := middleware.GetUserID(c)
	id, err := strconv.ParseUint(c.Param("id"), 10, 32)
	if err != nil {
		utils.RespondWithError(c, http.StatusBadRequest, "Invalid application ID")
		return
	}

	response, err := h.leaveApplicationService.CancelLeave(uint(id), userID)
	if err != nil {
		utils.HandleError(c, err)
		return
	}

	utils.RespondWithSuccess(c, "Leave application cancelled successfully", response)
}
