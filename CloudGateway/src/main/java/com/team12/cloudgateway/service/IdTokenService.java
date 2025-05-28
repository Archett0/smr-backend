package com.team12.cloudgateway.service;

import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class IdTokenService {
    private final ReactiveOAuth2AuthorizedClientService clientService;

    public IdTokenService(ReactiveOAuth2AuthorizedClientService clientService) {
        this.clientService = clientService;
    }

    public Mono<String> getIdToken(OAuth2AuthenticationToken authentication) {
        return clientService.loadAuthorizedClient(
                authentication.getAuthorizedClientRegistrationId(),
                authentication.getName()
        ).mapNotNull(oAuth2AuthorizedClient -> {
            if (oAuth2AuthorizedClient != null) {
                var principle = authentication.getPrincipal();
                if (principle instanceof OidcUser oidcUser) {
                    return oidcUser.getIdToken().getTokenValue();
                }
            }
            return null;
        });
    }
}
