package com.deybimotors.repository;

import com.deybimotors.entity.Stock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio Stock - Control de inventario
 */
@Repository
public interface StockRepository extends JpaRepository<Stock, Long> {

    // Buscar stock específico de un producto en una sede
    Optional<Stock> findByProductoIdAndSedeId(Long productoId, Long sedeId);

    // Obtener todo el stock de un producto en todas las sedes
    List<Stock> findByProductoId(Long productoId);

    // Obtener todo el stock de una sede
    List<Stock> findBySedeId(Long sedeId);

    // Productos sin stock en una sede específica
    @Query("SELECT s FROM Stock s WHERE s.sede.id = :sedeId AND s.cantidad = 0")
    List<Stock> findProductosSinStockPorSede(@Param("sedeId") Long sedeId);

    // Productos con stock bajo (menor al mínimo)
    @Query("SELECT s FROM Stock s WHERE s.sede.id = :sedeId AND s.cantidad > 0 AND s.cantidad <= s.producto.stockMinimo")
    List<Stock> findProductosStockBajoPorSede(@Param("sedeId") Long sedeId);

    // Contar productos sin stock
    @Query("SELECT COUNT(s) FROM Stock s WHERE s.sede.id = :sedeId AND s.cantidad = 0")
    long countProductosSinStock(@Param("sedeId") Long sedeId);

    // Contar productos con stock mínimo
    @Query("SELECT COUNT(s) FROM Stock s WHERE s.sede.id = :sedeId AND s.cantidad > 0 AND s.cantidad <= s.producto.stockMinimo")
    long countProductosStockMinimo(@Param("sedeId") Long sedeId);
}