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

// EmployeeHandler handles employee endpoints
type EmployeeHandler struct {
	employeeService *service.EmployeeService
}

// NewEmployeeHandler creates a new EmployeeHandler
func NewEmployeeHandler(employeeService *service.EmployeeService) *EmployeeHandler {
	return &EmployeeHandler{employeeService: employeeService}
}

// CreateEmployee creates a new employee
func (h *EmployeeHandler) CreateEmployee(c *gin.Context) {
	companyID := middleware.GetCompanyID(c)

	var req dto.EmployeeRequest
	if err := c.ShouldBindJSON(&req); err != nil {
		utils.RespondWithError(c, http.StatusBadRequest, "Invalid request body")
		return
	}

	response, err := h.employeeService.CreateEmployee(companyID, req)
	if err != nil {
		utils.HandleError(c, err)
		return
	}

	utils.RespondWithCreated(c, "Employee created successfully", response)
}

// GetEmployee gets an employee by ID
func (h *EmployeeHandler) GetEmployee(c *gin.Context) {
	companyID := middleware.GetCompanyID(c)
	id, err := strconv.ParseUint(c.Param("id"), 10, 32)
	if err != nil {
		utils.RespondWithError(c, http.StatusBadRequest, "Invalid employee ID")
		return
	}

	response, err := h.employeeService.GetEmployee(uint(id), companyID)
	if err != nil {
		utils.HandleError(c, err)
		return
	}

	utils.RespondWithSuccess(c, "Employee retrieved successfully", response)
}

// GetAllEmployees gets all employees
func (h *EmployeeHandler) GetAllEmployees(c *gin.Context) {
	companyID := middleware.GetCompanyID(c)

	response, err := h.employeeService.GetAllEmployees(companyID)
	if err != nil {
		utils.HandleError(c, err)
		return
	}

	utils.RespondWithSuccess(c, "Employees retrieved successfully", response)
}

// GetActiveEmployees gets all active employees
func (h *EmployeeHandler) GetActiveEmployees(c *gin.Context) {
	companyID := middleware.GetCompanyID(c)

	response, err := h.employeeService.GetActiveEmployees(companyID)
	if err != nil {
		utils.HandleError(c, err)
		return
	}

	utils.RespondWithSuccess(c, "Active employees retrieved successfully", response)
}

// CountActiveEmployees counts active employees
func (h *EmployeeHandler) CountActiveEmployees(c *gin.Context) {
	companyID := middleware.GetCompanyID(c)

	count, err := h.employeeService.CountActiveEmployees(companyID)
	if err != nil {
		utils.HandleError(c, err)
		return
	}

	utils.RespondWithSuccess(c, "Active employee count retrieved", map[string]int64{"count": count})
}

// UpdateEmployee updates an employee
func (h *EmployeeHandler) UpdateEmployee(c *gin.Context) {
	companyID := middleware.GetCompanyID(c)
	id, err := strconv.ParseUint(c.Param("id"), 10, 32)
	if err != nil {
		utils.RespondWithError(c, http.StatusBadRequest, "Invalid employee ID")
		return
	}

	var req dto.EmployeeUpdateRequest
	if err := c.ShouldBindJSON(&req); err != nil {
		utils.RespondWithError(c, http.StatusBadRequest, "Invalid request body")
		return
	}

	response, err := h.employeeService.UpdateEmployee(uint(id), companyID, req)
	if err != nil {
		utils.HandleError(c, err)
		return
	}

	utils.RespondWithSuccess(c, "Employee updated successfully", response)
}

// DeactivateEmployee deactivates an employee
func (h *EmployeeHandler) DeactivateEmployee(c *gin.Context) {
	companyID := middleware.GetCompanyID(c)
	id, err := strconv.ParseUint(c.Param("id"), 10, 32)
	if err != nil {
		utils.RespondWithError(c, http.StatusBadRequest, "Invalid employee ID")
		return
	}

	if err := h.employeeService.DeactivateEmployee(uint(id), companyID); err != nil {
		utils.HandleError(c, err)
		return
	}

	utils.RespondWithSuccess(c, "Employee deactivated successfully", nil)
}

// ReactivateEmployee reactivates an employee
func (h *EmployeeHandler) ReactivateEmployee(c *gin.Context) {
	companyID := middleware.GetCompanyID(c)
	id, err := strconv.ParseUint(c.Param("id"), 10, 32)
	if err != nil {
		utils.RespondWithError(c, http.StatusBadRequest, "Invalid employee ID")
		return
	}

	response, err := h.employeeService.ReactivateEmployee(uint(id), companyID)
	if err != nil {
		utils.HandleError(c, err)
		return
	}

	utils.RespondWithSuccess(c, "Employee reactivated successfully", response)
}
