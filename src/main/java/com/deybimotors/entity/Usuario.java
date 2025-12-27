package com.deybimotors.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Entidad Usuario - ✅ ACTUALIZADA para BD real
 * Campo rol es VARCHAR (ENUM en Java), NO FK
 */
@Entity
@Table(name = "usuarios")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(nullable = false)
    private String password; // Contraseña encriptada

    @Column(nullable = false, length = 100, name = "nombre_completo")
    private String nombreCompleto;

    @Column(length = 100)
    private String email;

    @Column(length = 20)
    private String telefono;

    // ✅ CORRECCIÓN: rol es VARCHAR en BD, NO FK
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Rol rol;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "sede_id", nullable = false)
    private Sede sede;

    @Column(nullable = false)
    private Boolean activo = true;

    @CreationTimestamp
    @Column(nullable = false, updatable = false, name = "fecha_creacion")
    private LocalDateTime fechaCreacion;

    @UpdateTimestamp
    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;

    @Column(length = 500)
    private String observaciones;

    // Enum para roles
    public enum Rol {
        ADMIN,
        VENDEDOR,
        ALMACENERO
    }
}