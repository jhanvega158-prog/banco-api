package com.banco.api.config;

import com.banco.api.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(
            JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    // BCrypt para encriptar las contraseñas
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http)
            throws Exception {

        http

                // Desactivar CSRF porque usamos JWT
                .csrf(csrf -> csrf.disable())

                // Permitir CORS
                .cors(cors -> {})

                // No utilizar sesiones
                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )

                // Configuración de permisos
                .authorizeHttpRequests(auth -> auth

                        // LOGIN PÚBLICO
                        .requestMatchers(
                                "/api/auth/**"
                        ).permitAll()

                        // PERMITIR CREAR USUARIOS
                        // sin estar autenticado
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/usuarios"
                        ).permitAll()

                        // ADMIN puede consultar todos los usuarios
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/usuarios"
                        ).hasRole("ADMIN")

                        // ADMIN puede administrar usuarios
                        .requestMatchers(
                                HttpMethod.PUT,
                                "/api/usuarios/**"
                        ).hasRole("ADMIN")

                        .requestMatchers(
                                HttpMethod.DELETE,
                                "/api/usuarios/**"
                        ).hasRole("ADMIN")

                        // ADMIN puede ver todos los movimientos
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/movimientos"
                        ).hasRole("ADMIN")

                        // Los movimientos específicos necesitan
                        // estar autenticados.
                        // La validación de si pertenecen al cliente
                        // se realiza en Controller/Service.
                        .requestMatchers(
                                "/api/movimientos/**"
                        ).authenticated()

                        // Cualquier otra petición necesita JWT
                        .anyRequest().authenticated()
                )

                // Ejecutar nuestro filtro JWT antes del filtro
                // estándar de autenticación
                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }
}