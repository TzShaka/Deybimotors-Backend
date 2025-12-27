package com.deybimotors.controller;

import com.deybimotors.dto.CompraDTO;
import com.deybimotors.security.SecurityUtils;
import com.deybimotors.service.CompraService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Controlador de Compras - RF-025 a RF-032
 * ✅ ACTUALIZADO: VENDEDOR también puede crear compras y verlas
 */
@RestController
@RequestMapping("/api/compras")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class CompraController {

    private final CompraService compraService;
    private final SecurityUtils securityUtils;

    /**
     * GET /api/compras
     * Listar todas las compras
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'ALMACENERO', 'VENDEDOR')")
    public ResponseEntity<List<CompraDTO.CompraResponse>> listarTodas() {
        return ResponseEntity.ok(compraService.listarTodas());
    }

    /**
     * GET /api/compras/ultimas
     * Listar últimas compras
     */
    @GetMapping("/ultimas")
    @PreAuthorize("hasAnyRole('ADMIN', 'ALMACENERO', 'VENDEDOR')")
    public ResponseEntity<List<CompraDTO.CompraResponse>> listarUltimas() {
        return ResponseEntity.ok(compraService.listarUltimas());
    }

    /**
     * GET /api/compras/estado/{estado}
     * Listar compras por estado
     */
    @GetMapping("/estado/{estado}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ALMACENERO', 'VENDEDOR')")
    public ResponseEntity<List<CompraDTO.CompraResponse>> listarPorEstado(@PathVariable String estado) {
        return ResponseEntity.ok(compraService.listarPorEstado(estado));
    }

    /**
     * GET /api/compras/proveedor/{proveedorId}
     * Listar compras por proveedor
     */
    @GetMapping("/proveedor/{proveedorId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ALMACENERO', 'VENDEDOR')")
    public ResponseEntity<List<CompraDTO.CompraResponse>> listarPorProveedor(@PathVariable Long proveedorId) {
        return ResponseEntity.ok(compraService.listarPorProveedor(proveedorId));
    }

    /**
     * GET /api/compras/sede/{sedeId}
     * Listar compras por sede
     */
    @GetMapping("/sede/{sedeId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ALMACENERO', 'VENDEDOR')")
    public ResponseEntity<List<CompraDTO.CompraResponse>> listarPorSede(@PathVariable Long sedeId) {
        return ResponseEntity.ok(compraService.listarPorSede(sedeId));
    }

    /**
     * GET /api/compras/fechas
     * Listar compras por rango de fechas
     */
    @GetMapping("/fechas")
    @PreAuthorize("hasAnyRole('ADMIN', 'ALMACENERO', 'VENDEDOR')")
    public ResponseEntity<List<CompraDTO.CompraResponse>> listarPorFechas(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaFin
    ) {
        return ResponseEntity.ok(compraService.listarPorFechas(fechaInicio, fechaFin));
    }

    /**
     * GET /api/compras/{id}
     * Obtener compra por ID
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ALMACENERO', 'VENDEDOR')")
    public ResponseEntity<CompraDTO.CompraResponse> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(compraService.obtenerPorId(id));
    }

    /**
     * POST /api/compras
     * Crear nueva compra
     * ✅ ACTUALIZADO: Obtiene usuario autenticado del token
     * ✅ VENDEDOR también puede crear compras
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'ALMACENERO', 'VENDEDOR')")
    public ResponseEntity<CompraDTO.CompraResponse> crear(
            @Valid @RequestBody CompraDTO.CrearCompraRequest request
    ) {
        Long usuarioId = securityUtils.getAuthenticatedUserId();
        CompraDTO.CompraResponse response = compraService.crear(request, usuarioId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * POST /api/compras/{id}/factura
     * Subir factura de compra
     * ✅ VENDEDOR también puede subir facturas
     */
    @PostMapping("/{id}/factura")
    @PreAuthorize("hasAnyRole('ADMIN', 'ALMACENERO', 'VENDEDOR')")
    public ResponseEntity<String> subirFactura(
            @PathVariable Long id,
            @RequestParam("archivo") MultipartFile archivo
    ) throws IOException {
        compraService.subirFactura(id, archivo);
        return ResponseEntity.ok("Factura subida correctamente");
    }

    /**
     * PATCH /api/compras/{id}/estado
     * Actualizar estado de compra
     * ✅ ACTUALIZADO: Obtiene usuario autenticado del token
     * Solo ADMIN y ALMACENERO pueden cambiar estados
     */
    @PatchMapping("/{id}/estado")
    @PreAuthorize("hasAnyRole('ADMIN', 'ALMACENERO')")
    public ResponseEntity<CompraDTO.CompraResponse> actualizarEstado(
            @PathVariable Long id,
            @Valid @RequestBody CompraDTO.ActualizarEstadoCompraRequest request
    ) {
        Long usuarioId = securityUtils.getAuthenticatedUserId();
        return ResponseEntity.ok(compraService.actualizarEstado(id, request, usuarioId));
    }

    /**
     * DELETE /api/compras/{id}
     * Eliminar compra (solo PENDIENTE)
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> eliminar(@PathVariable Long id) {
        compraService.eliminar(id);
        return ResponseEntity.ok("Compra eliminada correctamente");
    }
}