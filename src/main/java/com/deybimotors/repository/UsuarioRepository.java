package com.deybimotors.repository;

import com.deybimotors.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio Usuario - RF-001, RF-002
 * Operaciones de base de datos para usuarios
 */
@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    // Buscar usuario por username para autenticación
    Optional<Usuario> findByUsername(String username);

    // Verificar si existe un usuario con ese username
    boolean existsByUsername(String username);

    // Listar usuarios activos
    List<Usuario> findByActivoTrue();

    // Buscar usuarios por rol
    List<Usuario> findByRol(Usuario.Rol rol);

    // Buscar usuarios por sede
    List<Usuario> findBySedeId(Long sedeId);

    // Buscar usuarios activos por sede
    List<Usuario> findBySedeIdAndActivoTrue(Long sedeId);

    // Verificar si existe email
    boolean existsByEmail(String email);
}