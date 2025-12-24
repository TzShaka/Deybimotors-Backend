package com.deybimotors.controller;

import com.deybimotors.dto.ProductoDTO;
import com.deybimotors.service.ProductoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador de Catálogo Público - RF-058 a RF-062
 * ACTUALIZADO: Sin referencias a modeloId
 * Endpoints: /api/catalogo-publico/**
 * Acceso público (sin autenticación)
 */
@RestController
@RequestMapping("/api/catalogo-publico")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class CatalogoPublicoController {

    private final ProductoService productoService;

    /**
     * GET /api/catalogo-publico/productos
     * Listar productos públicos con filtros - ACTUALIZADO sin modeloId
     */
    @GetMapping("/productos")
    public ResponseEntity<List<ProductoDTO.ProductoCatalogoPublicoResponse>> listarProductos(
            @RequestParam(required = false) Long categoriaId,
            @RequestParam(required = false) Long marcaId
    ) {
        return ResponseEntity.ok(productoService.listarCatalogoPublico(categoriaId, marcaId));
    }
}