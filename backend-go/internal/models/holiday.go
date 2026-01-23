package models

import (
	"time"

	"github.com/leavemarker/backend-go/internal/enums"
)

// Holiday represents a holiday entity
type Holiday struct {
	BaseModel
	CompanyID uint              `gorm:"not null;index" json:"companyId"`
	Name      string            `gorm:"size:200;not null" json:"name"`
	Date      time.Time         `gorm:"type:date;not null" json:"date"`
	Type      enums.HolidayType `gorm:"size:20;not null" json:"type"`
	State     *enums.IndianState `gorm:"size:50" json:"state"`
	Active    bool              `gorm:"default:true" json:"active"`

	// Relationships
	Company Company `gorm:"foreignKey:CompanyID" json:"-"`
}

func (Holiday) TableName() string {
	return "holidays"
}
