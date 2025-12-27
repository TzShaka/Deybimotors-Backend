package com.deybimotors.config;

import com.deybimotors.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final UserDetailsService userDetailsService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .authorizeHttpRequests(auth -> auth
                        // Endpoints públicos
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/api/catalogo-publico/**").permitAll()
                        .requestMatchers("/uploads/**").permitAll()
                        .requestMatchers("/api/test/**").permitAll()

                        // Endpoints de administración (solo ADMIN)
                        .requestMatchers("/api/usuarios/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/**").hasRole("ADMIN")

                        // Endpoints de sedes (Admin, Almacenero y Vendedor)
                        .requestMatchers("/api/sedes/**").hasAnyRole("ADMIN", "ALMACENERO", "VENDEDOR")

                        // ✅ Endpoints de compras (Admin, Almacenero y Vendedor pueden crear)
                        .requestMatchers(HttpMethod.POST, "/api/compras").hasAnyRole("ADMIN", "ALMACENERO", "VENDEDOR")
                        .requestMatchers(HttpMethod.POST, "/api/compras/*/factura").hasAnyRole("ADMIN", "ALMACENERO", "VENDEDOR")
                        .requestMatchers(HttpMethod.PATCH, "/api/compras/*/estado").hasAnyRole("ADMIN", "ALMACENERO")
                        .requestMatchers(HttpMethod.GET, "/api/compras/**").hasAnyRole("ADMIN", "ALMACENERO", "VENDEDOR")

                        // ✅ Endpoints de stock (Admin, Almacenero y Vendedor)
                        .requestMatchers(HttpMethod.GET, "/api/stock/**").hasAnyRole("ADMIN", "ALMACENERO", "VENDEDOR")
                        .requestMatchers(HttpMethod.POST, "/api/stock/salida").hasAnyRole("ADMIN", "ALMACENERO", "VENDEDOR")
                        .requestMatchers(HttpMethod.POST, "/api/stock/ajustar").hasAnyRole("ADMIN", "ALMACENERO")

                        // ✅ Endpoints de Kardex (todos los roles autenticados pueden ver)
                        .requestMatchers(HttpMethod.GET, "/api/kardex/**").hasAnyRole("ADMIN", "ALMACENERO", "VENDEDOR")

                        // Resto de endpoints requieren autenticación
                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowedOriginPatterns(Arrays.asList("*"));

        configuration.setAllowedMethods(Arrays.asList(
                "GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"
        ));

        configuration.setAllowedHeaders(Arrays.asList("*"));

        configuration.setExposedHeaders(Arrays.asList(
                "Access-Control-Allow-Origin",
                "Access-Control-Allow-Credentials",
                "Authorization"
        ));

        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}