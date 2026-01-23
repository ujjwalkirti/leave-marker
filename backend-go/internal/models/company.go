package models

// Company represents a company entity
type Company struct {
	BaseModel
	Name     string `gorm:"size:200;not null" json:"name"`
	Email    string `gorm:"size:100;uniqueIndex;not null" json:"email"`
	Timezone string `gorm:"size:50;default:Asia/Kolkata" json:"timezone"`
	Active   bool   `gorm:"default:true" json:"active"`

	// Relationships
	Employees     []Employee     `gorm:"foreignKey:CompanyID" json:"-"`
	LeavePolicies []LeavePolicy  `gorm:"foreignKey:CompanyID" json:"-"`
	Holidays      []Holiday      `gorm:"foreignKey:CompanyID" json:"-"`
	Subscriptions []Subscription `gorm:"foreignKey:CompanyID" json:"-"`
	Payments      []Payment      `gorm:"foreignKey:CompanyID" json:"-"`
	AuditLogs     []AuditLog     `gorm:"foreignKey:CompanyID" json:"-"`
}

func (Company) TableName() string {
	return "companies"
}
