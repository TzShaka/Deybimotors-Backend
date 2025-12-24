package com.deybimotors.repository;

import com.deybimotors.entity.CompraDetalle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio CompraDetalle - RF-027, RF-028
 */
@Repository
public interface CompraDetalleRepository extends JpaRepository<CompraDetalle, Long> {

    // Obtener detalles de una compra
    List<CompraDetalle> findByCompraId(Long compraId);

    // Obtener detalles por producto
    List<CompraDetalle> findByProductoId(Long productoId);

    // Eliminar detalles de una compra
    void deleteByCompraId(Long compraId);
}