package com.deybimotors.repository;

import com.deybimotors.entity.MovimientoKardex;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repositorio MovimientoKardex - RF-033 a RF-039
 * ACTUALIZADO: tipo_movimiento es VARCHAR
 */
@Repository
public interface MovimientoKardexRepository extends JpaRepository<MovimientoKardex, Long> {

    List<MovimientoKardex> findTop50ByOrderByFechaMovimientoDesc();

    List<MovimientoKardex> findByProductoIdOrderByFechaMovimientoDesc(Long productoId);

    List<MovimientoKardex> findBySedeIdOrderByFechaMovimientoDesc(Long sedeId);

    @Query("SELECT m FROM MovimientoKardex m WHERE m.fechaMovimiento BETWEEN :fechaInicio AND :fechaFin ORDER BY m.fechaMovimiento DESC")
    List<MovimientoKardex> findByFechaMovimientoBetween(
            @Param("fechaInicio") LocalDateTime fechaInicio,
            @Param("fechaFin") LocalDateTime fechaFin
    );

    @Query("SELECT m FROM MovimientoKardex m WHERE m.producto.id = :productoId AND m.fechaMovimiento BETWEEN :fechaInicio AND :fechaFin ORDER BY m.fechaMovimiento DESC")
    List<MovimientoKardex> findByProductoAndFechas(
            @Param("productoId") Long productoId,
            @Param("fechaInicio") LocalDateTime fechaInicio,
            @Param("fechaFin") LocalDateTime fechaFin
    );
}