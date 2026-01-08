package com.cristian.backend.controller;

import com.cristian.backend.dto.AuthResponse;
import com.cristian.backend.dto.LoginRequest;
import com.cristian.backend.dto.RegisterRequest;
import com.cristian.backend.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600)
@Tag(name = "Authentication", description = "Endpoints for user registration, login, and verification")
public class AuthController {

    private final AuthService authService;

    @Operation(
        summary = "Register new user",
        description = "Creates a new user account. A verification email will be sent to the provided email address."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "User registered successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = AuthResponse.class),
                examples = @ExampleObject(value = "{\"id\": 1, \"username\": \"johndoe\", \"email\": \"johndoe@example.com\", \"firstName\": \"John\", \"lastName\": \"Doe\", \"message\": \"User registered successfully. Please verify your email.\", \"success\": true, \"role\": \"USER\"}")
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Registration error (invalid data, email already registered, etc.)",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = AuthResponse.class),
                examples = @ExampleObject(value = "{\"message\": \"Email is already registered\", \"success\": false}")
            )
        )
    })
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        if (response.getSuccess()) {
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @Operation(
        summary = "User login",
        description = "Authenticates the user with their credentials and returns a JWT token"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Login successful",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = AuthResponse.class),
                examples = @ExampleObject(value = "{\"id\": 1, \"username\": \"johndoe\", \"email\": \"johndoe@example.com\", \"firstName\": \"John\", \"lastName\": \"Doe\", \"message\": \"Login successful\", \"success\": true, \"token\": \"eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...\", \"role\": \"USER\"}")
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Invalid credentials or email not verified",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = AuthResponse.class),
                examples = @ExampleObject(value = "{\"message\": \"Invalid credentials\", \"success\": false}")
            )
        )
    })
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        if (response.getSuccess()) {
            return ResponseEntity.ok(response);
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    @Operation(
        summary = "Check service health",
        description = "Health check endpoint to verify that the authentication service is running"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Service running correctly",
            content = @Content(
                mediaType = "text/plain",
                examples = @ExampleObject(value = "Auth service is running")
            )
        )
    })
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Auth service is running");
    }

    @Operation(
        summary = "Verify email",
        description = "Verifies the user's email address using the token sent by email"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Email verified successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = AuthResponse.class),
                examples = @ExampleObject(value = "{\"message\": \"Email verified successfully\", \"success\": true}")
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid or expired token",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = AuthResponse.class),
                examples = @ExampleObject(value = "{\"message\": \"Invalid or expired verification token\", \"success\": false}")
            )
        )
    })
    @GetMapping("/verify")
    public ResponseEntity<AuthResponse> verifyEmail(
        @Parameter(description = "Verification token sent by email", required = true, example = "abc123-def456-ghi789")
        @RequestParam("token") String token
    ) {
        AuthResponse response = authService.verifyEmail(token);
        if (response.getSuccess()) {
            return ResponseEntity.ok(response);
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @Operation(
        summary = "Get current JWT token",
        description = "Retrieves the JWT token stored in cookies after logging in with OAuth2 (Google). Useful for testing endpoints in Swagger."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Token found",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(value = "{\"token\": \"eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...\", \"bearer\": \"Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...\", \"message\": \"Copy the 'bearer' value and paste it in Swagger\"}")
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Token not found. User must login first.",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(value = "{\"error\": \"No token found. Please login first.\"}")
            )
        )
    })
    @GetMapping("/token")
    public ResponseEntity<Map<String, String>> getToken(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("token".equals(cookie.getName())) {
                    return ResponseEntity.ok(Map.of(
                        "token", cookie.getValue(),
                        "bearer", "Bearer " + cookie.getValue(),
                        "message", "Copia el valor de 'bearer' y pégalo en Swagger"
                    ));
                }
            }
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("error", "No token found. Please login first."));
    }
}

