package dto

import "github.com/leavemarker/backend-go/internal/enums"

// SignupRequest for user registration
type SignupRequest struct {
	CompanyName  string            `json:"companyName" binding:"required,max=200"`
	CompanyEmail string            `json:"companyEmail" binding:"required,email,max=100"`
	FullName     string            `json:"fullName" binding:"required,max=100"`
	Email        string            `json:"email" binding:"required,email,max=100"`
	Password     string            `json:"password" binding:"required,min=6"`
	EmployeeID   string            `json:"employeeId" binding:"required,max=50"`
	WorkLocation enums.IndianState `json:"workLocation" binding:"required"`
}

// LoginRequest for user login
type LoginRequest struct {
	Email    string `json:"email" binding:"required,email"`
	Password string `json:"password" binding:"required"`
}

// JWTAuthResponse for JWT authentication response
type JWTAuthResponse struct {
	AccessToken string     `json:"accessToken"`
	TokenType   string     `json:"tokenType"`
	UserID      uint       `json:"userId"`
	Email       string     `json:"email"`
	FullName    string     `json:"fullName"`
	Role        enums.Role `json:"role"`
	CompanyID   uint       `json:"companyId"`
}

// UserSessionResponse for session verification
type UserSessionResponse struct {
	ID        uint       `json:"id"`
	Email     string     `json:"email"`
	FullName  string     `json:"fullName"`
	Role      enums.Role `json:"role"`
	CompanyID uint       `json:"companyId"`
}

// PasswordResetRequest for password reset request
type PasswordResetRequest struct {
	Email string `json:"email" binding:"required,email"`
}

// PasswordResetConfirmRequest for password reset confirmation
type PasswordResetConfirmRequest struct {
	Token       string `json:"token" binding:"required"`
	NewPassword string `json:"newPassword" binding:"required,min=6"`
}
