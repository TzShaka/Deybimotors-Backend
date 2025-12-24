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
 * Entidad Compra - RF-025 a RF-032
 * Cabecera de compra de productos
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

    @Column(nullable = false, unique = true, length = 50)
    private String numeroCompra; // Generado automáticamente

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "proveedor_id", nullable = false)
    private Proveedor proveedor;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "sede_id", nullable = false)
    private Sede sede;

    @Column(length = 500)
    private String rutaFactura; // Documento de factura subido

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoCompra estado = EstadoCompra.PENDIENTE;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal montoTotal = BigDecimal.ZERO;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_registro_id", nullable = false)
    private Usuario usuarioRegistro;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime fechaRegistro;

    @UpdateTimestamp
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