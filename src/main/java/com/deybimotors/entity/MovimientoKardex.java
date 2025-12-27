package com.deybimotors.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Entidad MovimientoKardex - RF-033 a RF-039
 * TABLA: kardex (según BD)
 */
@Entity
@Table(name = "kardex", indexes = {
        @Index(name = "idx_producto", columnList = "producto_id"),
        @Index(name = "idx_fecha", columnList = "fecha_movimiento"),
        @Index(name = "idx_tipo", columnList = "tipo_movimiento")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MovimientoKardex {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "producto_id")
    private Producto producto;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "sede_id")
    private Sede sede;

    @Column(length = 20, name = "tipo_movimiento")
    private String tipoMovimiento;

    @Column(length = 50, name = "referencia_tabla")
    private String referenciaTabla;

    @Column(name = "referencia_id")
    private Long referenciaId;

    @Column
    private Integer cantidad;

    @Column(name = "stock_anterior")
    private Integer stockAnterior;

    @Column(name = "stock_actual")
    private Integer stockActual;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "usuario_id")
    private Usuario usuarioResponsable;

    @CreationTimestamp
    @Column(nullable = false, updatable = false, name = "fecha_movimiento")
    private LocalDateTime fechaMovimiento;

    // Métodos de compatibilidad con código existente
    public Integer getStockNuevo() {
        return this.stockActual;
    }

    public void setStockNuevo(Integer stock) {
        this.stockActual = stock;
    }

    public String getMotivo() {
        return this.tipoMovimiento;
    }

    public void setMotivo(String motivo) {
        this.tipoMovimiento = motivo;
    }

    public String getReferencia() {
        return this.referenciaTabla;
    }

    public void setReferencia(String ref) {
        this.referenciaTabla = ref;
    }

    public String getObservaciones() {
        // No existe en BD, retornar null
        return null;
    }

    public void setObservaciones(String obs) {
        // No hacer nada, no existe en BD
    }

    // Enum para tipos de movimiento
    public enum TipoMovimiento {
        ENTRADA_COMPRA,
        SALIDA_VENTA,
        AJUSTE_POSITIVO,
        AJUSTE_NEGATIVO,
        TRASLADO_ENTRADA,
        TRASLADO_SALIDA,
        DEVOLUCION,
        MERMA
    }
}