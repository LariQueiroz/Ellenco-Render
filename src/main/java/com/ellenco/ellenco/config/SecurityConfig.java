package com.ellenco.ellenco.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;

/**
 * Autenticação por sessão (cookie HttpOnly / SameSite=Lax). O front é servido
 * pelo próprio Spring Boot, então tudo é "mesma origem".
 * As páginas estáticas são públicas; todos os dados (/api/**) exigem login.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
            .csrf(csrf -> csrf.disable())
            .exceptionHandling(e -> e.authenticationEntryPoint(
                    new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
            .authorizeHttpRequests(auth -> auth

                // Front (static)
                .requestMatchers("/", "/*.html", "/css/**", "/js/**", "/img/**",
                        "/service-worker.js", "/favicon.ico").permitAll()

                // Health check do Render
                .requestMatchers("/api/health").permitAll()

                // Autenticação
                .requestMatchers(HttpMethod.POST,
                        "/api/usuarios/cadastro",
                        "/api/usuarios/login",
                        "/api/usuarios/logout",
                        "/api/usuarios/esqueci-senha",
                        "/api/usuarios/redefinir-senha").permitAll()

                // Todo o resto (obras, registros, fotos, dashboard, perfil)
                .anyRequest().authenticated());

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * A autenticação é feita no UsuarioController; este bean existe só para
     * impedir que o Spring crie o usuário padrão com senha gerada no log.
     */
    @Bean
    public UserDetailsService userDetailsService() {
        return username -> {
            throw new UsernameNotFoundException(username);
        };
    }
}
