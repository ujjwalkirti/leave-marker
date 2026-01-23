package handler

import (
	"io"
	"net/http"
	"strconv"

	"github.com/gin-gonic/gin"
	"github.com/leavemarker/backend-go/internal/dto"
	"github.com/leavemarker/backend-go/internal/middleware"
	"github.com/leavemarker/backend-go/internal/service"
	"github.com/leavemarker/backend-go/internal/utils"
)

// PaymentHandler handles payment endpoints
type PaymentHandler struct {
	paymentService *service.PaymentService
}

// NewPaymentHandler creates a new PaymentHandler
func NewPaymentHandler(paymentService *service.PaymentService) *PaymentHandler {
	return &PaymentHandler{paymentService: paymentService}
}

// GetPayments gets all payments
func (h *PaymentHandler) GetPayments(c *gin.Context) {
	companyID := middleware.GetCompanyID(c)

	response, err := h.paymentService.GetPayments(companyID)
	if err != nil {
		utils.HandleError(c, err)
		return
	}

	utils.RespondWithSuccess(c, "Payments retrieved successfully", response)
}

// GetPayment gets a payment by ID
func (h *PaymentHandler) GetPayment(c *gin.Context) {
	companyID := middleware.GetCompanyID(c)
	id, err := strconv.ParseUint(c.Param("id"), 10, 32)
	if err != nil {
		utils.RespondWithError(c, http.StatusBadRequest, "Invalid payment ID")
		return
	}

	response, err := h.paymentService.GetPayment(uint(id), companyID)
	if err != nil {
		utils.HandleError(c, err)
		return
	}

	utils.RespondWithSuccess(c, "Payment retrieved successfully", response)
}

// InitiatePayment initiates a new payment
func (h *PaymentHandler) InitiatePayment(c *gin.Context) {
	companyID := middleware.GetCompanyID(c)

	var req dto.PaymentInitiateRequest
	if err := c.ShouldBindJSON(&req); err != nil {
		utils.RespondWithError(c, http.StatusBadRequest, "Invalid request body")
		return
	}

	ipAddress := c.ClientIP()
	userAgent := c.GetHeader("User-Agent")

	response, err := h.paymentService.InitiatePayment(companyID, req, ipAddress, userAgent)
	if err != nil {
		utils.HandleError(c, err)
		return
	}

	utils.RespondWithSuccess(c, "Payment initiated successfully", response)
}

// VerifyPayment verifies and completes a payment
func (h *PaymentHandler) VerifyPayment(c *gin.Context) {
	companyID := middleware.GetCompanyID(c)

	var req dto.PaymentVerifyRequest
	if err := c.ShouldBindJSON(&req); err != nil {
		utils.RespondWithError(c, http.StatusBadRequest, "Invalid request body")
		return
	}

	response, err := h.paymentService.VerifyPayment(companyID, req)
	if err != nil {
		utils.HandleError(c, err)
		return
	}

	utils.RespondWithSuccess(c, "Payment verified successfully", response)
}

// HandleWebhook handles Razorpay webhook
func (h *PaymentHandler) HandleWebhook(c *gin.Context) {
	payload, err := io.ReadAll(c.Request.Body)
	if err != nil {
		utils.RespondWithError(c, http.StatusBadRequest, "Failed to read request body")
		return
	}

	signature := c.GetHeader("X-Razorpay-Signature")

	if err := h.paymentService.HandleWebhook(payload, signature); err != nil {
		utils.HandleError(c, err)
		return
	}

	c.JSON(http.StatusOK, gin.H{"status": "ok"})
}

// RetryPayment retries a failed payment
func (h *PaymentHandler) RetryPayment(c *gin.Context) {
	companyID := middleware.GetCompanyID(c)
	id, err := strconv.ParseUint(c.Param("id"), 10, 32)
	if err != nil {
		utils.RespondWithError(c, http.StatusBadRequest, "Invalid payment ID")
		return
	}

	response, err := h.paymentService.RetryPayment(uint(id), companyID)
	if err != nil {
		utils.HandleError(c, err)
		return
	}

	utils.RespondWithSuccess(c, "Payment retry initiated successfully", response)
}
