package com.cristian.backend.service;

import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("EmailService Test Suite")
class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private MimeMessage mimeMessage;

    @InjectMocks
    private EmailService emailService;

    @BeforeEach
    void setUp() {
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
    }

    // ============ TESTS FOR sendEmail ============

    @Test
    @DisplayName("Should send email successfully with HTML content")
    void sendEmail_Success_HtmlContent() {
        // GIVEN
        String to = "test@example.com";
        String subject = "Test Subject";
        String body = "<h1>Hello</h1>";
        boolean isHtml = true;

        doNothing().when(mailSender).send(any(MimeMessage.class));

        // WHEN & THEN
        assertDoesNotThrow(() -> emailService.sendEmail(to, subject, body, isHtml));
        verify(mailSender, times(1)).createMimeMessage();
        verify(mailSender, times(1)).send(any(MimeMessage.class));
    }

    @Test
    @DisplayName("Should send email successfully with plain text content")
    void sendEmail_Success_PlainTextContent() {
        // GIVEN
        String to = "test@example.com";
        String subject = "Test Subject";
        String body = "Hello World";
        boolean isHtml = false;

        doNothing().when(mailSender).send(any(MimeMessage.class));

        // WHEN & THEN
        assertDoesNotThrow(() -> emailService.sendEmail(to, subject, body, isHtml));
        verify(mailSender, times(1)).createMimeMessage();
        verify(mailSender, times(1)).send(any(MimeMessage.class));
    }

    @Test
    @DisplayName("Should throw RuntimeException when mail sender fails")
    void sendEmail_MailSenderFails_ThrowsException() {
        // GIVEN
        String to = "test@example.com";
        String subject = "Test Subject";
        String body = "Hello World";
        boolean isHtml = false;

        doThrow(new RuntimeException("SMTP connection failed"))
                .when(mailSender).send(any(MimeMessage.class));

        // WHEN & THEN
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> emailService.sendEmail(to, subject, body, isHtml));

        assertTrue(exception.getMessage().contains("Error sending email"));
        verify(mailSender, times(1)).send(any(MimeMessage.class));
    }

    @Test
    @DisplayName("Should send email with empty body")
    void sendEmail_EmptyBody_Success() {
        // GIVEN
        String to = "test@example.com";
        String subject = "Test Subject";
        String body = "";
        boolean isHtml = false;

        doNothing().when(mailSender).send(any(MimeMessage.class));

        // WHEN & THEN
        assertDoesNotThrow(() -> emailService.sendEmail(to, subject, body, isHtml));
        verify(mailSender, times(1)).send(any(MimeMessage.class));
    }

    @Test
    @DisplayName("Should send email with special characters in subject")
    void sendEmail_SpecialCharactersInSubject_Success() {
        // GIVEN
        String to = "test@example.com";
        String subject = "¡Hola! Confirmación de cuenta - España 2026";
        String body = "Test body";
        boolean isHtml = false;

        doNothing().when(mailSender).send(any(MimeMessage.class));

        // WHEN & THEN
        assertDoesNotThrow(() -> emailService.sendEmail(to, subject, body, isHtml));
        verify(mailSender, times(1)).send(any(MimeMessage.class));
    }

    // ============ TESTS FOR sendVerificationEmail ============

    @Test
    @DisplayName("Should send verification email successfully")
    void sendVerificationEmail_Success() {
        // GIVEN
        String email = "newuser@example.com";
        String username = "newuser";
        String verificationToken = "abc123-verification-token";

        doNothing().when(mailSender).send(any(MimeMessage.class));

        // WHEN & THEN
        assertDoesNotThrow(() -> emailService.sendVerificationEmail(email, username, verificationToken));
        verify(mailSender, times(1)).createMimeMessage();
        verify(mailSender, times(1)).send(any(MimeMessage.class));
    }

    @Test
    @DisplayName("Should throw exception when sending verification email fails")
    void sendVerificationEmail_MailFails_ThrowsException() {
        // GIVEN
        String email = "newuser@example.com";
        String username = "newuser";
        String verificationToken = "abc123-verification-token";

        doThrow(new RuntimeException("Mail server unavailable"))
                .when(mailSender).send(any(MimeMessage.class));

        // WHEN & THEN
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> emailService.sendVerificationEmail(email, username, verificationToken));

        assertTrue(exception.getMessage().contains("Error sending email"));
    }

    @Test
    @DisplayName("Should send verification email with long username")
    void sendVerificationEmail_LongUsername_Success() {
        // GIVEN
        String email = "user@example.com";
        String username = "verylongusernamethathasmanymanycharacters";
        String verificationToken = "token123";

        doNothing().when(mailSender).send(any(MimeMessage.class));

        // WHEN & THEN
        assertDoesNotThrow(() -> emailService.sendVerificationEmail(email, username, verificationToken));
        verify(mailSender, times(1)).send(any(MimeMessage.class));
    }

    @Test
    @DisplayName("Should send verification email with special characters in username")
    void sendVerificationEmail_SpecialCharsUsername_Success() {
        // GIVEN
        String email = "jose@example.com";
        String username = "José García";
        String verificationToken = "token456";

        doNothing().when(mailSender).send(any(MimeMessage.class));

        // WHEN & THEN
        assertDoesNotThrow(() -> emailService.sendVerificationEmail(email, username, verificationToken));
        verify(mailSender, times(1)).send(any(MimeMessage.class));
    }

    // ============ TESTS FOR sendWelcomeEmail ============

    @Test
    @DisplayName("Should send welcome email successfully")
    void sendWelcomeEmail_Success() {
        // GIVEN
        String email = "verified@example.com";
        String username = "verifieduser";

        doNothing().when(mailSender).send(any(MimeMessage.class));

        // WHEN & THEN
        assertDoesNotThrow(() -> emailService.sendWelcomeEmail(email, username));
        verify(mailSender, times(1)).createMimeMessage();
        verify(mailSender, times(1)).send(any(MimeMessage.class));
    }

    @Test
    @DisplayName("Should throw exception when sending welcome email fails")
    void sendWelcomeEmail_MailFails_ThrowsException() {
        // GIVEN
        String email = "verified@example.com";
        String username = "verifieduser";

        doThrow(new RuntimeException("Connection timeout"))
                .when(mailSender).send(any(MimeMessage.class));

        // WHEN & THEN
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> emailService.sendWelcomeEmail(email, username));

        assertTrue(exception.getMessage().contains("Error sending email"));
    }

    @Test
    @DisplayName("Should send welcome email with special characters in username")
    void sendWelcomeEmail_SpecialCharsUsername_Success() {
        // GIVEN
        String email = "maria@example.com";
        String username = "María López";

        doNothing().when(mailSender).send(any(MimeMessage.class));

        // WHEN & THEN
        assertDoesNotThrow(() -> emailService.sendWelcomeEmail(email, username));
        verify(mailSender, times(1)).send(any(MimeMessage.class));
    }

    // ============ EDGE CASES ============

    @Test
    @DisplayName("Should handle multiple email sends in sequence")
    void sendEmail_MultipleEmailsInSequence_Success() {
        // GIVEN
        doNothing().when(mailSender).send(any(MimeMessage.class));

        // WHEN
        emailService.sendEmail("user1@example.com", "Subject 1", "Body 1", false);
        emailService.sendEmail("user2@example.com", "Subject 2", "Body 2", true);
        emailService.sendEmail("user3@example.com", "Subject 3", "Body 3", false);

        // THEN
        verify(mailSender, times(3)).createMimeMessage();
        verify(mailSender, times(3)).send(any(MimeMessage.class));
    }

    @Test
    @DisplayName("Should send email with very long body content")
    void sendEmail_VeryLongBody_Success() {
        // GIVEN
        String to = "test@example.com";
        String subject = "Long Email";
        String body = "A".repeat(10000); // 10000 character body
        boolean isHtml = false;

        doNothing().when(mailSender).send(any(MimeMessage.class));

        // WHEN & THEN
        assertDoesNotThrow(() -> emailService.sendEmail(to, subject, body, isHtml));
        verify(mailSender, times(1)).send(any(MimeMessage.class));
    }

    @Test
    @DisplayName("Should send email with complex HTML content")
    void sendEmail_ComplexHtmlContent_Success() {
        // GIVEN
        String to = "test@example.com";
        String subject = "HTML Email";
        String body = """
                <!DOCTYPE html>
                <html>
                <head><style>body { font-family: Arial; }</style></head>
                <body>
                    <h1>Welcome!</h1>
                    <p>This is a <strong>test</strong> email.</p>
                    <a href="https://example.com">Click here</a>
                </body>
                </html>
                """;
        boolean isHtml = true;

        doNothing().when(mailSender).send(any(MimeMessage.class));

        // WHEN & THEN
        assertDoesNotThrow(() -> emailService.sendEmail(to, subject, body, isHtml));
        verify(mailSender, times(1)).send(any(MimeMessage.class));
    }

    @Test
    @DisplayName("Should send verification email with UUID token format")
    void sendVerificationEmail_UuidToken_Success() {
        // GIVEN
        String email = "user@example.com";
        String username = "testuser";
        String verificationToken = "550e8400-e29b-41d4-a716-446655440000";

        doNothing().when(mailSender).send(any(MimeMessage.class));

        // WHEN & THEN
        assertDoesNotThrow(() -> emailService.sendVerificationEmail(email, username, verificationToken));
        verify(mailSender, times(1)).send(any(MimeMessage.class));
    }
}

