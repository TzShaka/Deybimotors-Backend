package com.deybimotors.config;

import com.deybimotors.entity.Sede;
import com.deybimotors.entity.Usuario;
import com.deybimotors.repository.SedeRepository;
import com.deybimotors.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final SedeRepository sedeRepository;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        try {
            log.info("========================================");
            log.info("🚀 Inicializando datos del sistema...");
            log.info("========================================");

            inicializarSede();
            inicializarUsuarioAdmin();

            log.info("========================================");
            log.info("✅ Sistema listo para usarse");
            log.info("========================================");

        } catch (Exception e) {
            log.error("❌ Error durante la inicialización: {}", e.getMessage());
            log.error("Stack trace:", e);
        }
    }

    private void inicializarSede() {
        try {
            if (sedeRepository.count() == 0) {
                Sede sede = new Sede();
                sede.setNombre("Sede Principal");
                sede.setCodigo("SP");
                sede.setDireccion("Dirección Principal");
                sede.setCiudad("Lima");
                sede.setTelefono("999999999");
                sede.setActivo(true);
                sede.setObservaciones("Sede principal del sistema");
                sedeRepository.save(sede);
                log.info("✅ Sede principal creada: {}", sede.getNombre());
            } else {
                log.info("ℹ️  Sede(s) ya existen: {} registro(s)", sedeRepository.count());
            }
        } catch (Exception e) {
            log.error("❌ Error al crear sede: {}", e.getMessage());
        }
    }

    private void inicializarUsuarioAdmin() {
        try {
            if (usuarioRepository.count() == 0) {
                Sede sedePrincipal = sedeRepository.findAll().stream()
                        .findFirst()
                        .orElseThrow(() -> new RuntimeException("No existe ninguna sede"));

                Usuario admin = new Usuario();
                admin.setUsername("admin");
                admin.setPassword(passwordEncoder.encode("admin123"));
                admin.setNombreCompleto("Administrador del Sistema");
                admin.setEmail("admin@deybimotors.com");
                admin.setTelefono("999999999");
                admin.setRol(Usuario.Rol.ADMIN);
                admin.setSede(sedePrincipal);
                admin.setActivo(true);
                admin.setObservaciones("Usuario administrador por defecto");
                usuarioRepository.save(admin);

                log.info("========================================");
                log.info("✅ Usuario ADMIN creado exitosamente");
                log.info("========================================");
                log.info("📧 Username: admin");
                log.info("🔑 Password: admin123");
                log.info("========================================");
                log.info("⚠️  IMPORTANTE: Cambia la contraseña después del primer login");
                log.info("========================================");

            } else {
                log.info("ℹ️  Usuario(s) ya existen: {} registro(s)", usuarioRepository.count());

                // Verificar si existe usuario admin
                usuarioRepository.findByUsername("admin").ifPresentOrElse(
                        admin -> log.info("✅ Usuario admin existente: {}", admin.getNombreCompleto()),
                        () -> log.warn("⚠️  No existe usuario 'admin', considera crearlo manualmente")
                );
            }
        } catch (Exception e) {
            log.error("❌ Error al crear usuario admin: {}", e.getMessage());
            log.error("Stack trace:", e);
        }
    }
}