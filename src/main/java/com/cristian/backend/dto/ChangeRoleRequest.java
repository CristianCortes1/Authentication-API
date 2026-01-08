package com.cristian.backend.dto;

import com.cristian.backend.model.User;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to change a user's role. Either userId or email must be provided.")
public class ChangeRoleRequest {

    @Schema(description = "User ID (optional if email is provided)", example = "1")
    private Long userId;

    @Schema(description = "User's email address (optional if userId is provided)", example = "johndoe@example.com")
    private String email;

    @Schema(description = "New role to assign to the user", example = "ADMIN", requiredMode = Schema.RequiredMode.REQUIRED, allowableValues = {"USER", "ADMIN"})
    @NotNull(message = "Role is required")
    private User.Role role;
}

