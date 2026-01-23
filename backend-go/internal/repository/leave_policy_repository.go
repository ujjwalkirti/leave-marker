package repository

import (
	"github.com/leavemarker/backend-go/internal/enums"
	"github.com/leavemarker/backend-go/internal/models"
	"gorm.io/gorm"
)

// LeavePolicyRepository handles leave policy data access
type LeavePolicyRepository struct {
	db *gorm.DB
}

// NewLeavePolicyRepository creates a new LeavePolicyRepository
func NewLeavePolicyRepository(db *gorm.DB) *LeavePolicyRepository {
	return &LeavePolicyRepository{db: db}
}

// Create creates a new leave policy
func (r *LeavePolicyRepository) Create(policy *models.LeavePolicy) error {
	return r.db.Create(policy).Error
}

// FindByID finds a leave policy by ID
func (r *LeavePolicyRepository) FindByID(id uint) (*models.LeavePolicy, error) {
	var policy models.LeavePolicy
	err := r.db.Where("id = ? AND deleted = false", id).First(&policy).Error
	if err != nil {
		return nil, err
	}
	return &policy, nil
}

// FindByCompanyID finds all leave policies by company ID
func (r *LeavePolicyRepository) FindByCompanyID(companyID uint) ([]models.LeavePolicy, error) {
	var policies []models.LeavePolicy
	err := r.db.Where("company_id = ? AND deleted = false", companyID).Find(&policies).Error
	return policies, err
}

// FindActiveByCompanyID finds all active leave policies by company ID
func (r *LeavePolicyRepository) FindActiveByCompanyID(companyID uint) ([]models.LeavePolicy, error) {
	var policies []models.LeavePolicy
	err := r.db.Where("company_id = ? AND active = true AND deleted = false", companyID).Find(&policies).Error
	return policies, err
}

// CountActiveByCompanyID counts active leave policies by company ID
func (r *LeavePolicyRepository) CountActiveByCompanyID(companyID uint) (int64, error) {
	var count int64
	err := r.db.Model(&models.LeavePolicy{}).Where("company_id = ? AND active = true AND deleted = false", companyID).Count(&count).Error
	return count, err
}

// FindByCompanyIDAndLeaveType finds a leave policy by company ID and leave type
func (r *LeavePolicyRepository) FindByCompanyIDAndLeaveType(companyID uint, leaveType enums.LeaveType) (*models.LeavePolicy, error) {
	var policy models.LeavePolicy
	err := r.db.Where("company_id = ? AND leave_type = ? AND deleted = false", companyID, leaveType).First(&policy).Error
	if err != nil {
		return nil, err
	}
	return &policy, nil
}

// Update updates a leave policy
func (r *LeavePolicyRepository) Update(policy *models.LeavePolicy) error {
	return r.db.Save(policy).Error
}

// Delete soft deletes a leave policy
func (r *LeavePolicyRepository) Delete(id uint) error {
	return r.db.Model(&models.LeavePolicy{}).Where("id = ?", id).Update("deleted", true).Error
}

// ExistsByCompanyIDAndLeaveType checks if a policy with the given company ID and leave type exists
func (r *LeavePolicyRepository) ExistsByCompanyIDAndLeaveType(companyID uint, leaveType enums.LeaveType) (bool, error) {
	var count int64
	err := r.db.Model(&models.LeavePolicy{}).Where("company_id = ? AND leave_type = ? AND deleted = false", companyID, leaveType).Count(&count).Error
	return count > 0, err
}
