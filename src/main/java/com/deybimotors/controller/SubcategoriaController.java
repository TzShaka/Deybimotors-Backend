package com.deybimotors.controller;

import com.deybimotors.dto.GenericDTO;
import com.deybimotors.service.SubcategoriaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador de Subcategorías - RF-046 a RF-049
 * Endpoints: /api/subcategorias/**
 */
@RestController
@RequestMapping("/api/subcategorias")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class SubcategoriaController {

    private final SubcategoriaService subcategoriaService;

    /**
     * GET /api/subcategorias
     * Listar todas las subcategorías
     */
    @GetMapping
    public ResponseEntity<List<GenericDTO.SubcategoriaResponse>> listarTodas() {
        return ResponseEntity.ok(subcategoriaService.listarTodas());
    }

    /**
     * GET /api/subcategorias/activas
     * Listar subcategorías activas
     */
    @GetMapping("/activas")
    public ResponseEntity<List<GenericDTO.SubcategoriaResponse>> listarActivas() {
        return ResponseEntity.ok(subcategoriaService.listarActivas());
    }

    /**
     * GET /api/subcategorias/categoria/{categoriaId}
     * Listar subcategorías por categoría
     */
    @GetMapping("/categoria/{categoriaId}")
    public ResponseEntity<List<GenericDTO.SubcategoriaResponse>> listarPorCategoria(@PathVariable Long categoriaId) {
        return ResponseEntity.ok(subcategoriaService.listarPorCategoria(categoriaId));
    }

    /**
     * GET /api/subcategorias/{id}
     * Obtener subcategoría por ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<GenericDTO.SubcategoriaResponse> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(subcategoriaService.obtenerPorId(id));
    }

    /**
     * POST /api/subcategorias
     * Crear nueva subcategoría
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'ALMACENERO')")
    public ResponseEntity<GenericDTO.SubcategoriaResponse> crear(@Valid @RequestBody GenericDTO.SubcategoriaRequest request) {
        GenericDTO.SubcategoriaResponse response = subcategoriaService.crear(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * PUT /api/subcategorias/{id}
     * Actualizar subcategoría
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ALMACENERO')")
    public ResponseEntity<GenericDTO.SubcategoriaResponse> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody GenericDTO.SubcategoriaRequest request
    ) {
        return ResponseEntity.ok(subcategoriaService.actualizar(id, request));
    }

    /**
     * DELETE /api/subcategorias/{id}
     * Eliminar subcategoría
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> eliminar(@PathVariable Long id) {
        subcategoriaService.eliminar(id);
        return ResponseEntity.ok("Subcategoría eliminada correctamente");
    }
}