package com.deybimotors.repository;

import com.deybimotors.entity.ProductoFoto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio ProductoFoto - RF-010
 */
@Repository
public interface ProductoFotoRepository extends JpaRepository<ProductoFoto, Long> {

    // Obtener todas las fotos de un producto
    List<ProductoFoto> findByProductoIdOrderByOrdenAsc(Long productoId);

    // Obtener foto principal de un producto
    Optional<ProductoFoto> findByProductoIdAndPrincipalTrue(Long productoId);

    // Eliminar todas las fotos de un producto
    void deleteByProductoId(Long productoId);
}