package com.deybimotors.controller;

import com.deybimotors.service.ImportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Controlador de Importaciones - RF-028
 * Endpoints: /api/importar/**
 */
@RestController
@RequestMapping("/api/importar")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@PreAuthorize("hasAnyRole('ADMIN', 'ALMACENERO')")
public class ImportController {

    private final ImportService importService;

    /**
     * POST /api/importar/productos/excel
     * Importar productos desde archivo Excel - RF-028
     */
    @PostMapping("/productos/excel")
    public ResponseEntity<Map<String, Object>> importarProductosExcel(
            @RequestParam("archivo") MultipartFile archivo
    ) throws IOException {

        // Validar tipo de archivo
        if (!archivo.getOriginalFilename().endsWith(".xlsx") &&
                !archivo.getOriginalFilename().endsWith(".xls")) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "El archivo debe ser un Excel (.xlsx o .xls)"));
        }

        // Importar productos
        ImportService.ImportResult resultado = importService.importarProductosDesdeExcel(archivo);

        // Construir respuesta
        Map<String, Object> response = new HashMap<>();
        response.put("productosImportados", resultado.getProductosImportados());
        response.put("totalErrores", resultado.getErrores().size());
        response.put("errores", resultado.getErrores());

        if (resultado.tieneErrores()) {
            response.put("mensaje",
                    String.format("Importación completada con errores. %d productos importados, %d errores",
                            resultado.getProductosImportados(), resultado.getErrores().size()));
            return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT).body(response);
        }

        response.put("mensaje",
                String.format("Importación exitosa. %d productos importados",
                        resultado.getProductosImportados()));
        return ResponseEntity.ok(response);
    }
}