package com.deybimotors.repository;

import com.deybimotors.entity.Sede;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio Sede - RF-040 a RF-042
 */
@Repository
public interface SedeRepository extends JpaRepository<Sede, Long> {

    // Buscar sede por nombre
    Optional<Sede> findByNombre(String nombre);

    // Verificar si existe sede con ese nombre
    boolean existsByNombre(String nombre);

    // Listar sedes activas
    List<Sede> findByActivoTrue();

    // Buscar por ciudad
    List<Sede> findByCiudad(String ciudad);
}