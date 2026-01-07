package com.cristian.backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {

    @NotBlank(message = "El nombre de usuario o email es requerido")
    private String usernameOrEmail;

    @NotBlank(message = "La contraseña es requerida")
    private String password;
}

