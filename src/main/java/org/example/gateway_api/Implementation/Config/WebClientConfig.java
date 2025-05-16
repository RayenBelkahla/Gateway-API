package org.example.gateway_api.Implementation.Config;

import org.example.gateway_api.Implementation.Components.CacheGatewayTokenManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.web.reactive.function.client.WebClient;
@Configuration
public class WebClientConfig {
    private final CacheGatewayTokenManager cacheGatewayTokenManager;
    public WebClientConfig(CacheGatewayTokenManager cacheGatewayTokenManager) {
        this.cacheGatewayTokenManager = cacheGatewayTokenManager;
    }
    @Bean
    public WebClient webClientService(
            WebClient.Builder builder,
            @Value("${bankerise.backend_url}") String baseUrl
    ) {
        String gwToken = cacheGatewayTokenManager.getAccessToken("keycloak")
                .map(OAuth2AccessToken::getTokenValue).block();
        return builder
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + gwToken)
                .build();
    }
}
