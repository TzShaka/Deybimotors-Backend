package com.deybimotors.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Entidad ProductoFoto - RF-010
 * Almacena múltiples fotos por producto
 */
@Entity
@Table(name = "producto_fotos")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductoFoto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "producto_id", nullable = false)
    private Producto producto;

    @Column(nullable = false, length = 500)
    private String rutaArchivo;

    @Column(length = 200)
    private String nombreArchivo;

    @Column(nullable = false)
    private Boolean principal = false;

    @Column(nullable = false)
    private Integer orden = 0;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime fechaSubida;
}