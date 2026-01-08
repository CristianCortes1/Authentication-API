package com.cristian.backend.controller;

import com.cristian.backend.dto.ChangeRoleRequest;
import com.cristian.backend.model.User;
import com.cristian.backend.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Tag(name = "Administration", description = "System administration endpoints. Requires ADMIN role.")
@SecurityRequirement(name = "Bearer Authentication")
public class AdminController {

    private final UserService userService;

    @Operation(
        summary = "Change user role",
        description = "Allows an administrator to change a user's role. The user can be identified by their ID or email address."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Role updated successfully",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(value = "{\"success\": true, \"message\": \"Role updated successfully\", \"user\": {\"id\": 1, \"email\": \"johndoe@example.com\", \"username\": \"johndoe\", \"role\": \"ADMIN\"}}")
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid request (missing userId or email)",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(value = "{\"success\": false, \"message\": \"You must provide userId or email\"}")
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized - Invalid or expired JWT token",
            content = @Content(mediaType = "application/json")
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Forbidden - User does not have administrator permissions",
            content = @Content(mediaType = "application/json")
        ),
        @ApiResponse(
            responseCode = "404",
            description = "User not found",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(value = "{\"success\": false, \"message\": \"User not found\"}")
            )
        )
    })
    @PutMapping("/users/role")
    public ResponseEntity<Map<String, Object>> changeUserRole(@Valid @RequestBody ChangeRoleRequest request) {
        User updatedUser;

        if (request.getUserId() != null) {
            updatedUser = userService.changeUserRoleById(request.getUserId(), request.getRole());
        } else if (request.getEmail() != null && !request.getEmail().isEmpty()) {
            updatedUser = userService.changeUserRole(request.getEmail(), request.getRole());
        } else {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Debes proporcionar userId o email"
            ));
        }

        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", "Rol actualizado correctamente",
            "user", Map.of(
                "id", updatedUser.getId(),
                "email", updatedUser.getEmail(),
                "username", updatedUser.getUsername(),
                "role", updatedUser.getRole().name()
            )
        ));
    }
}

