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
 */
@Repository
public interface MovimientoKardexRepository extends JpaRepository<MovimientoKardex, Long> {

    // Kardex general - últimos movimientos
    List<MovimientoKardex> findTop50ByOrderByFechaMovimientoDesc();

    // Kardex por producto - RF-034
    List<MovimientoKardex> findByProductoIdOrderByFechaMovimientoDesc(Long productoId);

    // Kardex por sede
    List<MovimientoKardex> findBySedeIdOrderByFechaMovimientoDesc(Long sedeId);

    // Kardex por tipo de movimiento - RF-036
    List<MovimientoKardex> findByTipoMovimientoOrderByFechaMovimientoDesc(
            MovimientoKardex.TipoMovimiento tipoMovimiento
    );

    // Kardex por rango de fechas - RF-035
    @Query("SELECT m FROM MovimientoKardex m WHERE m.fechaMovimiento BETWEEN :fechaInicio AND :fechaFin ORDER BY m.fechaMovimiento DESC")
    List<MovimientoKardex> findByFechaMovimientoBetween(
            @Param("fechaInicio") LocalDateTime fechaInicio,
            @Param("fechaFin") LocalDateTime fechaFin
    );

    // Kardex filtrado por producto y fechas
    @Query("SELECT m FROM MovimientoKardex m WHERE m.producto.id = :productoId AND m.fechaMovimiento BETWEEN :fechaInicio AND :fechaFin ORDER BY m.fechaMovimiento DESC")
    List<MovimientoKardex> findByProductoAndFechas(
            @Param("productoId") Long productoId,
            @Param("fechaInicio") LocalDateTime fechaInicio,
            @Param("fechaFin") LocalDateTime fechaFin
    );

    // Últimos movimientos por usuario responsable - RF-037
    List<MovimientoKardex> findByUsuarioResponsableIdOrderByFechaMovimientoDesc(Long usuarioId);
}