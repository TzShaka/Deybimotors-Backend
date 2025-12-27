package com.deybimotors.service;

import com.deybimotors.entity.Compatibilidad;
import com.deybimotors.entity.Producto;
import com.deybimotors.exception.ResourceNotFoundException;
import com.deybimotors.repository.ProductoRepository;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.oned.Code128Writer;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.itextpdf.io.image.ImageDataFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * Servicio de Etiquetas - RF-020, RF-024, RF-031
 * ✅ CORREGIDO COMPLETO: Sin referencias a campos inexistentes
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EtiquetaService {

    private final ProductoRepository productoRepository;

    /**
     * Generar etiquetas para productos - RF-020, RF-024
     */
    @Transactional(readOnly = true)
    public byte[] generarEtiquetas(List<Long> productosIds, int cantidadPorProducto) throws IOException, WriterException {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(baos);
        PdfDocument pdfDoc = new PdfDocument(writer);
        Document document = new Document(pdfDoc, PageSize.A4);

        document.setMargins(20, 20, 20, 20);

        Table table = new Table(UnitValue.createPercentArray(new float[]{33.33f, 33.33f, 33.33f}));
        table.setWidth(UnitValue.createPercentValue(100));

        int etiquetasGeneradas = 0;

        for (Long productoId : productosIds) {

            Producto producto = productoRepository.findById(productoId)
                    .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado: ID " + productoId));

            for (int i = 0; i < cantidadPorProducto; i++) {

                byte[] codigoBarras = generarCodigoBarras(producto.getCodigo());
                Table etiqueta = crearEtiqueta(producto, codigoBarras);
                table.addCell(etiqueta);

                etiquetasGeneradas++;

                if (etiquetasGeneradas == productosIds.size() * cantidadPorProducto) {
                    int celdasFaltantes = 3 - (etiquetasGeneradas % 3);
                    if (celdasFaltantes < 3) {
                        for (int j = 0; j < celdasFaltantes; j++) {
                            table.addCell("");
                        }
                    }
                }
            }
        }

        document.add(table);
        document.close();

        log.info("Etiquetas generadas: {} etiquetas para {} productos", etiquetasGeneradas, productosIds.size());

        return baos.toByteArray();
    }

    /**
     * Crear una etiqueta individual
     */
    private Table crearEtiqueta(Producto producto, byte[] codigoBarras) throws IOException {

        Table etiqueta = new Table(1);
        etiqueta.setWidth(UnitValue.createPercentValue(100));
        etiqueta.setPadding(5);

        // Nombre del producto
        String nombreCorto = producto.getNombre().length() > 40
                ? producto.getNombre().substring(0, 37) + "..."
                : producto.getNombre();

        Paragraph nombre = new Paragraph(nombreCorto)
                .setFontSize(8)
                .setTextAlignment(TextAlignment.CENTER)
                .setBold();

        // Código
        Paragraph codigo = new Paragraph("Código: " + producto.getCodigo())
                .setFontSize(7)
                .setTextAlignment(TextAlignment.CENTER);

        // ✅ CORREGIDO: Marca/Modelo desde compatibilidades
        String marcaModelo = "";
        if (producto.getCompatibilidades() != null && !producto.getCompatibilidades().isEmpty()) {
            Compatibilidad compat = producto.getCompatibilidades().get(0);
            if (compat.getMarcaAutomovil() != null) {
                marcaModelo = compat.getMarcaAutomovil().getNombre();
                if (compat.getModeloAutomovil() != null) {
                    marcaModelo += " " + compat.getModeloAutomovil().getNombre();
                }
            }
        }

        Paragraph info = new Paragraph(marcaModelo)
                .setFontSize(6)
                .setTextAlignment(TextAlignment.CENTER);

        // Precio
        Paragraph precio = new Paragraph("S/ " + producto.getPrecioVenta())
                .setFontSize(10)
                .setTextAlignment(TextAlignment.CENTER)
                .setBold();

        // Código de barras
        Image imagenBarras = new Image(ImageDataFactory.create(codigoBarras));
        imagenBarras.setWidth(UnitValue.createPercentValue(90));
        imagenBarras.setHorizontalAlignment(com.itextpdf.layout.properties.HorizontalAlignment.CENTER);

        // Agregar todo a la etiqueta
        etiqueta.addCell(nombre);
        etiqueta.addCell(codigo);
        if (!marcaModelo.isEmpty()) {
            etiqueta.addCell(info);
        }
        etiqueta.addCell(imagenBarras);
        etiqueta.addCell(precio);

        return etiqueta;
    }

    /**
     * Generar código de barras
     */
    private byte[] generarCodigoBarras(String codigo) throws WriterException, IOException {

        Code128Writer barcodeWriter = new Code128Writer();
        BitMatrix bitMatrix = barcodeWriter.encode(codigo, BarcodeFormat.CODE_128, 300, 80);
        BufferedImage bufferedImage = MatrixToImageWriter.toBufferedImage(bitMatrix);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(bufferedImage, "png", baos);

        return baos.toByteArray();
    }

    /**
     * Generar etiquetas después de una compra - RF-031
     */
    @Transactional(readOnly = true)
    public byte[] generarEtiquetasCompra(Long compraId) throws IOException, WriterException {

        List<Long> productosIds = productoRepository.findAll().stream()
                .map(Producto::getId)
                .toList();

        return generarEtiquetas(productosIds, 1);
    }
}