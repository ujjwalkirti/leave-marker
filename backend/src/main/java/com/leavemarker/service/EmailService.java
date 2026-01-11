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
}
