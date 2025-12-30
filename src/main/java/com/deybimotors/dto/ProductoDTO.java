package com.deybimotors.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO para Producto - ✅ CORREGIDO
 * - UN SOLO campo fotoUrl (String)
 * - SIN stock_minimo
 * - CON publicoCatalogo
 */
public class ProductoDTO {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProductoResponse {
        private Long id;
        private String codigo;
        private String codigoMarca;
        private String codigoReferencia;
        private String codigoOem;
        private String descripcion;

        // Categorización
        private Long categoriaId;
        private String categoriaNombre;
        private Long subcategoriaId;
        private String subcategoriaNombre;
        private Long marcaId;
        private String marcaNombre;

        // Datos del vehículo
        private String marcaAutomovil;
        private String modeloAutomovil;
        private String anio;
        private String motor;

        // Especificaciones técnicas
        private String origen;
        private String medida;
        private String diametro;
        private String tipo;
        private String medida2;

        // Precios
        private BigDecimal precioVenta;
        private BigDecimal precioCosto;
        private String codigoPrecio;

        // Control
        private Boolean activo;
        private Boolean publicoCatalogo;
        private LocalDateTime fechaCreacion;

        // ✅ FOTO - UN SOLO CAMPO
        private String fotoUrl;

        // Stock
        private Integer stockTotal;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProductoConStockResponse extends ProductoResponse {
        private Integer stockSede;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProductoRequest {
        @NotBlank(message = "El código es obligatorio")
        private String codigo;

        private String codigoMarca;
        private String codigoReferencia;
        private String codigoOem;

        @NotBlank(message = "El nombre es obligatorio")
        private String nombre;
        private String descripcion;

        @NotNull(message = "La categoría es obligatoria")
        private Long categoriaId;
        private Long subcategoriaId;

        @NotNull(message = "La marca es obligatoria")
        private Long marcaId;

        @NotNull(message = "La sede es obligatoria")
        private Long sedeId;

        // Datos del vehículo
        private String marcaAutomovil;
        private String modeloAutomovil;
        private String anio;
        private String motor;

        // Especificaciones
        private String origen;
        private String medida;
        private String diametro;
        private String tipo;
        private String medida2;

        @NotNull(message = "El precio de venta es obligatorio")
        @DecimalMin(value = "0.0", inclusive = false)
        private BigDecimal precioVenta;

        @DecimalMin(value = "0.0")
        private BigDecimal precioCosto;

        private String codigoPrecio;
        private Boolean publicoCatalogo = false;
        private String observaciones;

        // ✅ SIN fotoUrl - Las fotos se suben por separado con MultipartFile
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProductoCatalogoPublicoResponse {
        private Long id;
        private String codigo;
        private String nombre;
        private String descripcion;

        // Categorización
        private String categoriaNombre;
        private String subcategoriaNombre;
        private String marcaNombre;

        // Datos del vehículo
        private String marcaAutomovil;
        private String modeloAutomovil;
        private String motor;

        // Especificaciones
        private String origen;
        private String medida;
        private String diametro;
        private String tipo;

        // Precio y disponibilidad
        private BigDecimal precioVenta;
        private String disponibilidad; // DISPONIBLE, ULTIMAS_UNIDADES, AGOTADO

        // ✅ FOTO - UN SOLO CAMPO
        private String fotoUrl;
    }
}