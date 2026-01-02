package com.deybimotors.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;

/**
 * Configuración para servir archivos estáticos
 * Permite acceder a imágenes y documentos subidos
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${file.upload-dir}")
    private String uploadDir;

    @Value("${file.documents-dir}")
    private String documentsDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {

        // ✅ CORRECCIÓN: Convertir rutas a absolutas
        File imageDir = new File(uploadDir);
        File docDir = new File(documentsDir);

        // Servir imágenes de productos
        registry.addResourceHandler("/uploads/images/**")
                .addResourceLocations("file:" + imageDir.getAbsolutePath() + File.separator);

        // Servir documentos (facturas, etc.)
        registry.addResourceHandler("/uploads/documents/**")
                .addResourceLocations("file:" + docDir.getAbsolutePath() + File.separator);
    }
}