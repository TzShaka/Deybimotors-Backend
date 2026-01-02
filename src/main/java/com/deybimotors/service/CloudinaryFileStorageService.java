package com.deybimotors.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.deybimotors.exception.BadRequestException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.util.Map;

/**
 * Servicio de almacenamiento usando Cloudinary
 * ✅ GRATIS: 25 GB almacenamiento + 25 GB ancho de banda
 * ✅ Acceso global desde cualquier país
 */
@Service
@Slf4j
public class CloudinaryFileStorageService {

    @Value("${cloudinary.cloud-name}")
    private String cloudName;

    @Value("${cloudinary.api-key}")
    private String apiKey;

    @Value("${cloudinary.api-secret}")
    private String apiSecret;

    private Cloudinary cloudinary;

    @PostConstruct
    public void init() {
        cloudinary = new Cloudinary(ObjectUtils.asMap(
                "cloud_name", cloudName,
                "api_key", apiKey,
                "api_secret", apiSecret
        ));
        log.info("✅ Cloudinary inicializado: {}", cloudName);
    }

    /**
     * Subir imagen de producto
     */
    public String subirImagen(MultipartFile archivo, String carpeta) throws IOException {

        validarImagen(archivo);

        Map uploadResult = cloudinary.uploader().upload(
                archivo.getBytes(),
                ObjectUtils.asMap(
                        "folder", "deybimotors/" + carpeta,
                        "resource_type", "image"
                )
        );

        String url = (String) uploadResult.get("secure_url");
        log.info("✅ Imagen subida a Cloudinary: {}", url);

        return url;
    }

    /**
     * Subir documento (facturas, PDF, Excel, Word, etc.)
     * ✅ Soporta: PDF, DOC, DOCX, XLS, XLSX, imágenes
     */
    public String subirDocumento(MultipartFile archivo, String carpeta) throws IOException {

        validarArchivo(archivo);

        // Determinar el tipo de recurso según el archivo
        String resourceType = determinarTipoRecurso(archivo);

        Map uploadResult = cloudinary.uploader().upload(
                archivo.getBytes(),
                ObjectUtils.asMap(
                        "folder", "deybimotors/" + carpeta,
                        "resource_type", resourceType, // "image", "raw", o "auto"
                        "format", obtenerExtensionLimpia(archivo.getOriginalFilename()) // Mantener extensión original
                )
        );

        String url = (String) uploadResult.get("secure_url");
        log.info("✅ {} subido a Cloudinary: {}", resourceType.toUpperCase(), url);

        return url;
    }

    /**
     * Determinar tipo de recurso para Cloudinary
     */
    private String determinarTipoRecurso(MultipartFile archivo) {
        String contentType = archivo.getContentType();

        if (contentType == null) {
            return "raw"; // Por defecto, archivo sin procesar
        }

        // Si es imagen, usar tipo "image"
        if (contentType.startsWith("image/")) {
            return "image";
        }

        // Para PDFs, documentos, Excel, etc., usar "raw"
        return "raw";
    }

    /**
     * Obtener extensión limpia del archivo
     */
    private String obtenerExtensionLimpia(String nombreArchivo) {
        if (nombreArchivo == null || !nombreArchivo.contains(".")) {
            return "";
        }
        return nombreArchivo.substring(nombreArchivo.lastIndexOf(".") + 1).toLowerCase();
    }

    /**
     * Eliminar archivo de Cloudinary
     * ✅ Funciona para imágenes y documentos (PDF, Excel, etc.)
     */
    public void eliminarArchivo(String urlArchivo) {
        try {
            // Extraer public_id de la URL
            String publicId = extraerPublicId(urlArchivo);

            if (publicId == null || publicId.isEmpty()) {
                log.warn("⚠️ No se pudo extraer public_id de: {}", urlArchivo);
                return;
            }

            // Determinar el tipo de recurso desde la URL
            String resourceType = urlArchivo.contains("/image/upload/") ? "image" : "raw";

            // Eliminar con el tipo correcto
            Map result = cloudinary.uploader().destroy(
                    publicId,
                    ObjectUtils.asMap("resource_type", resourceType)
            );

            log.info("🗑️ Archivo eliminado de Cloudinary: {} (tipo: {}) - Resultado: {}",
                    publicId, resourceType, result.get("result"));

        } catch (Exception e) {
            log.error("❌ Error al eliminar de Cloudinary: {}", e.getMessage());
        }
    }

    /**
     * Extraer public_id de una URL de Cloudinary
     * ✅ Funciona para URLs de imagen y raw (documentos)
     *
     * Ejemplos:
     * - https://res.cloudinary.com/drdet81ws/image/upload/v123/deybimotors/facturas/archivo.jpg
     *   -> deybimotors/facturas/archivo
     *
     * - https://res.cloudinary.com/drdet81ws/raw/upload/v123/deybimotors/facturas/documento.pdf
     *   -> deybimotors/facturas/documento
     */
    private String extraerPublicId(String url) {
        try {
            if (url == null || !url.contains("/upload/")) {
                return null;
            }

            String[] partes = url.split("/upload/");
            if (partes.length < 2) return null;

            String despuesUpload = partes[1];

            // Remover version (v1234567890/)
            String sinVersion = despuesUpload.replaceFirst("v\\d+/", "");

            // Remover extensión del archivo
            int ultimoPunto = sinVersion.lastIndexOf('.');
            if (ultimoPunto > 0) {
                sinVersion = sinVersion.substring(0, ultimoPunto);
            }

            return sinVersion;

        } catch (Exception e) {
            log.error("❌ Error al extraer public_id: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Validar imagen
     */
    private void validarImagen(MultipartFile archivo) {
        if (archivo.isEmpty()) {
            throw new BadRequestException("El archivo está vacío");
        }

        // Máximo 10MB
        if (archivo.getSize() > 10 * 1024 * 1024) {
            throw new BadRequestException("La imagen excede 10MB");
        }

        String contentType = archivo.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new BadRequestException("El archivo debe ser una imagen (JPG, PNG, etc.)");
        }

        log.info("✅ Imagen validada: {} - Tamaño: {} KB",
                archivo.getOriginalFilename(),
                archivo.getSize() / 1024);
    }

    /**
     * Validar documento
     * ✅ Acepta: PDF, Excel, Word, imágenes
     */
    private void validarArchivo(MultipartFile archivo) {
        if (archivo.isEmpty()) {
            throw new BadRequestException("El archivo está vacío");
        }

        // Máximo 10MB
        if (archivo.getSize() > 10 * 1024 * 1024) {
            throw new BadRequestException("El archivo excede 10MB");
        }

        // Validar extensiones permitidas
        String nombreArchivo = archivo.getOriginalFilename();
        if (nombreArchivo != null) {
            String extension = nombreArchivo.substring(nombreArchivo.lastIndexOf(".")).toLowerCase();

            // ✅ Extensiones permitidas ampliadas
            String[] permitidas = {
                    ".pdf",           // PDF
                    ".jpg", ".jpeg",  // Imágenes
                    ".png", ".gif", ".bmp",
                    ".doc", ".docx",  // Word
                    ".xls", ".xlsx",  // Excel
                    ".txt",           // Texto plano
                    ".zip", ".rar"    // Archivos comprimidos
            };

            boolean valida = false;
            for (String ext : permitidas) {
                if (extension.equals(ext)) {
                    valida = true;
                    break;
                }
            }

            if (!valida) {
                throw new BadRequestException(
                        "Extensión no permitida. Permitidas: PDF, JPG, PNG, DOC, DOCX, XLS, XLSX, TXT, ZIP"
                );
            }
        }

        log.info("✅ Documento validado: {} - Tamaño: {} KB - Tipo: {}",
                archivo.getOriginalFilename(),
                archivo.getSize() / 1024,
                archivo.getContentType());
    }
}