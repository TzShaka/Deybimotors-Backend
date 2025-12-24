package com.deybimotors.repository;

import com.deybimotors.entity.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio Categoría - RF-046 a RF-049
 */
@Repository
public interface CategoriaRepository extends JpaRepository<Categoria, Long> {

    Optional<Categoria> findByNombre(String nombre);

    boolean existsByNombre(String nombre);

    List<Categoria> findByActivoTrue();
}