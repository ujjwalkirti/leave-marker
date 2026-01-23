package repository

import (
	"github.com/leavemarker/backend-go/internal/models"
	"gorm.io/gorm"
)

// AuditLogRepository handles audit log data access
type AuditLogRepository struct {
	db *gorm.DB
}

// NewAuditLogRepository creates a new AuditLogRepository
func NewAuditLogRepository(db *gorm.DB) *AuditLogRepository {
	return &AuditLogRepository{db: db}
}

// Create creates a new audit log
func (r *AuditLogRepository) Create(auditLog *models.AuditLog) error {
	return r.db.Create(auditLog).Error
}

// FindByCompanyID finds all audit logs by company ID
func (r *AuditLogRepository) FindByCompanyID(companyID uint) ([]models.AuditLog, error) {
	var auditLogs []models.AuditLog
	err := r.db.Where("company_id = ? AND deleted = false", companyID).Order("created_at DESC").Find(&auditLogs).Error
	return auditLogs, err
}

// FindByCompanyIDAndEntityType finds audit logs by company ID and entity type
func (r *AuditLogRepository) FindByCompanyIDAndEntityType(companyID uint, entityType string) ([]models.AuditLog, error) {
	var auditLogs []models.AuditLog
	err := r.db.Where("company_id = ? AND entity_type = ? AND deleted = false", companyID, entityType).Order("created_at DESC").Find(&auditLogs).Error
	return auditLogs, err
}

// FindByEntityIDAndEntityType finds audit logs by entity ID and entity type
func (r *AuditLogRepository) FindByEntityIDAndEntityType(entityID uint, entityType string) ([]models.AuditLog, error) {
	var auditLogs []models.AuditLog
	err := r.db.Where("entity_id = ? AND entity_type = ? AND deleted = false", entityID, entityType).Order("created_at DESC").Find(&auditLogs).Error
	return auditLogs, err
}
