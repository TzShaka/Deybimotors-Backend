package com.deybimotors.entity;

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
 * Entidad Producto - ✅ CORREGIDO
 * - UN SOLO campo fotoUrl
 * - SIN stock_minimo
 * - CON publicoCatalogo
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

    // RELACIÓN CON SEDE
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "sede_id", nullable = false)
    private Sede sede;

    // CÓDIGOS DEL PRODUCTO
    @Column(length = 50, name = "codigo_marca")
    private String codigoMarca;

    @Column(length = 50, name = "codigo_referencia")
    private String codigoReferencia;

    @Column(nullable = false, unique = true, length = 50, name = "codigo_interno")
    private String codigoInterno;

    // DESCRIPCIÓN
    @Column(columnDefinition = "TEXT")
    private String descripcion;

    // CATEGORIZACIÓN
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "categoria_id")
    private Categoria categoria;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "subcategoria_id")
    private Subcategoria subcategoria;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "marca_producto_id")
    private Marca marcaProducto;

    // ORIGEN
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "origen_id")
    private Origen origen;

    // ESPECIFICACIONES TÉCNICAS
    @Column(length = 50)
    private String medida;

    @Column(length = 50)
    private String diametro;

    @Column(length = 50)
    private String tipo;

    @Column(length = 50, name = "medida_2")
    private String medida2;

    // STOCK (EN LA TABLA PRODUCTOS)
    @Column(nullable = false)
    private Integer stock = 0;

    // ✅ FOTO - UN SOLO CAMPO
    @Column(length = 500, name = "foto_url")
    private String fotoUrl;

    // CÓDIGO DE PRECIO
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "codigo_precio_id")
    private CodigoPrecio codigoPrecio;

    // PRECIOS
    @Column(precision = 10, scale = 2, name = "precio_costo")
    private BigDecimal precioCosto;

    @Column(precision = 10, scale = 2, name = "precio_venta")
    private BigDecimal precioVenta;

    // ✅ NUEVO: Campo para catálogo público
    @Column(nullable = false, name = "publico_catalogo")
    private Boolean publicoCatalogo = false;

    // ESTADO
    @Column(nullable = false)
    private Boolean estado = true;

    // AUDITORÍA
    @CreationTimestamp
    @Column(nullable = false, updatable = false, name = "fecha_creacion")
    private LocalDateTime fechaCreacion;

    // RELACIONES
    @OneToMany(mappedBy = "producto", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Compatibilidad> compatibilidades = new ArrayList<>();

    @OneToMany(mappedBy = "producto", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductoOem> codigosOem = new ArrayList<>();

    // MÉTODOS DE COMPATIBILIDAD CON CÓDIGO EXISTENTE
    public String getCodigo() {
        return this.codigoInterno;
    }

    public void setCodigo(String codigo) {
        this.codigoInterno = codigo;
    }

    public Marca getMarca() {
        return this.marcaProducto;
    }

    public void setMarca(Marca marca) {
        this.marcaProducto = marca;
    }

    public String getNombre() {
        return this.descripcion;
    }

    public void setNombre(String nombre) {
        this.descripcion = nombre;
    }

    public Boolean getActivo() {
        return this.estado;
    }

    public void setActivo(Boolean activo) {
        this.estado = activo;
    }
}