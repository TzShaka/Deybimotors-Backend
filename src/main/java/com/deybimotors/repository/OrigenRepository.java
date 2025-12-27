package com.deybimotors.repository;

import com.deybimotors.entity.Origen;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrigenRepository extends JpaRepository<Origen, Long> {
    Optional<Origen> findByPais(String pais);
    boolean existsByPais(String pais);
}