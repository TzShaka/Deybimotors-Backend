package com.deybimotors.repository;

import com.deybimotors.entity.CodigoOem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repositorio para CodigoOem
 */
@Repository
public interface CodigoOemRepository extends JpaRepository<CodigoOem, Long> {
    Optional<CodigoOem> findByCodigoOem(String codigoOem);
    boolean existsByCodigoOem(String codigoOem);
}