package repository

import (
	"github.com/leavemarker/backend-go/internal/enums"
	"github.com/leavemarker/backend-go/internal/models"
	"gorm.io/gorm"
)

// EmployeeRepository handles employee data access
type EmployeeRepository struct {
	db *gorm.DB
}

// NewEmployeeRepository creates a new EmployeeRepository
func NewEmployeeRepository(db *gorm.DB) *EmployeeRepository {
	return &EmployeeRepository{db: db}
}

// Create creates a new employee
func (r *EmployeeRepository) Create(employee *models.Employee) error {
	return r.db.Create(employee).Error
}

// FindByID finds an employee by ID
func (r *EmployeeRepository) FindByID(id uint) (*models.Employee, error) {
	var employee models.Employee
	err := r.db.Preload("Company").Preload("Manager").Where("id = ? AND deleted = false", id).First(&employee).Error
	if err != nil {
		return nil, err
	}
	return &employee, nil
}

// FindByEmail finds an employee by email
func (r *EmployeeRepository) FindByEmail(email string) (*models.Employee, error) {
	var employee models.Employee
	err := r.db.Preload("Company").Where("email = ? AND deleted = false", email).First(&employee).Error
	if err != nil {
		return nil, err
	}
	return &employee, nil
}

// FindByCompanyID finds all employees by company ID
func (r *EmployeeRepository) FindByCompanyID(companyID uint) ([]models.Employee, error) {
	var employees []models.Employee
	err := r.db.Preload("Manager").Where("company_id = ? AND deleted = false", companyID).Find(&employees).Error
	return employees, err
}

// FindActiveByCompanyID finds all active employees by company ID
func (r *EmployeeRepository) FindActiveByCompanyID(companyID uint) ([]models.Employee, error) {
	var employees []models.Employee
	err := r.db.Preload("Manager").Where("company_id = ? AND status = ? AND deleted = false", companyID, enums.EmployeeStatusActive).Find(&employees).Error
	return employees, err
}

// CountActiveByCompanyID counts active employees by company ID
func (r *EmployeeRepository) CountActiveByCompanyID(companyID uint) (int64, error) {
	var count int64
	err := r.db.Model(&models.Employee{}).Where("company_id = ? AND status = ? AND deleted = false", companyID, enums.EmployeeStatusActive).Count(&count).Error
	return count, err
}

// FindByCompanyIDAndEmployeeID finds an employee by company ID and employee ID
func (r *EmployeeRepository) FindByCompanyIDAndEmployeeID(companyID uint, employeeID string) (*models.Employee, error) {
	var employee models.Employee
	err := r.db.Where("company_id = ? AND employee_id = ? AND deleted = false", companyID, employeeID).First(&employee).Error
	if err != nil {
		return nil, err
	}
	return &employee, nil
}

// FindByManagerID finds all employees by manager ID
func (r *EmployeeRepository) FindByManagerID(managerID uint) ([]models.Employee, error) {
	var employees []models.Employee
	err := r.db.Where("manager_id = ? AND deleted = false", managerID).Find(&employees).Error
	return employees, err
}

// Update updates an employee
func (r *EmployeeRepository) Update(employee *models.Employee) error {
	return r.db.Save(employee).Error
}

// Delete soft deletes an employee
func (r *EmployeeRepository) Delete(id uint) error {
	return r.db.Model(&models.Employee{}).Where("id = ?", id).Update("deleted", true).Error
}

// ExistsByEmail checks if an employee with the given email exists
func (r *EmployeeRepository) ExistsByEmail(email string) (bool, error) {
	var count int64
	err := r.db.Model(&models.Employee{}).Where("email = ? AND deleted = false", email).Count(&count).Error
	return count > 0, err
}

// FindByPasswordResetToken finds an employee by password reset token
func (r *EmployeeRepository) FindByPasswordResetToken(token string) (*models.Employee, error) {
	var employee models.Employee
	err := r.db.Where("password_reset_token = ? AND deleted = false", token).First(&employee).Error
	if err != nil {
		return nil, err
	}
	return &employee, nil
}
