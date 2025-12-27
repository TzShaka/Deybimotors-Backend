package com.deybimotors.service;

import com.deybimotors.entity.MovimientoKardex;
import com.deybimotors.entity.Producto;
import com.deybimotors.repository.MovimientoKardexRepository;
import com.deybimotors.repository.ProductoRepository;
import com.deybimotors.entity.Compatibilidad;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Servicio de Exportaciones - RF-016, RF-017, RF-038
 * ACTUALIZADO: Trabaja con stock en tabla productos
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ExportService {

    private final ProductoRepository productoRepository;
    private final MovimientoKardexRepository kardexRepository;

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    /**
     * Exportar lista de productos a Excel - RF-016
     */
    @Transactional(readOnly = true)
    public byte[] exportarProductosExcel(Long sedeId) throws IOException {

        List<Producto> productos = sedeId != null
                ? productoRepository.findBySedeId(sedeId)
                : productoRepository.findByEstadoTrue();

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Productos");

        // Estilos
        CellStyle headerStyle = workbook.createCellStyle();
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setColor(IndexedColors.WHITE.getIndex());
        headerStyle.setFont(headerFont);
        headerStyle.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        headerStyle.setAlignment(HorizontalAlignment.CENTER);

        // Estilo para precios
        CellStyle precioStyle = workbook.createCellStyle();
        precioStyle.setDataFormat(workbook.createDataFormat().getFormat("S/ #,##0.00"));

        // ✅ COLUMNAS COMPLETAS
        Row headerRow = sheet.createRow(0);
        String[] columnas = {
                "Código",
                "Código Marca",
                "Código Ref",
                "Código OEM",
                "Descripción",
                "Categoría",
                "Subcategoría",
                "Marca",
                "Marca Auto",
                "Modelo Auto",
                "Año",
                "Motor",
                "Origen",
                "Medida",
                "Diámetro",
                "Tipo",
                "Medida 2",
                "Stock",
                "Stock Mín",
                "Precio Costo",
                "Precio Venta",
                "Código Precio",
                "Sede"
        };

        for (int i = 0; i < columnas.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(columnas[i]);
            cell.setCellStyle(headerStyle);
        }

        // ✅ DATOS COMPLETOS
        int rowNum = 1;
        for (Producto producto : productos) {
            Row row = sheet.createRow(rowNum++);

            int colNum = 0;

            // Código
            row.createCell(colNum++).setCellValue(producto.getCodigoInterno());

            // Código Marca
            row.createCell(colNum++).setCellValue(
                    producto.getCodigoMarca() != null ? producto.getCodigoMarca() : ""
            );

            // Código Referencia
            row.createCell(colNum++).setCellValue(
                    producto.getCodigoReferencia() != null ? producto.getCodigoReferencia() : ""
            );

            // Código OEM
            String codigoOem = "";
            if (!producto.getCodigosOem().isEmpty()) {
                codigoOem = producto.getCodigosOem().get(0).getCodigoOem().getCodigoOem();
            }
            row.createCell(colNum++).setCellValue(codigoOem);

            // Descripción
            row.createCell(colNum++).setCellValue(producto.getDescripcion());

            // Categoría
            row.createCell(colNum++).setCellValue(producto.getCategoria().getNombre());

            // Subcategoría
            row.createCell(colNum++).setCellValue(
                    producto.getSubcategoria() != null ? producto.getSubcategoria().getNombre() : ""
            );

            // Marca
            row.createCell(colNum++).setCellValue(producto.getMarcaProducto().getNombre());

            // Marca Automóvil, Modelo, Año, Motor
            String marcaAuto = "";
            String modeloAuto = "";
            String anio = "";
            String motor = "";
            if (!producto.getCompatibilidades().isEmpty()) {
                Compatibilidad compat = producto.getCompatibilidades().get(0);
                if (compat.getMarcaAutomovil() != null) {
                    marcaAuto = compat.getMarcaAutomovil().getNombre();
                }
                if (compat.getModeloAutomovil() != null) {
                    modeloAuto = compat.getModeloAutomovil().getNombre();
                }
                anio = compat.getAnio() != null ? compat.getAnio() : "";
                motor = compat.getMotor() != null ? compat.getMotor() : "";
            }
            row.createCell(colNum++).setCellValue(marcaAuto);
            row.createCell(colNum++).setCellValue(modeloAuto);
            row.createCell(colNum++).setCellValue(anio);
            row.createCell(colNum++).setCellValue(motor);

            // Origen
            row.createCell(colNum++).setCellValue(
                    producto.getOrigen() != null ? producto.getOrigen().getPais() : ""
            );

            // Medida
            row.createCell(colNum++).setCellValue(
                    producto.getMedida() != null ? producto.getMedida() : ""
            );

            // Diámetro
            row.createCell(colNum++).setCellValue(
                    producto.getDiametro() != null ? producto.getDiametro() : ""
            );

            // Tipo
            row.createCell(colNum++).setCellValue(
                    producto.getTipo() != null ? producto.getTipo() : ""
            );

            // Medida 2
            row.createCell(colNum++).setCellValue(
                    producto.getMedida2() != null ? producto.getMedida2() : ""
            );

            // Stock
            row.createCell(colNum++).setCellValue(producto.getStock());

            // Stock Mínimo
            row.createCell(colNum++).setCellValue(producto.getStockMinimo());

            // Precio Costo
            Cell precioCostoCell = row.createCell(colNum++);
            precioCostoCell.setCellValue(
                    producto.getPrecioCosto() != null ? producto.getPrecioCosto().doubleValue() : 0
            );
            precioCostoCell.setCellStyle(precioStyle);

            // Precio Venta
            Cell precioVentaCell = row.createCell(colNum++);
            precioVentaCell.setCellValue(producto.getPrecioVenta().doubleValue());
            precioVentaCell.setCellStyle(precioStyle);

            // Código Precio
            row.createCell(colNum++).setCellValue(
                    producto.getCodigoPrecio() != null ? producto.getCodigoPrecio().getCodigo() : ""
            );

            // Sede
            row.createCell(colNum++).setCellValue(producto.getSede().getNombre());
        }

        // Ajustar columnas
        for (int i = 0; i < columnas.length; i++) {
            sheet.autoSizeColumn(i);
            // Asegurar ancho mínimo para columnas largas
            if (i == 4) { // Descripción
                sheet.setColumnWidth(i, 8000);
            }
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        workbook.write(baos);
        workbook.close();

        log.info("Productos exportados a Excel: {} registros con {} columnas", productos.size(), columnas.length);
        return baos.toByteArray();
    }

    /**
     * Exportar productos con stock bajo a Excel - RF-017
     */
    @Transactional(readOnly = true)
    public byte[] exportarProductosStockBajoExcel(Long sedeId) throws IOException {

        List<Producto> productos = productoRepository.findProductosStockBajoPorSede(sedeId);

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Productos Stock Bajo");

        // Estilos
        CellStyle headerStyle = workbook.createCellStyle();
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerStyle.setFont(headerFont);
        headerStyle.setFillForegroundColor(IndexedColors.RED.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        // Cabecera
        Row headerRow = sheet.createRow(0);
        String[] columnas = {
                "Código", "Descripción", "Categoría", "Stock Actual", "Stock Mínimo", "Diferencia"
        };

        for (int i = 0; i < columnas.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(columnas[i]);
            cell.setCellStyle(headerStyle);
        }

        // Datos
        int rowNum = 1;
        for (Producto producto : productos) {
            Row row = sheet.createRow(rowNum++);
            int diferencia = producto.getStockMinimo() - producto.getStock();

            row.createCell(0).setCellValue(producto.getCodigoInterno());
            row.createCell(1).setCellValue(producto.getDescripcion());
            row.createCell(2).setCellValue(producto.getCategoria().getNombre());
            row.createCell(3).setCellValue(producto.getStock());
            row.createCell(4).setCellValue(producto.getStockMinimo());
            row.createCell(5).setCellValue(diferencia);
        }

        for (int i = 0; i < columnas.length; i++) {
            sheet.autoSizeColumn(i);
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        workbook.write(baos);
        workbook.close();

        log.info("Productos con stock bajo exportados: {} registros", productos.size());
        return baos.toByteArray();
    }

    /**
     * Exportar kardex a PDF - RF-038
     */
    @Transactional(readOnly = true)
    public byte[] exportarKardexPDF(Long productoId, LocalDateTime fechaInicio, LocalDateTime fechaFin)
            throws IOException {

        List<MovimientoKardex> movimientos;

        if (productoId != null) {
            movimientos = (fechaInicio != null && fechaFin != null)
                    ? kardexRepository.findByProductoAndFechas(productoId, fechaInicio, fechaFin)
                    : kardexRepository.findByProductoIdOrderByFechaMovimientoDesc(productoId);
        } else {
            movimientos = (fechaInicio != null && fechaFin != null)
                    ? kardexRepository.findByFechaMovimientoBetween(fechaInicio, fechaFin)
                    : kardexRepository.findTop50ByOrderByFechaMovimientoDesc();
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(baos);
        PdfDocument pdfDoc = new PdfDocument(writer);
        Document document = new Document(pdfDoc, PageSize.A4.rotate());

        // Título
        Paragraph titulo = new Paragraph("REPORTE DE KARDEX")
                .setFontSize(16)
                .setBold()
                .setTextAlignment(TextAlignment.CENTER);
        document.add(titulo);

        // Subtítulo con fechas
        if (fechaInicio != null && fechaFin != null) {
            Paragraph subtitulo = new Paragraph(
                    "Periodo: " + fechaInicio.format(DATE_FORMATTER)
                            + " - " + fechaFin.format(DATE_FORMATTER)
            ).setFontSize(10).setTextAlignment(TextAlignment.CENTER);
            document.add(subtitulo);
        }

        document.add(new Paragraph("\n"));

        // Tabla
        float[] columnWidths = {8, 15, 12, 12, 8, 8, 8, 15};
        Table table = new Table(UnitValue.createPercentArray(columnWidths));
        table.setWidth(UnitValue.createPercentValue(100));

        String[] headers = {
                "Fecha", "Producto", "Sede", "Tipo", "Cant.", "Stock Ant.", "Stock Nvo.", "Usuario"
        };

        for (String header : headers) {
            com.itextpdf.layout.element.Cell cell =
                    new com.itextpdf.layout.element.Cell()
                            .add(new Paragraph(header).setBold())
                            .setBackgroundColor(ColorConstants.LIGHT_GRAY)
                            .setTextAlignment(TextAlignment.CENTER)
                            .setFontSize(8);
            table.addHeaderCell(cell);
        }

        for (MovimientoKardex m : movimientos) {
            table.addCell(new com.itextpdf.layout.element.Cell()
                    .add(new Paragraph(m.getFechaMovimiento().format(DATE_FORMATTER))).setFontSize(7));
            table.addCell(new com.itextpdf.layout.element.Cell()
                    .add(new Paragraph(m.getProducto().getDescripcion())).setFontSize(7));
            table.addCell(new com.itextpdf.layout.element.Cell()
                    .add(new Paragraph(m.getSede().getNombre())).setFontSize(7));
            table.addCell(new com.itextpdf.layout.element.Cell()
                    .add(new Paragraph(traducirTipo(m.getTipoMovimiento()))).setFontSize(7));
            table.addCell(new com.itextpdf.layout.element.Cell()
                    .add(new Paragraph(String.valueOf(m.getCantidad()))).setFontSize(7).setTextAlignment(TextAlignment.CENTER));
            table.addCell(new com.itextpdf.layout.element.Cell()
                    .add(new Paragraph(String.valueOf(m.getStockAnterior()))).setFontSize(7).setTextAlignment(TextAlignment.CENTER));
            table.addCell(new com.itextpdf.layout.element.Cell()
                    .add(new Paragraph(String.valueOf(m.getStockActual()))).setFontSize(7).setTextAlignment(TextAlignment.CENTER));
            table.addCell(new com.itextpdf.layout.element.Cell()
                    .add(new Paragraph(
                            m.getUsuarioResponsable() != null
                                    ? m.getUsuarioResponsable().getNombreCompleto()
                                    : "Sistema"
                    )).setFontSize(7));
        }

        document.add(table);
        document.close();

        log.info("Kardex exportado a PDF: {} movimientos", movimientos.size());
        return baos.toByteArray();
    }

    private String traducirTipo(String tipo) {
        return switch (tipo) {
            case "ENTRADA_COMPRA" -> "Entrada Compra";
            case "SALIDA_VENTA" -> "Salida Venta";
            case "AJUSTE_POSITIVO" -> "Ajuste +";
            case "AJUSTE_NEGATIVO" -> "Ajuste -";
            case "TRASLADO_ENTRADA" -> "Traslado Entrada";
            case "TRASLADO_SALIDA" -> "Traslado Salida";
            case "DEVOLUCION" -> "Devolución";
            case "MERMA" -> "Merma";
            default -> tipo;
        };
    }
}
