package com.deybimotors.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entidad ProductoOem
 * Relación producto-código OEM (un producto puede tener varios códigos OEM)
 */
@Entity
@Table(name = "producto_oem")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductoOem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "producto_id", nullable = false)
    private Producto producto;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "oem_id", nullable = false)
    private CodigoOem codigoOem;
}