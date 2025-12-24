package com.deybimotors.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTOs genéricos para entidades simples
 * Sede, Categoría, Subcategoría, Marca, Modelo, Proveedor
 */
public class GenericDTO {

    // ===== SEDE =====
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SedeResponse {
        private Long id;
        private String nombre;
        private String direccion;
        private String ciudad;
        private String telefono;
        private Boolean activo;
        private String observaciones;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SedeRequest {
        @NotBlank(message = "El nombre es obligatorio")
        private String nombre;
        private String direccion;
        private String ciudad;
        private String telefono;
        private String observaciones;
    }

    // ===== CATEGORÍA =====
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CategoriaResponse {
        private Long id;
        private String nombre;
        private String descripcion;
        private Boolean activo;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CategoriaRequest {
        @NotBlank(message = "El nombre es obligatorio")
        private String nombre;
        private String descripcion;
    }

    // ===== SUBCATEGORÍA =====
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SubcategoriaResponse {
        private Long id;
        private String nombre;
        private Long categoriaId;
        private String categoriaNombre;
        private String descripcion;
        private Boolean activo;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SubcategoriaRequest {
        @NotBlank(message = "El nombre es obligatorio")
        private String nombre;
        @NotNull(message = "La categoría es obligatoria") // ✅ CORREGIDO
        private Long categoriaId;
        private String descripcion;
    }

    // ===== MARCA =====
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MarcaResponse {
        private Long id;
        private String nombre;
        private String descripcion;
        private Boolean activo;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MarcaRequest {
        @NotBlank(message = "El nombre es obligatorio")
        private String nombre;
        private String descripcion;
    }

    // ===== MODELO =====
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ModeloResponse {
        private Long id;
        private String nombre;
        private Long marcaId;
        private String marcaNombre;
        private String descripcion;
        private Boolean activo;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ModeloRequest {
        @NotBlank(message = "El nombre es obligatorio")
        private String nombre;
        @NotNull(message = "La marca es obligatoria") // ✅ CORREGIDO
        private Long marcaId;
        private String descripcion;
    }

    // ===== PROVEEDOR =====
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProveedorResponse {
        private Long id;
        private String nombreEmpresa;
        private String ruc;
        private String contacto;
        private String telefono;
        private String email;
        private String direccion;
        private Boolean activo;
        private String observaciones;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProveedorRequest {
        @NotBlank(message = "El nombre de la empresa es obligatorio")
        private String nombreEmpresa;
        private String ruc;
        private String contacto;
        private String telefono;
        private String email;
        private String direccion;
        private String observaciones;
    }
}