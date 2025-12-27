package com.deybimotors.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entidad MarcaAutomovil
 * Marcas de automóviles (TOYOTA, NISSAN, etc.)
 */
@Entity
@Table(name = "marcas_automovil")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MarcaAutomovil {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String nombre;
}