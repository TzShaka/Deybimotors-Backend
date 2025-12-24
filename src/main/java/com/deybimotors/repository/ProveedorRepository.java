package com.deybimotors.repository;

import com.deybimotors.entity.Proveedor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio Proveedor - RF-043 a RF-045
 */
@Repository
public interface ProveedorRepository extends JpaRepository<Proveedor, Long> {

    Optional<Proveedor> findByRuc(String ruc);

    boolean existsByRuc(String ruc);

    List<Proveedor> findByActivoTrue();

    List<Proveedor> findByNombreEmpresaContainingIgnoreCase(String nombre);
}