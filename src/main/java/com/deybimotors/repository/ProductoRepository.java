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

    Optional<Producto> findByCodigo(String codigo);
    boolean existsByCodigo(String codigo);
    List<Producto> findByNombreContainingIgnoreCase(String nombre);
    List<Producto> findByActivoTrue();
    List<Producto> findByCategoriaId(Long categoriaId);
    List<Producto> findByMarcaId(Long marcaId);

    // ✅ MANTENER - Búsqueda por modelo del automóvil (campo de texto)
    List<Producto> findByModeloAutomovilContainingIgnoreCase(String modeloAutomovil);

    List<Producto> findByPublicoCatalogoTrueAndActivoTrue();

    @Query("SELECT COUNT(p) FROM Producto p WHERE p.activo = true")
    long countProductosActivos();

    @Query("SELECT COUNT(p) FROM Producto p WHERE p.categoria.id = :categoriaId AND p.activo = true")
    long countByCategoriaId(@Param("categoriaId") Long categoriaId);

    @Query("SELECT COUNT(p) FROM Producto p WHERE p.marca.id = :marcaId AND p.activo = true")
    long countByMarcaId(@Param("marcaId") Long marcaId);

    @Query("SELECT COUNT(p) FROM Producto p WHERE p.subcategoria.id = :subcategoriaId AND p.activo = true")
    long countBySubcategoriaId(@Param("subcategoriaId") Long subcategoriaId);
}