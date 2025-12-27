package com.deybimotors.repository;

import com.deybimotors.entity.Compra;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio Compra - ✅ ACTUALIZADO
 * Query corregida para campo numero_compra
 */
@Repository
public interface CompraRepository extends JpaRepository<Compra, Long> {

    Optional<Compra> findByNumeroCompra(String numeroCompra);

    List<Compra> findByProveedorId(Long proveedorId);

    List<Compra> findBySedeId(Long sedeId);

    List<Compra> findByEstado(Compra.EstadoCompra estado);

    @Query("SELECT c FROM Compra c WHERE c.fechaRegistro BETWEEN :fechaInicio AND :fechaFin ORDER BY c.fechaRegistro DESC")
    List<Compra> findByFechaRegistroBetween(
            @Param("fechaInicio") LocalDateTime fechaInicio,
            @Param("fechaFin") LocalDateTime fechaFin
    );

    List<Compra> findTop10ByOrderByFechaRegistroDesc();

    // ✅ CORRECCIÓN: Query corregida para formato CMP-2024-0001
    @Query("SELECT COALESCE(MAX(CAST(SUBSTRING(c.numeroCompra, 10) AS integer)), 0) " +
            "FROM Compra c WHERE c.numeroCompra LIKE CONCAT('CMP-', :year, '-%')")
    Integer findUltimoNumeroCompraDelAnio(@Param("year") String year);
}