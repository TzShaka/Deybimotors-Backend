package com.deybimotors.controller;

import com.deybimotors.dto.StockDTO;
import com.deybimotors.security.SecurityUtils;
import com.deybimotors.service.StockService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador de Stock - RF-011, RF-018, RF-019
 * ✅ ACTUALIZADO: Usa SecurityUtils para obtener usuario autenticado
 */
@RestController
@RequestMapping("/api/stock")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@PreAuthorize("hasAnyRole('ADMIN', 'ALMACENERO')")
public class StockController {

    private final StockService stockService;
    private final SecurityUtils securityUtils;

    /**
     * GET /api/stock/producto/{productoId}
     * Obtener stock de un producto en todas las sedes
     */
    @GetMapping("/producto/{productoId}")
    public ResponseEntity<List<StockDTO.StockResponse>> obtenerStockProducto(@PathVariable Long productoId) {
        return ResponseEntity.ok(stockService.obtenerStockProducto(productoId));
    }

    /**
     * GET /api/stock/sede/{sedeId}
     * Obtener todo el stock de una sede
     */
    @GetMapping("/sede/{sedeId}")
    public ResponseEntity<List<StockDTO.StockResponse>> obtenerStockSede(@PathVariable Long sedeId) {
        return ResponseEntity.ok(stockService.obtenerStockSede(sedeId));
    }

    /**
     * GET /api/stock/sede/{sedeId}/sin-stock
     * Obtener productos sin stock en una sede
     */
    @GetMapping("/sede/{sedeId}/sin-stock")
    public ResponseEntity<List<StockDTO.StockResponse>> obtenerProductosSinStock(@PathVariable Long sedeId) {
        return ResponseEntity.ok(stockService.obtenerProductosSinStock(sedeId));
    }

    /**
     * GET /api/stock/sede/{sedeId}/stock-bajo
     * Obtener productos con stock bajo en una sede
     */
    @GetMapping("/sede/{sedeId}/stock-bajo")
    public ResponseEntity<List<StockDTO.StockResponse>> obtenerProductosStockBajo(@PathVariable Long sedeId) {
        return ResponseEntity.ok(stockService.obtenerProductosStockBajo(sedeId));
    }

    /**
     * POST /api/stock/ajustar
     * Ajustar stock manualmente
     * ✅ ACTUALIZADO: Obtiene usuario autenticado del token
     */
    @PostMapping("/ajustar")
    public ResponseEntity<StockDTO.StockResponse> ajustarStock(
            @Valid @RequestBody StockDTO.AjusteStockRequest request
    ) {
        Long usuarioId = securityUtils.getAuthenticatedUserId();
        return ResponseEntity.ok(stockService.ajustarStock(request, usuarioId));
    }

    /**
     * POST /api/stock/salida
     * Registrar salida de productos (carrito)
     * ✅ ACTUALIZADO: Obtiene usuario autenticado del token
     */
    @PostMapping("/salida")
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDEDOR', 'ALMACENERO')") // ✅ VENDEDOR también puede hacer ventas
    public ResponseEntity<String> registrarSalida(
            @Valid @RequestBody StockDTO.ConfirmarSalidaRequest request
    ) {
        Long usuarioId = securityUtils.getAuthenticatedUserId();
        stockService.registrarSalida(request, usuarioId);
        return ResponseEntity.ok("Salida registrada correctamente");
    }
}