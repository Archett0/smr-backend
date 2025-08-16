package com.team12.notificationservice;

import com.team12.notificationservice.config.WebSecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class WebSecurityConfigTest {

    @Test
    void converter_shouldMergeDefaultAndCustomRoles() {
        WebSecurityConfig cfg = new WebSecurityConfig();
        JwtAuthenticationConverter converter = cfg.converter();

        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "none")
                .claim("scope", "read")
                .claim("https://smr.com/roles", List.of("ADMIN", "AGENT"))
                .claims(c -> c.putAll(Map.of()))
                .build();

        JwtAuthenticationToken auth = (JwtAuthenticationToken) converter.convert(jwt);
        assertNotNull(auth);

        Set<String> names = auth.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());

        assertTrue(names.contains("SCOPE_read"), "should include SCOPE_* right");
        assertTrue(names.contains("ROLE_ADMIN"), "should include custom ROLE_ADMIN");
        assertTrue(names.contains("ROLE_AGENT"), "should include custom ROLE_AGENT");
    }
}
