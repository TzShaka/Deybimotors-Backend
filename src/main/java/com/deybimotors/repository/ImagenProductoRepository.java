package com.deybimotors.repository;

import com.deybimotors.entity.ImagenProducto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio ImagenProducto - RF-010
 */
@Repository
public interface ImagenProductoRepository extends JpaRepository<ImagenProducto, Long> {

    List<ImagenProducto> findByProductoIdOrderByOrdenAsc(Long productoId);

    Optional<ImagenProducto> findByProductoIdAndEsPrincipalTrue(Long productoId);

    void deleteByProductoId(Long productoId);
}