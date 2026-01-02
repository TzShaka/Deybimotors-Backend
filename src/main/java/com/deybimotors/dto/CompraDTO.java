package com.deybimotors.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO para Compra - RF-025 a RF-032
 * ✅ ACTUALIZADO: Incluye archivoUrl y archivoNombre
 */
public class CompraDTO {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CompraResponse {
        private Long id;
        private String numeroCompra;
        private Long proveedorId;
        private String proveedorNombre;
        private Long sedeId;
        private String sedeNombre;
        private String estado;
        private BigDecimal montoTotal;
        private String rutaFactura;

        // ✅ NUEVO: URL completa del archivo (para frontend)
        private String archivoUrl;
        private String archivoNombre;

        private LocalDateTime fechaRegistro;
        private String usuarioRegistro;
        private String observaciones;
        private List<CompraDetalleResponse> detalles;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CompraDetalleResponse {
        private Long id;
        private Long productoId;
        private String productoCodigo;
        private String productoNombre;
        private Integer cantidad;
        private BigDecimal precioUnitario;
        private BigDecimal subtotal;
        private String observaciones;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CrearCompraRequest {
        @NotNull(message = "El proveedor es obligatorio")
        private Long proveedorId;

        @NotNull(message = "La sede es obligatoria")
        private Long sedeId;

        private String observaciones;

        @NotNull(message = "Los detalles son obligatorios")
        private List<CompraDetalleRequest> detalles;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CompraDetalleRequest {
        @NotNull(message = "El producto es obligatorio")
        private Long productoId;

        @NotNull(message = "La cantidad es obligatoria")
        @Min(value = 1, message = "La cantidad debe ser mayor a 0")
        private Integer cantidad;

        @NotNull(message = "El precio unitario es obligatorio")
        @DecimalMin(value = "0.0", inclusive = false, message = "El precio debe ser mayor a 0")
        private BigDecimal precioUnitario;

        private String observaciones;
    }

    /**
     * ✅ NUEVO: DTO para actualizar compra
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ActualizarCompraRequest {
        @NotNull(message = "El proveedor es obligatorio")
        private Long proveedorId;

        private String observaciones;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ActualizarEstadoCompraRequest {
        @NotNull(message = "El estado es obligatorio")
        private String estado; // PENDIENTE, COMPLETADO, CANCELADO
    }
}