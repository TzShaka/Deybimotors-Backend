package com.deybimotors.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entidad ImagenProducto - RF-010
 * Almacena múltiples imágenes por producto
 * TABLA: imagenes_producto (según BD)
 */
@Entity
@Table(name = "imagenes_producto")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ImagenProducto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "producto_id", nullable = false)
    private Producto producto;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String url;

    @Column
    private Integer orden = 1;

    @Column(nullable = false, name = "es_principal")
    private Boolean esPrincipal = false;

    // Métodos de compatibilidad con código existente
    public String getRutaArchivo() {
        return this.url;
    }

    public void setRutaArchivo(String ruta) {
        this.url = ruta;
    }

    public Boolean getPrincipal() {
        return this.esPrincipal;
    }

    public void setPrincipal(Boolean principal) {
        this.esPrincipal = principal;
    }

    public String getNombreArchivo() {
        // Extraer nombre del archivo de la URL
        if (this.url != null && this.url.contains("/")) {
            return this.url.substring(this.url.lastIndexOf("/") + 1);
        }
        return this.url;
    }

    public void setNombreArchivo(String nombre) {
        // No hacer nada, el nombre está incluido en la URL
    }
}