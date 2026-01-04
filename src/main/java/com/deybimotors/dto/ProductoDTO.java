package com.deybimotors.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO para Producto - ✅ CORREGIDO
 * - SIN codigo_referencia
 * - CON lista de codigosOem (desde producto_oem)
 * - CON lista de compatibilidades
 */
public class ProductoDTO {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProductoResponse {
        private Long id;
        private String codigo;
        private String codigoMarca;
        private String descripcion;

        // Categorización
        private Long categoriaId;
        private String categoriaNombre;
        private Long subcategoriaId;
        private String subcategoriaNombre;
        private Long marcaId;
        private String marcaNombre;

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

        // Foto
        private String fotoUrl;

        // Stock
        private Integer stockTotal;

        // ✅ CÓDIGOS OEM - Lista completa
        private List<String> codigosOem;

        // ✅ COMPATIBILIDADES - Lista completa
        private List<CompatibilidadInfo> compatibilidades;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CompatibilidadInfo {
        private Long id;
        private String marcaAutomovil;
        private String modeloAutomovil;
        private String anio;
        private String motor;
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

        // ✅ CÓDIGOS OEM - Lista de IDs para asignar
        private List<Long> codigosOemIds;

        // ✅ COMPATIBILIDADES - Lista para crear
        private List<CompatibilidadRequest> compatibilidades;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CompatibilidadRequest {
        private Long marcaAutomovilId;
        private Long modeloAutomovilId;
        private String anio;
        private String motor;
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

        // Especificaciones
        private String origen;
        private String medida;
        private String diametro;
        private String tipo;

        // Precio y disponibilidad
        private BigDecimal precioVenta;
        private String disponibilidad;

        // Foto
        private String fotoUrl;

        // ✅ CÓDIGOS OEM
        private List<String> codigosOem;

        // ✅ COMPATIBILIDADES
        private List<CompatibilidadInfo> compatibilidades;
    }
}