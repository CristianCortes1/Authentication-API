package com.cristian.backend.service;

import com.cristian.backend.dto.AuthResponse;
import com.cristian.backend.dto.LoginRequest;
import com.cristian.backend.dto.RegisterRequest;
import com.cristian.backend.model.User;
import com.cristian.backend.repository.UserRepository;
import com.cristian.backend.security.JwtService;
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
        // Verificar si el usuario ya existe
        if (userRepository.existsByUsername(request.getUsername())) {
            return AuthResponse.builder()
                    .success(false)
                    .message("El nombre de usuario ya existe")
                    .build();
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            return AuthResponse.builder()
                    .success(false)
                    .message("El email ya está registrado")
                    .build();
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

            System.err.println("Error al enviar email de verificación: " + e.getMessage());
        }

        return AuthResponse.builder()
                .id(savedUser.getId())
                .username(savedUser.getUsername())
                .email(savedUser.getEmail())
                .firstName(savedUser.getFirstName())
                .lastName(savedUser.getLastName())
                .success(true)
                .message("Usuario registrado exitosamente. Por favor verifica tu email.")
                .build();
    }

    public AuthResponse login(LoginRequest request) {
        String identifier = request.getUsernameOrEmail();

        User user = null;
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";

        if (identifier.matches(emailRegex)) {
            user = userRepository.findByEmail(identifier).orElse(null);
        } else {
            user = userRepository.findByUsername(identifier).orElse(null);
        }

        if (user == null) {
            return AuthResponse.builder()
                    .success(false)
                    .message("Usuario no encontrado")
                    .build();
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            return AuthResponse.builder()
                    .success(false)
                    .message("Contraseña incorrecta")
                    .build();
        }

        if (!user.getEnabled()) {
            return AuthResponse.builder()
                    .success(false)
                    .message("Por favor verifica tu email antes de iniciar sesión")
                    .build();
        }

        return AuthResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .success(true)
                .message("Inicio de sesión exitoso")
                .build();
    }

    public AuthResponse verifyEmail(String token) {
        // Validar el token JWT
        if (!jwtService.validateVerificationToken(token)) {
            return AuthResponse.builder()
                    .success(false)
                    .message("Token de verificación inválido o expirado")
                    .build();
        }

        // Extraer el email del token
        String email;
        try {
            email = jwtService.extractSubject(token);
        } catch (Exception e) {
            return AuthResponse.builder()
                    .success(false)
                    .message("Error al procesar el token")
                    .build();
        }

        // Buscar el usuario por email y token
        User user = userRepository.findByVerificationToken(token)
                .orElse(null);

        if (user == null) {
            return AuthResponse.builder()
                    .success(false)
                    .message("Token de verificación inválido")
                    .build();
        }

        // Verificar que el email del token coincida con el email del usuario
        if (!user.getEmail().equals(email)) {
            return AuthResponse.builder()
                    .success(false)
                    .message("Token de verificación no coincide con el usuario")
                    .build();
        }

        if (user.getEnabled()) {
            return AuthResponse.builder()
                    .success(true)
                    .message("El email ya fue verificado anteriormente")
                    .build();
        }

        user.setEnabled(true);
        user.setVerificationToken(null);
        userRepository.save(user);

        try {
            emailService.sendWelcomeEmail(user.getEmail(), user.getUsername());
        } catch (Exception e) {
            System.err.println("Error al enviar email de bienvenida: " + e.getMessage());
        }

        return AuthResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .success(true)
                .message("Email verificado exitosamente")
                .build();
    }
}

