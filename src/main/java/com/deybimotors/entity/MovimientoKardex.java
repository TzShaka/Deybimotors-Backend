package com.deybimotors.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Entidad MovimientoKardex - RF-033 a RF-039
 * Registro de todos los movimientos de inventario
 */
@Entity
@Table(name = "movimientos_kardex", indexes = {
        @Index(name = "idx_producto", columnList = "producto_id"),
        @Index(name = "idx_fecha", columnList = "fechaMovimiento"),
        @Index(name = "idx_tipo", columnList = "tipoMovimiento")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MovimientoKardex {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "producto_id", nullable = false)
    private Producto producto;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "sede_id", nullable = false)
    private Sede sede;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoMovimiento tipoMovimiento;

    @Column(nullable = false)
    private Integer cantidad;

    @Column(nullable = false)
    private Integer stockAnterior;

    @Column(nullable = false)
    private Integer stockNuevo;

    @Column(length = 500)
    private String motivo;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "usuario_responsable_id", nullable = false)
    private Usuario usuarioResponsable;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime fechaMovimiento;

    @Column(length = 50)
    private String referencia; // Número de compra, venta, etc.

    @Column(length = 1000)
    private String observaciones;

    // Tipos de movimiento
    public enum TipoMovimiento {
        ENTRADA_COMPRA,      // Entrada por compra
        SALIDA_VENTA,        // Salida por venta
        AJUSTE_POSITIVO,     // Ajuste manual positivo
        AJUSTE_NEGATIVO,     // Ajuste manual negativo
        TRASLADO_ENTRADA,    // Entrada por traslado
        TRASLADO_SALIDA,     // Salida por traslado
        DEVOLUCION,          // Devolución de cliente
        MERMA                // Pérdida/daño de producto
    }
}