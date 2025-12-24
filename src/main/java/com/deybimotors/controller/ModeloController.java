package com.deybimotors.controller;

import com.deybimotors.dto.GenericDTO;
import com.deybimotors.service.ModeloService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador de Modelos - RF-050 a RF-053
 * Endpoints: /api/modelos/**
 */
@RestController
@RequestMapping("/api/modelos")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ModeloController {

    private final ModeloService modeloService;

    /**
     * GET /api/modelos
     * Listar todos los modelos
     */
    @GetMapping
    public ResponseEntity<List<GenericDTO.ModeloResponse>> listarTodos() {
        return ResponseEntity.ok(modeloService.listarTodos());
    }

    /**
     * GET /api/modelos/activos
     * Listar modelos activos
     */
    @GetMapping("/activos")
    public ResponseEntity<List<GenericDTO.ModeloResponse>> listarActivos() {
        return ResponseEntity.ok(modeloService.listarActivos());
    }

    /**
     * GET /api/modelos/marca/{marcaId}
     * Listar modelos por marca
     */
    @GetMapping("/marca/{marcaId}")
    public ResponseEntity<List<GenericDTO.ModeloResponse>> listarPorMarca(@PathVariable Long marcaId) {
        return ResponseEntity.ok(modeloService.listarPorMarca(marcaId));
    }

    /**
     * GET /api/modelos/{id}
     * Obtener modelo por ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<GenericDTO.ModeloResponse> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(modeloService.obtenerPorId(id));
    }

    /**
     * POST /api/modelos
     * Crear nuevo modelo
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'ALMACENERO')")
    public ResponseEntity<GenericDTO.ModeloResponse> crear(@Valid @RequestBody GenericDTO.ModeloRequest request) {
        GenericDTO.ModeloResponse response = modeloService.crear(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * PUT /api/modelos/{id}
     * Actualizar modelo
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ALMACENERO')")
    public ResponseEntity<GenericDTO.ModeloResponse> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody GenericDTO.ModeloRequest request
    ) {
        return ResponseEntity.ok(modeloService.actualizar(id, request));
    }

    /**
     * DELETE /api/modelos/{id}
     * Eliminar modelo
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> eliminar(@PathVariable Long id) {
        modeloService.eliminar(id);
        return ResponseEntity.ok("Modelo eliminado correctamente");
    }
}