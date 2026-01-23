package repository

import (
	"time"

	"github.com/leavemarker/backend-go/internal/enums"
	"github.com/leavemarker/backend-go/internal/models"
	"gorm.io/gorm"
)

// SubscriptionRepository handles subscription data access
type SubscriptionRepository struct {
	db *gorm.DB
}

// NewSubscriptionRepository creates a new SubscriptionRepository
func NewSubscriptionRepository(db *gorm.DB) *SubscriptionRepository {
	return &SubscriptionRepository{db: db}
}

// Create creates a new subscription
func (r *SubscriptionRepository) Create(subscription *models.Subscription) error {
	return r.db.Create(subscription).Error
}

// FindByID finds a subscription by ID
func (r *SubscriptionRepository) FindByID(id uint) (*models.Subscription, error) {
	var subscription models.Subscription
	err := r.db.Preload("Plan").Where("id = ? AND deleted = false", id).First(&subscription).Error
	if err != nil {
		return nil, err
	}
	return &subscription, nil
}

// FindByCompanyID finds all subscriptions by company ID
func (r *SubscriptionRepository) FindByCompanyID(companyID uint) ([]models.Subscription, error) {
	var subscriptions []models.Subscription
	err := r.db.Preload("Plan").Where("company_id = ? AND deleted = false", companyID).Order("created_at DESC").Find(&subscriptions).Error
	return subscriptions, err
}

// FindActiveByCompanyID finds the active subscription by company ID
func (r *SubscriptionRepository) FindActiveByCompanyID(companyID uint) (*models.Subscription, error) {
	var subscription models.Subscription
	err := r.db.Preload("Plan").Where("company_id = ? AND status = ? AND deleted = false", companyID, enums.SubscriptionStatusActive).First(&subscription).Error
	if err != nil {
		return nil, err
	}
	return &subscription, nil
}

// FindByCompanyIDAndStatus finds subscriptions by company ID and status
func (r *SubscriptionRepository) FindByCompanyIDAndStatus(companyID uint, status enums.SubscriptionStatus) ([]models.Subscription, error) {
	var subscriptions []models.Subscription
	err := r.db.Preload("Plan").Where("company_id = ? AND status = ? AND deleted = false", companyID, status).Find(&subscriptions).Error
	return subscriptions, err
}

// FindExpiredForRenewal finds subscriptions that have expired and need renewal
func (r *SubscriptionRepository) FindExpiredForRenewal(before time.Time) ([]models.Subscription, error) {
	var subscriptions []models.Subscription
	err := r.db.Preload("Plan").Preload("Company").
		Where("end_date < ? AND status = ? AND auto_renew = true AND deleted = false", before, enums.SubscriptionStatusActive).
		Find(&subscriptions).Error
	return subscriptions, err
}

// Update updates a subscription
func (r *SubscriptionRepository) Update(subscription *models.Subscription) error {
	return r.db.Save(subscription).Error
}

// Delete soft deletes a subscription
func (r *SubscriptionRepository) Delete(id uint) error {
	return r.db.Model(&models.Subscription{}).Where("id = ?", id).Update("deleted", true).Error
}
