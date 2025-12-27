package com.deybimotors.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Entidad CodigoPrecio
 * Códigos de precio para productos
 */
@Entity
@Table(name = "codigos_precio")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CodigoPrecio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String codigo;

    @Column(length = 100)
    private String descripcion;

    @Column(precision = 10, scale = 2)
    private BigDecimal factor;
}