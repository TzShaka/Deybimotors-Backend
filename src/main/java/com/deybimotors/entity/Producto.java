package com.deybimotors.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidad Producto - RF-004 a RF-017
 * CORREGIDA: Incluye todos los campos necesarios para repuestos automotrices
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

    // ===== CÓDIGOS DEL PRODUCTO =====
    @Column(nullable = false, unique = true, length = 50)
    private String codigo; // CODIGO INTERNO (ej: DP359191)

    @Column(length = 50)
    private String codigoMarca; // CODIGO MARCA (puede estar vacío)

    @Column(length = 50)
    private String codigoReferencia; // CODIGO REFERENCIA (puede estar vacío)

    @Column(length = 50)
    private String codigoOem; // CODIGO OEM (ej: 13011-11064)

    // ===== INFORMACIÓN BÁSICA =====
    @Column(nullable = false, length = 300)
    private String nombre; // DESCRIPCION completa

    @Column(length = 2000)
    private String descripcion; // Descripción adicional si se necesita

    // ===== CATEGORIZACIÓN =====
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "categoria_id", nullable = false)
    private Categoria categoria; // MOTOR, FRENOS, etc.

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "subcategoria_id")
    private Subcategoria subcategoria; // ANILLOS DE MOTOR, etc.

    // ===== MARCA DEL PRODUCTO (Fabricante del repuesto) =====
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "marca_id", nullable = false)
    private Marca marca; // TP (Toyota Parts), etc.

    // ===== DATOS DEL VEHÍCULO AL QUE APLICA =====
    @Column(length = 50)
    private String marcaAutomovil; // TOYOTA, NISSAN, etc.

    @Column(length = 100)
    private String modeloAutomovil; // COROLLA-CORONA, CANTER, etc.

    @Column(length = 20)
    private String anio; // Año del vehículo

    @Column(length = 50)
    private String motor; // 2E, 4E-5E, etc.

    // ===== ESPECIFICACIONES TÉCNICAS =====
    @Column(length = 50)
    private String origen; // JAPAN, CHINA, etc.

    @Column(length = 50)
    private String medida; // STD, 0.50, 0.75, etc.

    @Column(length = 50)
    private String diametro; // 73mm, 74mm, etc.

    @Column(length = 50)
    private String tipo; // Tipo de pieza

    @Column(length = 100)
    private String medida2; // 1.5*1.5*3, etc.

    // ===== PRECIOS =====
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal precioVenta; // PV

    @Column(precision = 10, scale = 2)
    private BigDecimal precioCosto; // PC

    @Column(length = 10)
    private String codigoPrecio; // DT, ES, DP, etc.

    // ===== CONTROL DE INVENTARIO =====
    @Column(nullable = false)
    private Integer stockMinimo = 0;

    @Column(nullable = false)
    private Boolean activo = true;

    @Column(nullable = false)
    private Boolean publicoCatalogo = false; // RF-058

    // ===== AUDITORÍA =====
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @UpdateTimestamp
    private LocalDateTime fechaActualizacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_creacion_id")
    private Usuario usuarioCreacion;

    @Column(length = 1000)
    private String observaciones;

    // ===== RELACIÓN CON FOTOS =====
    @OneToMany(mappedBy = "producto", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductoFoto> fotos = new ArrayList<>();
}