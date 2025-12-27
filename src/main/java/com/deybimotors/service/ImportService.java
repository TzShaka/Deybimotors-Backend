package com.deybimotors.service;

import com.deybimotors.entity.*;
import com.deybimotors.exception.BadRequestException;
import com.deybimotors.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Servicio de Importación - RF-028
 * ✅ CORREGIDO: Eliminadas referencias a campos inexistentes
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ImportService {

    private final ProductoRepository productoRepository;
    private final CategoriaRepository categoriaRepository;
    private final SubcategoriaRepository subcategoriaRepository;
    private final MarcaRepository marcaRepository;
    private final SedeRepository sedeRepository;

    /**
     * Importar productos desde Excel - RF-028
     * Formato esperado del Excel:
     * CODIGO | NOMBRE | CATEGORIA | SUBCATEGORIA | MARCA | PRECIO_VENTA | STOCK_MINIMO | SEDE
     */
    @Transactional
    public ImportResult importarProductosDesdeExcel(MultipartFile archivo) throws IOException {

        ImportResult resultado = new ImportResult();

        Workbook workbook = new XSSFWorkbook(archivo.getInputStream());
        Sheet sheet = workbook.getSheetAt(0);

        // Saltar la primera fila (cabecera)
        int filaActual = 1;

        for (Row row : sheet) {

            if (row.getRowNum() == 0) continue; // Saltar cabecera

            try {

                // Leer datos de la fila
                String codigo = getCellValueAsString(row.getCell(0));
                String nombre = getCellValueAsString(row.getCell(1));
                String categoriaNombre = getCellValueAsString(row.getCell(2));
                String subcategoriaNombre = getCellValueAsString(row.getCell(3));
                String marcaNombre = getCellValueAsString(row.getCell(4));
                String precioVentaStr = getCellValueAsString(row.getCell(5));
                String stockMinimoStr = getCellValueAsString(row.getCell(6));
                String sedeNombre = getCellValueAsString(row.getCell(7));

                // Validaciones básicas
                if (codigo == null || codigo.isEmpty()) {
                    resultado.agregarError(filaActual, "Código es obligatorio");
                    filaActual++;
                    continue;
                }

                if (nombre == null || nombre.isEmpty()) {
                    resultado.agregarError(filaActual, "Nombre es obligatorio");
                    filaActual++;
                    continue;
                }

                // Verificar si el producto ya existe
                if (productoRepository.existsByCodigo(codigo)) {
                    resultado.agregarError(filaActual, "El código " + codigo + " ya existe");
                    filaActual++;
                    continue;
                }

                // Buscar o crear categoría
                Categoria categoria = categoriaRepository.findByNombre(categoriaNombre)
                        .orElseGet(() -> {
                            Categoria nuevaCategoria = new Categoria();
                            nuevaCategoria.setNombre(categoriaNombre);
                            nuevaCategoria.setActivo(true);
                            return categoriaRepository.save(nuevaCategoria);
                        });

                // Buscar o crear subcategoría
                Subcategoria subcategoria = null;
                if (subcategoriaNombre != null && !subcategoriaNombre.isEmpty()) {
                    subcategoria = subcategoriaRepository.findByNombreAndCategoriaId(subcategoriaNombre, categoria.getId())
                            .orElseGet(() -> {
                                Subcategoria nuevaSubcategoria = new Subcategoria();
                                nuevaSubcategoria.setNombre(subcategoriaNombre);
                                nuevaSubcategoria.setCategoria(categoria);
                                nuevaSubcategoria.setActivo(true);
                                return subcategoriaRepository.save(nuevaSubcategoria);
                            });
                }

                // Buscar o crear marca
                Marca marca = marcaRepository.findByNombre(marcaNombre)
                        .orElseGet(() -> {
                            Marca nuevaMarca = new Marca();
                            nuevaMarca.setNombre(marcaNombre);
                            nuevaMarca.setActivo(true);
                            return marcaRepository.save(nuevaMarca);
                        });

                // Buscar sede
                Sede sede = sedeRepository.findByNombre(sedeNombre != null ? sedeNombre : "Sede Principal")
                        .orElseGet(() -> sedeRepository.findAll().stream()
                                .findFirst()
                                .orElseThrow(() -> new BadRequestException("No existe ninguna sede")));

                // Crear producto
                Producto producto = new Producto();
                producto.setCodigoInterno(codigo);
                producto.setDescripcion(nombre);
                producto.setCategoria(categoria);
                producto.setSubcategoria(subcategoria);
                producto.setMarcaProducto(marca);
                producto.setSede(sede);

                // Precio de venta
                try {
                    producto.setPrecioVenta(new BigDecimal(precioVentaStr != null ? precioVentaStr : "0"));
                } catch (Exception e) {
                    producto.setPrecioVenta(BigDecimal.ZERO);
                }

                // Stock mínimo
                try {
                    producto.setStockMinimo(Integer.parseInt(stockMinimoStr != null ? stockMinimoStr : "0"));
                } catch (Exception e) {
                    producto.setStockMinimo(0);
                }

                // Stock inicial
                producto.setStock(0);

                // Estado
                producto.setEstado(true);

                productoRepository.save(producto);
                resultado.incrementarExitosos();

            } catch (Exception e) {
                resultado.agregarError(filaActual, "Error al procesar: " + e.getMessage());
                log.error("Error en fila {}: {}", filaActual, e.getMessage());
            }

            filaActual++;
        }

        workbook.close();

        log.info("Importación completada: {} exitosos, {} errores",
                resultado.getProductosImportados(), resultado.getErrores().size());

        return resultado;
    }

    /**
     * Obtener valor de celda como String
     */
    private String getCellValueAsString(Cell cell) {
        if (cell == null) return null;

        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue().trim();
            case NUMERIC -> String.valueOf((int) cell.getNumericCellValue());
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            case FORMULA -> cell.getCellFormula();
            default -> null;
        };
    }

    /**
     * Clase para resultado de importación
     */
    public static class ImportResult {
        private int productosImportados = 0;
        private final List<String> errores = new ArrayList<>();

        public void incrementarExitosos() {
            this.productosImportados++;
        }

        public void agregarError(int fila, String mensaje) {
            this.errores.add("Fila " + fila + ": " + mensaje);
        }

        public int getProductosImportados() {
            return productosImportados;
        }

        public List<String> getErrores() {
            return errores;
        }

        public boolean tieneErrores() {
            return !errores.isEmpty();
        }
    }
}