package com.deybimotors.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entidad Origen
 * País de origen del producto
 */
@Entity
@Table(name = "origenes")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Origen {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String pais;
}