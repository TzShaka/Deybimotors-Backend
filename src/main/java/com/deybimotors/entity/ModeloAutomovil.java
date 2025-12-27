package com.deybimotors.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entidad ModeloAutomovil
 * Modelos de automóviles asociados a marcas
 */
@Entity
@Table(name = "modelos_automovil")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ModeloAutomovil {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nombre;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "marca_automovil_id", nullable = false)
    private MarcaAutomovil marcaAutomovil;
}