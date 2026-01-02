package com.deybimotors.service;

import com.deybimotors.exception.BadRequestException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

/**
 * Servicio para gestión de archivos
 * Guarda archivos en el servidor y retorna la ruta
 */
@Service
@Slf4j
public class FileStorageService {

    @Value("${file.upload-dir}")
    private String imageUploadDir;

    @Value("${file.documents-dir}")
    private String documentsUploadDir;

    // Extensiones permitidas para documentos
    private static final String[] ALLOWED_EXTENSIONS = {
            "pdf", "jpg", "jpeg", "png", "doc", "docx", "xls", "xlsx"
    };

    // Tamaño máximo: 10MB
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;

    /**
     * Guardar documento (facturas, comprobantes, etc.)
     */
    public String guardarDocumento(MultipartFile archivo, String subdirectorio) throws IOException {

        // Validar archivo
        validarArchivo(archivo);

        // Crear directorio si no existe
        Path uploadPath = Paths.get(documentsUploadDir, subdirectorio);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
            log.info("✅ Directorio creado: {}", uploadPath);
        }

        // Generar nombre único para el archivo
        String nombreOriginal = StringUtils.cleanPath(archivo.getOriginalFilename());
        String extension = obtenerExtension(nombreOriginal);
        String nombreUnico = UUID.randomUUID().toString() + "." + extension;

        // Guardar archivo
        Path rutaDestino = uploadPath.resolve(nombreUnico);
        Files.copy(archivo.getInputStream(), rutaDestino, StandardCopyOption.REPLACE_EXISTING);

        // ✅ CORRECCIÓN: Retornar ruta completa con prefijo
        String rutaRelativa = "uploads/documents/" + subdirectorio + "/" + nombreUnico;

        log.info("✅ Archivo guardado: {}", rutaRelativa);
        return rutaRelativa;
    }

    /**
     * Guardar imagen de producto
     */
    public String guardarImagen(MultipartFile archivo, String subdirectorio) throws IOException {

        // Validar que sea imagen
        validarImagen(archivo);

        // Crear directorio si no existe
        Path uploadPath = Paths.get(imageUploadDir, subdirectorio);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
            log.info("✅ Directorio creado: {}", uploadPath);
        }

        // Generar nombre único
        String nombreOriginal = StringUtils.cleanPath(archivo.getOriginalFilename());
        String extension = obtenerExtension(nombreOriginal);
        String nombreUnico = UUID.randomUUID().toString() + "." + extension;

        // Guardar archivo
        Path rutaDestino = uploadPath.resolve(nombreUnico);
        Files.copy(archivo.getInputStream(), rutaDestino, StandardCopyOption.REPLACE_EXISTING);

        // ✅ CORRECCIÓN: Retornar ruta completa con prefijo
        String rutaRelativa = "uploads/images/" + subdirectorio + "/" + nombreUnico;

        log.info("✅ Imagen guardada: {}", rutaRelativa);
        return rutaRelativa;
    }

    /**
     * Eliminar archivo
     */
    public void eliminarArchivo(String rutaRelativa) {
        try {
            // ✅ CORRECCIÓN: Extraer la ruta sin el prefijo "uploads/documents/" o "uploads/images/"
            String rutaSinPrefijo = rutaRelativa
                    .replace("uploads/documents/", "")
                    .replace("uploads/images/", "");

            // Intentar eliminar de documentos
            Path rutaDocumento = Paths.get(documentsUploadDir, rutaSinPrefijo);
            if (Files.exists(rutaDocumento)) {
                Files.delete(rutaDocumento);
                log.info("✅ Archivo eliminado: {}", rutaRelativa);
                return;
            }

            // Intentar eliminar de imágenes
            Path rutaImagen = Paths.get(imageUploadDir, rutaSinPrefijo);
            if (Files.exists(rutaImagen)) {
                Files.delete(rutaImagen);
                log.info("✅ Imagen eliminada: {}", rutaRelativa);
                return;
            }

            log.warn("⚠️ Archivo no encontrado: {}", rutaRelativa);

        } catch (IOException e) {
            log.error("❌ Error al eliminar archivo: {}", e.getMessage());
        }
    }

    /**
     * Validar archivo (extensión y tamaño)
     */
    private void validarArchivo(MultipartFile archivo) {

        if (archivo.isEmpty()) {
            throw new BadRequestException("El archivo está vacío");
        }

        // Validar tamaño
        if (archivo.getSize() > MAX_FILE_SIZE) {
            throw new BadRequestException("El archivo excede el tamaño máximo permitido (10MB)");
        }

        // Validar extensión
        String nombreOriginal = archivo.getOriginalFilename();
        String extension = obtenerExtension(nombreOriginal);

        boolean extensionValida = false;
        for (String ext : ALLOWED_EXTENSIONS) {
            if (ext.equalsIgnoreCase(extension)) {
                extensionValida = true;
                break;
            }
        }

        if (!extensionValida) {
            throw new BadRequestException(
                    "Extensión de archivo no permitida. Permitidas: " + String.join(", ", ALLOWED_EXTENSIONS)
            );
        }

        log.info("✅ Archivo validado: {} ({})", nombreOriginal, formatearTamanio(archivo.getSize()));
    }

    /**
     * Validar que sea imagen
     */
    private void validarImagen(MultipartFile archivo) {

        if (archivo.isEmpty()) {
            throw new BadRequestException("El archivo está vacío");
        }

        // Validar tamaño
        if (archivo.getSize() > MAX_FILE_SIZE) {
            throw new BadRequestException("La imagen excede el tamaño máximo permitido (10MB)");
        }

        // Validar que sea imagen
        String contentType = archivo.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new BadRequestException("El archivo debe ser una imagen (JPG, JPEG, PNG)");
        }

        log.info("✅ Imagen validada: {} ({})",
                archivo.getOriginalFilename(),
                formatearTamanio(archivo.getSize())
        );
    }

    /**
     * Obtener extensión del archivo
     */
    private String obtenerExtension(String nombreArchivo) {
        if (nombreArchivo == null || !nombreArchivo.contains(".")) {
            throw new BadRequestException("Nombre de archivo inválido");
        }
        return nombreArchivo.substring(nombreArchivo.lastIndexOf(".") + 1).toLowerCase();
    }

    /**
     * Formatear tamaño de archivo
     */
    private String formatearTamanio(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.2f KB", bytes / 1024.0);
        return String.format("%.2f MB", bytes / (1024.0 * 1024.0));
    }

    /**
     * Obtener ruta completa del archivo
     */
    public Path obtenerRutaCompleta(String rutaRelativa, boolean esImagen) {
        String baseDir = esImagen ? imageUploadDir : documentsUploadDir;
        return Paths.get(baseDir, rutaRelativa);
    }
}