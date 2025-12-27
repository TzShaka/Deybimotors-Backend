package com.deybimotors.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO para Usuario - RF-002, RF-054 a RF-057
 * ✅ CORREGIDO: Agregado @EqualsAndHashCode
 */
public class UsuarioDTO {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UsuarioResponse {
        private Long id;
        private String username;
        private String nombreCompleto;
        private String email;
        private String telefono;
        private String rol;
        private Long sedeId;
        private String sedeNombre;
        private Boolean activo;
        private LocalDateTime fechaCreacion;
        private String observaciones;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UsuarioRequest {
        @NotBlank(message = "El usuario es obligatorio")
        private String username;

        @NotBlank(message = "El nombre completo es obligatorio")
        private String nombreCompleto;

        @Email(message = "El email debe ser válido")
        private String email;

        private String telefono;

        @NotBlank(message = "El rol es obligatorio")
        private String rol; // ADMIN, VENDEDOR, ALMACENERO

        @NotNull(message = "La sede es obligatoria")
        private Long sedeId;

        private String observaciones;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode(callSuper = false) // ✅ CORREGIDO
    public static class CrearUsuarioRequest extends UsuarioRequest {
        @NotBlank(message = "La contraseña es obligatoria")
        private String password;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ResetPasswordRequest {
        @NotBlank(message = "La nueva contraseña es obligatoria")
        private String nuevaPassword;
    }
}