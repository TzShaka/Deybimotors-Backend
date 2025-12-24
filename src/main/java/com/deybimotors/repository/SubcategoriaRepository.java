package com.deybimotors.repository;

import com.deybimotors.entity.Subcategoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio Subcategoría - RF-046 a RF-049
 */
@Repository
public interface SubcategoriaRepository extends JpaRepository<Subcategoria, Long> {

    List<Subcategoria> findByCategoriaId(Long categoriaId);

    Optional<Subcategoria> findByNombreAndCategoriaId(String nombre, Long categoriaId);

    boolean existsByNombreAndCategoriaId(String nombre, Long categoriaId);

    List<Subcategoria> findByActivoTrue();

    List<Subcategoria> findByCategoriaIdAndActivoTrue(Long categoriaId);
}