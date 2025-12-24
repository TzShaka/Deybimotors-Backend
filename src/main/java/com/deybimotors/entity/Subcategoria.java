package com.deybimotors.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entidad Subcategoría - RF-046 a RF-049
 * Subcategorías asociadas a categorías principales
 */
@Entity
@Table(name = "subcategorias")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Subcategoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nombre;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "categoria_id", nullable = false)
    private Categoria categoria;

    @Column(length = 500)
    private String descripcion;

    @Column(nullable = false)
    private Boolean activo = true;
}