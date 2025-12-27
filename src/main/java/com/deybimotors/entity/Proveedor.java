package com.deybimotors.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Entidad Proveedor - ✅ ACTUALIZADA para BD real
 * Campo razon_social → nombre_empresa
 */
@Entity
@Table(name = "proveedores")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Proveedor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ✅ CORRECCIÓN: En BD es nombre_empresa, no razon_social
    @Column(nullable = false, length = 200, name = "nombre_empresa")
    private String nombreEmpresa;

    @Column(unique = true, length = 20)
    private String ruc;

    @Column(length = 100)
    private String contacto;

    @Column(length = 20)
    private String telefono;

    @Column(length = 100)
    private String email;

    @Column(length = 200)
    private String direccion;

    @Column(nullable = false)
    private Boolean activo = true;

    @CreationTimestamp
    @Column(nullable = false, updatable = false, name = "fecha_creacion")
    private LocalDateTime fechaCreacion;

    @Column(length = 500)
    private String observaciones;
}