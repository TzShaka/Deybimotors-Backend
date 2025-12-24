package com.deybimotors.controller;

import com.deybimotors.dto.GenericDTO;
import com.deybimotors.service.ProveedorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador de Proveedores - RF-043 a RF-045
 * Endpoints: /api/proveedores/**
 */
@RestController
@RequestMapping("/api/proveedores")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@PreAuthorize("hasAnyRole('ADMIN', 'ALMACENERO')")
public class ProveedorController {

    private final ProveedorService proveedorService;

    /**
     * GET /api/proveedores
     * Listar todos los proveedores
     */
    @GetMapping
    public ResponseEntity<List<GenericDTO.ProveedorResponse>> listarTodos() {
        return ResponseEntity.ok(proveedorService.listarTodos());
    }

    /**
     * GET /api/proveedores/activos
     * Listar proveedores activos
     */
    @GetMapping("/activos")
    public ResponseEntity<List<GenericDTO.ProveedorResponse>> listarActivos() {
        return ResponseEntity.ok(proveedorService.listarActivos());
    }

    /**
     * GET /api/proveedores/{id}
     * Obtener proveedor por ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<GenericDTO.ProveedorResponse> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(proveedorService.obtenerPorId(id));
    }

    /**
     * GET /api/proveedores/buscar
     * Buscar proveedores por nombre
     */
    @GetMapping("/buscar")
    public ResponseEntity<List<GenericDTO.ProveedorResponse>> buscarPorNombre(@RequestParam String nombre) {
        return ResponseEntity.ok(proveedorService.buscarPorNombre(nombre));
    }

    /**
     * POST /api/proveedores
     * Crear nuevo proveedor
     */
    @PostMapping
    public ResponseEntity<GenericDTO.ProveedorResponse> crear(@Valid @RequestBody GenericDTO.ProveedorRequest request) {
        GenericDTO.ProveedorResponse response = proveedorService.crear(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * PUT /api/proveedores/{id}
     * Actualizar proveedor
     */
    @PutMapping("/{id}")
    public ResponseEntity<GenericDTO.ProveedorResponse> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody GenericDTO.ProveedorRequest request
    ) {
        return ResponseEntity.ok(proveedorService.actualizar(id, request));
    }

    /**
     * PATCH /api/proveedores/{id}/estado
     * Activar/Desactivar proveedor
     */
    @PatchMapping("/{id}/estado")
    public ResponseEntity<String> cambiarEstado(
            @PathVariable Long id,
            @RequestParam boolean activo
    ) {
        proveedorService.cambiarEstado(id, activo);
        return ResponseEntity.ok("Estado actualizado correctamente");
    }

    /**
     * DELETE /api/proveedores/{id}
     * Eliminar proveedor
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> eliminar(@PathVariable Long id) {
        proveedorService.eliminar(id);
        return ResponseEntity.ok("Proveedor eliminado correctamente");
    }
}