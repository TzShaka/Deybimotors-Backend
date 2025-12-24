package com.deybimotors.controller;

import com.deybimotors.dto.AuthDTO;
import com.deybimotors.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador de Autenticación - RF-001
 * Endpoints: /api/auth/**
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AuthController {

    private final AuthService authService;

    /**
     * POST /api/auth/login
     * Iniciar sesión y obtener token JWT
     */
    @PostMapping("/login")
    public ResponseEntity<AuthDTO.LoginResponse> login(@Valid @RequestBody AuthDTO.LoginRequest request) {
        AuthDTO.LoginResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    /**
     * POST /api/auth/cambiar-password
     * Cambiar contraseña del usuario actual
     */
    @PostMapping("/cambiar-password")
    public ResponseEntity<String> cambiarPassword(
            @Valid @RequestBody AuthDTO.CambiarPasswordRequest request,
            Authentication authentication
    ) {
        authService.cambiarPassword(authentication.getName(), request);
        return ResponseEntity.ok("Contraseña actualizada correctamente");
    }

    /**
     * GET /api/auth/validate
     * Validar si el token es válido (útil para frontend)
     */
    @GetMapping("/validate")
    public ResponseEntity<String> validateToken(Authentication authentication) {
        return ResponseEntity.ok("Token válido para usuario: " + authentication.getName());
    }
}