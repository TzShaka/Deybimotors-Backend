package com.deybimotors.controller;

import com.deybimotors.dto.KardexDTO;
import com.deybimotors.service.KardexService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Controlador de Kardex - RF-033 a RF-039
 * ✅ ACTUALIZADO: VENDEDOR también puede ver el kardex
 */
@RestController
@RequestMapping("/api/kardex")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@PreAuthorize("hasAnyRole('ADMIN', 'ALMACENERO', 'VENDEDOR')")
public class KardexController {

    private final KardexService kardexService;

    /**
     * GET /api/kardex
     * Kardex general - últimos movimientos
     */
    @GetMapping
    public ResponseEntity<List<KardexDTO.MovimientoResponse>> listarTodos() {
        return ResponseEntity.ok(kardexService.listarTodos());
    }

    /**
     * GET /api/kardex/producto/{productoId}
     * Kardex por producto
     */
    @GetMapping("/producto/{productoId}")
    public ResponseEntity<List<KardexDTO.MovimientoResponse>> listarPorProducto(@PathVariable Long productoId) {
        return ResponseEntity.ok(kardexService.listarPorProducto(productoId));
    }

    /**
     * GET /api/kardex/sede/{sedeId}
     * Kardex por sede
     */
    @GetMapping("/sede/{sedeId}")
    public ResponseEntity<List<KardexDTO.MovimientoResponse>> listarPorSede(@PathVariable Long sedeId) {
        return ResponseEntity.ok(kardexService.listarPorSede(sedeId));
    }

    /**
     * GET /api/kardex/tipo/{tipo}
     * Kardex por tipo de movimiento
     */
    @GetMapping("/tipo/{tipo}")
    public ResponseEntity<List<KardexDTO.MovimientoResponse>> listarPorTipo(@PathVariable String tipo) {
        return ResponseEntity.ok(kardexService.listarPorTipo(tipo));
    }

    /**
     * GET /api/kardex/fechas
     * Kardex por rango de fechas
     */
    @GetMapping("/fechas")
    public ResponseEntity<List<KardexDTO.MovimientoResponse>> listarPorFechas(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaFin
    ) {
        return ResponseEntity.ok(kardexService.listarPorFechas(fechaInicio, fechaFin));
    }

    /**
     * GET /api/kardex/producto/{productoId}/fechas
     * Kardex filtrado por producto y fechas
     */
    @GetMapping("/producto/{productoId}/fechas")
    public ResponseEntity<List<KardexDTO.MovimientoResponse>> listarPorProductoYFechas(
            @PathVariable Long productoId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaFin
    ) {
        return ResponseEntity.ok(kardexService.listarPorProductoYFechas(productoId, fechaInicio, fechaFin));
    }

    /**
     * GET /api/kardex/usuario/{usuarioId}
     * Kardex por usuario responsable
     */
    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<List<KardexDTO.MovimientoResponse>> listarPorUsuario(@PathVariable Long usuarioId) {
        return ResponseEntity.ok(kardexService.listarPorUsuario(usuarioId));
    }
}