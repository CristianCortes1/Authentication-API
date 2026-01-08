package com.cristian.backend.controller;

import com.cristian.backend.dto.ChangeRoleRequest;
import com.cristian.backend.exception.UserNotFoundException;
import com.cristian.backend.model.User;
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
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Admin Controller Test Suite")
public class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @MockitoBean
    private EmailService emailService;

    @Autowired
    private ObjectMapper objectMapper;

    private ChangeRoleRequest validRequestWithEmail;
    private ChangeRoleRequest validRequestWithUserId;

    @BeforeEach
    public void setUp() {
        validRequestWithEmail = ChangeRoleRequest.builder()
                .email("johndoe@example.com")
                .role(User.Role.ADMIN)
                .build();

        validRequestWithUserId = ChangeRoleRequest.builder()
                .userId(1L)
                .role(User.Role.ADMIN)
                .build();
    }

    // ============ SUCCESS CASES ============

    @Test
    @DisplayName("Should successfully change user role by email when admin is authenticated")
    @WithMockUser(roles = "ADMIN")
    public void testChangeUserRoleByEmail_Success() throws Exception {
        User updatedUser = User.builder()
                .id(1L)
                .username("johndoe")
                .email("johndoe@example.com")
                .firstName("John")
                .lastName("Doe")
                .role(User.Role.ADMIN)
                .enabled(true)
                .build();

        when(userService.changeUserRole(eq("johndoe@example.com"), eq(User.Role.ADMIN)))
                .thenReturn(updatedUser);

        mockMvc.perform(put("/api/admin/users/role")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequestWithEmail)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Role updated successfully"))
                .andExpect(jsonPath("$.user.id").value(1))
                .andExpect(jsonPath("$.user.email").value("johndoe@example.com"))
                .andExpect(jsonPath("$.user.username").value("johndoe"))
                .andExpect(jsonPath("$.user.role").value("ADMIN"));
    }

    @Test
    @DisplayName("Should successfully change user role by userId when admin is authenticated")
    @WithMockUser(roles = "ADMIN")
    public void testChangeUserRoleByUserId_Success() throws Exception {
        User updatedUser = User.builder()
                .id(1L)
                .username("johndoe")
                .email("johndoe@example.com")
                .firstName("John")
                .lastName("Doe")
                .role(User.Role.ADMIN)
                .enabled(true)
                .build();

        when(userService.changeUserRoleById(eq(1L), eq(User.Role.ADMIN)))
                .thenReturn(updatedUser);

        mockMvc.perform(put("/api/admin/users/role")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequestWithUserId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Role updated successfully"))
                .andExpect(jsonPath("$.user.id").value(1))
                .andExpect(jsonPath("$.user.role").value("ADMIN"));
    }

    @Test
    @DisplayName("Should successfully change role from ADMIN to USER")
    @WithMockUser(roles = "ADMIN")
    public void testChangeUserRole_AdminToUser_Success() throws Exception {
        ChangeRoleRequest request = ChangeRoleRequest.builder()
                .email("admin@example.com")
                .role(User.Role.USER)
                .build();

        User updatedUser = User.builder()
                .id(2L)
                .username("adminuser")
                .email("admin@example.com")
                .role(User.Role.USER)
                .enabled(true)
                .build();

        when(userService.changeUserRole(eq("admin@example.com"), eq(User.Role.USER)))
                .thenReturn(updatedUser);

        mockMvc.perform(put("/api/admin/users/role")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.user.role").value("USER"));
    }

    // ============ VALIDATION FAILURE CASES ============

    @Test
    @DisplayName("Should return bad request when neither userId nor email is provided")
    @WithMockUser(roles = "ADMIN")
    public void testChangeUserRole_MissingUserIdAndEmail_BadRequest() throws Exception {
        ChangeRoleRequest invalidRequest = ChangeRoleRequest.builder()
                .role(User.Role.ADMIN)
                .build();

        mockMvc.perform(put("/api/admin/users/role")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("You must provide userId or email"));
    }

    @Test
    @DisplayName("Should return bad request when email is empty string")
    @WithMockUser(roles = "ADMIN")
    public void testChangeUserRole_EmptyEmail_BadRequest() throws Exception {
        ChangeRoleRequest invalidRequest = ChangeRoleRequest.builder()
                .email("")
                .role(User.Role.ADMIN)
                .build();

        mockMvc.perform(put("/api/admin/users/role")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("Should return bad request when role is missing")
    @WithMockUser(roles = "ADMIN")
    public void testChangeUserRole_MissingRole_BadRequest() throws Exception {
        ChangeRoleRequest invalidRequest = ChangeRoleRequest.builder()
                .email("johndoe@example.com")
                .build();

        mockMvc.perform(put("/api/admin/users/role")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    // ============ USER NOT FOUND CASES ============

    @Test
    @DisplayName("Should return not found when user email does not exist")
    @WithMockUser(roles = "ADMIN")
    public void testChangeUserRole_UserNotFoundByEmail() throws Exception {
        when(userService.changeUserRole(eq("nonexistent@example.com"), any(User.Role.class)))
                .thenThrow(new UserNotFoundException());

        ChangeRoleRequest request = ChangeRoleRequest.builder()
                .email("nonexistent@example.com")
                .role(User.Role.ADMIN)
                .build();

        mockMvc.perform(put("/api/admin/users/role")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should return not found when user ID does not exist")
    @WithMockUser(roles = "ADMIN")
    public void testChangeUserRole_UserNotFoundById() throws Exception {
        when(userService.changeUserRoleById(eq(999L), any(User.Role.class)))
                .thenThrow(new UserNotFoundException());

        ChangeRoleRequest request = ChangeRoleRequest.builder()
                .userId(999L)
                .role(User.Role.ADMIN)
                .build();

        mockMvc.perform(put("/api/admin/users/role")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    // ============ AUTHORIZATION FAILURE CASES ============

    @Test
    @DisplayName("Should return forbidden when user role is not ADMIN")
    @WithMockUser(roles = "USER")
    public void testChangeUserRole_Forbidden_NotAdmin() throws Exception {
        mockMvc.perform(put("/api/admin/users/role")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequestWithEmail)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should return unauthorized when no authentication is provided")
    public void testChangeUserRole_Unauthorized_NoAuth() throws Exception {
        mockMvc.perform(put("/api/admin/users/role")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequestWithEmail)))
                .andExpect(status().isUnauthorized());
    }

    // ============ EDGE CASES ============

    @Test
    @DisplayName("Should prioritize userId over email when both are provided")
    @WithMockUser(roles = "ADMIN")
    public void testChangeUserRole_BothUserIdAndEmail_UsesUserId() throws Exception {
        ChangeRoleRequest request = ChangeRoleRequest.builder()
                .userId(1L)
                .email("johndoe@example.com")
                .role(User.Role.ADMIN)
                .build();

        User updatedUser = User.builder()
                .id(1L)
                .username("johndoe")
                .email("johndoe@example.com")
                .role(User.Role.ADMIN)
                .enabled(true)
                .build();

        when(userService.changeUserRoleById(eq(1L), eq(User.Role.ADMIN)))
                .thenReturn(updatedUser);

        mockMvc.perform(put("/api/admin/users/role")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("Should handle request with special characters in email")
    @WithMockUser(roles = "ADMIN")
    public void testChangeUserRole_SpecialCharsInEmail_Success() throws Exception {
        ChangeRoleRequest request = ChangeRoleRequest.builder()
                .email("user+tag@example.com")
                .role(User.Role.ADMIN)
                .build();

        User updatedUser = User.builder()
                .id(3L)
                .username("userplustag")
                .email("user+tag@example.com")
                .role(User.Role.ADMIN)
                .enabled(true)
                .build();

        when(userService.changeUserRole(eq("user+tag@example.com"), eq(User.Role.ADMIN)))
                .thenReturn(updatedUser);

        mockMvc.perform(put("/api/admin/users/role")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.user.email").value("user+tag@example.com"));
    }

    @Test
    @DisplayName("Should return bad request for invalid JSON body")
    @WithMockUser(roles = "ADMIN")
    public void testChangeUserRole_InvalidJson_BadRequest() throws Exception {
        String invalidJson = "this is not valid json";
        mockMvc.perform(put("/api/admin/users/role")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return bad request for empty request body")
    @WithMockUser(roles = "ADMIN")
    public void testChangeUserRole_EmptyBody_BadRequest() throws Exception {
        mockMvc.perform(put("/api/admin/users/role")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }
}

