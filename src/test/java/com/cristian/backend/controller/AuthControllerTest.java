package com.cristian.backend.controller;

import com.cristian.backend.dto.AuthResponse;
import com.cristian.backend.dto.LoginRequest;
import com.cristian.backend.dto.RegisterRequest;
import com.cristian.backend.security.JwtService;
import com.cristian.backend.security.OAuth2LoginSuccessHandler;
import com.cristian.backend.service.AuthService;
import com.cristian.backend.service.EmailService;
import com.cristian.backend.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Authentication Controller Test Suite")
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @Autowired
    private ObjectMapper objectMapper;

    private RegisterRequest validRegisterRequest;
    private LoginRequest validLoginRequest;
    private AuthResponse successAuthResponse;
    private AuthResponse failureAuthResponse;
    @MockitoBean  // AGREGAR ESTO
    private EmailService emailService;
    @BeforeEach
    public void setUp() {
        validRegisterRequest = RegisterRequest.builder()
                .username("cristian")
                .email("bejaranno05cortes@gmail.com")
                .password("123456")
                .firstName("Cristian")
                .lastName("Cortes")
                .build();

        validLoginRequest = LoginRequest.builder()
                .usernameOrEmail("cristian")
                .password("123456")
                .build();

        successAuthResponse = AuthResponse.builder()
                .id(1L)
                .username("cristian")
                .email("bejaranno05cortes@gmail.com")
                .firstName("Cristian")
                .lastName("Cortes")
                .success(true)
                .message("User registered successfully")
                .build();

        failureAuthResponse = AuthResponse.builder()
                .success(false)
                .message("Registration failed")
                .build();
    }

    @Test
    @DisplayName("Should successfully register a user with valid data")
    public void testRegisterSuccess() throws Exception {
        when(authService.register(any(RegisterRequest.class)))
                .thenReturn(successAuthResponse);

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRegisterRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.username").value("cristian"))
                .andExpect(jsonPath("$.email").value("bejaranno05cortes@gmail.com"))
                .andExpect(jsonPath("$.firstName").value("Cristian"))
                .andExpect(jsonPath("$.lastName").value("Cortes"))
                .andExpect(jsonPath("$.message").value("User registered successfully"));
    }

    @Test
    @DisplayName("Should fail registration when service returns error")
    public void testRegisterFailure() throws Exception {
        when(authService.register(any(RegisterRequest.class)))
                .thenReturn(failureAuthResponse);

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRegisterRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Registration failed"));
    }

    @Test
    @DisplayName("Should reject registration with invalid email")
    public void testRegisterWithInvalidEmail() throws Exception {
        RegisterRequest invalidRequest = RegisterRequest.builder()
                .username("cristian")
                .email("invalid-email")
                .password("123456")
                .firstName("Cristian")
                .lastName("Cortes")
                .build();

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should reject registration with a very short password")
    public void testRegisterWithShortPassword() throws Exception {
        RegisterRequest invalidRequest = RegisterRequest.builder()
                .username("cristian")
                .email("bejaranno05cortes@gmail.com")
                .password("123")
                .firstName("Cristian")
                .lastName("Cortes")
                .build();

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should reject registration when required fields are missing")
    public void testRegisterWithMissingFields() throws Exception {
        RegisterRequest invalidRequest = RegisterRequest.builder()
                .username("cristian")
                .build();

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should successfully login with valid credentials")
    public void testLoginSuccess() throws Exception {
        when(authService.login(any(LoginRequest.class)))
                .thenReturn(successAuthResponse);

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validLoginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.username").value("cristian"))
                .andExpect(jsonPath("$.message").value("User registered successfully"));
    }

    @Test
    @DisplayName("Should fail login when credentials are invalid")
    public void testLoginFailure() throws Exception {
        when(authService.login(any(LoginRequest.class)))
                .thenReturn(failureAuthResponse);

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validLoginRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("Should reject login when password is missing")
    public void testLoginWithMissingPassword() throws Exception {
        LoginRequest invalidRequest = LoginRequest.builder()
                .usernameOrEmail("cristian")
                .build();

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should verify that authentication service is running")
    public void testHealthCheck() throws Exception {
        mockMvc.perform(get("/api/auth/health"))
                .andExpect(status().isOk())
                .andExpect(content().string("Auth service is running"));
    }

    @Test
    @DisplayName("Should successfully verify email with valid token")
    public void testVerifyEmailSuccess() throws Exception {
        AuthResponse verifySuccessResponse = AuthResponse.builder()
                .success(true)
                .message("Email verified successfully")
                .build();

        when(authService.verifyEmail("valid-token"))
                .thenReturn(verifySuccessResponse);

        mockMvc.perform(get("/api/auth/verify")
                .param("token", "valid-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Email verified successfully"));
    }

    @Test
    @DisplayName("Should fail email verification with invalid or expired token")
    public void testVerifyEmailFailure() throws Exception {
        AuthResponse verifyFailureResponse = AuthResponse.builder()
                .success(false)
                .message("Invalid or expired token")
                .build();

        when(authService.verifyEmail("invalid-token"))
                .thenReturn(verifyFailureResponse);

        mockMvc.perform(get("/api/auth/verify")
                .param("token", "invalid-token"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Invalid or expired token"));
    }

    @Test
    @DisplayName("Should reject email verification without token")
    public void testVerifyEmailMissingToken() throws Exception {
        mockMvc.perform(get("/api/auth/verify"))
                .andExpect(status().isBadRequest());
    }
}

