package com.cristian.backend.service;

import jakarta.mail.internet.MimeMessage;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.mail.javamail.JavaMailSender;

@Service
@Slf4j
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;


    public void sendEmail(String to, String subject, String body, boolean isHtml) {

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper =
                    new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom("hello@demomailtrap.co");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body, isHtml); // 👈 CLAVE

            mailSender.send(message);

        } catch (Exception e) {
            throw new RuntimeException("Error sending email", e);
        }
    }



    private String buildVerificationEmailHtml(String username, String verificationUrl) {
        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <style>
                        body {
                            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
                            line-height: 1.6;
                            color: #333;
                            max-width: 600px;
                            margin: 0 auto;
                            padding: 20px;
                            background-color: #f0f2f5;
                        }
                        .container {
                            background-color: #ffffff;
                            border-radius: 12px;
                            padding: 40px;
                            box-shadow: 0 4px 6px rgba(0,0,0,0.1);
                        }
                        .header {
                            text-align: center;
                            margin-bottom: 30px;
                        }
                        .header h1 {
                            color: #1a73e8;
                            margin: 0;
                            font-size: 28px;
                        }
                        .header .subtitle {
                            color: #5f6368;
                            font-size: 14px;
                            margin-top: 8px;
                        }
                        .icon {
                            font-size: 48px;
                            margin-bottom: 15px;
                        }
                        .button {
                            display: inline-block;
                            padding: 14px 32px;
                            background-color: #1a73e8;
                            color: white !important;
                            text-decoration: none;
                            border-radius: 8px;
                            margin: 20px 0;
                            font-weight: 600;
                            font-size: 16px;
                        }
                        .button:hover {
                            background-color: #1557b0;
                        }
                        .footer {
                            margin-top: 40px;
                            font-size: 12px;
                            color: #5f6368;
                            text-align: center;
                            border-top: 1px solid #e0e0e0;
                            padding-top: 20px;
                        }
                        .token-box {
                            background-color: #f8f9fa;
                            padding: 15px;
                            border-radius: 8px;
                            margin: 20px 0;
                            word-break: break-all;
                            border: 1px solid #e0e0e0;
                            font-family: 'Courier New', monospace;
                            font-size: 13px;
                        }
                        .security-note {
                            background-color: #fff3cd;
                            border: 1px solid #ffc107;
                            border-radius: 8px;
                            padding: 12px;
                            margin: 20px 0;
                            font-size: 13px;
                        }
                        .security-note strong {
                            color: #856404;
                        }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <div class="icon">🔐</div>
                            <h1>Verify Your Account</h1>
                            <p class="subtitle">Secure Authentication API</p>
                        </div>
                        
                        <p>Hello <strong>%s</strong>,</p>
                        <p>Thank you for registering with our Authentication API. To complete your registration and secure your account, please verify your email address.</p>

                        <div style="text-align: center;">
                            <a href="%s" class="button">Verify Email Address</a>
                        </div>

                        <p>Or copy and paste the following link into your browser:</p>
                        <div class="token-box">
                            <code>%s</code>
                        </div>

                        <div class="security-note">
                            <strong>⏰ Security Notice:</strong> This verification link will expire in 24 hours for your protection.
                        </div>

                        <div class="footer">
                            <p>If you didn't create an account, you can safely ignore this email.</p>
                            <p>🔒 Secured with JWT & OAuth 2.0</p>
                            <p>&copy; 2026 Authentication API. All rights reserved.</p>
                        </div>
                    </div>
                </body>
                </html>
                """.formatted(username, verificationUrl, verificationUrl);
    }

    public void sendVerificationEmail(@NotBlank(message = "Email is required") @Email(message = "Email must be valid") String email, @NotBlank(message = "Username is required") String username, String verificationToken) {
        // Change the URL to frontend URL when in production
        String verificationUrl = "http://localhost:8080/api/auth/verify?token=" + verificationToken;
        String subject = "Verify Your Account - Authentication API";

        sendEmail(email, subject, buildVerificationEmailHtml(username, verificationUrl),true);


    }

    public void sendWelcomeEmail(@NotBlank(message = "Email is required") @Email(message = "Email must be valid") String email, @NotBlank(message = "Username is required") String username) {
        String subject = "Account Verified Successfully!";
        sendEmail(email, subject, buildWelcomeEmailHtml(username),true);

    }

    private String buildWelcomeEmailHtml(String username) {
        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <style>
                        body {
                            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
                            line-height: 1.6;
                            color: #333;
                            max-width: 600px;
                            margin: 0 auto;
                            padding: 20px;
                            background-color: #f0f2f5;
                        }
                        .container {
                            background-color: #ffffff;
                            border-radius: 12px;
                            padding: 40px;
                            box-shadow: 0 4px 6px rgba(0,0,0,0.1);
                        }
                        .header {
                            text-align: center;
                            margin-bottom: 30px;
                        }
                        .header h1 {
                            color: #34a853;
                            margin: 0;
                            font-size: 28px;
                        }
                        .icon {
                            font-size: 64px;
                            margin-bottom: 15px;
                        }
                        .success-box {
                            background-color: #e6f4ea;
                            border: 1px solid #34a853;
                            border-radius: 8px;
                            padding: 20px;
                            margin: 20px 0;
                            text-align: center;
                        }
                        .success-box p {
                            margin: 0;
                            color: #137333;
                            font-weight: 500;
                        }
                        .features {
                            background-color: #f8f9fa;
                            border-radius: 8px;
                            padding: 20px;
                            margin: 20px 0;
                        }
                        .features h3 {
                            color: #1a73e8;
                            margin-top: 0;
                            font-size: 16px;
                        }
                        .features ul {
                            margin: 0;
                            padding-left: 20px;
                        }
                        .features li {
                            margin: 8px 0;
                            color: #5f6368;
                        }
                        .footer {
                            margin-top: 40px;
                            font-size: 12px;
                            color: #5f6368;
                            text-align: center;
                            border-top: 1px solid #e0e0e0;
                            padding-top: 20px;
                        }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <div class="icon">✅</div>
                            <h1>Account Verified!</h1>
                        </div>
                        
                        <p>Hello <strong>%s</strong>,</p>
                        
                        <div class="success-box">
                            <p>🎉 Your account has been successfully verified!</p>
                        </div>
                        
                        <p>You now have full access to our Authentication API. Your account is secured with industry-standard protocols.</p>
                        
                        <div class="features">
                            <h3>🔐 Security Features Enabled:</h3>
                            <ul>
                                <li>JWT Token Authentication</li>
                                <li>OAuth 2.0 Integration</li>
                                <li>Secure Session Management</li>
                                <li>Role-Based Access Control</li>
                            </ul>
                        </div>
                        
                        <p>Thank you for choosing our platform!</p>
                        
                        <p style="margin-top: 30px;">Best regards,<br><strong>The Authentication API Team</strong></p>
                        
                        <div class="footer">
                            <p>🔒 Secured with JWT & OAuth 2.0</p>
                            <p>&copy; 2026 Authentication API. All rights reserved.</p>
                        </div>
                    </div>
                </body>
                </html>
                """.formatted(username);
    }
}

