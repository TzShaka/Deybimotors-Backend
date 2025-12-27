package com.deybimotors.repository;

import com.deybimotors.entity.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Long>,
        JpaSpecificationExecutor<Producto> {

    // Búsqueda por código interno
    Optional<Producto> findByCodigoInterno(String codigoInterno);
    boolean existsByCodigoInterno(String codigoInterno);

    // Búsqueda por descripción
    List<Producto> findByDescripcionContainingIgnoreCase(String descripcion);

    // Productos activos (estado = true)
    List<Producto> findByEstadoTrue();

    // Por categoría
    List<Producto> findByCategoriaId(Long categoriaId);

    // Por marca de producto
    List<Producto> findByMarcaProductoId(Long marcaId);

    // Por subcategoría
    List<Producto> findBySubcategoriaId(Long subcategoriaId);

    // Por sede
    List<Producto> findBySedeId(Long sedeId);

    // Productos sin stock en una sede específica
    @Query("SELECT p FROM Producto p WHERE p.sede.id = :sedeId AND p.stock = 0 AND p.estado = true")
    List<Producto> findProductosSinStockPorSede(@Param("sedeId") Long sedeId);

    // Productos con stock bajo
    @Query("SELECT p FROM Producto p WHERE p.sede.id = :sedeId AND p.stock > 0 AND p.stock <= p.stockMinimo AND p.estado = true")
    List<Producto> findProductosStockBajoPorSede(@Param("sedeId") Long sedeId);

    // Contar productos activos
    @Query("SELECT COUNT(p) FROM Producto p WHERE p.estado = true")
    long countProductosActivos();

    // Contar por categoría
    @Query("SELECT COUNT(p) FROM Producto p WHERE p.categoria.id = :categoriaId AND p.estado = true")
    long countByCategoriaId(@Param("categoriaId") Long categoriaId);

    // Contar por marca
    @Query("SELECT COUNT(p) FROM Producto p WHERE p.marcaProducto.id = :marcaId AND p.estado = true")
    long countByMarcaProductoId(@Param("marcaId") Long marcaId);

    // Contar por subcategoría
    @Query("SELECT COUNT(p) FROM Producto p WHERE p.subcategoria.id = :subcategoriaId AND p.estado = true")
    long countBySubcategoriaId(@Param("subcategoriaId") Long subcategoriaId);

    // Contar sin stock en una sede
    @Query("SELECT COUNT(p) FROM Producto p WHERE p.sede.id = :sedeId AND p.stock = 0 AND p.estado = true")
    long countProductosSinStock(@Param("sedeId") Long sedeId);

    // Contar con stock mínimo
    @Query("SELECT COUNT(p) FROM Producto p WHERE p.sede.id = :sedeId AND p.stock > 0 AND p.stock <= p.stockMinimo AND p.estado = true")
    long countProductosStockMinimo(@Param("sedeId") Long sedeId);

    // Métodos de compatibilidad con código existente
    default Optional<Producto> findByCodigo(String codigo) {
        return findByCodigoInterno(codigo);
    }

    default boolean existsByCodigo(String codigo) {
        return existsByCodigoInterno(codigo);
    }

    default List<Producto> findByNombreContainingIgnoreCase(String nombre) {
        return findByDescripcionContainingIgnoreCase(nombre);
    }

    default List<Producto> findByActivoTrue() {
        return findByEstadoTrue();
    }

    default List<Producto> findByMarcaId(Long marcaId) {
        return findByMarcaProductoId(marcaId);
    }

    default long countByMarcaId(Long marcaId) {
        return countByMarcaProductoId(marcaId);
    }
}