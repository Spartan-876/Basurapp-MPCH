package com.utp.Basurapp.Config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Desactivamos CSRF porque para APIs REST con tokens (o en pruebas) no es necesario
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        // Permitimos que todos entren a los endpoints de la API de usuarios
                        .requestMatchers("/api/usuarios/**").permitAll()
                        // Cualquier otra ruta sí requerirá autenticación
                        .anyRequest().authenticated()
                );

        return http.build();
    }
}