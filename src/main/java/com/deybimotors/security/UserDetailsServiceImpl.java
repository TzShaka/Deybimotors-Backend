package com.deybimotors.security;

import com.deybimotors.entity.Usuario;
import com.deybimotors.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;

/**
 * Servicio de UserDetails para Spring Security
 * Carga los datos del usuario para autenticación
 */
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Buscar usuario en la base de datos
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + username));

        // Validar que el usuario esté activo - RF-001
        if (!usuario.getActivo()) {
            throw new UsernameNotFoundException("Usuario desactivado: " + username);
        }

        // Construir UserDetails con rol
        return User.builder()
                .username(usuario.getUsername())
                .password(usuario.getPassword())
                .authorities(getAuthorities(usuario))
                .accountExpired(false)
                .accountLocked(!usuario.getActivo())
                .credentialsExpired(false)
                .disabled(!usuario.getActivo())
                .build();
    }

    // Convertir rol a GrantedAuthority
    private Collection<? extends GrantedAuthority> getAuthorities(Usuario usuario) {
        return Collections.singletonList(
                new SimpleGrantedAuthority("ROLE_" + usuario.getRol().name())
        );
    }
}