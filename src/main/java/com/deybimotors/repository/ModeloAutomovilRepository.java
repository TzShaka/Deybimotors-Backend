package com.deybimotors.repository;

import com.deybimotors.entity.ModeloAutomovil;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ModeloAutomovilRepository extends JpaRepository<ModeloAutomovil, Long> {
    List<ModeloAutomovil> findByMarcaAutomovilId(Long marcaId);
    Optional<ModeloAutomovil> findByNombreAndMarcaAutomovilId(String nombre, Long marcaId);
    boolean existsByNombreAndMarcaAutomovilId(String nombre, Long marcaId);
}