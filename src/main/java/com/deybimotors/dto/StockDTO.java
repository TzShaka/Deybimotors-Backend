package com.deybimotors.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO para Stock - ✅ ACTUALIZADO
 */
public class StockDTO {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StockResponse {
        private Long id;
        private Long productoId;
        private String productoCodigo;
        private String productoNombre;
        private Long sedeId;
        private String sedeNombre;
        private Integer cantidad;
        private LocalDateTime fechaActualizacion;
        private String estado; // NORMAL, BAJO, AGOTADO
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AjusteStockRequest {
        @NotNull(message = "El producto es obligatorio")
        private Long productoId;

        @NotNull(message = "La sede es obligatoria")
        private Long sedeId;

        @NotNull(message = "La cantidad es obligatoria")
        private Integer cantidadNueva;

        @NotBlank(message = "El motivo es obligatorio")
        private String motivo;

        private String observaciones;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SalidaStockRequest {
        @NotNull(message = "El producto es obligatorio")
        private Long productoId;

        @NotNull(message = "La cantidad es obligatoria")
        @Min(value = 1, message = "La cantidad debe ser mayor a 0")
        private Integer cantidad;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ConfirmarSalidaRequest {
        @NotNull(message = "La sede es obligatoria")
        private Long sedeId;

        @NotBlank(message = "El motivo es obligatorio")
        private String motivo; // VENTA, TRASLADO, OTRO

        private String observaciones;

        @NotNull(message = "Los productos son obligatorios")
        private List<SalidaStockRequest> productos;
    }
}