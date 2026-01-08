package com.cristian.backend.service;

import com.cristian.backend.dto.AuthResponse;
import com.cristian.backend.dto.LoginRequest;
import com.cristian.backend.dto.RegisterRequest;
import com.cristian.backend.model.User;
import com.cristian.backend.repository.UserRepository;
import com.cristian.backend.security.JwtService;
import com.cristian.backend.exception.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final JwtService jwtService;

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new UsernameAlreadyExistsException();
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyRegisteredException();
        }

        // Generar token de verificación JWT
        String verificationToken = jwtService.generateVerificationToken(request.getEmail());

        // Crear nuevo usuario
        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .enabled(false)
                .verificationToken(verificationToken)
                .build();

        User savedUser = userRepository.save(user);

        try {
            emailService.sendVerificationEmail(
                savedUser.getEmail(),
                savedUser.getUsername(),
                verificationToken
            );
        } catch (Exception e) {

            System.err.println("Error sending verification email: " + e.getMessage());
        }

        return AuthResponse.builder()
                .id(savedUser.getId())
                .username(savedUser.getUsername())
                .email(savedUser.getEmail())
                .firstName(savedUser.getFirstName())
                .lastName(savedUser.getLastName())
                .success(true)
                .message("User registered successfully. Please verify your email.")
                .build();
    }

    public AuthResponse login(LoginRequest request) {
        String identifier = request.getUsernameOrEmail();

        User user;
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";

        if (identifier.matches(emailRegex)) {
            user = userRepository.findByEmail(identifier).orElse(null);
        } else {
            user = userRepository.findByUsername(identifier).orElse(null);
        }

        if (user == null) {
            throw new UserNotFoundException();
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new IncorrectPasswordException();
        }

        if (!user.getEnabled()) {
            throw new EmailNotVerifiedException();
        }

        // Generar token con el rol del usuario
        String token = jwtService.generateToken(user.getEmail(), user.getRole().name());

        return AuthResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .token(token)
                .role(user.getRole().name())
                .success(true)
                .message("Login successful")
                .build();
    }

    public AuthResponse verifyEmail(String token) {
        if (!jwtService.validateVerificationToken(token)) {
            throw new VerificationTokenInvalidException();
        }

        String email;
        try {
            email = jwtService.extractSubject(token);
        } catch (Exception e) {
            throw new ErrorExtractingEmailException();
        }

        User user = userRepository.findByVerificationToken(token)
                .orElse(null);

        if (user == null) {
            throw new InvalidVerificationTokenException();
        }

        if (!user.getEmail().equals(email)) {
            throw new TokenEmailDoesNotMatchException();
        }

        if (user.getEnabled()) {
            return AuthResponse.builder()
                    .success(true)
                    .message("Email already verified")
                    .build();
        }

        user.setEnabled(true);
        user.setVerificationToken(null);
        userRepository.save(user);

        try {
            emailService.sendWelcomeEmail(user.getEmail(), user.getUsername());
        } catch (Exception e) {
            System.err.println("Error sending welcome message " + e.getMessage());
        }

        return AuthResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .success(true)
                .message("Email verified successfully")
                .build();
    }
}
