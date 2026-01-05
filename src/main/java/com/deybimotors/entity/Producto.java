package com.deybimotors.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidad Producto - ✅ ACTUALIZADO CON MÚLTIPLES IMÁGENES
 * - SIN codigo_referencia
 * - CON relaciones correctas a producto_oem y compatibilidades
 * - ✅ CON múltiples imágenes (producto_imagenes)
 */
@Entity
@Table(name = "productos")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "sede_id", nullable = false)
    private Sede sede;

    @Column(length = 50, name = "codigo_marca")
    private String codigoMarca;

    @Column(nullable = false, unique = true, length = 50, name = "codigo_interno")
    private String codigoInterno;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "categoria_id")
    private Categoria categoria;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "subcategoria_id")
    private Subcategoria subcategoria;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "marca_producto_id")
    private Marca marcaProducto;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "origen_id")
    private Origen origen;

    @Column(length = 50)
    private String medida;

    @Column(length = 50)
    private String diametro;

    @Column(length = 50)
    private String tipo;

    @Column(length = 50, name = "medida_2")
    private String medida2;

    @Column(nullable = false)
    private Integer stock = 0;

    @Column(length = 500, name = "foto_url")
    private String fotoUrl;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "codigo_precio_id")
    private CodigoPrecio codigoPrecio;

    @Column(precision = 10, scale = 2, name = "precio_costo")
    private BigDecimal precioCosto;

    @Column(precision = 10, scale = 2, name = "precio_venta")
    private BigDecimal precioVenta;

    @Column(nullable = false, name = "publico_catalogo")
    @Convert(converter = org.hibernate.type.NumericBooleanConverter.class)
    private Boolean publicoCatalogo = false;

    @Column(nullable = false)
    @Convert(converter = org.hibernate.type.NumericBooleanConverter.class)
    private Boolean estado = true;

    @CreationTimestamp
    @Column(nullable = false, updatable = false, name = "fecha_creacion")
    private LocalDateTime fechaCreacion;

    // ✅ RELACIONES EXISTENTES
    @OneToMany(mappedBy = "producto", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<Compatibilidad> compatibilidades = new ArrayList<>();

    @OneToMany(mappedBy = "producto", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<ProductoOem> codigosOem = new ArrayList<>();

    // ✅ NUEVA RELACIÓN: MÚLTIPLES IMÁGENES
    @OneToMany(mappedBy = "producto", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @OrderBy("orden ASC")
    private List<ProductoImagen> imagenes = new ArrayList<>();

    // ========================================
    // MÉTODOS DE COMPATIBILIDAD CON @JsonIgnore
    // ========================================

    @JsonIgnore
    public String getCodigo() {
        return this.codigoInterno;
    }

    @JsonIgnore
    public void setCodigo(String codigo) {
        this.codigoInterno = codigo;
    }

    @JsonIgnore
    public String getNombre() {
        return this.descripcion;
    }

    @JsonIgnore
    public void setNombre(String nombre) {
        this.descripcion = nombre;
    }

    @JsonIgnore
    public Boolean getActivo() {
        return this.estado;
    }

    @JsonIgnore
    public void setActivo(Boolean activo) {
        this.estado = activo;
    }

    @JsonIgnore
    public Marca getMarca() {
        return this.marcaProducto;
    }

    @JsonIgnore
    public void setMarca(Marca marca) {
        this.marcaProducto = marca;
    }
}