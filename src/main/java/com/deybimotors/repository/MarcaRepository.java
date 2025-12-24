package com.deybimotors.repository;

import com.deybimotors.entity.Marca;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio Marca - RF-050 a RF-053
 */
@Repository
public interface MarcaRepository extends JpaRepository<Marca, Long> {

    Optional<Marca> findByNombre(String nombre);

    boolean existsByNombre(String nombre);

    List<Marca> findByActivoTrue();
}