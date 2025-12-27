package com.deybimotors.repository;

import com.deybimotors.entity.Compatibilidad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CompatibilidadRepository extends JpaRepository<Compatibilidad, Long> {
    List<Compatibilidad> findByProductoId(Long productoId);
    List<Compatibilidad> findByMarcaAutomovilId(Long marcaId);
    List<Compatibilidad> findByModeloAutomovilId(Long modeloId);
    void deleteByProductoId(Long productoId);
}