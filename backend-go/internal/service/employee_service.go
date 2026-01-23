package service

import (
	"github.com/leavemarker/backend-go/internal/dto"
	"github.com/leavemarker/backend-go/internal/enums"
	"github.com/leavemarker/backend-go/internal/models"
	"github.com/leavemarker/backend-go/internal/repository"
	"github.com/leavemarker/backend-go/internal/utils"
)

// EmployeeService handles employee business logic
type EmployeeService struct {
	employeeRepo          *repository.EmployeeRepository
	planValidationService *PlanValidationService
}

// NewEmployeeService creates a new EmployeeService
func NewEmployeeService(
	employeeRepo *repository.EmployeeRepository,
	planValidationService *PlanValidationService,
) *EmployeeService {
	return &EmployeeService{
		employeeRepo:          employeeRepo,
		planValidationService: planValidationService,
	}
}

// CreateEmployee creates a new employee
func (s *EmployeeService) CreateEmployee(companyID uint, req dto.EmployeeRequest) (*dto.EmployeeResponse, error) {
	// Validate plan limit
	if err := s.planValidationService.ValidateEmployeeLimit(companyID); err != nil {
		return nil, err
	}

	// Check if email exists
	exists, err := s.employeeRepo.ExistsByEmail(req.Email)
	if err != nil {
		return nil, utils.NewInternalServerError("Failed to check email")
	}
	if exists {
		return nil, utils.NewBadRequestError("Email already exists")
	}

	// Check if employee ID exists in company
	existing, err := s.employeeRepo.FindByCompanyIDAndEmployeeID(companyID, req.EmployeeID)
	if err == nil && existing != nil {
		return nil, utils.NewBadRequestError("Employee ID already exists in company")
	}

	// Hash password
	hashedPassword, err := utils.HashPassword(req.Password)
	if err != nil {
		return nil, utils.NewInternalServerError("Failed to hash password")
	}

	employee := &models.Employee{
		CompanyID:      companyID,
		EmployeeID:     req.EmployeeID,
		FullName:       req.FullName,
		Email:          req.Email,
		Password:       hashedPassword,
		Role:           req.Role,
		Department:     req.Department,
		JobTitle:       req.JobTitle,
		DateOfJoining:  req.DateOfJoining,
		EmploymentType: req.EmploymentType,
		WorkLocation:   req.WorkLocation,
		ManagerID:      req.ManagerID,
		Status:         enums.EmployeeStatusActive,
	}

	if err := s.employeeRepo.Create(employee); err != nil {
		return nil, utils.NewInternalServerError("Failed to create employee")
	}

	return s.toEmployeeResponse(employee), nil
}

// GetEmployee gets an employee by ID
func (s *EmployeeService) GetEmployee(id, companyID uint) (*dto.EmployeeResponse, error) {
	employee, err := s.employeeRepo.FindByID(id)
	if err != nil {
		return nil, utils.NewNotFoundError("Employee not found")
	}

	if employee.CompanyID != companyID {
		return nil, utils.NewForbiddenError("Access denied")
	}

	return s.toEmployeeResponse(employee), nil
}

// GetAllEmployees gets all employees for a company
func (s *EmployeeService) GetAllEmployees(companyID uint) ([]dto.EmployeeResponse, error) {
	employees, err := s.employeeRepo.FindByCompanyID(companyID)
	if err != nil {
		return nil, utils.NewInternalServerError("Failed to get employees")
	}

	responses := make([]dto.EmployeeResponse, len(employees))
	for i, emp := range employees {
		responses[i] = *s.toEmployeeResponse(&emp)
	}

	return responses, nil
}

// GetActiveEmployees gets all active employees for a company
func (s *EmployeeService) GetActiveEmployees(companyID uint) ([]dto.EmployeeResponse, error) {
	employees, err := s.employeeRepo.FindActiveByCompanyID(companyID)
	if err != nil {
		return nil, utils.NewInternalServerError("Failed to get employees")
	}

	responses := make([]dto.EmployeeResponse, len(employees))
	for i, emp := range employees {
		responses[i] = *s.toEmployeeResponse(&emp)
	}

	return responses, nil
}

// CountActiveEmployees counts active employees for a company
func (s *EmployeeService) CountActiveEmployees(companyID uint) (int64, error) {
	return s.employeeRepo.CountActiveByCompanyID(companyID)
}

// UpdateEmployee updates an employee
func (s *EmployeeService) UpdateEmployee(id, companyID uint, req dto.EmployeeUpdateRequest) (*dto.EmployeeResponse, error) {
	employee, err := s.employeeRepo.FindByID(id)
	if err != nil {
		return nil, utils.NewNotFoundError("Employee not found")
	}

	if employee.CompanyID != companyID {
		return nil, utils.NewForbiddenError("Access denied")
	}

	// Check if email is being changed and if new email exists
	if req.Email != "" && req.Email != employee.Email {
		exists, err := s.employeeRepo.ExistsByEmail(req.Email)
		if err != nil {
			return nil, utils.NewInternalServerError("Failed to check email")
		}
		if exists {
			return nil, utils.NewBadRequestError("Email already exists")
		}
		employee.Email = req.Email
	}

	if req.EmployeeID != "" {
		employee.EmployeeID = req.EmployeeID
	}
	if req.FullName != "" {
		employee.FullName = req.FullName
	}
	if req.Role != "" {
		employee.Role = req.Role
	}
	if req.Department != "" {
		employee.Department = req.Department
	}
	if req.JobTitle != "" {
		employee.JobTitle = req.JobTitle
	}
	if req.DateOfJoining != nil {
		employee.DateOfJoining = req.DateOfJoining
	}
	if req.EmploymentType != "" {
		employee.EmploymentType = req.EmploymentType
	}
	if req.WorkLocation != "" {
		employee.WorkLocation = req.WorkLocation
	}
	employee.ManagerID = req.ManagerID

	if err := s.employeeRepo.Update(employee); err != nil {
		return nil, utils.NewInternalServerError("Failed to update employee")
	}

	return s.toEmployeeResponse(employee), nil
}

// DeactivateEmployee deactivates an employee
func (s *EmployeeService) DeactivateEmployee(id, companyID uint) error {
	employee, err := s.employeeRepo.FindByID(id)
	if err != nil {
		return utils.NewNotFoundError("Employee not found")
	}

	if employee.CompanyID != companyID {
		return utils.NewForbiddenError("Access denied")
	}

	employee.Status = enums.EmployeeStatusInactive
	return s.employeeRepo.Update(employee)
}

// ReactivateEmployee reactivates an employee
func (s *EmployeeService) ReactivateEmployee(id, companyID uint) (*dto.EmployeeResponse, error) {
	employee, err := s.employeeRepo.FindByID(id)
	if err != nil {
		return nil, utils.NewNotFoundError("Employee not found")
	}

	if employee.CompanyID != companyID {
		return nil, utils.NewForbiddenError("Access denied")
	}

	// Validate plan limit before reactivating
	if err := s.planValidationService.ValidateEmployeeLimit(companyID); err != nil {
		return nil, err
	}

	employee.Status = enums.EmployeeStatusActive
	if err := s.employeeRepo.Update(employee); err != nil {
		return nil, utils.NewInternalServerError("Failed to reactivate employee")
	}

	return s.toEmployeeResponse(employee), nil
}

func (s *EmployeeService) toEmployeeResponse(emp *models.Employee) *dto.EmployeeResponse {
	response := &dto.EmployeeResponse{
		ID:             emp.ID,
		EmployeeID:     emp.EmployeeID,
		FullName:       emp.FullName,
		Email:          emp.Email,
		Role:           emp.Role,
		Department:     emp.Department,
		JobTitle:       emp.JobTitle,
		DateOfJoining:  emp.DateOfJoining,
		EmploymentType: emp.EmploymentType,
		WorkLocation:   emp.WorkLocation,
		Status:         emp.Status,
		ManagerID:      emp.ManagerID,
		CompanyID:      emp.CompanyID,
		CreatedAt:      emp.CreatedAt,
		UpdatedAt:      emp.UpdatedAt,
	}

	if emp.Manager != nil {
		response.ManagerName = emp.Manager.FullName
	}

	if emp.Company.ID != 0 {
		response.CompanyName = emp.Company.Name
	}

	return response
}
