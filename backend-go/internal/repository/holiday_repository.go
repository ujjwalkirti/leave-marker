package repository

import (
	"time"

	"github.com/leavemarker/backend-go/internal/models"
	"gorm.io/gorm"
)

// HolidayRepository handles holiday data access
type HolidayRepository struct {
	db *gorm.DB
}

// NewHolidayRepository creates a new HolidayRepository
func NewHolidayRepository(db *gorm.DB) *HolidayRepository {
	return &HolidayRepository{db: db}
}

// Create creates a new holiday
func (r *HolidayRepository) Create(holiday *models.Holiday) error {
	return r.db.Create(holiday).Error
}

// FindByID finds a holiday by ID
func (r *HolidayRepository) FindByID(id uint) (*models.Holiday, error) {
	var holiday models.Holiday
	err := r.db.Where("id = ? AND deleted = false", id).First(&holiday).Error
	if err != nil {
		return nil, err
	}
	return &holiday, nil
}

// FindByCompanyID finds all holidays by company ID
func (r *HolidayRepository) FindByCompanyID(companyID uint) ([]models.Holiday, error) {
	var holidays []models.Holiday
	err := r.db.Where("company_id = ? AND deleted = false", companyID).Order("date ASC").Find(&holidays).Error
	return holidays, err
}

// FindActiveByCompanyID finds all active holidays by company ID
func (r *HolidayRepository) FindActiveByCompanyID(companyID uint) ([]models.Holiday, error) {
	var holidays []models.Holiday
	err := r.db.Where("company_id = ? AND active = true AND deleted = false", companyID).Order("date ASC").Find(&holidays).Error
	return holidays, err
}

// CountActiveByCompanyID counts active holidays by company ID
func (r *HolidayRepository) CountActiveByCompanyID(companyID uint) (int64, error) {
	var count int64
	err := r.db.Model(&models.Holiday{}).Where("company_id = ? AND active = true AND deleted = false", companyID).Count(&count).Error
	return count, err
}

// FindByCompanyIDAndDateRange finds holidays by company ID and date range
func (r *HolidayRepository) FindByCompanyIDAndDateRange(companyID uint, startDate, endDate time.Time) ([]models.Holiday, error) {
	var holidays []models.Holiday
	err := r.db.Where("company_id = ? AND date >= ? AND date <= ? AND deleted = false", companyID, startDate, endDate).Order("date ASC").Find(&holidays).Error
	return holidays, err
}

// Update updates a holiday
func (r *HolidayRepository) Update(holiday *models.Holiday) error {
	return r.db.Save(holiday).Error
}

// Delete soft deletes a holiday
func (r *HolidayRepository) Delete(id uint) error {
	return r.db.Model(&models.Holiday{}).Where("id = ?", id).Update("deleted", true).Error
}
