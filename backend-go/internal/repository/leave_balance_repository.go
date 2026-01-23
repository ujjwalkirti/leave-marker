package repository

import (
	"github.com/leavemarker/backend-go/internal/enums"
	"github.com/leavemarker/backend-go/internal/models"
	"gorm.io/gorm"
)

// LeaveBalanceRepository handles leave balance data access
type LeaveBalanceRepository struct {
	db *gorm.DB
}

// NewLeaveBalanceRepository creates a new LeaveBalanceRepository
func NewLeaveBalanceRepository(db *gorm.DB) *LeaveBalanceRepository {
	return &LeaveBalanceRepository{db: db}
}

// Create creates a new leave balance
func (r *LeaveBalanceRepository) Create(balance *models.LeaveBalance) error {
	return r.db.Create(balance).Error
}

// FindByID finds a leave balance by ID
func (r *LeaveBalanceRepository) FindByID(id uint) (*models.LeaveBalance, error) {
	var balance models.LeaveBalance
	err := r.db.Where("id = ? AND deleted = false", id).First(&balance).Error
	if err != nil {
		return nil, err
	}
	return &balance, nil
}

// FindByEmployeeID finds all leave balances by employee ID
func (r *LeaveBalanceRepository) FindByEmployeeID(employeeID uint) ([]models.LeaveBalance, error) {
	var balances []models.LeaveBalance
	err := r.db.Where("employee_id = ? AND deleted = false", employeeID).Find(&balances).Error
	return balances, err
}

// FindByEmployeeIDAndYear finds all leave balances by employee ID and year
func (r *LeaveBalanceRepository) FindByEmployeeIDAndYear(employeeID uint, year int) ([]models.LeaveBalance, error) {
	var balances []models.LeaveBalance
	err := r.db.Where("employee_id = ? AND year = ? AND deleted = false", employeeID, year).Find(&balances).Error
	return balances, err
}

// FindByEmployeeIDAndLeaveTypeAndYear finds a leave balance by employee ID, leave type, and year
func (r *LeaveBalanceRepository) FindByEmployeeIDAndLeaveTypeAndYear(employeeID uint, leaveType enums.LeaveType, year int) (*models.LeaveBalance, error) {
	var balance models.LeaveBalance
	err := r.db.Where("employee_id = ? AND leave_type = ? AND year = ? AND deleted = false", employeeID, leaveType, year).First(&balance).Error
	if err != nil {
		return nil, err
	}
	return &balance, nil
}

// Update updates a leave balance
func (r *LeaveBalanceRepository) Update(balance *models.LeaveBalance) error {
	return r.db.Save(balance).Error
}

// Delete soft deletes a leave balance
func (r *LeaveBalanceRepository) Delete(id uint) error {
	return r.db.Model(&models.LeaveBalance{}).Where("id = ?", id).Update("deleted", true).Error
}

// Upsert creates or updates a leave balance
func (r *LeaveBalanceRepository) Upsert(balance *models.LeaveBalance) error {
	return r.db.Save(balance).Error
}
