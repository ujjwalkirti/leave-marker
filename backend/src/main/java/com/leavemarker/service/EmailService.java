package com.leavemarker.service;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.contact-email}")
    private String contactEmail;

    @Async
    public void sendPasswordResetEmail(String toEmail, String resetToken) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Password Reset Request - Leave Management System");
            message.setText(buildPasswordResetEmailBody(resetToken));

            mailSender.send(message);
            logger.info("Password reset email sent to: {}", toEmail);
        } catch (Exception e) {
            logger.error("Failed to send password reset email to: {}", toEmail, e);
        }
    }

    @Async
    public void sendLeaveApplicationNotification(String toEmail, String employeeName,
                                                  String leaveType, String startDate, String endDate) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("New Leave Application - Leave Management System");
            message.setText(buildLeaveApplicationEmailBody(employeeName, leaveType, startDate, endDate));

            mailSender.send(message);
            logger.info("Leave application notification sent to: {}", toEmail);
        } catch (Exception e) {
            logger.error("Failed to send leave application notification to: {}", toEmail, e);
        }
    }

    @Async
    public void sendLeaveApprovalNotification(String toEmail, String approverName,
                                              String leaveType, String startDate, String endDate,
                                              boolean approved, String reason) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject((approved ? "Leave Approved" : "Leave Rejected") + " - Leave Management System");
            message.setText(buildLeaveApprovalEmailBody(approverName, leaveType, startDate, endDate, approved, reason));

            mailSender.send(message);
            logger.info("Leave approval notification sent to: {}", toEmail);
        } catch (Exception e) {
            logger.error("Failed to send leave approval notification to: {}", toEmail, e);
        }
    }

    @Async
    public void sendAttendanceCorrectionNotification(String toEmail, String employeeName,
                                                     String date, String reason) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Attendance Correction Request - Leave Management System");
            message.setText(buildAttendanceCorrectionEmailBody(employeeName, date, reason));

            mailSender.send(message);
            logger.info("Attendance correction notification sent to: {}", toEmail);
        } catch (Exception e) {
            logger.error("Failed to send attendance correction notification to: {}", toEmail, e);
        }
    }

    private String buildPasswordResetEmailBody(String resetToken) {
        return String.format(
            "Dear User,\n\n" +
            "You have requested to reset your password.\n\n" +
            "Your password reset token is: %s\n\n" +
            "This token will expire in 24 hours.\n\n" +
            "If you did not request this, please ignore this email.\n\n" +
            "Best regards,\n" +
            "Leave Management System Team",
            resetToken
        );
    }

    private String buildLeaveApplicationEmailBody(String employeeName, String leaveType,
                                                   String startDate, String endDate) {
        return String.format(
            "Dear Manager,\n\n" +
            "%s has applied for leave.\n\n" +
            "Leave Type: %s\n" +
            "Start Date: %s\n" +
            "End Date: %s\n\n" +
            "Please review and approve/reject the leave application.\n\n" +
            "Best regards,\n" +
            "Leave Management System",
            employeeName, leaveType, startDate, endDate
        );
    }

    private String buildLeaveApprovalEmailBody(String approverName, String leaveType,
                                               String startDate, String endDate,
                                               boolean approved, String reason) {
        String status = approved ? "approved" : "rejected";
        String body = String.format(
            "Dear Employee,\n\n" +
            "Your leave application has been %s by %s.\n\n" +
            "Leave Type: %s\n" +
            "Start Date: %s\n" +
            "End Date: %s\n",
            status, approverName, leaveType, startDate, endDate
        );

        if (!approved && reason != null) {
            body += "\nReason: " + reason + "\n";
        }

        body += "\nBest regards,\n" +
                "Leave Management System";

        return body;
    }

    private String buildAttendanceCorrectionEmailBody(String employeeName, String date, String reason) {
        return String.format(
            "Dear Manager,\n\n" +
            "%s has requested attendance correction for %s.\n\n" +
            "Reason: %s\n\n" +
            "Please review and approve/reject the request.\n\n" +
            "Best regards,\n" +
            "Leave Management System",
            employeeName, date, reason
        );
    }

    /**
     * Send contact/demo request email to the configured contact email address.
     * This is a synchronous method as it's used for public contact form submissions
     * and we need to confirm delivery to the user.
     */
    public void sendContactEmail(String senderName, String senderEmail, String phone, String message) {
        try {
            SimpleMailMessage mailMessage = new SimpleMailMessage();
            mailMessage.setFrom(fromEmail);
            mailMessage.setTo(contactEmail);
            mailMessage.setReplyTo(senderEmail);
            mailMessage.setSubject("Demo Request from " + senderName + " - LeaveMarker");
            mailMessage.setText(buildContactEmailBody(senderName, senderEmail, phone, message));

            mailSender.send(mailMessage);
            logger.info("Contact email sent from: {} to: {}", senderEmail, contactEmail);
        } catch (Exception e) {
            logger.error("Failed to send contact email from: {}", senderEmail, e);
            throw new RuntimeException("Failed to send email. Please try again later.");
        }
    }

    private String buildContactEmailBody(String name, String email, String phone, String message) {
        StringBuilder body = new StringBuilder();
        body.append("New Demo Request from LeaveMarker Website\n");
        body.append("==========================================\n\n");
        body.append("Name: ").append(name).append("\n");
        body.append("Email: ").append(email).append("\n");
        if (phone != null && !phone.isBlank()) {
            body.append("Phone: ").append(phone).append("\n");
        }
        body.append("\nMessage:\n");
        body.append("--------\n");
        body.append(message).append("\n");
        body.append("\n==========================================\n");
        body.append("This email was sent via the LeaveMarker contact form.");
        return body.toString();
    }
}
