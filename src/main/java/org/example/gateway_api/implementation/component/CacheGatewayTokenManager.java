package org.example.gateway_api.implementation.component;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class CacheGatewayTokenManager {
    private final ReactiveOAuth2AuthorizedClientManager authorizedClientManager;

    public CacheGatewayTokenManager(
            @Qualifier("serviceAuthorizedClientManager")
            ReactiveOAuth2AuthorizedClientManager authorizedClientManager
    ) {
        this.authorizedClientManager = authorizedClientManager;
    }

    public Mono<OAuth2AccessToken> getAccessToken(String registrationId) {
        OAuth2AuthorizeRequest req = OAuth2AuthorizeRequest
                .withClientRegistrationId(registrationId)
                .principal(registrationId)
                .build();

        return authorizedClientManager.authorize(req)
                .map(OAuth2AuthorizedClient::getAccessToken);
    }
}
