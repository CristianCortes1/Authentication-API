package com.cristian.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Authentication response containing user information and JWT token")
public class AuthResponse {

    @Schema(description = "Unique user ID", example = "1")
    private Long id;

    @Schema(description = "Username", example = "johndoe")
    private String username;

    @Schema(description = "User's email address", example = "johndoe@example.com")
    private String email;

    @Schema(description = "User's first name", example = "John")
    private String firstName;

    @Schema(description = "User's last name", example = "Doe")
    private String lastName;

    @Schema(description = "Descriptive message of the operation result", example = "User registered successfully. Please verify your email.")
    private String message;

    @Schema(description = "Indicates whether the operation was successful", example = "true")
    private Boolean success;

    @Schema(description = "JWT token for authentication", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String token;

    @Schema(description = "User's role in the system", example = "USER", allowableValues = {"USER", "ADMIN"})
    private String role;
}

