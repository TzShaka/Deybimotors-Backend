package com.deybimotors.service;

import com.deybimotors.dto.AuthDTO;
import com.deybimotors.entity.Usuario;
import com.deybimotors.exception.BadRequestException;
import com.deybimotors.exception.ResourceNotFoundException;
import com.deybimotors.repository.UsuarioRepository;
import com.deybimotors.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

/**
 * Servicio de Autenticación - RF-001
 * Maneja login y generación de tokens JWT
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;

    /**
     * Iniciar sesión - RF-001
     */
    @Transactional(readOnly = true)
    public AuthDTO.LoginResponse login(AuthDTO.LoginRequest request) {

        // Autenticar usuario
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        // Cargar datos del usuario
        Usuario usuario = usuarioRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        // Validar que esté activo - RF-001
        if (!usuario.getActivo()) {
            throw new BadRequestException("Usuario desactivado. Contacte al administrador.");
        }

        // Generar token JWT con información adicional
        UserDetails userDetails = userDetailsService.loadUserByUsername(request.getUsername());

        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("userId", usuario.getId());
        extraClaims.put("rol", usuario.getRol().name());
        extraClaims.put("sedeId", usuario.getSede().getId());

        String token = jwtUtil.generateToken(userDetails, extraClaims);

        // Construir respuesta
        return new AuthDTO.LoginResponse(
                token,
                "Bearer",
                usuario.getId(),
                usuario.getUsername(),
                usuario.getNombreCompleto(),
                usuario.getRol().name(),
                usuario.getSede().getId(),
                usuario.getSede().getNombre()
        );
    }

    /**
     * Cambiar contraseña del usuario actual
     */
    @Transactional
    public void cambiarPassword(String username, AuthDTO.CambiarPasswordRequest request) {

        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        // Validar contraseña actual
        if (!passwordEncoder.matches(request.getPasswordActual(), usuario.getPassword())) {
            throw new BadRequestException("La contraseña actual es incorrecta");
        }

        // Actualizar contraseña
        usuario.setPassword(passwordEncoder.encode(request.getPasswordNueva()));
        usuarioRepository.save(usuario);
    }
}