package com.deybimotors.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entidad Compatibilidad
 * Relación producto-vehículo compatible
 */
@Entity
@Table(name = "compatibilidades")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Compatibilidad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "producto_id", nullable = false)
    private Producto producto;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "marca_automovil_id", nullable = false)
    private MarcaAutomovil marcaAutomovil;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "modelo_automovil_id")
    private ModeloAutomovil modeloAutomovil;

    @Column(length = 20)
    private String anio;

    @Column(length = 50)
    private String motor;
}