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
 * Servicio de Usuarios - ✅ ACTUALIZADO
 * Manejo de rol como String (ENUM), no FK
 */
@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final SedeRepository sedeRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public List<UsuarioDTO.UsuarioResponse> listarTodos() {
        return usuarioRepository.findAll().stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<UsuarioDTO.UsuarioResponse> listarActivos() {
        return usuarioRepository.findByActivoTrue().stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

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

        if (usuarioRepository.existsByUsername(request.getUsername())) {
            throw new ConflictException("El usuario '" + request.getUsername() + "' ya existe");
        }

        if (request.getEmail() != null && usuarioRepository.existsByEmail(request.getEmail())) {
            throw new ConflictException("El email ya está registrado");
        }

        Sede sede = sedeRepository.findById(request.getSedeId())
                .orElseThrow(() -> new ResourceNotFoundException("Sede no encontrada"));

        // ✅ Validar rol
        Usuario.Rol rol;
        try {
            rol = Usuario.Rol.valueOf(request.getRol().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Rol inválido. Valores permitidos: ADMIN, VENDEDOR, ALMACENERO");
        }

        Usuario usuario = new Usuario();
        usuario.setUsername(request.getUsername());
        usuario.setPassword(passwordEncoder.encode(request.getPassword()));
        usuario.setNombreCompleto(request.getNombreCompleto());
        usuario.setEmail(request.getEmail());
        usuario.setTelefono(request.getTelefono());
        usuario.setRol(rol);
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

        if (!usuario.getUsername().equals(request.getUsername())) {
            if (usuarioRepository.existsByUsername(request.getUsername())) {
                throw new ConflictException("El usuario ya existe");
            }
            usuario.setUsername(request.getUsername());
        }

        if (request.getEmail() != null && !request.getEmail().equals(usuario.getEmail())) {
            if (usuarioRepository.existsByEmail(request.getEmail())) {
                throw new ConflictException("El email ya está registrado");
            }
            usuario.setEmail(request.getEmail());
        }

        if (!usuario.getSede().getId().equals(request.getSedeId())) {
            Sede sede = sedeRepository.findById(request.getSedeId())
                    .orElseThrow(() -> new ResourceNotFoundException("Sede no encontrada"));
            usuario.setSede(sede);
        }

        // ✅ Validar y actualizar rol
        Usuario.Rol rol;
        try {
            rol = Usuario.Rol.valueOf(request.getRol().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Rol inválido. Valores permitidos: ADMIN, VENDEDOR, ALMACENERO");
        }

        usuario.setNombreCompleto(request.getNombreCompleto());
        usuario.setTelefono(request.getTelefono());
        usuario.setRol(rol);
        usuario.setObservaciones(request.getObservaciones());

        Usuario actualizado = usuarioRepository.save(usuario);
        return convertirADTO(actualizado);
    }

    @Transactional
    public void cambiarEstado(Long id, boolean activo) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        usuario.setActivo(activo);
        usuarioRepository.save(usuario);
    }

    @Transactional
    public void eliminar(Long id, String usernameActual) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        if (usuario.getUsername().equals(usernameActual)) {
            throw new BadRequestException("No puedes eliminarte a ti mismo");
        }

        usuarioRepository.delete(usuario);
    }

    @Transactional
    public void resetearPassword(Long id, UsuarioDTO.ResetPasswordRequest request) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        usuario.setPassword(passwordEncoder.encode(request.getNuevaPassword()));
        usuarioRepository.save(usuario);
    }

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