package com.deybimotors.service;

import com.deybimotors.exception.BadRequestException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

/**
 * Servicio de almacenamiento de archivos
 * Maneja la subida y eliminación de imágenes y documentos
 */
@Service
@Slf4j
public class FileStorageService {

    @Value("${file.upload-dir}")
    private String uploadDir;

    @Value("${file.documents-dir}")
    private String documentsDir;

    /**
     * Guardar imagen de producto - RF-010
     */
    public String guardarImagen(MultipartFile archivo, String carpeta) throws IOException {

        // Validar que sea una imagen
        String contentType = archivo.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new BadRequestException("El archivo debe ser una imagen");
        }

        // Validar tamaño (máximo 10MB ya configurado en application.properties)

        // Crear directorio si no existe
        Path dirPath = Paths.get(uploadDir, carpeta);
        Files.createDirectories(dirPath);

        // Generar nombre único
        String extension = obtenerExtension(archivo.getOriginalFilename());
        String nombreArchivo = UUID.randomUUID().toString() + extension;

        // Guardar archivo
        Path rutaDestino = dirPath.resolve(nombreArchivo);
        Files.copy(archivo.getInputStream(), rutaDestino, StandardCopyOption.REPLACE_EXISTING);

        log.info("Imagen guardada: {}", rutaDestino);

        return String.format("%s/%s/%s", uploadDir, carpeta, nombreArchivo);
    }

    /**
     * Guardar documento (factura, etc.) - RF-025
     */
    public String guardarDocumento(MultipartFile archivo, String carpeta) throws IOException {

        // Validar tipo de documento
        String contentType = archivo.getContentType();
        if (contentType == null ||
                (!contentType.equals("application/pdf") &&
                        !contentType.startsWith("image/"))) {
            throw new BadRequestException("Solo se permiten archivos PDF o imágenes");
        }

        // Crear directorio si no existe
        Path dirPath = Paths.get(documentsDir, carpeta);
        Files.createDirectories(dirPath);

        // Generar nombre único
        String extension = obtenerExtension(archivo.getOriginalFilename());
        String nombreArchivo = UUID.randomUUID().toString() + extension;

        // Guardar archivo
        Path rutaDestino = dirPath.resolve(nombreArchivo);
        Files.copy(archivo.getInputStream(), rutaDestino, StandardCopyOption.REPLACE_EXISTING);

        log.info("Documento guardado: {}", rutaDestino);

        return String.format("%s/%s/%s", documentsDir, carpeta, nombreArchivo);
    }

    /**
     * Eliminar archivo
     */
    public void eliminarArchivo(String rutaArchivo) {
        try {
            Path path = Paths.get(rutaArchivo);
            Files.deleteIfExists(path);
            log.info("Archivo eliminado: {}", rutaArchivo);
        } catch (IOException e) {
            log.error("Error al eliminar archivo: {}", rutaArchivo, e);
        }
    }

    /**
     * Obtener extensión del archivo
     */
    private String obtenerExtension(String nombreArchivo) {
        if (nombreArchivo == null || !nombreArchivo.contains(".")) {
            return "";
        }
        return nombreArchivo.substring(nombreArchivo.lastIndexOf("."));
    }
}