package com.deybimotors.controller;

import com.deybimotors.dto.GenericDTO;
import com.deybimotors.service.MarcaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador de Marcas - RF-050 a RF-053
 * Endpoints: /api/marcas/**
 */
@RestController
@RequestMapping("/api/marcas")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class MarcaController {

    private final MarcaService marcaService;

    /**
     * GET /api/marcas
     * Listar todas las marcas
     */
    @GetMapping
    public ResponseEntity<List<GenericDTO.MarcaResponse>> listarTodas() {
        return ResponseEntity.ok(marcaService.listarTodas());
    }

    /**
     * GET /api/marcas/activas
     * Listar marcas activas
     */
    @GetMapping("/activas")
    public ResponseEntity<List<GenericDTO.MarcaResponse>> listarActivas() {
        return ResponseEntity.ok(marcaService.listarActivas());
    }

    /**
     * GET /api/marcas/{id}
     * Obtener marca por ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<GenericDTO.MarcaResponse> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(marcaService.obtenerPorId(id));
    }

    /**
     * POST /api/marcas
     * Crear nueva marca
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'ALMACENERO')")
    public ResponseEntity<GenericDTO.MarcaResponse> crear(@Valid @RequestBody GenericDTO.MarcaRequest request) {
        GenericDTO.MarcaResponse response = marcaService.crear(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * PUT /api/marcas/{id}
     * Actualizar marca
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ALMACENERO')")
    public ResponseEntity<GenericDTO.MarcaResponse> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody GenericDTO.MarcaRequest request
    ) {
        return ResponseEntity.ok(marcaService.actualizar(id, request));
    }

    /**
     * DELETE /api/marcas/{id}
     * Eliminar marca
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> eliminar(@PathVariable Long id) {
        marcaService.eliminar(id);
        return ResponseEntity.ok("Marca eliminada correctamente");
    }
}