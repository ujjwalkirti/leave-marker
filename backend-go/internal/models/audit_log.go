package models

// AuditLog represents an audit log entity
type AuditLog struct {
	BaseModel
	CompanyID  uint    `gorm:"not null;index" json:"companyId"`
	EmployeeID *uint   `gorm:"index" json:"employeeId"`
	Action     string  `gorm:"size:100;not null" json:"action"`
	EntityType string  `gorm:"size:100;not null" json:"entityType"`
	EntityID   uint    `gorm:"not null" json:"entityId"`
	OldValue   *string `gorm:"type:text" json:"oldValue"`
	NewValue   *string `gorm:"type:text" json:"newValue"`
	IPAddress  *string `gorm:"size:50" json:"ipAddress"`

	// Relationships
	Company  Company   `gorm:"foreignKey:CompanyID" json:"-"`
	Employee *Employee `gorm:"foreignKey:EmployeeID" json:"-"`
}

func (AuditLog) TableName() string {
	return "audit_logs"
}
