package com.deybimotors.controller;

import com.deybimotors.service.EtiquetaService;
import com.google.zxing.WriterException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

/**
 * Controlador de Etiquetas - RF-020, RF-024, RF-031
 * Endpoints: /api/etiquetas/**
 */
@RestController
@RequestMapping("/api/etiquetas")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@PreAuthorize("hasAnyRole('ADMIN', 'ALMACENERO')")
public class EtiquetaController {

    private final EtiquetaService etiquetaService;

    /**
     * POST /api/etiquetas/generar
     * Generar etiquetas con códigos de barras para productos seleccionados - RF-020, RF-024
     */
    @PostMapping("/generar")
    public ResponseEntity<byte[]> generarEtiquetas(
            @RequestBody GenerarEtiquetasRequest request
    ) throws IOException, WriterException {

        byte[] pdfBytes = etiquetaService.generarEtiquetas(
                request.getProductosIds(),
                request.getCantidadPorProducto()
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "etiquetas.pdf");

        return ResponseEntity.ok()
                .headers(headers)
                .body(pdfBytes);
    }

    /**
     * POST /api/etiquetas/compra/{compraId}
     * Generar etiquetas después de una compra - RF-031
     */
    @PostMapping("/compra/{compraId}")
    public ResponseEntity<byte[]> generarEtiquetasCompra(@PathVariable Long compraId)
            throws IOException, WriterException {

        byte[] pdfBytes = etiquetaService.generarEtiquetasCompra(compraId);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "etiquetas-compra-" + compraId + ".pdf");

        return ResponseEntity.ok()
                .headers(headers)
                .body(pdfBytes);
    }

    // DTO para request
    public static class GenerarEtiquetasRequest {
        private List<Long> productosIds;
        private int cantidadPorProducto = 1;

        public List<Long> getProductosIds() {
            return productosIds;
        }

        public void setProductosIds(List<Long> productosIds) {
            this.productosIds = productosIds;
        }

        public int getCantidadPorProducto() {
            return cantidadPorProducto;
        }

        public void setCantidadPorProducto(int cantidadPorProducto) {
            this.cantidadPorProducto = cantidadPorProducto;
        }
    }
}