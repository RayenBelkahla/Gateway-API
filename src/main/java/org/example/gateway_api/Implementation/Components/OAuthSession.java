package org.example.gateway_api.Implementation.Components;

import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@Component
public class OAuthSession {
    private final ReactiveOAuth2AuthorizedClientManager authorizedClientManager;

    public OAuthSession(ReactiveOAuth2AuthorizedClientManager authorizedClientManager) {
        this.authorizedClientManager = authorizedClientManager;
    }

    public Mono<Map<String, Object>> getSession(String clientId, ServerWebExchange exchange) {
        Map<String, Object> data = new HashMap<>();

        return ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication)
                .flatMap(authentication -> {
                    OAuth2AuthorizeRequest authorizeRequest = OAuth2AuthorizeRequest
                            .withClientRegistrationId(clientId)
                            .principal(authentication)
                            .attribute(ServerWebExchange.class.getName(), exchange)
                            .build();

                    return authorizedClientManager.authorize(authorizeRequest);
                })
                .map(client -> {
                    if (client != null) {
                        OAuth2AccessToken accessToken = client.getAccessToken();
                        data.put("principalName", client.getPrincipalName());
                        data.put("clientRegistrationId", client.getClientRegistration().getRegistrationId());
                        if (accessToken != null) {
                            data.put("accessTokenValue", accessToken.getTokenValue());
                            data.put("accessTokenExpiresAt", accessToken.getExpiresAt());
                        }
                    }


                    return data;
                })
                .defaultIfEmpty(data);
    }
}