package com.deybimotors.repository;

import com.deybimotors.entity.MarcaAutomovil;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MarcaAutomovilRepository extends JpaRepository<MarcaAutomovil, Long> {
    Optional<MarcaAutomovil> findByNombre(String nombre);
    boolean existsByNombre(String nombre);
}