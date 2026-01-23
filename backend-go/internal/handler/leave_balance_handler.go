package handler

import (
	"net/http"
	"strconv"

	"github.com/gin-gonic/gin"
	"github.com/leavemarker/backend-go/internal/middleware"
	"github.com/leavemarker/backend-go/internal/service"
	"github.com/leavemarker/backend-go/internal/utils"
)

// LeaveBalanceHandler handles leave balance endpoints
type LeaveBalanceHandler struct {
	leaveBalanceService *service.LeaveBalanceService
}

// NewLeaveBalanceHandler creates a new LeaveBalanceHandler
func NewLeaveBalanceHandler(leaveBalanceService *service.LeaveBalanceService) *LeaveBalanceHandler {
	return &LeaveBalanceHandler{leaveBalanceService: leaveBalanceService}
}

// GetMyLeaveBalances gets leave balances for the current user
func (h *LeaveBalanceHandler) GetMyLeaveBalances(c *gin.Context) {
	userID := middleware.GetUserID(c)
	companyID := middleware.GetCompanyID(c)

	response, err := h.leaveBalanceService.GetLeaveBalances(userID, companyID)
	if err != nil {
		utils.HandleError(c, err)
		return
	}

	utils.RespondWithSuccess(c, "Leave balances retrieved successfully", response)
}

// GetLeaveBalancesByYear gets leave balances by year
func (h *LeaveBalanceHandler) GetLeaveBalancesByYear(c *gin.Context) {
	userID := middleware.GetUserID(c)
	companyID := middleware.GetCompanyID(c)
	year, err := strconv.Atoi(c.Query("year"))
	if err != nil {
		utils.RespondWithError(c, http.StatusBadRequest, "Invalid year")
		return
	}

	response, err := h.leaveBalanceService.GetLeaveBalancesByYear(userID, companyID, year)
	if err != nil {
		utils.HandleError(c, err)
		return
	}

	utils.RespondWithSuccess(c, "Leave balances retrieved successfully", response)
}

// GetEmployeeLeaveBalances gets leave balances for a specific employee (manager/HR only)
func (h *LeaveBalanceHandler) GetEmployeeLeaveBalances(c *gin.Context) {
	companyID := middleware.GetCompanyID(c)
	employeeID, err := strconv.ParseUint(c.Param("employeeId"), 10, 32)
	if err != nil {
		utils.RespondWithError(c, http.StatusBadRequest, "Invalid employee ID")
		return
	}

	response, err := h.leaveBalanceService.GetLeaveBalances(uint(employeeID), companyID)
	if err != nil {
		utils.HandleError(c, err)
		return
	}

	utils.RespondWithSuccess(c, "Leave balances retrieved successfully", response)
}
