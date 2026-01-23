package service

import (
	"time"

	"github.com/leavemarker/backend-go/internal/dto"
	"github.com/leavemarker/backend-go/internal/enums"
	"github.com/leavemarker/backend-go/internal/middleware"
	"github.com/leavemarker/backend-go/internal/models"
	"github.com/leavemarker/backend-go/internal/repository"
	"github.com/leavemarker/backend-go/internal/utils"
	"github.com/shopspring/decimal"
	"gorm.io/gorm"
)

// AuthService handles authentication logic
type AuthService struct {
	db               *gorm.DB
	companyRepo      *repository.CompanyRepository
	employeeRepo     *repository.EmployeeRepository
	planRepo         *repository.PlanRepository
	subscriptionRepo *repository.SubscriptionRepository
	jwtService       *middleware.JWTService
	emailService     *EmailService
}

// NewAuthService creates a new AuthService
func NewAuthService(
	db *gorm.DB,
	companyRepo *repository.CompanyRepository,
	employeeRepo *repository.EmployeeRepository,
	planRepo *repository.PlanRepository,
	subscriptionRepo *repository.SubscriptionRepository,
	jwtService *middleware.JWTService,
	emailService *EmailService,
) *AuthService {
	return &AuthService{
		db:               db,
		companyRepo:      companyRepo,
		employeeRepo:     employeeRepo,
		planRepo:         planRepo,
		subscriptionRepo: subscriptionRepo,
		jwtService:       jwtService,
		emailService:     emailService,
	}
}

// Signup registers a new company and admin user
func (s *AuthService) Signup(req dto.SignupRequest) (*dto.JWTAuthResponse, error) {
	// Check if company email exists
	exists, err := s.companyRepo.ExistsByEmail(req.CompanyEmail)
	if err != nil {
		return nil, utils.NewInternalServerError("Failed to check company email")
	}
	if exists {
		return nil, utils.NewBadRequestError("Company email already exists")
	}

	// Check if employee email exists
	exists, err = s.employeeRepo.ExistsByEmail(req.Email)
	if err != nil {
		return nil, utils.NewInternalServerError("Failed to check employee email")
	}
	if exists {
		return nil, utils.NewBadRequestError("Employee email already exists")
	}

	// Hash password
	hashedPassword, err := utils.HashPassword(req.Password)
	if err != nil {
		return nil, utils.NewInternalServerError("Failed to hash password")
	}

	var employee *models.Employee

	// Start transaction
	err = s.db.Transaction(func(tx *gorm.DB) error {
		// Create company
		company := &models.Company{
			Name:     req.CompanyName,
			Email:    req.CompanyEmail,
			Timezone: "Asia/Kolkata",
			Active:   true,
		}
		if err := tx.Create(company).Error; err != nil {
			return err
		}

		// Create admin employee
		employee = &models.Employee{
			CompanyID:      company.ID,
			EmployeeID:     req.EmployeeID,
			FullName:       req.FullName,
			Email:          req.Email,
			Password:       hashedPassword,
			Role:           enums.RoleSuperAdmin,
			WorkLocation:   req.WorkLocation,
			EmploymentType: enums.EmploymentTypeFullTime,
			Status:         enums.EmployeeStatusActive,
		}
		if err := tx.Create(employee).Error; err != nil {
			return err
		}

		// Create free subscription
		freePlan, err := s.planRepo.FindFreePlan()
		if err != nil {
			// Create default free plan if not exists
			freePlan = &models.Plan{
				Name:             "Free",
				Description:      "Free tier plan",
				Tier:             enums.PlanTierFree,
				PlanType:         enums.PlanTypeFree,
				MaxEmployees:     10,
				MaxLeavePolicies: 1,
				MaxHolidays:      6,
				Active:           true,
			}
			if err := tx.Create(freePlan).Error; err != nil {
				return err
			}
		}

		now := time.Now()
		subscription := &models.Subscription{
			CompanyID:          company.ID,
			PlanID:             freePlan.ID,
			Status:             enums.SubscriptionStatusActive,
			BillingCycle:       enums.BillingCycleMonthly,
			StartDate:          now,
			EndDate:            now.AddDate(100, 0, 0), // Free plan doesn't expire
			CurrentPeriodStart: now,
			CurrentPeriodEnd:   now.AddDate(100, 0, 0),
			Amount:             decimal.Zero,
			AutoRenew:          false,
			IsPaid:             true,
		}
		if err := tx.Create(subscription).Error; err != nil {
			return err
		}

		return nil
	})

	if err != nil {
		return nil, utils.NewInternalServerError("Failed to create account")
	}

	// Generate JWT token
	token, err := s.jwtService.GenerateToken(employee.ID, employee.Email, employee.FullName, employee.Role, employee.CompanyID)
	if err != nil {
		return nil, utils.NewInternalServerError("Failed to generate token")
	}

	return &dto.JWTAuthResponse{
		AccessToken: token,
		TokenType:   "Bearer",
		UserID:      employee.ID,
		Email:       employee.Email,
		FullName:    employee.FullName,
		Role:        employee.Role,
		CompanyID:   employee.CompanyID,
	}, nil
}

// Login authenticates a user
func (s *AuthService) Login(req dto.LoginRequest) (*dto.JWTAuthResponse, error) {
	employee, err := s.employeeRepo.FindByEmail(req.Email)
	if err != nil {
		return nil, utils.NewUnauthorizedError("Invalid email or password")
	}

	if employee.Status != enums.EmployeeStatusActive {
		return nil, utils.NewUnauthorizedError("Account is inactive")
	}

	if !utils.CheckPassword(req.Password, employee.Password) {
		return nil, utils.NewUnauthorizedError("Invalid email or password")
	}

	// Generate JWT token
	token, err := s.jwtService.GenerateToken(employee.ID, employee.Email, employee.FullName, employee.Role, employee.CompanyID)
	if err != nil {
		return nil, utils.NewInternalServerError("Failed to generate token")
	}

	return &dto.JWTAuthResponse{
		AccessToken: token,
		TokenType:   "Bearer",
		UserID:      employee.ID,
		Email:       employee.Email,
		FullName:    employee.FullName,
		Role:        employee.Role,
		CompanyID:   employee.CompanyID,
	}, nil
}

// VerifySession verifies a user session
func (s *AuthService) VerifySession(userID uint) (*dto.UserSessionResponse, error) {
	employee, err := s.employeeRepo.FindByID(userID)
	if err != nil {
		return nil, utils.NewUnauthorizedError("Session invalid")
	}

	if employee.Status != enums.EmployeeStatusActive {
		return nil, utils.NewUnauthorizedError("Account is inactive")
	}

	return &dto.UserSessionResponse{
		ID:        employee.ID,
		Email:     employee.Email,
		FullName:  employee.FullName,
		Role:      employee.Role,
		CompanyID: employee.CompanyID,
	}, nil
}

// RequestPasswordReset initiates password reset
func (s *AuthService) RequestPasswordReset(req dto.PasswordResetRequest) error {
	employee, err := s.employeeRepo.FindByEmail(req.Email)
	if err != nil {
		// Don't reveal if email exists
		return nil
	}

	// Generate reset token
	token, err := utils.GenerateRandomToken(32)
	if err != nil {
		return utils.NewInternalServerError("Failed to generate reset token")
	}

	// Set token and expiry (24 hours)
	expiry := time.Now().Add(24 * time.Hour)
	employee.PasswordResetToken = &token
	employee.PasswordResetTokenExpiry = &expiry

	if err := s.employeeRepo.Update(employee); err != nil {
		return utils.NewInternalServerError("Failed to save reset token")
	}

	// Send email (ignore error for security)
	_ = s.emailService.SendPasswordResetEmail(employee.Email, token, employee.FullName)

	return nil
}

// ResetPassword resets password with token
func (s *AuthService) ResetPassword(req dto.PasswordResetConfirmRequest) error {
	employee, err := s.employeeRepo.FindByPasswordResetToken(req.Token)
	if err != nil {
		return utils.NewBadRequestError("Invalid or expired reset token")
	}

	// Check if token is expired
	if employee.PasswordResetTokenExpiry == nil || time.Now().After(*employee.PasswordResetTokenExpiry) {
		return utils.NewBadRequestError("Reset token has expired")
	}

	// Hash new password
	hashedPassword, err := utils.HashPassword(req.NewPassword)
	if err != nil {
		return utils.NewInternalServerError("Failed to hash password")
	}

	// Update password and clear token
	employee.Password = hashedPassword
	employee.PasswordResetToken = nil
	employee.PasswordResetTokenExpiry = nil

	if err := s.employeeRepo.Update(employee); err != nil {
		return utils.NewInternalServerError("Failed to update password")
	}

	return nil
}
