package com.deybimotors.repository;

import com.deybimotors.entity.ProductoImagen;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio para ProductoImagen
 */
@Repository
public interface ProductoImagenRepository extends JpaRepository<ProductoImagen, Long> {

    /**
     * Obtener todas las imágenes de un producto ordenadas
     */
    List<ProductoImagen> findByProductoIdOrderByOrdenAsc(Long productoId);

    /**
     * Eliminar todas las imágenes de un producto
     */
    void deleteByProductoId(Long productoId);

    /**
     * Contar imágenes de un producto
     */
    long countByProductoId(Long productoId);
}