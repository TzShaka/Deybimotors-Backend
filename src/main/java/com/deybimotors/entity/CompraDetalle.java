package com.deybimotors.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Entidad CompraDetalle - ✅ ACTUALIZADA para BD real
 * Campo observaciones agregado por migración
 */
@Entity
@Table(name = "detalle_compras")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CompraDetalle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "compra_id", nullable = false)
    private Compra compra;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "producto_id", nullable = false)
    private Producto producto;

    @Column(nullable = false)
    private Integer cantidad;

    @Column(precision = 10, scale = 2, name = "precio_unitario")
    private BigDecimal precioUnitario;

    // ✅ NUEVO: Campo agregado por migración
    @Column(length = 500)
    private String observaciones;

    // Método calculado para subtotal (NO existe en BD)
    @Transient
    public BigDecimal getSubtotal() {
        if (precioUnitario != null && cantidad != null) {
            return precioUnitario.multiply(new BigDecimal(cantidad));
        }
        return BigDecimal.ZERO;
    }
}