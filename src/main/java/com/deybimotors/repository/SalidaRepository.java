package com.deybimotors.repository;

import com.deybimotors.entity.Salida;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SalidaRepository extends JpaRepository<Salida, Long> {
    List<Salida> findBySedeId(Long sedeId);
    List<Salida> findByUsuarioId(Long usuarioId);
    List<Salida> findByFechaCreacionBetween(LocalDateTime inicio, LocalDateTime fin);
    List<Salida> findTop10ByOrderByFechaCreacionDesc();
}