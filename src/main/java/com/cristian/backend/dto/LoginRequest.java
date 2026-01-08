package com.cristian.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Login request with user credentials")
public class LoginRequest {

    @Schema(description = "Username or email address", example = "johndoe@example.com", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Username or email is required")
    private String usernameOrEmail;

    @Schema(description = "User's password", example = "password123", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Password is required")
    private String password;
}

