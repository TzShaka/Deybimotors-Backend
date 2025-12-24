package com.deybimotors.repository;

import com.deybimotors.entity.Modelo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio Modelo - RF-050 a RF-053
 */
@Repository
public interface ModeloRepository extends JpaRepository<Modelo, Long> {

    List<Modelo> findByMarcaId(Long marcaId);

    Optional<Modelo> findByNombreAndMarcaId(String nombre, Long marcaId);

    boolean existsByNombreAndMarcaId(String nombre, Long marcaId);

    List<Modelo> findByActivoTrue();

    List<Modelo> findByMarcaIdAndActivoTrue(Long marcaId);
}