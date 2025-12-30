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
 * Servicio de Importación - ✅ ACTUALIZADO
 * ❌ SIN stockMinimo en importación
 * Formato Excel: CODIGO | NOMBRE | CATEGORIA | SUBCATEGORIA | MARCA | PRECIO_VENTA | SEDE
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

    @Transactional
    public ImportResult importarProductosDesdeExcel(MultipartFile archivo) throws IOException {

        ImportResult resultado = new ImportResult();

        Workbook workbook = new XSSFWorkbook(archivo.getInputStream());
        Sheet sheet = workbook.getSheetAt(0);

        int filaActual = 1;

        for (Row row : sheet) {

            if (row.getRowNum() == 0) continue;

            try {

                String codigo = getCellValueAsString(row.getCell(0));
                String nombre = getCellValueAsString(row.getCell(1));
                String categoriaNombre = getCellValueAsString(row.getCell(2));
                String subcategoriaNombre = getCellValueAsString(row.getCell(3));
                String marcaNombre = getCellValueAsString(row.getCell(4));
                String precioVentaStr = getCellValueAsString(row.getCell(5));
                String sedeNombre = getCellValueAsString(row.getCell(6));

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

                if (productoRepository.existsByCodigo(codigo)) {
                    resultado.agregarError(filaActual, "El código " + codigo + " ya existe");
                    filaActual++;
                    continue;
                }

                Categoria categoria = categoriaRepository.findByNombre(categoriaNombre)
                        .orElseGet(() -> {
                            Categoria nuevaCategoria = new Categoria();
                            nuevaCategoria.setNombre(categoriaNombre);
                            nuevaCategoria.setActivo(true);
                            return categoriaRepository.save(nuevaCategoria);
                        });

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

                Marca marca = marcaRepository.findByNombre(marcaNombre)
                        .orElseGet(() -> {
                            Marca nuevaMarca = new Marca();
                            nuevaMarca.setNombre(marcaNombre);
                            nuevaMarca.setActivo(true);
                            return marcaRepository.save(nuevaMarca);
                        });

                Sede sede = sedeRepository.findByNombre(sedeNombre != null ? sedeNombre : "Sede Principal")
                        .orElseGet(() -> sedeRepository.findAll().stream()
                                .findFirst()
                                .orElseThrow(() -> new BadRequestException("No existe ninguna sede")));

                Producto producto = new Producto();
                producto.setCodigoInterno(codigo);
                producto.setDescripcion(nombre);
                producto.setCategoria(categoria);
                producto.setSubcategoria(subcategoria);
                producto.setMarcaProducto(marca);
                producto.setSede(sede);

                try {
                    producto.setPrecioVenta(new BigDecimal(precioVentaStr != null ? precioVentaStr : "0"));
                } catch (Exception e) {
                    producto.setPrecioVenta(BigDecimal.ZERO);
                }

                producto.setStock(0);
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