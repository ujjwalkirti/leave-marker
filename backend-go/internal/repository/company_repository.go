package repository

import (
	"github.com/leavemarker/backend-go/internal/models"
	"gorm.io/gorm"
)

// CompanyRepository handles company data access
type CompanyRepository struct {
	db *gorm.DB
}

// NewCompanyRepository creates a new CompanyRepository
func NewCompanyRepository(db *gorm.DB) *CompanyRepository {
	return &CompanyRepository{db: db}
}

// Create creates a new company
func (r *CompanyRepository) Create(company *models.Company) error {
	return r.db.Create(company).Error
}

// FindByID finds a company by ID
func (r *CompanyRepository) FindByID(id uint) (*models.Company, error) {
	var company models.Company
	err := r.db.Where("id = ? AND deleted = false", id).First(&company).Error
	if err != nil {
		return nil, err
	}
	return &company, nil
}

// FindByEmail finds a company by email
func (r *CompanyRepository) FindByEmail(email string) (*models.Company, error) {
	var company models.Company
	err := r.db.Where("email = ? AND deleted = false", email).First(&company).Error
	if err != nil {
		return nil, err
	}
	return &company, nil
}

// Update updates a company
func (r *CompanyRepository) Update(company *models.Company) error {
	return r.db.Save(company).Error
}

// Delete soft deletes a company
func (r *CompanyRepository) Delete(id uint) error {
	return r.db.Model(&models.Company{}).Where("id = ?", id).Update("deleted", true).Error
}

// ExistsByEmail checks if a company with the given email exists
func (r *CompanyRepository) ExistsByEmail(email string) (bool, error) {
	var count int64
	err := r.db.Model(&models.Company{}).Where("email = ? AND deleted = false", email).Count(&count).Error
	return count > 0, err
}
