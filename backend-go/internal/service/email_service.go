package service

import (
	"fmt"

	"github.com/leavemarker/backend-go/internal/config"
	"gopkg.in/gomail.v2"
)

// EmailService handles email sending
type EmailService struct {
	cfg    *config.Config
	dialer *gomail.Dialer
}

// NewEmailService creates a new EmailService
func NewEmailService(cfg *config.Config) *EmailService {
	dialer := gomail.NewDialer(cfg.Mail.Host, cfg.Mail.Port, cfg.Mail.Username, cfg.Mail.Password)
	return &EmailService{
		cfg:    cfg,
		dialer: dialer,
	}
}

// SendPasswordResetEmail sends a password reset email
func (s *EmailService) SendPasswordResetEmail(to, token, fullName string) error {
	resetURL := fmt.Sprintf("http://localhost:3000/reset-password?token=%s", token)

	m := gomail.NewMessage()
	m.SetHeader("From", s.cfg.Mail.From)
	m.SetHeader("To", to)
	m.SetHeader("Subject", "Password Reset Request - LeaveMarker")
	m.SetBody("text/html", fmt.Sprintf(`
		<html>
		<body>
			<h2>Password Reset Request</h2>
			<p>Hello %s,</p>
			<p>We received a request to reset your password. Click the link below to reset your password:</p>
			<p><a href="%s">Reset Password</a></p>
			<p>This link will expire in 24 hours.</p>
			<p>If you didn't request a password reset, please ignore this email.</p>
			<br>
			<p>Best regards,</p>
			<p>LeaveMarker Team</p>
		</body>
		</html>
	`, fullName, resetURL))

	return s.dialer.DialAndSend(m)
}

// SendLeaveApprovalEmail sends a leave approval notification email
func (s *EmailService) SendLeaveApprovalEmail(to, fullName, leaveType, startDate, endDate, status string) error {
	subject := fmt.Sprintf("Leave Application %s - LeaveMarker", status)

	m := gomail.NewMessage()
	m.SetHeader("From", s.cfg.Mail.From)
	m.SetHeader("To", to)
	m.SetHeader("Subject", subject)
	m.SetBody("text/html", fmt.Sprintf(`
		<html>
		<body>
			<h2>Leave Application Update</h2>
			<p>Hello %s,</p>
			<p>Your leave application has been <strong>%s</strong>.</p>
			<p><strong>Details:</strong></p>
			<ul>
				<li>Leave Type: %s</li>
				<li>Start Date: %s</li>
				<li>End Date: %s</li>
			</ul>
			<br>
			<p>Best regards,</p>
			<p>LeaveMarker Team</p>
		</body>
		</html>
	`, fullName, status, leaveType, startDate, endDate))

	return s.dialer.DialAndSend(m)
}

// SendContactFormEmail sends a contact form notification email
func (s *EmailService) SendContactFormEmail(name, email, subject, message string) error {
	m := gomail.NewMessage()
	m.SetHeader("From", s.cfg.Mail.From)
	m.SetHeader("To", s.cfg.Mail.From) // Send to admin
	m.SetHeader("Reply-To", email)
	m.SetHeader("Subject", fmt.Sprintf("Contact Form: %s", subject))
	m.SetBody("text/html", fmt.Sprintf(`
		<html>
		<body>
			<h2>Contact Form Submission</h2>
			<p><strong>Name:</strong> %s</p>
			<p><strong>Email:</strong> %s</p>
			<p><strong>Subject:</strong> %s</p>
			<p><strong>Message:</strong></p>
			<p>%s</p>
		</body>
		</html>
	`, name, email, subject, message))

	return s.dialer.DialAndSend(m)
}
