package com.team12.cloudgateway.controller;

import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientService;
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

    public AuthenticationController(ReactiveOAuth2AuthorizedClientService clientService) {
        this.clientService = clientService;
    }

    /**
     * FOR TEST USAGE ONLY
     */
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

    /**
     * FOR TEST USAGE ONLY
     */
    @GetMapping("/test")
    public String printToken() {
        return "Pass";
    }
}
