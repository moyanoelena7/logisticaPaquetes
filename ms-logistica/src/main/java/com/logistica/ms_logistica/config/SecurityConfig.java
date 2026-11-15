package com.logistica.ms_logistica.config;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;


@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .authorizeHttpRequests(authorize -> authorize
                .requestMatchers("/actuator/health").permitAll()

                .requestMatchers(HttpMethod.GET, "/envios")
                    .hasRole("ADMINISTRADOR")

                .requestMatchers(HttpMethod.GET, "/envios/{id}/ruta")
                    .hasAnyRole("ADMINISTRADOR", "REPARTIDOR")

                .requestMatchers(HttpMethod.GET, "/envios/{id}")
                    .hasAnyRole("ADMINISTRADOR", "REPARTIDOR")

                .requestMatchers(HttpMethod.POST, "/envios")
                    .hasRole("ADMINISTRADOR")

                .requestMatchers(HttpMethod.POST, "/envios/{id}/asignaciones")
                    .hasRole("ADMINISTRADOR")

                .requestMatchers(HttpMethod.POST, "/envios/{id}/ruta")
                    .hasRole("ADMINISTRADOR")

                .requestMatchers(
                    HttpMethod.PATCH,
                    "/envios/{id}/inicio",
                    "/envios/{id}/paquetes/{paqueteId}/resultado"
                ).hasRole("REPARTIDOR")

                .anyRequest().denyAll()
            )
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter()))
            );

        return http.build();
    }
    
    @Bean
    JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();

        converter.setJwtGrantedAuthoritiesConverter(jwt -> {
            Collection<GrantedAuthority> authorities = new ArrayList<>();

            Object realmAccessClaim = jwt.getClaims().get("realm_access");
            if (realmAccessClaim instanceof Map<?, ?> realmAccess) {
                Object rolesClaim = realmAccess.get("roles");

                if (rolesClaim instanceof Collection<?> roles) {
                    for (Object role : roles) {
                        if (role instanceof String roleName) {
                            authorities.add(
                                new SimpleGrantedAuthority("ROLE_" + roleName)
                            );
                        }
                    }
                }
            }

            return authorities;
        });

        return converter;
    }
   
}