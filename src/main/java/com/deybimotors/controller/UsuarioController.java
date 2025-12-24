package com.deybimotors.controller;

import com.deybimotors.dto.UsuarioDTO;
import com.deybimotors.service.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador de Usuarios - RF-002, RF-054 a RF-057
 * Endpoints: /api/usuarios/**
 * Solo accesible por ADMIN
 */
@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@PreAuthorize("hasRole('ADMIN')")
public class UsuarioController {

    private final UsuarioService usuarioService;

    /**
     * GET /api/usuarios
     * Listar todos los usuarios
     */
    @GetMapping
    public ResponseEntity<List<UsuarioDTO.UsuarioResponse>> listarTodos() {
        return ResponseEntity.ok(usuarioService.listarTodos());
    }

    /**
     * GET /api/usuarios/activos
     * Listar usuarios activos
     */
    @GetMapping("/activos")
    public ResponseEntity<List<UsuarioDTO.UsuarioResponse>> listarActivos() {
        return ResponseEntity.ok(usuarioService.listarActivos());
    }

    /**
     * GET /api/usuarios/{id}
     * Obtener usuario por ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<UsuarioDTO.UsuarioResponse> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(usuarioService.obtenerPorId(id));
    }

    /**
     * POST /api/usuarios
     * Crear nuevo usuario
     */
    @PostMapping
    public ResponseEntity<UsuarioDTO.UsuarioResponse> crear(@Valid @RequestBody UsuarioDTO.CrearUsuarioRequest request) {
        UsuarioDTO.UsuarioResponse response = usuarioService.crear(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * PUT /api/usuarios/{id}
     * Actualizar usuario
     */
    @PutMapping("/{id}")
    public ResponseEntity<UsuarioDTO.UsuarioResponse> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody UsuarioDTO.UsuarioRequest request
    ) {
        return ResponseEntity.ok(usuarioService.actualizar(id, request));
    }

    /**
     * PATCH /api/usuarios/{id}/estado
     * Activar/Desactivar usuario
     */
    @PatchMapping("/{id}/estado")
    public ResponseEntity<String> cambiarEstado(
            @PathVariable Long id,
            @RequestParam boolean activo
    ) {
        usuarioService.cambiarEstado(id, activo);
        return ResponseEntity.ok("Estado actualizado correctamente");
    }

    /**
     * DELETE /api/usuarios/{id}
     * Eliminar usuario
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<String> eliminar(@PathVariable Long id, Authentication authentication) {
        usuarioService.eliminar(id, authentication.getName());
        return ResponseEntity.ok("Usuario eliminado correctamente");
    }

    /**
     * POST /api/usuarios/{id}/resetear-password
     * Resetear contraseña (solo Admin)
     */
    @PostMapping("/{id}/resetear-password")
    public ResponseEntity<String> resetearPassword(
            @PathVariable Long id,
            @Valid @RequestBody UsuarioDTO.ResetPasswordRequest request
    ) {
        usuarioService.resetearPassword(id, request);
        return ResponseEntity.ok("Contraseña reseteada correctamente");
    }
}