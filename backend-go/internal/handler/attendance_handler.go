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

// AttendanceHandler handles attendance endpoints
type AttendanceHandler struct {
	attendanceService *service.AttendanceService
}

// NewAttendanceHandler creates a new AttendanceHandler
func NewAttendanceHandler(attendanceService *service.AttendanceService) *AttendanceHandler {
	return &AttendanceHandler{attendanceService: attendanceService}
}

// PunchInOut handles punch in/out
func (h *AttendanceHandler) PunchInOut(c *gin.Context) {
	userID := middleware.GetUserID(c)
	companyID := middleware.GetCompanyID(c)

	var req dto.AttendancePunchRequest
	if err := c.ShouldBindJSON(&req); err != nil {
		utils.RespondWithError(c, http.StatusBadRequest, "Invalid request body")
		return
	}

	response, err := h.attendanceService.PunchInOut(userID, companyID, req)
	if err != nil {
		utils.HandleError(c, err)
		return
	}

	utils.RespondWithSuccess(c, "Attendance recorded successfully", response)
}

// GetTodayAttendance gets today's attendance
func (h *AttendanceHandler) GetTodayAttendance(c *gin.Context) {
	userID := middleware.GetUserID(c)

	response, err := h.attendanceService.GetTodayAttendance(userID)
	if err != nil {
		utils.HandleError(c, err)
		return
	}

	utils.RespondWithSuccess(c, "Attendance retrieved successfully", response)
}

// GetAttendance gets an attendance record by ID
func (h *AttendanceHandler) GetAttendance(c *gin.Context) {
	companyID := middleware.GetCompanyID(c)
	id, err := strconv.ParseUint(c.Param("id"), 10, 32)
	if err != nil {
		utils.RespondWithError(c, http.StatusBadRequest, "Invalid attendance ID")
		return
	}

	response, err := h.attendanceService.GetAttendance(uint(id), companyID)
	if err != nil {
		utils.HandleError(c, err)
		return
	}

	utils.RespondWithSuccess(c, "Attendance retrieved successfully", response)
}

// GetMyAttendance gets all attendance records for the current user
func (h *AttendanceHandler) GetMyAttendance(c *gin.Context) {
	userID := middleware.GetUserID(c)

	response, err := h.attendanceService.GetMyAttendance(userID)
	if err != nil {
		utils.HandleError(c, err)
		return
	}

	utils.RespondWithSuccess(c, "Attendance records retrieved successfully", response)
}

// GetMyAttendanceByDateRange gets attendance by date range
func (h *AttendanceHandler) GetMyAttendanceByDateRange(c *gin.Context) {
	userID := middleware.GetUserID(c)

	var req dto.DateRangeRequest
	if err := c.ShouldBindQuery(&req); err != nil {
		utils.RespondWithError(c, http.StatusBadRequest, "Invalid date range")
		return
	}

	response, err := h.attendanceService.GetMyAttendanceByDateRange(userID, req.StartDate, req.EndDate)
	if err != nil {
		utils.HandleError(c, err)
		return
	}

	utils.RespondWithSuccess(c, "Attendance records retrieved successfully", response)
}

// GetAttendanceRate gets attendance rate
func (h *AttendanceHandler) GetAttendanceRate(c *gin.Context) {
	userID := middleware.GetUserID(c)
	companyID := middleware.GetCompanyID(c)

	var req dto.DateRangeRequest
	if err := c.ShouldBindQuery(&req); err != nil {
		utils.RespondWithError(c, http.StatusBadRequest, "Invalid date range")
		return
	}

	response, err := h.attendanceService.GetAttendanceRate(userID, companyID, req.StartDate, req.EndDate)
	if err != nil {
		utils.HandleError(c, err)
		return
	}

	utils.RespondWithSuccess(c, "Attendance rate retrieved successfully", response)
}

// GetAttendanceByDateRange gets attendance by date range for company
func (h *AttendanceHandler) GetAttendanceByDateRange(c *gin.Context) {
	companyID := middleware.GetCompanyID(c)

	var req dto.DateRangeRequest
	if err := c.ShouldBindQuery(&req); err != nil {
		utils.RespondWithError(c, http.StatusBadRequest, "Invalid date range")
		return
	}

	response, err := h.attendanceService.GetAttendanceByDateRange(companyID, req.StartDate, req.EndDate)
	if err != nil {
		utils.HandleError(c, err)
		return
	}

	utils.RespondWithSuccess(c, "Attendance records retrieved successfully", response)
}

// RequestCorrection requests attendance correction
func (h *AttendanceHandler) RequestCorrection(c *gin.Context) {
	userID := middleware.GetUserID(c)
	id, err := strconv.ParseUint(c.Param("id"), 10, 32)
	if err != nil {
		utils.RespondWithError(c, http.StatusBadRequest, "Invalid attendance ID")
		return
	}

	var req dto.AttendanceCorrectionRequest
	if err := c.ShouldBindJSON(&req); err != nil {
		utils.RespondWithError(c, http.StatusBadRequest, "Invalid request body")
		return
	}

	response, err := h.attendanceService.RequestCorrection(uint(id), userID, req)
	if err != nil {
		utils.HandleError(c, err)
		return
	}

	utils.RespondWithSuccess(c, "Correction requested successfully", response)
}

// ApproveCorrection approves attendance correction
func (h *AttendanceHandler) ApproveCorrection(c *gin.Context) {
	companyID := middleware.GetCompanyID(c)
	id, err := strconv.ParseUint(c.Param("id"), 10, 32)
	if err != nil {
		utils.RespondWithError(c, http.StatusBadRequest, "Invalid attendance ID")
		return
	}

	response, err := h.attendanceService.ApproveCorrection(uint(id), companyID)
	if err != nil {
		utils.HandleError(c, err)
		return
	}

	utils.RespondWithSuccess(c, "Correction approved successfully", response)
}

// RejectCorrection rejects attendance correction
func (h *AttendanceHandler) RejectCorrection(c *gin.Context) {
	companyID := middleware.GetCompanyID(c)
	id, err := strconv.ParseUint(c.Param("id"), 10, 32)
	if err != nil {
		utils.RespondWithError(c, http.StatusBadRequest, "Invalid attendance ID")
		return
	}

	response, err := h.attendanceService.RejectCorrection(uint(id), companyID)
	if err != nil {
		utils.HandleError(c, err)
		return
	}

	utils.RespondWithSuccess(c, "Correction rejected successfully", response)
}

// GetPendingCorrections gets pending corrections
func (h *AttendanceHandler) GetPendingCorrections(c *gin.Context) {
	companyID := middleware.GetCompanyID(c)

	response, err := h.attendanceService.GetPendingCorrections(companyID)
	if err != nil {
		utils.HandleError(c, err)
		return
	}

	utils.RespondWithSuccess(c, "Pending corrections retrieved successfully", response)
}

// MarkAttendance marks attendance for an employee (HR only)
func (h *AttendanceHandler) MarkAttendance(c *gin.Context) {
	companyID := middleware.GetCompanyID(c)

	var req dto.AttendanceMarkRequest
	if err := c.ShouldBindJSON(&req); err != nil {
		utils.RespondWithError(c, http.StatusBadRequest, "Invalid request body")
		return
	}

	response, err := h.attendanceService.MarkAttendance(companyID, req)
	if err != nil {
		utils.HandleError(c, err)
		return
	}

	utils.RespondWithSuccess(c, "Attendance marked successfully", response)
}
