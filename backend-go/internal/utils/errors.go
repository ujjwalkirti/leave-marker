package utils

import (
	"errors"
	"net/http"

	"github.com/gin-gonic/gin"
	"github.com/leavemarker/backend-go/internal/dto"
	"gorm.io/gorm"
)

// AppError represents an application error
type AppError struct {
	Code    int
	Message string
}

func (e *AppError) Error() string {
	return e.Message
}

// Common errors
var (
	ErrNotFound       = &AppError{Code: http.StatusNotFound, Message: "Resource not found"}
	ErrBadRequest     = &AppError{Code: http.StatusBadRequest, Message: "Bad request"}
	ErrUnauthorized   = &AppError{Code: http.StatusUnauthorized, Message: "Unauthorized"}
	ErrForbidden      = &AppError{Code: http.StatusForbidden, Message: "Forbidden"}
	ErrInternalServer = &AppError{Code: http.StatusInternalServerError, Message: "Internal server error"}
)

// NewNotFoundError creates a not found error
func NewNotFoundError(message string) *AppError {
	return &AppError{Code: http.StatusNotFound, Message: message}
}

// NewBadRequestError creates a bad request error
func NewBadRequestError(message string) *AppError {
	return &AppError{Code: http.StatusBadRequest, Message: message}
}

// NewUnauthorizedError creates an unauthorized error
func NewUnauthorizedError(message string) *AppError {
	return &AppError{Code: http.StatusUnauthorized, Message: message}
}

// NewForbiddenError creates a forbidden error
func NewForbiddenError(message string) *AppError {
	return &AppError{Code: http.StatusForbidden, Message: message}
}

// NewInternalServerError creates an internal server error
func NewInternalServerError(message string) *AppError {
	return &AppError{Code: http.StatusInternalServerError, Message: message}
}

// HandleError handles errors and sends appropriate response
func HandleError(c *gin.Context, err error) {
	var appErr *AppError
	if errors.As(err, &appErr) {
		c.JSON(appErr.Code, dto.NewErrorResponse(appErr.Message))
		return
	}

	// Handle GORM errors
	if errors.Is(err, gorm.ErrRecordNotFound) {
		c.JSON(http.StatusNotFound, dto.NewErrorResponse("Resource not found"))
		return
	}

	// Default to internal server error
	c.JSON(http.StatusInternalServerError, dto.NewErrorResponse("An unexpected error occurred"))
}

// RespondWithError sends an error response
func RespondWithError(c *gin.Context, code int, message string) {
	c.JSON(code, dto.NewErrorResponse(message))
}

// RespondWithSuccess sends a success response
func RespondWithSuccess(c *gin.Context, message string, data interface{}) {
	c.JSON(http.StatusOK, dto.NewSuccessResponse(message, data))
}

// RespondWithCreated sends a created response
func RespondWithCreated(c *gin.Context, message string, data interface{}) {
	c.JSON(http.StatusCreated, dto.NewSuccessResponse(message, data))
}
