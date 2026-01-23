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

// HolidayHandler handles holiday endpoints
type HolidayHandler struct {
	holidayService *service.HolidayService
}

// NewHolidayHandler creates a new HolidayHandler
func NewHolidayHandler(holidayService *service.HolidayService) *HolidayHandler {
	return &HolidayHandler{holidayService: holidayService}
}

// CreateHoliday creates a new holiday
func (h *HolidayHandler) CreateHoliday(c *gin.Context) {
	companyID := middleware.GetCompanyID(c)

	var req dto.HolidayRequest
	if err := c.ShouldBindJSON(&req); err != nil {
		utils.RespondWithError(c, http.StatusBadRequest, "Invalid request body")
		return
	}

	response, err := h.holidayService.CreateHoliday(companyID, req)
	if err != nil {
		utils.HandleError(c, err)
		return
	}

	utils.RespondWithCreated(c, "Holiday created successfully", response)
}

// GetHoliday gets a holiday by ID
func (h *HolidayHandler) GetHoliday(c *gin.Context) {
	companyID := middleware.GetCompanyID(c)
	id, err := strconv.ParseUint(c.Param("id"), 10, 32)
	if err != nil {
		utils.RespondWithError(c, http.StatusBadRequest, "Invalid holiday ID")
		return
	}

	response, err := h.holidayService.GetHoliday(uint(id), companyID)
	if err != nil {
		utils.HandleError(c, err)
		return
	}

	utils.RespondWithSuccess(c, "Holiday retrieved successfully", response)
}

// GetAllHolidays gets all holidays
func (h *HolidayHandler) GetAllHolidays(c *gin.Context) {
	companyID := middleware.GetCompanyID(c)

	response, err := h.holidayService.GetAllHolidays(companyID)
	if err != nil {
		utils.HandleError(c, err)
		return
	}

	utils.RespondWithSuccess(c, "Holidays retrieved successfully", response)
}

// GetActiveHolidays gets all active holidays
func (h *HolidayHandler) GetActiveHolidays(c *gin.Context) {
	companyID := middleware.GetCompanyID(c)

	response, err := h.holidayService.GetActiveHolidays(companyID)
	if err != nil {
		utils.HandleError(c, err)
		return
	}

	utils.RespondWithSuccess(c, "Active holidays retrieved successfully", response)
}

// GetHolidaysByDateRange gets holidays by date range
func (h *HolidayHandler) GetHolidaysByDateRange(c *gin.Context) {
	companyID := middleware.GetCompanyID(c)

	var req dto.DateRangeRequest
	if err := c.ShouldBindQuery(&req); err != nil {
		utils.RespondWithError(c, http.StatusBadRequest, "Invalid date range")
		return
	}

	response, err := h.holidayService.GetHolidaysByDateRange(companyID, req.StartDate, req.EndDate)
	if err != nil {
		utils.HandleError(c, err)
		return
	}

	utils.RespondWithSuccess(c, "Holidays retrieved successfully", response)
}

// UpdateHoliday updates a holiday
func (h *HolidayHandler) UpdateHoliday(c *gin.Context) {
	companyID := middleware.GetCompanyID(c)
	id, err := strconv.ParseUint(c.Param("id"), 10, 32)
	if err != nil {
		utils.RespondWithError(c, http.StatusBadRequest, "Invalid holiday ID")
		return
	}

	var req dto.HolidayRequest
	if err := c.ShouldBindJSON(&req); err != nil {
		utils.RespondWithError(c, http.StatusBadRequest, "Invalid request body")
		return
	}

	response, err := h.holidayService.UpdateHoliday(uint(id), companyID, req)
	if err != nil {
		utils.HandleError(c, err)
		return
	}

	utils.RespondWithSuccess(c, "Holiday updated successfully", response)
}

// DeleteHoliday deletes a holiday
func (h *HolidayHandler) DeleteHoliday(c *gin.Context) {
	companyID := middleware.GetCompanyID(c)
	id, err := strconv.ParseUint(c.Param("id"), 10, 32)
	if err != nil {
		utils.RespondWithError(c, http.StatusBadRequest, "Invalid holiday ID")
		return
	}

	if err := h.holidayService.DeleteHoliday(uint(id), companyID); err != nil {
		utils.HandleError(c, err)
		return
	}

	utils.RespondWithSuccess(c, "Holiday deleted successfully", nil)
}
