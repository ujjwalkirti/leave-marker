package main

import (
	"fmt"
	"log"

	"github.com/gin-gonic/gin"
	"github.com/leavemarker/backend-go/internal/config"
	"github.com/leavemarker/backend-go/internal/enums"
	"github.com/leavemarker/backend-go/internal/handler"
	"github.com/leavemarker/backend-go/internal/middleware"
	"github.com/leavemarker/backend-go/internal/repository"
	"github.com/leavemarker/backend-go/internal/service"
)

func main() {
	// Load configuration
	cfg := config.Load()

	// Initialize database
	db, err := config.InitDB(cfg)
	if err != nil {
		log.Fatalf("Failed to connect to database: %v", err)
	}

	// Run migrations
	if err := config.AutoMigrate(db); err != nil {
		log.Fatalf("Failed to run migrations: %v", err)
	}

	// Initialize repositories
	companyRepo := repository.NewCompanyRepository(db)
	employeeRepo := repository.NewEmployeeRepository(db)
	leavePolicyRepo := repository.NewLeavePolicyRepository(db)
	leaveApplicationRepo := repository.NewLeaveApplicationRepository(db)
	leaveBalanceRepo := repository.NewLeaveBalanceRepository(db)
	attendanceRepo := repository.NewAttendanceRepository(db)
	holidayRepo := repository.NewHolidayRepository(db)
	planRepo := repository.NewPlanRepository(db)
	subscriptionRepo := repository.NewSubscriptionRepository(db)
	paymentRepo := repository.NewPaymentRepository(db)

	// Initialize JWT service
	jwtService := middleware.NewJWTService(cfg)

	// Initialize services
	emailService := service.NewEmailService(cfg)
	planValidationService := service.NewPlanValidationService(employeeRepo, leavePolicyRepo, holidayRepo, subscriptionRepo)
	authService := service.NewAuthService(db, companyRepo, employeeRepo, planRepo, subscriptionRepo, jwtService, emailService)
	employeeService := service.NewEmployeeService(employeeRepo, planValidationService)
	leavePolicyService := service.NewLeavePolicyService(leavePolicyRepo, planValidationService)
	leaveBalanceService := service.NewLeaveBalanceService(leaveBalanceRepo, leavePolicyRepo, employeeRepo)
	leaveApplicationService := service.NewLeaveApplicationService(leaveApplicationRepo, employeeRepo, leaveBalanceService, emailService)
	attendanceService := service.NewAttendanceService(attendanceRepo, employeeRepo, planValidationService)
	holidayService := service.NewHolidayService(holidayRepo, planValidationService)
	planService := service.NewPlanService(planRepo)
	subscriptionService := service.NewSubscriptionService(subscriptionRepo, planRepo)
	paymentService := service.NewPaymentService(paymentRepo, subscriptionRepo, planRepo, cfg)

	// Initialize handlers
	authHandler := handler.NewAuthHandler(authService)
	employeeHandler := handler.NewEmployeeHandler(employeeService)
	leavePolicyHandler := handler.NewLeavePolicyHandler(leavePolicyService)
	leaveApplicationHandler := handler.NewLeaveApplicationHandler(leaveApplicationService)
	leaveBalanceHandler := handler.NewLeaveBalanceHandler(leaveBalanceService)
	attendanceHandler := handler.NewAttendanceHandler(attendanceService)
	holidayHandler := handler.NewHolidayHandler(holidayService)
	planHandler := handler.NewPlanHandler(planService)
	subscriptionHandler := handler.NewSubscriptionHandler(subscriptionService)
	paymentHandler := handler.NewPaymentHandler(paymentService)
	healthHandler := handler.NewHealthHandler()
	contactHandler := handler.NewContactHandler(emailService)

	// Initialize Gin router
	router := gin.Default()

	// Apply CORS middleware
	router.Use(middleware.CORSMiddleware(cfg))

	// API routes
	api := router.Group(cfg.Server.ContextPath)
	{
		// Health check (public)
		api.GET("/health", healthHandler.HealthCheck)

		// Contact (public)
		api.POST("/contact", contactHandler.SubmitContactForm)

		// Auth routes (public)
		auth := api.Group("/auth")
		{
			auth.POST("/signup", authHandler.Signup)
			auth.POST("/login", authHandler.Login)
			auth.POST("/logout", authHandler.Logout)
			auth.POST("/password-reset-request", authHandler.RequestPasswordReset)
			auth.POST("/password-reset-confirm", authHandler.ResetPassword)
			auth.GET("/verify-session", middleware.AuthMiddleware(jwtService), authHandler.VerifySession)
		}

		// Plans routes (public for viewing)
		plans := api.Group("/plans")
		{
			plans.GET("/", planHandler.GetAllPlans)
			plans.GET("/active", planHandler.GetActivePlans)
			plans.GET("/:id", planHandler.GetPlan)

			// Admin only routes
			adminPlans := plans.Group("")
			adminPlans.Use(middleware.AuthMiddleware(jwtService), middleware.RoleMiddleware(enums.RoleSuperAdmin))
			{
				adminPlans.POST("/", planHandler.CreatePlan)
				adminPlans.PUT("/:id", planHandler.UpdatePlan)
				adminPlans.DELETE("/:id", planHandler.DeletePlan)
			}
		}

		// Payment webhook (public - signature verified in handler)
		api.POST("/payments/webhook", paymentHandler.HandleWebhook)

		// Protected routes
		protected := api.Group("")
		protected.Use(middleware.AuthMiddleware(jwtService))
		{
			// Employees
			employees := protected.Group("/employees")
			{
				employees.GET("/", employeeHandler.GetAllEmployees)
				employees.GET("/active", employeeHandler.GetActiveEmployees)
				employees.GET("/:id", employeeHandler.GetEmployee)

				// HR/Admin only
				adminEmployees := employees.Group("")
				adminEmployees.Use(middleware.RoleMiddleware(enums.RoleHRAdmin, enums.RoleSuperAdmin))
				{
					adminEmployees.POST("/", employeeHandler.CreateEmployee)
					adminEmployees.PUT("/:id", employeeHandler.UpdateEmployee)
					adminEmployees.DELETE("/:id", employeeHandler.DeactivateEmployee)
					adminEmployees.PUT("/:id/reactivate", employeeHandler.ReactivateEmployee)
					adminEmployees.GET("/active/count", employeeHandler.CountActiveEmployees)
				}
			}

			// Leave Policies
			leavePolicies := protected.Group("/leave-policies")
			{
				leavePolicies.GET("/", leavePolicyHandler.GetAllLeavePolicies)
				leavePolicies.GET("/active", leavePolicyHandler.GetActiveLeavePolicies)
				leavePolicies.GET("/:id", leavePolicyHandler.GetLeavePolicy)

				// HR/Admin only
				adminPolicies := leavePolicies.Group("")
				adminPolicies.Use(middleware.RoleMiddleware(enums.RoleHRAdmin, enums.RoleSuperAdmin))
				{
					adminPolicies.POST("/", leavePolicyHandler.CreateLeavePolicy)
					adminPolicies.PUT("/:id", leavePolicyHandler.UpdateLeavePolicy)
					adminPolicies.DELETE("/:id", leavePolicyHandler.DeleteLeavePolicy)
				}
			}

			// Leave Applications
			leaveApplications := protected.Group("/leave-applications")
			{
				leaveApplications.POST("/", leaveApplicationHandler.ApplyLeave)
				leaveApplications.GET("/:id", leaveApplicationHandler.GetLeaveApplication)
				leaveApplications.GET("/my-leaves", leaveApplicationHandler.GetMyLeaveApplications)
				leaveApplications.GET("/my-leaves/pending/count", leaveApplicationHandler.CountPendingApplications)
				leaveApplications.POST("/:id/cancel", leaveApplicationHandler.CancelLeave)

				// Manager only
				managerApprovals := leaveApplications.Group("")
				managerApprovals.Use(middleware.RoleMiddleware(enums.RoleManager))
				{
					managerApprovals.GET("/pending-approvals/manager", leaveApplicationHandler.GetPendingApprovalsForManager)
					managerApprovals.POST("/:id/approve/manager", leaveApplicationHandler.ApproveByManager)
				}

				// HR/Admin only
				hrApprovals := leaveApplications.Group("")
				hrApprovals.Use(middleware.RoleMiddleware(enums.RoleHRAdmin, enums.RoleSuperAdmin))
				{
					hrApprovals.GET("/pending-approvals/hr", leaveApplicationHandler.GetPendingApprovalsForHR)
					hrApprovals.POST("/:id/approve/hr", leaveApplicationHandler.ApproveByHR)
					hrApprovals.GET("/date-range", leaveApplicationHandler.GetLeaveApplicationsByDateRange)
				}
			}

			// Leave Balances
			leaveBalances := protected.Group("/leave-balances")
			{
				leaveBalances.GET("/", leaveBalanceHandler.GetMyLeaveBalances)
				leaveBalances.GET("/by-year", leaveBalanceHandler.GetLeaveBalancesByYear)

				// Manager/HR/Admin can view employee balances
				adminBalances := leaveBalances.Group("")
				adminBalances.Use(middleware.RoleMiddleware(enums.RoleManager, enums.RoleHRAdmin, enums.RoleSuperAdmin))
				{
					adminBalances.GET("/employee/:employeeId", leaveBalanceHandler.GetEmployeeLeaveBalances)
				}
			}

			// Attendance
			attendance := protected.Group("/attendance")
			{
				attendance.POST("/punch", attendanceHandler.PunchInOut)
				attendance.GET("/today", attendanceHandler.GetTodayAttendance)
				attendance.GET("/:id", attendanceHandler.GetAttendance)
				attendance.GET("/my-attendance", attendanceHandler.GetMyAttendance)
				attendance.GET("/my-attendance/date-range", attendanceHandler.GetMyAttendanceByDateRange)
				attendance.GET("/my-attendance/rate", attendanceHandler.GetAttendanceRate)
				attendance.POST("/:id/request-correction", attendanceHandler.RequestCorrection)

				// Manager/HR/Admin only
				adminAttendance := attendance.Group("")
				adminAttendance.Use(middleware.RoleMiddleware(enums.RoleManager, enums.RoleHRAdmin, enums.RoleSuperAdmin))
				{
					adminAttendance.GET("/date-range", attendanceHandler.GetAttendanceByDateRange)
					adminAttendance.POST("/:id/approve-correction", attendanceHandler.ApproveCorrection)
					adminAttendance.POST("/:id/reject-correction", attendanceHandler.RejectCorrection)
					adminAttendance.GET("/pending-corrections", attendanceHandler.GetPendingCorrections)
				}

				// HR/Admin only
				hrAttendance := attendance.Group("")
				hrAttendance.Use(middleware.RoleMiddleware(enums.RoleHRAdmin, enums.RoleSuperAdmin))
				{
					hrAttendance.POST("/mark", attendanceHandler.MarkAttendance)
				}
			}

			// Holidays
			holidays := protected.Group("/holidays")
			{
				holidays.GET("/", holidayHandler.GetAllHolidays)
				holidays.GET("/active", holidayHandler.GetActiveHolidays)
				holidays.GET("/:id", holidayHandler.GetHoliday)
				holidays.GET("/date-range", holidayHandler.GetHolidaysByDateRange)

				// HR/Admin only
				adminHolidays := holidays.Group("")
				adminHolidays.Use(middleware.RoleMiddleware(enums.RoleHRAdmin, enums.RoleSuperAdmin))
				{
					adminHolidays.POST("/", holidayHandler.CreateHoliday)
					adminHolidays.PUT("/:id", holidayHandler.UpdateHoliday)
					adminHolidays.DELETE("/:id", holidayHandler.DeleteHoliday)
				}
			}

			// Subscriptions
			subscriptions := protected.Group("/subscriptions")
			subscriptions.Use(middleware.RoleMiddleware(enums.RoleHRAdmin, enums.RoleSuperAdmin))
			{
				subscriptions.GET("/active", subscriptionHandler.GetActiveSubscription)
				subscriptions.GET("/", subscriptionHandler.GetAllSubscriptions)
				subscriptions.POST("/", subscriptionHandler.CreateSubscription)
				subscriptions.PUT("/:id", subscriptionHandler.UpdateSubscription)
				subscriptions.POST("/:id/cancel", subscriptionHandler.CancelSubscription)
			}
			// Features available to all authenticated users
			protected.GET("/subscriptions/features", subscriptionHandler.GetSubscriptionFeatures)

			// Payments
			payments := protected.Group("/payments")
			payments.Use(middleware.RoleMiddleware(enums.RoleHRAdmin, enums.RoleSuperAdmin))
			{
				payments.GET("/", paymentHandler.GetPayments)
				payments.GET("/:id", paymentHandler.GetPayment)
				payments.POST("/initiate", paymentHandler.InitiatePayment)
				payments.POST("/verify", paymentHandler.VerifyPayment)
				payments.POST("/:id/retry", paymentHandler.RetryPayment)
			}
		}
	}

	// Start server
	addr := fmt.Sprintf(":%s", cfg.Server.Port)
	log.Printf("Starting server on %s", addr)
	if err := router.Run(addr); err != nil {
		log.Fatalf("Failed to start server: %v", err)
	}
}
