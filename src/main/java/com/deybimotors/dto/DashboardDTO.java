package com.deybimotors.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO para Dashboard - RF-003
 */
public class DashboardDTO {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DashboardResponse {
        private Long totalProductos;
        private Long productosSinStock;
        private Long productosStockMinimo;
        private Long totalCategorias;
        private List<UltimaActualizacion> ultimasActualizaciones;
        private List<Movimiento> movimientosRecientes; // ✅ CORREGIDO
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UltimaActualizacion {
        private String tipo; // PRODUCTO, COMPRA, AJUSTE
        private String descripcion;
        private String fecha;
        private String usuario;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Movimiento {
        private String tipo;
        private String productoNombre;
        private Integer cantidad;
        private String sede;
        private String fecha;
    }
}