package dto

import (
	"time"

	"github.com/leavemarker/backend-go/internal/enums"
)

// HolidayRequest for creating/updating holiday
type HolidayRequest struct {
	Name   string              `json:"name" binding:"required,max=200"`
	Date   time.Time           `json:"date" binding:"required"`
	Type   enums.HolidayType   `json:"type" binding:"required"`
	State  *enums.IndianState  `json:"state"`
	Active bool                `json:"active"`
}

// HolidayResponse for holiday response
type HolidayResponse struct {
	ID        uint               `json:"id"`
	Name      string             `json:"name"`
	Date      time.Time          `json:"date"`
	Type      enums.HolidayType  `json:"type"`
	State     *enums.IndianState `json:"state"`
	Active    bool               `json:"active"`
	CreatedAt time.Time          `json:"createdAt"`
	UpdatedAt time.Time          `json:"updatedAt"`
}
