package com.deybimotors.service;

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
 * Generación de etiquetas con códigos de barras
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EtiquetaService {

    private final ProductoRepository productoRepository;

    /**
     * Generar etiquetas para productos - RF-020, RF-024
     * Genera un PDF con etiquetas que incluyen código de barras
     */
    @Transactional(readOnly = true)
    public byte[] generarEtiquetas(List<Long> productosIds, int cantidadPorProducto) throws IOException, WriterException {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(baos);
        PdfDocument pdfDoc = new PdfDocument(writer);
        Document document = new Document(pdfDoc, PageSize.A4);

        // Configurar márgenes
        document.setMargins(20, 20, 20, 20);

        // Crear tabla de 3 columnas para etiquetas
        Table table = new Table(UnitValue.createPercentArray(new float[]{33.33f, 33.33f, 33.33f}));
        table.setWidth(UnitValue.createPercentValue(100));

        int etiquetasGeneradas = 0;

        for (Long productoId : productosIds) {

            Producto producto = productoRepository.findById(productoId)
                    .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado: ID " + productoId));

            // Generar múltiples etiquetas según cantidad solicitada
            for (int i = 0; i < cantidadPorProducto; i++) {

                // Generar código de barras
                byte[] codigoBarras = generarCodigoBarras(producto.getCodigo());

                // Crear celda de etiqueta
                Table etiqueta = crearEtiqueta(producto, codigoBarras);
                table.addCell(etiqueta);

                etiquetasGeneradas++;

                // Completar fila con celdas vacías si es necesario
                if (etiquetasGeneradas % 3 == 0) {
                    // Fila completa, no hacer nada
                } else if (etiquetasGeneradas == productosIds.size() * cantidadPorProducto) {
                    // Última etiqueta, completar fila
                    int celdasFaltantes = 3 - (etiquetasGeneradas % 3);
                    for (int j = 0; j < celdasFaltantes; j++) {
                        table.addCell("");
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

        // Nombre del producto (truncado si es muy largo)
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

        // Marca y modelo
        String marcaModelo = producto.getMarcaAutomovil() != null
                ? producto.getMarcaAutomovil() + " " + (producto.getModeloAutomovil() != null ? producto.getModeloAutomovil() : "")
                : "";

        Paragraph info = new Paragraph(marcaModelo)
                .setFontSize(6)
                .setTextAlignment(TextAlignment.CENTER);

        // Precio
        Paragraph precio = new Paragraph("S/ " + producto.getPrecioVenta())
                .setFontSize(10)
                .setTextAlignment(TextAlignment.CENTER)
                .setBold();

        // Imagen del código de barras
        Image imagenBarras = new Image(ImageDataFactory.create(codigoBarras));
        imagenBarras.setWidth(UnitValue.createPercentValue(90));
        imagenBarras.setHorizontalAlignment(com.itextpdf.layout.properties.HorizontalAlignment.CENTER);

        // Agregar elementos a la etiqueta
        etiqueta.addCell(nombre);
        etiqueta.addCell(codigo);
        etiqueta.addCell(info);
        etiqueta.addCell(imagenBarras);
        etiqueta.addCell(precio);

        return etiqueta;
    }

    /**
     * Generar código de barras usando Code128
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

        // Obtener productos de la compra
        List<Long> productosIds = productoRepository.findAll().stream()
                .map(Producto::getId)
                .toList();

        // Generar 1 etiqueta por cada producto
        return generarEtiquetas(productosIds, 1);
    }
}