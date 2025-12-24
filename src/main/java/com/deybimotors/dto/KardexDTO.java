package com.deybimotors.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO para Kardex - RF-033 a RF-039
 */
public class KardexDTO {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MovimientoResponse {
        private Long id;
        private Long productoId;
        private String productoCodigo;
        private String productoNombre;
        private Long sedeId;
        private String sedeNombre;
        private String tipoMovimiento;
        private String tipoMovimientoDescripcion;
        private Integer cantidad;
        private Integer stockAnterior;
        private Integer stockNuevo;
        private String motivo;
        private String usuarioResponsable;
        private LocalDateTime fechaMovimiento;
        private String referencia;
        private String observaciones;
    }
}