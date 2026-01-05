package com.deybimotors.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Entidad ProductoImagen
 * Permite almacenar múltiples imágenes por producto
 */
@Entity
@Table(name = "producto_imagenes")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductoImagen {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "producto_id", nullable = false)
    @JsonIgnore
    private Producto producto;

    @Column(nullable = false, length = 500)
    private String url;

    @Column(name = "orden")
    private Integer orden = 0;

    @Column(name = "es_principal")
    @Convert(converter = org.hibernate.type.NumericBooleanConverter.class)
    private Boolean esPrincipal = false;

    @CreationTimestamp
    @Column(nullable = false, updatable = false, name = "fecha_creacion")
    private LocalDateTime fechaCreacion;
}