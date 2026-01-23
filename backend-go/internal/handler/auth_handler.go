package handler

import (
	"net/http"

	"github.com/gin-gonic/gin"
	"github.com/leavemarker/backend-go/internal/dto"
	"github.com/leavemarker/backend-go/internal/middleware"
	"github.com/leavemarker/backend-go/internal/service"
	"github.com/leavemarker/backend-go/internal/utils"
)

// AuthHandler handles authentication endpoints
type AuthHandler struct {
	authService *service.AuthService
}

// NewAuthHandler creates a new AuthHandler
func NewAuthHandler(authService *service.AuthService) *AuthHandler {
	return &AuthHandler{authService: authService}
}

// Signup handles user registration
func (h *AuthHandler) Signup(c *gin.Context) {
	var req dto.SignupRequest
	if err := c.ShouldBindJSON(&req); err != nil {
		utils.RespondWithError(c, http.StatusBadRequest, "Invalid request body")
		return
	}

	response, err := h.authService.Signup(req)
	if err != nil {
		utils.HandleError(c, err)
		return
	}

	// Set cookie
	c.SetCookie("jwt", response.AccessToken, 86400, "/", "", false, true)

	utils.RespondWithCreated(c, "Account created successfully", response)
}

// Login handles user login
func (h *AuthHandler) Login(c *gin.Context) {
	var req dto.LoginRequest
	if err := c.ShouldBindJSON(&req); err != nil {
		utils.RespondWithError(c, http.StatusBadRequest, "Invalid request body")
		return
	}

	response, err := h.authService.Login(req)
	if err != nil {
		utils.HandleError(c, err)
		return
	}

	// Set cookie
	c.SetCookie("jwt", response.AccessToken, 86400, "/", "", false, true)

	utils.RespondWithSuccess(c, "Login successful", response)
}

// Logout handles user logout
func (h *AuthHandler) Logout(c *gin.Context) {
	c.SetCookie("jwt", "", -1, "/", "", false, true)
	utils.RespondWithSuccess(c, "Logout successful", nil)
}

// VerifySession verifies user session
func (h *AuthHandler) VerifySession(c *gin.Context) {
	userID := middleware.GetUserID(c)

	response, err := h.authService.VerifySession(userID)
	if err != nil {
		utils.HandleError(c, err)
		return
	}

	utils.RespondWithSuccess(c, "Session valid", response)
}

// RequestPasswordReset handles password reset request
func (h *AuthHandler) RequestPasswordReset(c *gin.Context) {
	var req dto.PasswordResetRequest
	if err := c.ShouldBindJSON(&req); err != nil {
		utils.RespondWithError(c, http.StatusBadRequest, "Invalid request body")
		return
	}

	if err := h.authService.RequestPasswordReset(req); err != nil {
		utils.HandleError(c, err)
		return
	}

	utils.RespondWithSuccess(c, "If the email exists, a reset link has been sent", nil)
}

// ResetPassword handles password reset confirmation
func (h *AuthHandler) ResetPassword(c *gin.Context) {
	var req dto.PasswordResetConfirmRequest
	if err := c.ShouldBindJSON(&req); err != nil {
		utils.RespondWithError(c, http.StatusBadRequest, "Invalid request body")
		return
	}

	if err := h.authService.ResetPassword(req); err != nil {
		utils.HandleError(c, err)
		return
	}

	utils.RespondWithSuccess(c, "Password reset successful", nil)
}
