package middleware

import (
	"errors"
	"net/http"
	"strings"
	"time"

	"github.com/gin-gonic/gin"
	"github.com/golang-jwt/jwt/v5"
	"github.com/leavemarker/backend-go/internal/config"
	"github.com/leavemarker/backend-go/internal/dto"
	"github.com/leavemarker/backend-go/internal/enums"
)

// Claims represents JWT claims
type Claims struct {
	UserID    uint       `json:"userId"`
	Email     string     `json:"email"`
	FullName  string     `json:"fullName"`
	Role      enums.Role `json:"role"`
	CompanyID uint       `json:"companyId"`
	jwt.RegisteredClaims
}

// JWTService handles JWT operations
type JWTService struct {
	secret     []byte
	expiration time.Duration
}

// NewJWTService creates a new JWTService
func NewJWTService(cfg *config.Config) *JWTService {
	return &JWTService{
		secret:     []byte(cfg.JWT.Secret),
		expiration: time.Duration(cfg.JWT.Expiration) * time.Millisecond,
	}
}

// GenerateToken generates a new JWT token
func (s *JWTService) GenerateToken(userID uint, email, fullName string, role enums.Role, companyID uint) (string, error) {
	claims := &Claims{
		UserID:    userID,
		Email:     email,
		FullName:  fullName,
		Role:      role,
		CompanyID: companyID,
		RegisteredClaims: jwt.RegisteredClaims{
			ExpiresAt: jwt.NewNumericDate(time.Now().Add(s.expiration)),
			IssuedAt:  jwt.NewNumericDate(time.Now()),
			Subject:   email,
		},
	}

	token := jwt.NewWithClaims(jwt.SigningMethodHS256, claims)
	return token.SignedString(s.secret)
}

// ValidateToken validates a JWT token and returns the claims
func (s *JWTService) ValidateToken(tokenString string) (*Claims, error) {
	token, err := jwt.ParseWithClaims(tokenString, &Claims{}, func(token *jwt.Token) (interface{}, error) {
		if _, ok := token.Method.(*jwt.SigningMethodHMAC); !ok {
			return nil, errors.New("unexpected signing method")
		}
		return s.secret, nil
	})

	if err != nil {
		return nil, err
	}

	if claims, ok := token.Claims.(*Claims); ok && token.Valid {
		return claims, nil
	}

	return nil, errors.New("invalid token")
}

// AuthMiddleware is the authentication middleware
func AuthMiddleware(jwtService *JWTService) gin.HandlerFunc {
	return func(c *gin.Context) {
		tokenString := extractToken(c)
		if tokenString == "" {
			c.JSON(http.StatusUnauthorized, dto.NewErrorResponse("No authorization token provided"))
			c.Abort()
			return
		}

		claims, err := jwtService.ValidateToken(tokenString)
		if err != nil {
			c.JSON(http.StatusUnauthorized, dto.NewErrorResponse("Invalid or expired token"))
			c.Abort()
			return
		}

		// Set user info in context
		c.Set("userID", claims.UserID)
		c.Set("email", claims.Email)
		c.Set("fullName", claims.FullName)
		c.Set("role", claims.Role)
		c.Set("companyID", claims.CompanyID)

		c.Next()
	}
}

// RoleMiddleware checks if user has required role
func RoleMiddleware(allowedRoles ...enums.Role) gin.HandlerFunc {
	return func(c *gin.Context) {
		role, exists := c.Get("role")
		if !exists {
			c.JSON(http.StatusUnauthorized, dto.NewErrorResponse("User not authenticated"))
			c.Abort()
			return
		}

		userRole := role.(enums.Role)
		for _, allowedRole := range allowedRoles {
			if userRole == allowedRole {
				c.Next()
				return
			}
		}

		c.JSON(http.StatusForbidden, dto.NewErrorResponse("Insufficient permissions"))
		c.Abort()
	}
}

// extractToken extracts token from Authorization header or cookie
func extractToken(c *gin.Context) string {
	// Check Authorization header first
	authHeader := c.GetHeader("Authorization")
	if authHeader != "" {
		parts := strings.Split(authHeader, " ")
		if len(parts) == 2 && strings.ToLower(parts[0]) == "bearer" {
			return parts[1]
		}
	}

	// Check cookie
	cookie, err := c.Cookie("jwt")
	if err == nil && cookie != "" {
		return cookie
	}

	return ""
}

// GetUserID gets user ID from context
func GetUserID(c *gin.Context) uint {
	userID, _ := c.Get("userID")
	return userID.(uint)
}

// GetCompanyID gets company ID from context
func GetCompanyID(c *gin.Context) uint {
	companyID, _ := c.Get("companyID")
	return companyID.(uint)
}

// GetRole gets role from context
func GetRole(c *gin.Context) enums.Role {
	role, _ := c.Get("role")
	return role.(enums.Role)
}

// GetEmail gets email from context
func GetEmail(c *gin.Context) string {
	email, _ := c.Get("email")
	return email.(string)
}

// GetFullName gets full name from context
func GetFullName(c *gin.Context) string {
	fullName, _ := c.Get("fullName")
	return fullName.(string)
}
