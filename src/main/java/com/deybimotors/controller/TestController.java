package com.deybimotors.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

/**
 * CONTROLADOR TEMPORAL - ELIMINAR EN PRODUCCIÓN
 * Solo para generar el hash de la contraseña
 */
@RestController
@RequestMapping("/api/test")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class TestController {

    private final PasswordEncoder passwordEncoder;

    /**
     * TEMPORAL: Generar hash BCrypt
     * Llamar: GET http://localhost:8080/api/test/hash?password=admin123
     */
    @GetMapping("/hash")
    public String generarHash(@RequestParam String password) {
        String hash = passwordEncoder.encode(password);
        return "Password: " + password + "\nHash: " + hash;
    }
}