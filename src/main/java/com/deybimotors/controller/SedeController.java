package com.deybimotors.controller;

import com.deybimotors.dto.GenericDTO;
import com.deybimotors.service.SedeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador de Sedes - RF-040 a RF-042
 * Endpoints: /api/sedes/**
 */
@RestController
@RequestMapping("/api/sedes")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@PreAuthorize("hasAnyRole('ADMIN', 'ALMACENERO')")
public class SedeController {

    private final SedeService sedeService;

    /**
     * GET /api/sedes
     * Listar todas las sedes
     */
    @GetMapping
    public ResponseEntity<List<GenericDTO.SedeResponse>> listarTodas() {
        return ResponseEntity.ok(sedeService.listarTodas());
    }

    /**
     * GET /api/sedes/activas
     * Listar sedes activas
     */
    @GetMapping("/activas")
    public ResponseEntity<List<GenericDTO.SedeResponse>> listarActivas() {
        return ResponseEntity.ok(sedeService.listarActivas());
    }

    /**
     * GET /api/sedes/{id}
     * Obtener sede por ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<GenericDTO.SedeResponse> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(sedeService.obtenerPorId(id));
    }

    /**
     * POST /api/sedes
     * Crear nueva sede
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<GenericDTO.SedeResponse> crear(@Valid @RequestBody GenericDTO.SedeRequest request) {
        GenericDTO.SedeResponse response = sedeService.crear(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * PUT /api/sedes/{id}
     * Actualizar sede
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<GenericDTO.SedeResponse> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody GenericDTO.SedeRequest request
    ) {
        return ResponseEntity.ok(sedeService.actualizar(id, request));
    }

    /**
     * PATCH /api/sedes/{id}/estado
     * Activar/Desactivar sede
     */
    @PatchMapping("/{id}/estado")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> cambiarEstado(
            @PathVariable Long id,
            @RequestParam boolean activo
    ) {
        sedeService.cambiarEstado(id, activo);
        return ResponseEntity.ok("Estado actualizado correctamente");
    }

    /**
     * DELETE /api/sedes/{id}
     * Eliminar sede
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> eliminar(@PathVariable Long id) {
        sedeService.eliminar(id);
        return ResponseEntity.ok("Sede eliminada correctamente");
    }
}