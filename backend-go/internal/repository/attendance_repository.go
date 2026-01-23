package repository

import (
	"time"

	"github.com/leavemarker/backend-go/internal/enums"
	"github.com/leavemarker/backend-go/internal/models"
	"gorm.io/gorm"
)

// AttendanceRepository handles attendance data access
type AttendanceRepository struct {
	db *gorm.DB
}

// NewAttendanceRepository creates a new AttendanceRepository
func NewAttendanceRepository(db *gorm.DB) *AttendanceRepository {
	return &AttendanceRepository{db: db}
}

// Create creates a new attendance record
func (r *AttendanceRepository) Create(attendance *models.Attendance) error {
	return r.db.Create(attendance).Error
}

// FindByID finds an attendance record by ID
func (r *AttendanceRepository) FindByID(id uint) (*models.Attendance, error) {
	var attendance models.Attendance
	err := r.db.Preload("Employee").Where("id = ? AND deleted = false", id).First(&attendance).Error
	if err != nil {
		return nil, err
	}
	return &attendance, nil
}

// FindByEmployeeIDAndDate finds an attendance record by employee ID and date
func (r *AttendanceRepository) FindByEmployeeIDAndDate(employeeID uint, date time.Time) (*models.Attendance, error) {
	var attendance models.Attendance
	err := r.db.Where("employee_id = ? AND date = ? AND deleted = false", employeeID, date).First(&attendance).Error
	if err != nil {
		return nil, err
	}
	return &attendance, nil
}

// FindByEmployeeID finds all attendance records by employee ID
func (r *AttendanceRepository) FindByEmployeeID(employeeID uint) ([]models.Attendance, error) {
	var attendances []models.Attendance
	err := r.db.Where("employee_id = ? AND deleted = false", employeeID).Order("date DESC").Find(&attendances).Error
	return attendances, err
}

// FindByEmployeeIDAndDateRange finds attendance records by employee ID and date range
func (r *AttendanceRepository) FindByEmployeeIDAndDateRange(employeeID uint, startDate, endDate time.Time) ([]models.Attendance, error) {
	var attendances []models.Attendance
	err := r.db.Where("employee_id = ? AND date >= ? AND date <= ? AND deleted = false", employeeID, startDate, endDate).Order("date ASC").Find(&attendances).Error
	return attendances, err
}

// FindByCompanyIDAndDateRange finds attendance records by company ID and date range
func (r *AttendanceRepository) FindByCompanyIDAndDateRange(companyID uint, startDate, endDate time.Time) ([]models.Attendance, error) {
	var attendances []models.Attendance
	err := r.db.Preload("Employee").
		Joins("JOIN employees ON employees.id = attendances.employee_id").
		Where("employees.company_id = ? AND attendances.date >= ? AND attendances.date <= ? AND attendances.deleted = false", companyID, startDate, endDate).
		Order("attendances.date ASC").
		Find(&attendances).Error
	return attendances, err
}

// FindPendingCorrections finds attendance records with pending corrections
func (r *AttendanceRepository) FindPendingCorrections(companyID uint) ([]models.Attendance, error) {
	var attendances []models.Attendance
	err := r.db.Preload("Employee").
		Joins("JOIN employees ON employees.id = attendances.employee_id").
		Where("employees.company_id = ? AND attendances.correction_requested = true AND attendances.correction_approved = false AND attendances.deleted = false", companyID).
		Order("attendances.date DESC").
		Find(&attendances).Error
	return attendances, err
}

// CountByEmployeeIDAndStatusAndDateRange counts attendance by status and date range
func (r *AttendanceRepository) CountByEmployeeIDAndStatusAndDateRange(employeeID uint, status enums.AttendanceStatus, startDate, endDate time.Time) (int64, error) {
	var count int64
	err := r.db.Model(&models.Attendance{}).
		Where("employee_id = ? AND status = ? AND date >= ? AND date <= ? AND deleted = false", employeeID, status, startDate, endDate).
		Count(&count).Error
	return count, err
}

// Update updates an attendance record
func (r *AttendanceRepository) Update(attendance *models.Attendance) error {
	return r.db.Save(attendance).Error
}

// Delete soft deletes an attendance record
func (r *AttendanceRepository) Delete(id uint) error {
	return r.db.Model(&models.Attendance{}).Where("id = ?", id).Update("deleted", true).Error
}

// Upsert creates or updates an attendance record
func (r *AttendanceRepository) Upsert(attendance *models.Attendance) error {
	return r.db.Save(attendance).Error
}
