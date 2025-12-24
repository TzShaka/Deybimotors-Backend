package com.deybimotors.controller;

import com.deybimotors.dto.GenericDTO;
import com.deybimotors.service.CategoriaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador de Categorías - RF-046 a RF-049
 * Endpoints: /api/categorias/**
 */
@RestController
@RequestMapping("/api/categorias")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class CategoriaController {

    private final CategoriaService categoriaService;

    /**
     * GET /api/categorias
     * Listar todas las categorías
     */
    @GetMapping
    public ResponseEntity<List<GenericDTO.CategoriaResponse>> listarTodas() {
        return ResponseEntity.ok(categoriaService.listarTodas());
    }

    /**
     * GET /api/categorias/activas
     * Listar categorías activas
     */
    @GetMapping("/activas")
    public ResponseEntity<List<GenericDTO.CategoriaResponse>> listarActivas() {
        return ResponseEntity.ok(categoriaService.listarActivas());
    }

    /**
     * GET /api/categorias/{id}
     * Obtener categoría por ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<GenericDTO.CategoriaResponse> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(categoriaService.obtenerPorId(id));
    }

    /**
     * POST /api/categorias
     * Crear nueva categoría
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'ALMACENERO')")
    public ResponseEntity<GenericDTO.CategoriaResponse> crear(@Valid @RequestBody GenericDTO.CategoriaRequest request) {
        GenericDTO.CategoriaResponse response = categoriaService.crear(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * PUT /api/categorias/{id}
     * Actualizar categoría
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ALMACENERO')")
    public ResponseEntity<GenericDTO.CategoriaResponse> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody GenericDTO.CategoriaRequest request
    ) {
        return ResponseEntity.ok(categoriaService.actualizar(id, request));
    }

    /**
     * DELETE /api/categorias/{id}
     * Eliminar categoría
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> eliminar(@PathVariable Long id) {
        categoriaService.eliminar(id);
        return ResponseEntity.ok("Categoría eliminada correctamente");
    }
}