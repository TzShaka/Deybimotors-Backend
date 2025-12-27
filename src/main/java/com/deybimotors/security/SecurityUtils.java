package com.deybimotors.security;

import com.deybimotors.entity.Usuario;
import com.deybimotors.exception.ResourceNotFoundException;
import com.deybimotors.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * Utilidad para obtener información del usuario autenticado
 * Extrae el usuario actual desde el contexto de seguridad
 */
@Component
@RequiredArgsConstructor
public class SecurityUtils {

    private final UsuarioRepository usuarioRepository;

    /**
     * Obtener el username del usuario autenticado
     */
    public String getAuthenticatedUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("No hay usuario autenticado");
        }

        return authentication.getName();
    }

    /**
     * Obtener el ID del usuario autenticado
     */
    public Long getAuthenticatedUserId() {
        String username = getAuthenticatedUsername();

        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + username));

        return usuario.getId();
    }

    /**
     * Obtener la entidad completa del usuario autenticado
     */
    public Usuario getAuthenticatedUser() {
        String username = getAuthenticatedUsername();

        return usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + username));
    }

    /**
     * Verificar si el usuario actual tiene un rol específico
     */
    public boolean hasRole(Usuario.Rol rol) {
        Usuario usuario = getAuthenticatedUser();
        return usuario.getRol().equals(rol);
    }

    /**
     * Verificar si el usuario actual es ADMIN
     */
    public boolean isAdmin() {
        return hasRole(Usuario.Rol.ADMIN);
    }

    /**
     * Verificar si el usuario actual es VENDEDOR
     */
    public boolean isVendedor() {
        return hasRole(Usuario.Rol.VENDEDOR);
    }

    /**
     * Verificar si el usuario actual es ALMACENERO
     */
    public boolean isAlmacenero() {
        return hasRole(Usuario.Rol.ALMACENERO);
    }
}