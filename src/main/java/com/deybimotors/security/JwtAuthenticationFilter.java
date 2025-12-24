package com.deybimotors.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filtro de autenticación JWT
 * Intercepta cada petición y valida el token
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        // Obtener el header Authorization
        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String username;

        // Verificar si el header existe y tiene el formato correcto
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Extraer el token (sin "Bearer ")
        jwt = authHeader.substring(7);

        try {
            // Extraer username del token
            username = jwtUtil.extractUsername(jwt);

            // Si el username existe y no hay autenticación previa
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                // Cargar detalles del usuario
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);

                // Validar el token
                if (jwtUtil.validateToken(jwt, userDetails)) {

                    // Crear autenticación
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );

                    authToken.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request)
                    );

                    // Establecer autenticación en el contexto
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        } catch (Exception e) {
            // Log del error pero continuar con el filtro
            logger.error("Error al procesar token JWT: " + e.getMessage());
        }

        // Continuar con la cadena de filtros
        filterChain.doFilter(request, response);
    }
}