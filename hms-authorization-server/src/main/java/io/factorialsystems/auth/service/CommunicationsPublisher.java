package io.factorialsystems.auth.service;

import io.factorialsystems.auth.config.RabbitMQConfig;
import io.factorialsystems.auth.model.dto.messaging.EmailMessageDto;
import io.factorialsystems.auth.model.dto.messaging.SmsMessageDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Service for publishing email and SMS messages to the Communications Server via RabbitMQ.
 * All methods are async to avoid blocking the main business logic.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CommunicationsPublisher {

    private final RabbitTemplate rabbitTemplate;

    /**
     * Publish an email message to the communications exchange.
     * The message will be consumed by the HMS Communications Server.
     *
     * @param emailMessage the email message to send
     */
    @Async
    public void publishEmail(EmailMessageDto emailMessage) {
        try {
            log.info("Publishing email message to communications server: to={}, subject={}, tenantId={}",
                    emailMessage.getToEmail(), emailMessage.getSubject(), emailMessage.getTenantId());

            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.COMMUNICATIONS_EXCHANGE,
                    RabbitMQConfig.EMAIL_ROUTING_KEY,
                    emailMessage
            );

            log.debug("Email message published successfully: to={}", emailMessage.getToEmail());

        } catch (Exception e) {
            log.error("Failed to publish email message to communications server: to={}, error={}",
                    emailMessage.getToEmail(), e.getMessage(), e);
            // Don't throw exception to avoid breaking the main business flow
            // The communications server has retry logic and error handling
        }
    }

    /**
     * Publish an SMS message to the communications exchange.
     * The message will be consumed by the HMS Communications Server.
     *
     * @param smsMessage the SMS message to send
     */
    @Async
    public void publishSms(SmsMessageDto smsMessage) {
        try {
            log.info("Publishing SMS message to communications server: to={}, tenantId={}",
                    smsMessage.getToPhone(), smsMessage.getTenantId());

            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.COMMUNICATIONS_EXCHANGE,
                    RabbitMQConfig.SMS_ROUTING_KEY,
                    smsMessage
            );

            log.debug("SMS message published successfully: to={}", smsMessage.getToPhone());

        } catch (Exception e) {
            log.error("Failed to publish SMS message to communications server: to={}, error={}",
                    smsMessage.getToPhone(), e.getMessage(), e);
            // Don't throw exception to avoid breaking the main business flow
        }
    }

    /**
     * Send a welcome email to a new user with their temporary password.
     *
     * @param tenantId the tenant ID
     * @param userEmail the user's email
     * @param userName the user's full name
     * @param tempPassword the temporary password
     * @param facilityName the facility name
     */
    public void sendWelcomeEmail(UUID tenantId, String userEmail, String userName,
                                 String tempPassword, String facilityName) {
        EmailMessageDto email = EmailMessageDto.builder()
                .tenantId(tenantId)
                .toEmail(userEmail)
                .toName(userName)
                .subject("Welcome to " + facilityName + " - HMS Platform")
                .htmlContent(buildWelcomeEmailHtml(userName, tempPassword, facilityName))
                .textContent(buildWelcomeEmailText(userName, tempPassword, facilityName))
                .build();

        publishEmail(email);
    }

    /**
     * Send a password reset email.
     *
     * @param tenantId the tenant ID
     * @param userEmail the user's email
     * @param userName the user's full name
     * @param resetToken the password reset token
     * @param facilityName the facility name
     */
    public void sendPasswordResetEmail(UUID tenantId, String userEmail, String userName,
                                       String resetToken, String facilityName) {
        EmailMessageDto email = EmailMessageDto.builder()
                .tenantId(tenantId)
                .toEmail(userEmail)
                .toName(userName)
                .subject("Password Reset Request - " + facilityName)
                .htmlContent(buildPasswordResetEmailHtml(userName, resetToken, facilityName))
                .textContent(buildPasswordResetEmailText(userName, resetToken, facilityName))
                .build();

        publishEmail(email);
    }

    /**
     * Send a tenant registration confirmation email.
     *
     * @param tenantId the tenant ID
     * @param adminEmail the admin email
     * @param adminName the admin's full name
     * @param facilityName the facility name
     * @param tenantCode the tenant code
     */
    public void sendTenantRegistrationEmail(UUID tenantId, String adminEmail, String adminName,
                                            String facilityName, String tenantCode) {
        EmailMessageDto email = EmailMessageDto.builder()
                .tenantId(tenantId)
                .toEmail(adminEmail)
                .toName(adminName)
                .subject("Facility Registration Successful - HMS Platform")
                .htmlContent(buildTenantRegistrationEmailHtml(adminName, facilityName, tenantCode))
                .textContent(buildTenantRegistrationEmailText(adminName, facilityName, tenantCode))
                .build();

        publishEmail(email);
    }

    // HTML Email Templates

    private String buildWelcomeEmailHtml(String userName, String tempPassword, String facilityName) {
        return String.format("""
                <!DOCTYPE html>
                <html>
                <head>
                    <style>
                        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                        .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                        .header { background-color: #007bff; color: white; padding: 20px; text-align: center; }
                        .content { padding: 20px; background-color: #f9f9f9; }
                        .password-box { background-color: #fff; border: 2px solid #007bff; padding: 15px; margin: 20px 0; text-align: center; font-size: 18px; font-weight: bold; }
                        .footer { text-align: center; padding: 20px; font-size: 12px; color: #666; }
                        .warning { color: #dc3545; font-weight: bold; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <h1>Welcome to %s</h1>
                            <p>HMS Platform</p>
                        </div>
                        <div class="content">
                            <p>Hello %s,</p>
                            <p>Your account has been created successfully. Below are your login credentials:</p>
                            <div class="password-box">
                                Temporary Password: %s
                            </div>
                            <p class="warning">⚠️ Important: You must change this password on your first login for security reasons.</p>
                            <p>To access the system, please visit the HMS Platform login page and use your email address along with the temporary password above.</p>
                            <p>If you have any questions or need assistance, please contact your system administrator.</p>
                        </div>
                        <div class="footer">
                            <p>&copy; 2026 HMS Platform - Health Management System</p>
                            <p>This is an automated message, please do not reply.</p>
                        </div>
                    </div>
                </body>
                </html>
                """, facilityName, userName, tempPassword);
    }

    private String buildPasswordResetEmailHtml(String userName, String resetToken, String facilityName) {
        return String.format("""
                <!DOCTYPE html>
                <html>
                <head>
                    <style>
                        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                        .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                        .header { background-color: #dc3545; color: white; padding: 20px; text-align: center; }
                        .content { padding: 20px; background-color: #f9f9f9; }
                        .token-box { background-color: #fff; border: 2px solid #dc3545; padding: 15px; margin: 20px 0; text-align: center; font-size: 16px; font-weight: bold; word-break: break-all; }
                        .footer { text-align: center; padding: 20px; font-size: 12px; color: #666; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <h1>Password Reset Request</h1>
                            <p>%s - HMS Platform</p>
                        </div>
                        <div class="content">
                            <p>Hello %s,</p>
                            <p>We received a request to reset your password. Use the reset token below to complete the process:</p>
                            <div class="token-box">%s</div>
                            <p>This reset token will expire in 1 hour for security reasons.</p>
                            <p>If you did not request a password reset, please ignore this email or contact your system administrator if you have concerns.</p>
                        </div>
                        <div class="footer">
                            <p>&copy; 2026 HMS Platform - Health Management System</p>
                            <p>This is an automated message, please do not reply.</p>
                        </div>
                    </div>
                </body>
                </html>
                """, facilityName, userName, resetToken);
    }

    private String buildTenantRegistrationEmailHtml(String adminName, String facilityName, String tenantCode) {
        return String.format("""
                <!DOCTYPE html>
                <html>
                <head>
                    <style>
                        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                        .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                        .header { background-color: #28a745; color: white; padding: 20px; text-align: center; }
                        .content { padding: 20px; background-color: #f9f9f9; }
                        .info-box { background-color: #fff; border: 2px solid #28a745; padding: 15px; margin: 20px 0; }
                        .footer { text-align: center; padding: 20px; font-size: 12px; color: #666; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <h1>🎉 Registration Successful!</h1>
                            <p>HMS Platform</p>
                        </div>
                        <div class="content">
                            <p>Hello %s,</p>
                            <p>Congratulations! Your facility <strong>%s</strong> has been successfully registered on the HMS Platform.</p>
                            <div class="info-box">
                                <p><strong>Facility Code:</strong> %s</p>
                                <p><strong>Facility Name:</strong> %s</p>
                            </div>
                            <p>Your administrator account has been created, and you should receive a separate email with your login credentials shortly.</p>
                            <p>Welcome to the HMS Platform! We're excited to have you on board.</p>
                        </div>
                        <div class="footer">
                            <p>&copy; 2026 HMS Platform - Health Management System</p>
                            <p>This is an automated message, please do not reply.</p>
                        </div>
                    </div>
                </body>
                </html>
                """, adminName, facilityName, tenantCode, facilityName);
    }

    // Plain Text Email Templates

    private String buildWelcomeEmailText(String userName, String tempPassword, String facilityName) {
        return String.format("""
                Welcome to %s - HMS Platform

                Hello %s,

                Your account has been created successfully. Below are your login credentials:

                Temporary Password: %s

                ⚠️ IMPORTANT: You must change this password on your first login for security reasons.

                To access the system, please visit the HMS Platform login page and use your email address along with the temporary password above.

                If you have any questions or need assistance, please contact your system administrator.

                ---
                © 2026 HMS Platform - Health Management System
                This is an automated message, please do not reply.
                """, facilityName, userName, tempPassword);
    }

    private String buildPasswordResetEmailText(String userName, String resetToken, String facilityName) {
        return String.format("""
                Password Reset Request - %s - HMS Platform

                Hello %s,

                We received a request to reset your password. Use the reset token below to complete the process:

                Reset Token: %s

                This reset token will expire in 1 hour for security reasons.

                If you did not request a password reset, please ignore this email or contact your system administrator if you have concerns.

                ---
                © 2026 HMS Platform - Health Management System
                This is an automated message, please do not reply.
                """, facilityName, userName, resetToken);
    }

    private String buildTenantRegistrationEmailText(String adminName, String facilityName, String tenantCode) {
        return String.format("""
                Registration Successful! - HMS Platform

                Hello %s,

                Congratulations! Your facility '%s' has been successfully registered on the HMS Platform.

                Facility Code: %s
                Facility Name: %s

                Your administrator account has been created, and you should receive a separate email with your login credentials shortly.

                Welcome to the HMS Platform! We're excited to have you on board.

                ---
                © 2026 HMS Platform - Health Management System
                This is an automated message, please do not reply.
                """, adminName, facilityName, tenantCode, facilityName);
    }
}
