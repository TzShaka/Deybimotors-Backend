package com.deybimotors.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entidad CodigoOem
 * Códigos OEM de los productos
 */
@Entity
@Table(name = "codigos_oem")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CodigoOem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100, name = "codigo_oem")
    private String codigoOem;
}