package com.deybimotors.repository;

import com.deybimotors.entity.DetalleSalida;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DetalleSalidaRepository extends JpaRepository<DetalleSalida, Long> {
    List<DetalleSalida> findBySalidaId(Long salidaId);
    List<DetalleSalida> findByProductoId(Long productoId);
    void deleteBySalidaId(Long salidaId);
}