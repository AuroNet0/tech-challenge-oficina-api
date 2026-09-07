
package com.oficina.api.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    private final JwtFilter jwtFilter;

    public SecurityConfig(JwtFilter jwtFilter) {
        this.jwtFilter = jwtFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/auth/**", "/public/**", "/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**").permitAll()

                        .requestMatchers("/usuarios/**").hasRole("GERENTE")

                        .requestMatchers(HttpMethod.DELETE, "/clientes/**", "/veiculos/**", "/pecas/**", "/servicos/**").hasRole("GERENTE")

                        .requestMatchers(HttpMethod.POST, "/clientes/**", "/veiculos/**").hasAnyRole("ATENDENTE", "GERENTE")
                        .requestMatchers(HttpMethod.PUT, "/clientes/**", "/veiculos/**").hasAnyRole("ATENDENTE", "GERENTE")
                        .requestMatchers(HttpMethod.GET, "/clientes/**", "/veiculos/**").hasAnyRole("ATENDENTE", "GERENTE", "MECANICO")

                        .requestMatchers(HttpMethod.GET, "/pecas/**", "/servicos/**").hasAnyRole("ATENDENTE", "GERENTE", "MECANICO")
                        .requestMatchers(HttpMethod.POST, "/pecas/**", "/servicos/**").hasAnyRole("GERENTE", "MECANICO")
                        .requestMatchers(HttpMethod.PUT, "/pecas/**", "/servicos/**").hasAnyRole("GERENTE", "MECANICO")
                        .requestMatchers(HttpMethod.PATCH, "/pecas/**").hasAnyRole("GERENTE", "MECANICO")

                        .requestMatchers(HttpMethod.GET, "/ordens-servico/minhas").hasRole("CLIENTE")
                        .requestMatchers(HttpMethod.GET, "/ordens-servico/**").hasAnyRole("ATENDENTE", "GERENTE", "MECANICO")
                        .requestMatchers(HttpMethod.POST, "/ordens-servico").hasAnyRole("ATENDENTE", "GERENTE")
                        .requestMatchers(HttpMethod.POST, "/ordens-servico/*/servicos", "/ordens-servico/*/pecas").hasAnyRole("MECANICO", "GERENTE")
                        .requestMatchers(HttpMethod.POST, "/ordens-servico/*/enviar-orcamento").hasAnyRole("ATENDENTE", "GERENTE")
                        .requestMatchers(HttpMethod.PATCH, "/ordens-servico/*/status").hasAnyRole("MECANICO", "GERENTE")
                        .requestMatchers(HttpMethod.PATCH, "/ordens-servico/*/orcamento").hasAnyRole("ATENDENTE", "GERENTE")

                        .anyRequest().hasAnyRole("ATENDENTE", "GERENTE", "MECANICO"))
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
