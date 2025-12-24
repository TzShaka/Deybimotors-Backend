package com.deybimotors.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para autenticación - RF-001
 */
public class AuthDTO {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LoginRequest {
        @NotBlank(message = "El usuario es obligatorio")
        private String username;

        @NotBlank(message = "La contraseña es obligatoria")
        private String password;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LoginResponse {
        private String token;
        private String tipo = "Bearer";
        private Long id;
        private String username;
        private String nombreCompleto;
        private String rol;
        private Long sedeId;
        private String sedeNombre;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CambiarPasswordRequest {
        @NotBlank(message = "La contraseña actual es obligatoria")
        private String passwordActual;

        @NotBlank(message = "La nueva contraseña es obligatoria")
        private String passwordNueva;
    }
}