package com.cristian.backend.service;

import com.cristian.backend.dto.AuthResponse;
import com.cristian.backend.dto.RegisterRequest;
import com.cristian.backend.model.User;
import com.cristian.backend.repository.UserRepository;
import com.cristian.backend.security.JwtService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService Test Suite")
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtService jwtService;

    @Mock
    private EmailService emailService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    @Test
    void registerSuccessful() {
        // ---------- GIVEN (preparation) ----------
        RegisterRequest request = RegisterRequest.builder()
                .username("testUser")
                .email("testUser@test.com")
                .password("123456")
                .firstName("testUser")
                .lastName("Cortes")
                .build();

        when(userRepository.existsByUsername("testUser")).thenReturn(false);
        when(userRepository.existsByEmail("testUser@test.com")).thenReturn(false);

        when(jwtService.generateVerificationToken("testUser@test.com"))
                .thenReturn("verification-token");

        when(passwordEncoder.encode("123456"))
                .thenReturn("hashed-password");

        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> {
                    User user = invocation.getArgument(0);
                    user.setId(1L);
                    return user;
                });

        // ---------- WHEN (action) ----------
        AuthResponse response = authService.register(request);

        // ---------- THEN (verification) ----------
        assertTrue(response.getSuccess());
        assertEquals("testUser", response.getUsername());
        assertEquals("testUser@test.com", response.getEmail());
        assertEquals(1L, response.getId());

        verify(emailService, times(1))
                .sendVerificationEmail(
                        "testUser@test.com",
                        "testUser",
                        "verification-token"
                );

        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("Register fails when username already exists")
    void registerUsernameExists() {
        // ---------- GIVEN (preparation) ----------
        RegisterRequest request = RegisterRequest.builder()
                .username("testUserExistent")
                .email("testUser@test.com")
                .password("123456")
                .firstName("testUser")
                .lastName("Cortes")
                .build();

        when(userRepository.existsByUsername("testUserExistent"))
                .thenReturn(true);

        // ---------- WHEN ----------
        assertThrows(com.cristian.backend.exception.UsernameAlreadyExistsException.class, () -> authService.register(request));

        // ---------- THEN ----------
        verify(userRepository, never()).save(any());
        verify(emailService, never()).sendVerificationEmail(any(), any(), any());
        verify(jwtService, never()).generateVerificationToken(any());
        verify(passwordEncoder, never()).encode(any());
    }

    @Test
    @DisplayName("Register fails when email already exists")
    void registerEmailExists() {
        // ---------- GIVEN ----------
        RegisterRequest request = RegisterRequest.builder()
                .username("newUser")
                .email("existing@email.com")
                .password("password123")
                .firstName("New")
                .lastName("User")
                .build();

        when(userRepository.existsByUsername("newUser")).thenReturn(false);
        when(userRepository.existsByEmail("existing@email.com")).thenReturn(true);

        // ---------- WHEN ----------
        assertThrows(com.cristian.backend.exception.EmailAlreadyRegisteredException.class, () -> authService.register(request));

        // ---------- THEN ----------
        verify(userRepository, never()).save(any());
        verify(emailService, never()).sendVerificationEmail(any(), any(), any());
        verify(jwtService, never()).generateVerificationToken(any());
        verify(passwordEncoder, never()).encode(any());
    }

    @Test
    @DisplayName("Register succeeds even if sending verification email fails")
    void registerErrorSendingVerificationEmail() {
        // ---------- GIVEN ----------
        RegisterRequest request = RegisterRequest.builder()
                .username("userEmailError")
                .email("userEmailError@email.com")
                .password("password123")
                .firstName("User")
                .lastName("EmailError")
                .build();

        when(userRepository.existsByUsername("userEmailError")).thenReturn(false);
        when(userRepository.existsByEmail("userEmailError@email.com")).thenReturn(false);
        when(jwtService.generateVerificationToken("userEmailError@email.com")).thenReturn("token-error");
        when(passwordEncoder.encode("password123")).thenReturn("hashed-password");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(2L);
            return user;
        });
        doThrow(new RuntimeException("Mail error")).when(emailService).sendVerificationEmail(
                eq("userEmailError@email.com"),
                eq("userEmailError"),
                eq("token-error")
        );

        // ---------- WHEN ----------
        AuthResponse response = authService.register(request);

        // ---------- THEN ----------
        assertTrue(response.getSuccess());
        assertEquals("User registered successfully. Please verify your email.", response.getMessage());
        verify(emailService, times(1)).sendVerificationEmail(
                "userEmailError@email.com",
                "userEmailError",
                "token-error"
        );
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("Login successful with correct credentials")
    void loginSuccessful() {
        // GIVEN
        String identifier = "user@test.com";
        String password = "password123";
        User user = User.builder()
                .id(10L)
                .username("user")
                .email(identifier)
                .password("hashed-password")
                .firstName("User")
                .lastName("Test")
                .enabled(true)
                .build();
        when(userRepository.findByEmail(identifier)).thenReturn(java.util.Optional.of(user));
        when(passwordEncoder.matches(password, "hashed-password")).thenReturn(true);
        // WHEN
        AuthResponse response = authService.login(new com.cristian.backend.dto.LoginRequest(identifier, password));
        // THEN
        assertTrue(response.getSuccess());
        assertEquals("Login successful", response.getMessage());
        assertEquals(user.getId(), response.getId());
        assertEquals(user.getUsername(), response.getUsername());
        assertEquals(user.getEmail(), response.getEmail());
    }

    @Test
    @DisplayName("Login fails when user not found")
    void loginUserNotFound() {
        // GIVEN
        String identifier = "notfound@test.com";
        when(userRepository.findByEmail(identifier)).thenReturn(java.util.Optional.empty());
        // WHEN
        assertThrows(com.cristian.backend.exception.UserNotFoundException.class, () -> authService.login(new com.cristian.backend.dto.LoginRequest(identifier, "irrelevant")));
        // THEN
    }

    @Test
    @DisplayName("Login fails with incorrect password")
    void loginIncorrectPassword() {
        // GIVEN
        String identifier = "user@test.com";
        User user = User.builder()
                .id(11L)
                .username("user")
                .email(identifier)
                .password("hashed-password")
                .enabled(true)
                .build();
        when(userRepository.findByEmail(identifier)).thenReturn(java.util.Optional.of(user));
        when(passwordEncoder.matches("wrongpass", "hashed-password")).thenReturn(false);
        // WHEN
        assertThrows(com.cristian.backend.exception.IncorrectPasswordException.class, () -> authService.login(new com.cristian.backend.dto.LoginRequest(identifier, "wrongpass")));
        // THEN
    }

    @Test
    @DisplayName("Login fails when user is not enabled")
    void loginUserNotEnabled() {
        // GIVEN
        String identifier = "user@test.com";
        User user = User.builder()
                .id(12L)
                .username("user")
                .email(identifier)
                .password("hashed-password")
                .enabled(false)
                .build();
        when(userRepository.findByEmail(identifier)).thenReturn(java.util.Optional.of(user));
        when(passwordEncoder.matches("password123", "hashed-password")).thenReturn(true);
        // WHEN
        assertThrows(com.cristian.backend.exception.EmailNotVerifiedException.class, () -> authService.login(new com.cristian.backend.dto.LoginRequest(identifier, "password123")));
        // THEN
    }

    @Test
    @DisplayName("Verify email successful with valid token")
    void verifyEmailSuccessful() {
        // GIVEN
        String token = "valid-token";
        String email = "user@test.com";
        User user = User.builder()
                .id(20L)
                .username("user")
                .email(email)
                .firstName("User")
                .lastName("Test")
                .enabled(false)
                .verificationToken(token)
                .build();
        when(jwtService.validateVerificationToken(token)).thenReturn(true);
        when(jwtService.extractSubject(token)).thenReturn(email);
        when(userRepository.findByVerificationToken(token)).thenReturn(java.util.Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        // WHEN
        AuthResponse response = authService.verifyEmail(token);
        // THEN
        assertTrue(response.getSuccess());
        assertEquals("Email verified successfully", response.getMessage());
        assertEquals(user.getId(), response.getId());
        verify(emailService, times(1)).sendWelcomeEmail(email, user.getUsername());
    }

    @Test
    @DisplayName("Verify email fails with invalid token")
    void verifyEmailTokenInvalid() {
        // GIVEN
        String token = "invalid-token";
        when(jwtService.validateVerificationToken(token)).thenReturn(false);
        // WHEN
        assertThrows(com.cristian.backend.exception.VerificationTokenInvalidException.class, () -> authService.verifyEmail(token));
        // THEN
    }

    @Test
    @DisplayName("Verify email fails when error extracting email from token")
    void verifyEmailErrorExtractingEmail() {
        // GIVEN
        String token = "token-error";
        when(jwtService.validateVerificationToken(token)).thenReturn(true);
        when(jwtService.extractSubject(token)).thenThrow(new RuntimeException("extract error"));
        // WHEN
        assertThrows(com.cristian.backend.exception.ErrorExtractingEmailException.class, () -> authService.verifyEmail(token));
        // THEN
    }

    @Test
    @DisplayName("Verify email fails when user not found for token")
    void verifyEmailUserNotFound() {
        // GIVEN
        String token = "notfound-token";
        String email = "user@test.com";
        when(jwtService.validateVerificationToken(token)).thenReturn(true);
        when(jwtService.extractSubject(token)).thenReturn(email);
        when(userRepository.findByVerificationToken(token)).thenReturn(java.util.Optional.empty());
        // WHEN
        assertThrows(com.cristian.backend.exception.InvalidVerificationTokenException.class, () -> authService.verifyEmail(token));
        // THEN
    }

    @Test
    @DisplayName("Verify email fails when token email does not match user email")
    void verifyEmailTokenEmailDoesNotMatch() {
        // GIVEN
        String token = "token-mismatch";
        String email = "user@test.com";
        User user = User.builder()
                .id(21L)
                .username("user")
                .email("other@test.com")
                .verificationToken(token)
                .enabled(false)
                .build();
        when(jwtService.validateVerificationToken(token)).thenReturn(true);
        when(jwtService.extractSubject(token)).thenReturn(email);
        when(userRepository.findByVerificationToken(token)).thenReturn(java.util.Optional.of(user));
        // WHEN
        assertThrows(com.cristian.backend.exception.TokenEmailDoesNotMatchException.class, () -> authService.verifyEmail(token));
        // THEN
    }

    @Test
    @DisplayName("Verify email when email already verified")
    void verifyEmailAlreadyVerified() {
        // GIVEN
        String token = "already-verified-token";
        String email = "user@test.com";
        User user = User.builder()
                .id(22L)
                .username("user")
                .email(email)
                .verificationToken(token)
                .enabled(true)
                .build();
        when(jwtService.validateVerificationToken(token)).thenReturn(true);
        when(jwtService.extractSubject(token)).thenReturn(email);
        when(userRepository.findByVerificationToken(token)).thenReturn(java.util.Optional.of(user));
        // WHEN
        AuthResponse response = authService.verifyEmail(token);
        // THEN
        assertTrue(response.getSuccess());
        assertEquals("Email already verified", response.getMessage());
    }
}
