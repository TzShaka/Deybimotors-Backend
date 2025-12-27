package com.deybimotors.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidad Salida
 * Cabecera de salidas de productos
 */
@Entity
@Table(name = "salidas")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Salida {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "sede_id")
    private Sede sede;

    @Column(length = 150)
    private String motivo;

    @Column(columnDefinition = "TEXT")
    private String observacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @CreationTimestamp
    @Column(nullable = false, updatable = false, name = "fecha_creacion")
    private LocalDateTime fechaCreacion;

    @OneToMany(mappedBy = "salida", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DetalleSalida> detalles = new ArrayList<>();
}