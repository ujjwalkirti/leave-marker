package handler

import (
	"net/http"

	"github.com/gin-gonic/gin"
	"github.com/leavemarker/backend-go/internal/dto"
	"github.com/leavemarker/backend-go/internal/service"
	"github.com/leavemarker/backend-go/internal/utils"
)

// HealthHandler handles health check endpoints
type HealthHandler struct{}

// NewHealthHandler creates a new HealthHandler
func NewHealthHandler() *HealthHandler {
	return &HealthHandler{}
}

// HealthCheck handles health check
func (h *HealthHandler) HealthCheck(c *gin.Context) {
	c.JSON(http.StatusOK, dto.NewSuccessResponse("Service is healthy", map[string]string{
		"status": "UP",
	}))
}

// ContactHandler handles contact form endpoints
type ContactHandler struct {
	emailService *service.EmailService
}

// NewContactHandler creates a new ContactHandler
func NewContactHandler(emailService *service.EmailService) *ContactHandler {
	return &ContactHandler{emailService: emailService}
}

// SubmitContactForm handles contact form submission
func (h *ContactHandler) SubmitContactForm(c *gin.Context) {
	var req dto.ContactRequest
	if err := c.ShouldBindJSON(&req); err != nil {
		utils.RespondWithError(c, http.StatusBadRequest, "Invalid request body")
		return
	}

	if err := h.emailService.SendContactFormEmail(req.Name, req.Email, req.Subject, req.Message); err != nil {
		// Log error but don't fail for user
		utils.RespondWithSuccess(c, "Message sent successfully", nil)
		return
	}

	utils.RespondWithSuccess(c, "Message sent successfully", nil)
}
