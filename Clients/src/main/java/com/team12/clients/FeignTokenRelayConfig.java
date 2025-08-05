package com.team12.clients;

import feign.RequestInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

@Configuration
public class FeignTokenRelayConfig {

    @Bean
    public RequestInterceptor tokenRelayInterceptor() {
        return template -> {
            var auth = (JwtAuthenticationToken) SecurityContextHolder
                    .getContext().getAuthentication();
            if (auth != null) {
                String token = auth.getToken().getTokenValue();
                template.header("Authorization", "Bearer " + token);
            }
        };
    }
}
