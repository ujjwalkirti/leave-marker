package repository

import (
	"time"

	"github.com/leavemarker/backend-go/internal/enums"
	"github.com/leavemarker/backend-go/internal/models"
	"gorm.io/gorm"
)

// LeaveApplicationRepository handles leave application data access
type LeaveApplicationRepository struct {
	db *gorm.DB
}

// NewLeaveApplicationRepository creates a new LeaveApplicationRepository
func NewLeaveApplicationRepository(db *gorm.DB) *LeaveApplicationRepository {
	return &LeaveApplicationRepository{db: db}
}

// Create creates a new leave application
func (r *LeaveApplicationRepository) Create(application *models.LeaveApplication) error {
	return r.db.Create(application).Error
}

// FindByID finds a leave application by ID
func (r *LeaveApplicationRepository) FindByID(id uint) (*models.LeaveApplication, error) {
	var application models.LeaveApplication
	err := r.db.Preload("Employee").Preload("ApprovedByManager").Preload("ApprovedByHR").Where("id = ? AND deleted = false", id).First(&application).Error
	if err != nil {
		return nil, err
	}
	return &application, nil
}

// FindByEmployeeID finds all leave applications by employee ID
func (r *LeaveApplicationRepository) FindByEmployeeID(employeeID uint) ([]models.LeaveApplication, error) {
	var applications []models.LeaveApplication
	err := r.db.Where("employee_id = ? AND deleted = false", employeeID).Order("created_at DESC").Find(&applications).Error
	return applications, err
}

// CountPendingByEmployeeID counts pending leave applications by employee ID
func (r *LeaveApplicationRepository) CountPendingByEmployeeID(employeeID uint) (int64, error) {
	var count int64
	err := r.db.Model(&models.LeaveApplication{}).Where("employee_id = ? AND status = ? AND deleted = false", employeeID, enums.LeaveStatusPending).Count(&count).Error
	return count, err
}

// FindPendingByManagerID finds pending leave applications for a manager
func (r *LeaveApplicationRepository) FindPendingByManagerID(managerID uint) ([]models.LeaveApplication, error) {
	var applications []models.LeaveApplication
	err := r.db.Preload("Employee").
		Joins("JOIN employees ON employees.id = leave_applications.employee_id").
		Where("employees.manager_id = ? AND leave_applications.status = ? AND leave_applications.deleted = false", managerID, enums.LeaveStatusPending).
		Order("leave_applications.created_at DESC").
		Find(&applications).Error
	return applications, err
}

// FindPendingForHR finds pending leave applications that require HR approval
func (r *LeaveApplicationRepository) FindPendingForHR(companyID uint) ([]models.LeaveApplication, error) {
	var applications []models.LeaveApplication
	err := r.db.Preload("Employee").
		Joins("JOIN employees ON employees.id = leave_applications.employee_id").
		Where("employees.company_id = ? AND leave_applications.requires_hr_approval = true AND leave_applications.approved_by_manager_id IS NOT NULL AND leave_applications.approved_by_hr_id IS NULL AND leave_applications.status = ? AND leave_applications.deleted = false", companyID, enums.LeaveStatusPending).
		Order("leave_applications.created_at DESC").
		Find(&applications).Error
	return applications, err
}

// FindByCompanyIDAndDateRange finds leave applications by company ID and date range
func (r *LeaveApplicationRepository) FindByCompanyIDAndDateRange(companyID uint, startDate, endDate time.Time) ([]models.LeaveApplication, error) {
	var applications []models.LeaveApplication
	err := r.db.Preload("Employee").
		Joins("JOIN employees ON employees.id = leave_applications.employee_id").
		Where("employees.company_id = ? AND leave_applications.start_date >= ? AND leave_applications.end_date <= ? AND leave_applications.deleted = false", companyID, startDate, endDate).
		Order("leave_applications.start_date ASC").
		Find(&applications).Error
	return applications, err
}

// FindOverlapping finds overlapping leave applications for an employee
func (r *LeaveApplicationRepository) FindOverlapping(employeeID uint, startDate, endDate time.Time, excludeID *uint) ([]models.LeaveApplication, error) {
	var applications []models.LeaveApplication
	query := r.db.Where("employee_id = ? AND status IN (?, ?) AND ((start_date <= ? AND end_date >= ?) OR (start_date <= ? AND end_date >= ?) OR (start_date >= ? AND end_date <= ?)) AND deleted = false",
		employeeID, enums.LeaveStatusPending, enums.LeaveStatusApproved, endDate, startDate, startDate, startDate, startDate, endDate)
	if excludeID != nil {
		query = query.Where("id != ?", *excludeID)
	}
	err := query.Find(&applications).Error
	return applications, err
}

// Update updates a leave application
func (r *LeaveApplicationRepository) Update(application *models.LeaveApplication) error {
	return r.db.Save(application).Error
}

// Delete soft deletes a leave application
func (r *LeaveApplicationRepository) Delete(id uint) error {
	return r.db.Model(&models.LeaveApplication{}).Where("id = ?", id).Update("deleted", true).Error
}
