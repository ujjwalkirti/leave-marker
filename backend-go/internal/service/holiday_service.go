package service

import (
	"time"

	"github.com/leavemarker/backend-go/internal/dto"
	"github.com/leavemarker/backend-go/internal/models"
	"github.com/leavemarker/backend-go/internal/repository"
	"github.com/leavemarker/backend-go/internal/utils"
)

// HolidayService handles holiday business logic
type HolidayService struct {
	holidayRepo           *repository.HolidayRepository
	planValidationService *PlanValidationService
}

// NewHolidayService creates a new HolidayService
func NewHolidayService(
	holidayRepo *repository.HolidayRepository,
	planValidationService *PlanValidationService,
) *HolidayService {
	return &HolidayService{
		holidayRepo:           holidayRepo,
		planValidationService: planValidationService,
	}
}

// CreateHoliday creates a new holiday
func (s *HolidayService) CreateHoliday(companyID uint, req dto.HolidayRequest) (*dto.HolidayResponse, error) {
	// Validate plan limit
	if err := s.planValidationService.ValidateHolidayLimit(companyID); err != nil {
		return nil, err
	}

	holiday := &models.Holiday{
		CompanyID: companyID,
		Name:      req.Name,
		Date:      req.Date,
		Type:      req.Type,
		State:     req.State,
		Active:    req.Active,
	}

	if err := s.holidayRepo.Create(holiday); err != nil {
		return nil, utils.NewInternalServerError("Failed to create holiday")
	}

	return s.toHolidayResponse(holiday), nil
}

// GetHoliday gets a holiday by ID
func (s *HolidayService) GetHoliday(id, companyID uint) (*dto.HolidayResponse, error) {
	holiday, err := s.holidayRepo.FindByID(id)
	if err != nil {
		return nil, utils.NewNotFoundError("Holiday not found")
	}

	if holiday.CompanyID != companyID {
		return nil, utils.NewForbiddenError("Access denied")
	}

	return s.toHolidayResponse(holiday), nil
}

// GetAllHolidays gets all holidays for a company
func (s *HolidayService) GetAllHolidays(companyID uint) ([]dto.HolidayResponse, error) {
	holidays, err := s.holidayRepo.FindByCompanyID(companyID)
	if err != nil {
		return nil, utils.NewInternalServerError("Failed to get holidays")
	}

	responses := make([]dto.HolidayResponse, len(holidays))
	for i, holiday := range holidays {
		responses[i] = *s.toHolidayResponse(&holiday)
	}

	return responses, nil
}

// GetActiveHolidays gets all active holidays for a company
func (s *HolidayService) GetActiveHolidays(companyID uint) ([]dto.HolidayResponse, error) {
	holidays, err := s.holidayRepo.FindActiveByCompanyID(companyID)
	if err != nil {
		return nil, utils.NewInternalServerError("Failed to get holidays")
	}

	responses := make([]dto.HolidayResponse, len(holidays))
	for i, holiday := range holidays {
		responses[i] = *s.toHolidayResponse(&holiday)
	}

	return responses, nil
}

// GetHolidaysByDateRange gets holidays by date range for a company
func (s *HolidayService) GetHolidaysByDateRange(companyID uint, startDate, endDate time.Time) ([]dto.HolidayResponse, error) {
	holidays, err := s.holidayRepo.FindByCompanyIDAndDateRange(companyID, startDate, endDate)
	if err != nil {
		return nil, utils.NewInternalServerError("Failed to get holidays")
	}

	responses := make([]dto.HolidayResponse, len(holidays))
	for i, holiday := range holidays {
		responses[i] = *s.toHolidayResponse(&holiday)
	}

	return responses, nil
}

// UpdateHoliday updates a holiday
func (s *HolidayService) UpdateHoliday(id, companyID uint, req dto.HolidayRequest) (*dto.HolidayResponse, error) {
	holiday, err := s.holidayRepo.FindByID(id)
	if err != nil {
		return nil, utils.NewNotFoundError("Holiday not found")
	}

	if holiday.CompanyID != companyID {
		return nil, utils.NewForbiddenError("Access denied")
	}

	holiday.Name = req.Name
	holiday.Date = req.Date
	holiday.Type = req.Type
	holiday.State = req.State
	holiday.Active = req.Active

	if err := s.holidayRepo.Update(holiday); err != nil {
		return nil, utils.NewInternalServerError("Failed to update holiday")
	}

	return s.toHolidayResponse(holiday), nil
}

// DeleteHoliday soft deletes a holiday
func (s *HolidayService) DeleteHoliday(id, companyID uint) error {
	holiday, err := s.holidayRepo.FindByID(id)
	if err != nil {
		return utils.NewNotFoundError("Holiday not found")
	}

	if holiday.CompanyID != companyID {
		return utils.NewForbiddenError("Access denied")
	}

	return s.holidayRepo.Delete(id)
}

func (s *HolidayService) toHolidayResponse(holiday *models.Holiday) *dto.HolidayResponse {
	return &dto.HolidayResponse{
		ID:        holiday.ID,
		Name:      holiday.Name,
		Date:      holiday.Date,
		Type:      holiday.Type,
		State:     holiday.State,
		Active:    holiday.Active,
		CreatedAt: holiday.CreatedAt,
		UpdatedAt: holiday.UpdatedAt,
	}
}
