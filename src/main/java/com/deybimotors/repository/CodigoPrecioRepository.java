package com.deybimotors.repository;

import com.deybimotors.entity.CodigoPrecio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CodigoPrecioRepository extends JpaRepository<CodigoPrecio, Long> {
    Optional<CodigoPrecio> findByCodigo(String codigo);
    boolean existsByCodigo(String codigo);
}