package repository

import (
	"github.com/leavemarker/backend-go/internal/enums"
	"github.com/leavemarker/backend-go/internal/models"
	"gorm.io/gorm"
)

// PlanRepository handles plan data access
type PlanRepository struct {
	db *gorm.DB
}

// NewPlanRepository creates a new PlanRepository
func NewPlanRepository(db *gorm.DB) *PlanRepository {
	return &PlanRepository{db: db}
}

// Create creates a new plan
func (r *PlanRepository) Create(plan *models.Plan) error {
	return r.db.Create(plan).Error
}

// FindByID finds a plan by ID
func (r *PlanRepository) FindByID(id uint) (*models.Plan, error) {
	var plan models.Plan
	err := r.db.Where("id = ? AND deleted = false", id).First(&plan).Error
	if err != nil {
		return nil, err
	}
	return &plan, nil
}

// FindAll finds all plans
func (r *PlanRepository) FindAll() ([]models.Plan, error) {
	var plans []models.Plan
	err := r.db.Where("deleted = false").Find(&plans).Error
	return plans, err
}

// FindActive finds all active plans
func (r *PlanRepository) FindActive() ([]models.Plan, error) {
	var plans []models.Plan
	err := r.db.Where("active = true AND deleted = false").Find(&plans).Error
	return plans, err
}

// FindByTier finds plans by tier
func (r *PlanRepository) FindByTier(tier enums.PlanTier) ([]models.Plan, error) {
	var plans []models.Plan
	err := r.db.Where("tier = ? AND active = true AND deleted = false", tier).Find(&plans).Error
	return plans, err
}

// FindFreePlan finds the free plan
func (r *PlanRepository) FindFreePlan() (*models.Plan, error) {
	var plan models.Plan
	err := r.db.Where("tier = ? AND active = true AND deleted = false", enums.PlanTierFree).First(&plan).Error
	if err != nil {
		return nil, err
	}
	return &plan, nil
}

// Update updates a plan
func (r *PlanRepository) Update(plan *models.Plan) error {
	return r.db.Save(plan).Error
}

// Delete soft deletes a plan
func (r *PlanRepository) Delete(id uint) error {
	return r.db.Model(&models.Plan{}).Where("id = ?", id).Update("deleted", true).Error
}
