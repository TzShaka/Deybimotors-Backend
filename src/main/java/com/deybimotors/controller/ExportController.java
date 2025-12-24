package com.deybimotors.controller;

import com.deybimotors.service.ExportService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDateTime;

/**
 * Controlador de Exportaciones - RF-016, RF-017, RF-038
 * Endpoints: /api/exportar/**
 */
@RestController
@RequestMapping("/api/exportar")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@PreAuthorize("hasAnyRole('ADMIN', 'ALMACENERO')")
public class ExportController {

    private final ExportService exportService;

    /**
     * GET /api/exportar/productos/excel
     * Exportar lista de productos a Excel - RF-016
     */
    @GetMapping("/productos/excel")
    public ResponseEntity<byte[]> exportarProductosExcel(
            @RequestParam(required = false) Long sedeId
    ) throws IOException {

        byte[] excelBytes = exportService.exportarProductosExcel(sedeId);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", "productos.xlsx");

        return ResponseEntity.ok()
                .headers(headers)
                .body(excelBytes);
    }

    /**
     * GET /api/exportar/productos/stock-bajo/excel
     * Exportar productos con stock bajo a Excel - RF-017
     */
    @GetMapping("/productos/stock-bajo/excel")
    public ResponseEntity<byte[]> exportarProductosStockBajoExcel(
            @RequestParam Long sedeId
    ) throws IOException {

        byte[] excelBytes = exportService.exportarProductosStockBajoExcel(sedeId);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", "productos-stock-bajo.xlsx");

        return ResponseEntity.ok()
                .headers(headers)
                .body(excelBytes);
    }

    /**
     * GET /api/exportar/kardex/pdf
     * Exportar kardex a PDF - RF-038
     */
    @GetMapping("/kardex/pdf")
    public ResponseEntity<byte[]> exportarKardexPDF(
            @RequestParam(required = false) Long productoId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaFin
    ) throws IOException {

        byte[] pdfBytes = exportService.exportarKardexPDF(productoId, fechaInicio, fechaFin);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "kardex.pdf");

        return ResponseEntity.ok()
                .headers(headers)
                .body(pdfBytes);
    }
}