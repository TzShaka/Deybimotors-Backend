package com.deybimotors.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Entidad Stock - Control de inventario por sede
 * Registra la cantidad disponible de cada producto en cada sede
 */
@Entity
@Table(name = "stock", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"producto_id", "sede_id"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Stock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "producto_id", nullable = false)
    private Producto producto;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "sede_id", nullable = false)
    private Sede sede;

    @Column(nullable = false)
    private Integer cantidad = 0;

    @UpdateTimestamp
    private LocalDateTime fechaActualizacion;
}