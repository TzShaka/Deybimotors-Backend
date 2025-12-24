package com.deybimotors.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entidad Modelo - RF-050 a RF-053
 * Modelos asociados a marcas
 */
@Entity
@Table(name = "modelos")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Modelo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nombre;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "marca_id", nullable = false)
    private Marca marca;

    @Column(length = 500)
    private String descripcion;

    @Column(nullable = false)
    private Boolean activo = true;
}