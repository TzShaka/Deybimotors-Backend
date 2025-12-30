package com.deybimotors.controller;

import com.deybimotors.dto.ProductoDTO;
import com.deybimotors.security.SecurityUtils;
import com.deybimotors.service.ProductoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

/**
 * Controlador de Productos - ✅ CORREGIDO
 * RF-004 a RF-017
 */
@RestController
@RequestMapping("/api/productos")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ProductoController {

    private final ProductoService productoService;
    private final SecurityUtils securityUtils;

    /**
     * GET /api/productos
     * Listar todos los productos
     */
    @GetMapping
    public ResponseEntity<List<ProductoDTO.ProductoResponse>> listarTodos() {
        return ResponseEntity.ok(productoService.listarTodos());
    }

    /**
     * GET /api/productos/paginado
     * Listar productos con paginación
     */
    @GetMapping("/paginado")
    public ResponseEntity<Page<ProductoDTO.ProductoResponse>> listarConPaginacion(Pageable pageable) {
        return ResponseEntity.ok(productoService.listarConPaginacion(pageable));
    }

    /**
     * GET /api/productos/sede/{sedeId}
     * Listar productos con stock de una sede específica
     */
    @GetMapping("/sede/{sedeId}")
    public ResponseEntity<List<ProductoDTO.ProductoConStockResponse>> listarConStockPorSede(@PathVariable Long sedeId) {
        return ResponseEntity.ok(productoService.listarConStockPorSede(sedeId));
    }

    /**
     * GET /api/productos/{id}
     * Obtener producto por ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<ProductoDTO.ProductoResponse> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(productoService.obtenerPorId(id));
    }

    /**
     * GET /api/productos/codigo/{codigo}
     * Obtener producto por código
     */
    @GetMapping("/codigo/{codigo}")
    public ResponseEntity<ProductoDTO.ProductoResponse> obtenerPorCodigo(@PathVariable String codigo) {
        return ResponseEntity.ok(productoService.obtenerPorCodigo(codigo));
    }

    /**
     * GET /api/productos/buscar
     * Buscar productos con filtros avanzados
     */
    @GetMapping("/buscar")
    public ResponseEntity<List<ProductoDTO.ProductoResponse>> buscarConFiltros(
            @RequestParam(required = false) String nombre,
            @RequestParam(required = false) Long categoriaId,
            @RequestParam(required = false) Long subcategoriaId,
            @RequestParam(required = false) Long marcaId,
            @RequestParam(required = false) String codigo
    ) {
        return ResponseEntity.ok(productoService.buscarConFiltros(nombre, categoriaId, subcategoriaId, marcaId, codigo));
    }

    /**
     * POST /api/productos
     * Crear nuevo producto
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'ALMACENERO')")
    public ResponseEntity<ProductoDTO.ProductoResponse> crear(
            @Valid @RequestBody ProductoDTO.ProductoRequest request
    ) {
        Long usuarioId = securityUtils.getAuthenticatedUserId();
        ProductoDTO.ProductoResponse response = productoService.crear(request, usuarioId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * PUT /api/productos/{id}
     * Actualizar producto completo
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ALMACENERO')")
    public ResponseEntity<ProductoDTO.ProductoResponse> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody ProductoDTO.ProductoRequest request
    ) {
        return ResponseEntity.ok(productoService.actualizar(id, request));
    }

    /**
     * PATCH /api/productos/{id}/campo
     * Edición inline de un campo específico
     */
    @PatchMapping("/{id}/campo")
    @PreAuthorize("hasAnyRole('ADMIN', 'ALMACENERO')")
    public ResponseEntity<ProductoDTO.ProductoResponse> actualizarCampo(
            @PathVariable Long id,
            @RequestParam String campo,
            @RequestParam Object valor
    ) {
        return ResponseEntity.ok(productoService.actualizarCampo(id, campo, valor));
    }

    /**
     * DELETE /api/productos/{id}
     * Eliminar producto
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> eliminar(@PathVariable Long id) {
        productoService.eliminar(id);
        return ResponseEntity.ok("Producto eliminado correctamente");
    }

    /**
     * POST /api/productos/{id}/foto
     * ✅ CORREGIDO: Subir/actualizar foto de producto
     */
    @PostMapping("/{id}/foto")
    @PreAuthorize("hasAnyRole('ADMIN', 'ALMACENERO')")
    public ResponseEntity<String> subirFoto(
            @PathVariable Long id,
            @RequestParam("archivo") MultipartFile archivo
    ) throws IOException {
        productoService.subirFoto(id, archivo);
        return ResponseEntity.ok("Foto actualizada correctamente");
    }

    /**
     * DELETE /api/productos/{id}/foto
     * ✅ NUEVO: Eliminar foto de producto
     */
    @DeleteMapping("/{id}/foto")
    @PreAuthorize("hasAnyRole('ADMIN', 'ALMACENERO')")
    public ResponseEntity<String> eliminarFoto(@PathVariable Long id) {
        productoService.eliminarFoto(id);
        return ResponseEntity.ok("Foto eliminada correctamente");
    }
}