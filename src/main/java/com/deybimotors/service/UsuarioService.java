package com.deybimotors.service;

import com.deybimotors.dto.UsuarioDTO;
import com.deybimotors.entity.Sede;
import com.deybimotors.entity.Usuario;
import com.deybimotors.exception.BadRequestException;
import com.deybimotors.exception.ConflictException;
import com.deybimotors.exception.ResourceNotFoundException;
import com.deybimotors.repository.SedeRepository;
import com.deybimotors.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio de Usuarios - RF-002, RF-054 a RF-057
 * Gestión completa de usuarios
 */
@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final SedeRepository sedeRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Listar todos los usuarios
     */
    @Transactional(readOnly = true)
    public List<UsuarioDTO.UsuarioResponse> listarTodos() {
        return usuarioRepository.findAll().stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    /**
     * Listar usuarios activos
     */
    @Transactional(readOnly = true)
    public List<UsuarioDTO.UsuarioResponse> listarActivos() {
        return usuarioRepository.findByActivoTrue().stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    /**
     * Obtener usuario por ID
     */
    @Transactional(readOnly = true)
    public UsuarioDTO.UsuarioResponse obtenerPorId(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con ID: " + id));
        return convertirADTO(usuario);
    }

    /**
     * Crear nuevo usuario - RF-002
     */
    @Transactional
    public UsuarioDTO.UsuarioResponse crear(UsuarioDTO.CrearUsuarioRequest request) {

        // Validar que no exista el username
        if (usuarioRepository.existsByUsername(request.getUsername())) {
            throw new ConflictException("El usuario '" + request.getUsername() + "' ya existe");
        }

        // Validar que no exista el email
        if (request.getEmail() != null && usuarioRepository.existsByEmail(request.getEmail())) {
            throw new ConflictException("El email ya está registrado");
        }

        // Buscar sede
        Sede sede = sedeRepository.findById(request.getSedeId())
                .orElseThrow(() -> new ResourceNotFoundException("Sede no encontrada"));

        // Crear usuario
        Usuario usuario = new Usuario();
        usuario.setUsername(request.getUsername());
        usuario.setPassword(passwordEncoder.encode(request.getPassword())); // Encriptar - RF-002
        usuario.setNombreCompleto(request.getNombreCompleto());
        usuario.setEmail(request.getEmail());
        usuario.setTelefono(request.getTelefono());
        usuario.setRol(Usuario.Rol.valueOf(request.getRol()));
        usuario.setSede(sede);
        usuario.setActivo(true);
        usuario.setObservaciones(request.getObservaciones());

        Usuario guardado = usuarioRepository.save(usuario);
        return convertirADTO(guardado);
    }

    /**
     * Actualizar usuario - RF-002
     */
    @Transactional
    public UsuarioDTO.UsuarioResponse actualizar(Long id, UsuarioDTO.UsuarioRequest request) {

        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        // Validar username si cambió
        if (!usuario.getUsername().equals(request.getUsername())) {
            if (usuarioRepository.existsByUsername(request.getUsername())) {
                throw new ConflictException("El usuario ya existe");
            }
            usuario.setUsername(request.getUsername());
        }

        // Validar email si cambió
        if (request.getEmail() != null && !request.getEmail().equals(usuario.getEmail())) {
            if (usuarioRepository.existsByEmail(request.getEmail())) {
                throw new ConflictException("El email ya está registrado");
            }
            usuario.setEmail(request.getEmail());
        }

        // Buscar sede si cambió
        if (!usuario.getSede().getId().equals(request.getSedeId())) {
            Sede sede = sedeRepository.findById(request.getSedeId())
                    .orElseThrow(() -> new ResourceNotFoundException("Sede no encontrada"));
            usuario.setSede(sede);
        }

        // Actualizar campos
        usuario.setNombreCompleto(request.getNombreCompleto());
        usuario.setTelefono(request.getTelefono());
        usuario.setRol(Usuario.Rol.valueOf(request.getRol()));
        usuario.setObservaciones(request.getObservaciones());

        Usuario actualizado = usuarioRepository.save(usuario);
        return convertirADTO(actualizado);
    }

    /**
     * Activar/Desactivar usuario - RF-002
     */
    @Transactional
    public void cambiarEstado(Long id, boolean activo) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        usuario.setActivo(activo);
        usuarioRepository.save(usuario);
    }

    /**
     * Eliminar usuario - RF-002
     */
    @Transactional
    public void eliminar(Long id, String usernameActual) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        // No permitir que un admin se elimine a sí mismo - RF-002
        if (usuario.getUsername().equals(usernameActual)) {
            throw new BadRequestException("No puedes eliminarte a ti mismo");
        }

        // Aquí podrías agregar validación de historial asociado
        // Por ahora, permitimos eliminación
        usuarioRepository.delete(usuario);
    }

    /**
     * Resetear contraseña (solo Admin) - RF-002
     */
    @Transactional
    public void resetearPassword(Long id, UsuarioDTO.ResetPasswordRequest request) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        usuario.setPassword(passwordEncoder.encode(request.getNuevaPassword()));
        usuarioRepository.save(usuario);
    }

    // Convertir entidad a DTO
    private UsuarioDTO.UsuarioResponse convertirADTO(Usuario usuario) {
        return new UsuarioDTO.UsuarioResponse(
                usuario.getId(),
                usuario.getUsername(),
                usuario.getNombreCompleto(),
                usuario.getEmail(),
                usuario.getTelefono(),
                usuario.getRol().name(),
                usuario.getSede().getId(),
                usuario.getSede().getNombre(),
                usuario.getActivo(),
                usuario.getFechaCreacion(),
                usuario.getObservaciones()
        );
    }
}