package com.team12.cloudgateway.controller;

import com.team12.cloudgateway.service.IdTokenService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.security.Principal;

@RestController
@RequestMapping("/authenticate")
public class AuthenticationController {

    private final ReactiveOAuth2AuthorizedClientService clientService;
    private final IdTokenService idTokenService;

    public AuthenticationController(ReactiveOAuth2AuthorizedClientService clientService, IdTokenService idTokenService) {
        this.clientService = clientService;
        this.idTokenService = idTokenService;
    }

    /**
     * FOR TEST USAGE ONLY
     */
    @PreAuthorize("hasAnyRole('ADMIN')")
    @GetMapping("/print-token")
    public Mono<String> printToken(Principal principal) {
        return clientService.loadAuthorizedClient("auth0", principal.getName())
                .map(oAuth2AuthorizedClient -> {
                    OAuth2AccessToken accessToken = oAuth2AuthorizedClient.getAccessToken();
                    System.out.println("AT value: " + accessToken.getTokenValue());
                    System.out.println("Token type: " + accessToken.getTokenType());
                    System.out.println("Expires at: " + accessToken.getExpiresAt());
                    return accessToken.getTokenValue();
                })
                .defaultIfEmpty("No Access Token found");
    }

    @PreAuthorize("hasAnyRole('ADMIN')")
    @GetMapping("/test-intercept-id-token")
    public Mono<String> testInterceptIdToken(OAuth2AuthenticationToken authentication) {
        return idTokenService.getIdToken(authentication);
    }
}
