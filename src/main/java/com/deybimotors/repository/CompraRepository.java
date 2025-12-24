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
 * Repositorio Compra - RF-025 a RF-032
 */
@Repository
public interface CompraRepository extends JpaRepository<Compra, Long> {

    Optional<Compra> findByNumeroCompra(String numeroCompra);

    // Listar compras por proveedor
    List<Compra> findByProveedorId(Long proveedorId);

    // Listar compras por sede
    List<Compra> findBySedeId(Long sedeId);

    // Listar compras por estado
    List<Compra> findByEstado(Compra.EstadoCompra estado);

    // Listar compras por rango de fechas
    @Query("SELECT c FROM Compra c WHERE c.fechaRegistro BETWEEN :fechaInicio AND :fechaFin ORDER BY c.fechaRegistro DESC")
    List<Compra> findByFechaRegistroBetween(
            @Param("fechaInicio") LocalDateTime fechaInicio,
            @Param("fechaFin") LocalDateTime fechaFin
    );

    // Últimas compras
    List<Compra> findTop10ByOrderByFechaRegistroDesc();

    // Generar número de compra automático
    @Query("SELECT COALESCE(MAX(CAST(SUBSTRING(c.numeroCompra, 4) AS integer)), 0) FROM Compra c WHERE c.numeroCompra LIKE CONCAT('CMP', :year, '%')")
    Integer findUltimoNumeroCompraDelAnio(@Param("year") String year);
}