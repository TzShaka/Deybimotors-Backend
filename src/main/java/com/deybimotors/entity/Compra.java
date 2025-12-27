package com.deybimotors.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidad Compra - ✅ ACTUALIZADA para BD real
 * Campos corregidos según estructura real de BD
 */
@Entity
@Table(name = "compras")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Compra {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ✅ CORRECCIÓN: numero_compra en BD (antes numero_factura)
    @Column(nullable = false, unique = true, length = 50, name = "numero_compra")
    private String numeroCompra;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "proveedor_id", nullable = false)
    private Proveedor proveedor;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "sede_id", nullable = false)
    private Sede sede;

    // ✅ CORRECCIÓN: ruta_factura en BD (antes archivo_factura_url)
    @Column(length = 500, name = "ruta_factura")
    private String rutaFactura;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoCompra estado = EstadoCompra.PENDIENTE;

    // ✅ NUEVO: Campo agregado por migración
    @Column(nullable = false, precision = 12, scale = 2, name = "monto_total")
    private BigDecimal montoTotal = BigDecimal.ZERO;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuarioRegistro;

    // ✅ CORRECCIÓN: fecha_registro en BD (antes fecha_creacion)
    @CreationTimestamp
    @Column(nullable = false, updatable = false, name = "fecha_creacion")
    private LocalDateTime fechaRegistro;

    @UpdateTimestamp
    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;

    @Column(length = 1000)
    private String observaciones;

    // Detalle de la compra
    @OneToMany(mappedBy = "compra", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CompraDetalle> detalles = new ArrayList<>();

    // Estados posibles de una compra
    public enum EstadoCompra {
        PENDIENTE,      // Registrada pero no completada
        COMPLETADO,     // Completada y stock actualizado
        CANCELADO       // Cancelada
    }
}