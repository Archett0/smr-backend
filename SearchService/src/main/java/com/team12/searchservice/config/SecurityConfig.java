package com.team12.searchservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // Health check and monitoring endpoints
                .requestMatchers("/actuator/**").permitAll()
                // Public search endpoints (optionally protected)
                .requestMatchers("/api/search/properties/search").permitAll()
                .requestMatchers("/api/search/properties/nearby").permitAll()
                .requestMatchers("/api/search/properties/suggest").permitAll()
                .requestMatchers("/api/search/properties/trending").permitAll()
                .requestMatchers("/api/search/properties/aggregations/**").permitAll()
                // Authenticated endpoints
                .anyRequest().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> {})
            )
            .csrf(csrf -> csrf.disable())
            .cors(cors -> {});

        return http.build();
    }
} 