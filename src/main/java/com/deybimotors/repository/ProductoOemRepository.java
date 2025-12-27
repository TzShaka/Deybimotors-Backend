package com.deybimotors.repository;

import com.deybimotors.entity.ProductoOem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductoOemRepository extends JpaRepository<ProductoOem, Long> {
    List<ProductoOem> findByProductoId(Long productoId);
    List<ProductoOem> findByCodigoOemId(Long oemId);
    void deleteByProductoId(Long productoId);
}