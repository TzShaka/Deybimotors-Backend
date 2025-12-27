package com.deybimotors.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entidad DetalleSalida
 * Detalle de productos en una salida
 */
@Entity
@Table(name = "detalle_salidas")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DetalleSalida {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "salida_id")
    private Salida salida;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "producto_id")
    private Producto producto;

    @Column
    private Integer cantidad;
}