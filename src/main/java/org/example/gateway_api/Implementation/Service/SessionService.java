package org.example.gateway_api.Implementation.Service;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2RefreshToken;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import java.util.HashMap;
import java.util.Map;
@Service
public class SessionService {


    public Mono<Map<String, Object>> getSessionAttributes(ServerWebExchange exchange)
    {
        return exchange.getSession()
                .flatMap(webSession -> {
                    Map<String, Object> sessionAttributes = webSession.getAttributes();
                    return Mono.just(sessionAttributes);
                });
    }

    public Mono<Map<String, Object>> getSessionAttribute(String attribute, ServerWebExchange exchange) {
        Map<String, Object> data = new HashMap<>();

        return exchange.getSession()
                .flatMap(webSession -> {
                    Map<String, Object> authorizedClients = webSession.getAttribute(
                            "org.springframework.security.oauth2.client.web.server." +
                                    "WebSessionServerOAuth2AuthorizedClientRepository.AUTHORIZED_CLIENTS"
                    );

                    if (authorizedClients != null) {
                        Object Obj = authorizedClients.get(attribute);
                        if (Obj instanceof OAuth2AuthorizedClient client) {
                            OAuth2AccessToken accessToken = client.getAccessToken();
                            OAuth2RefreshToken refreshToken = client.getRefreshToken();

                            data.put("principalName", client.getPrincipalName());
                            data.put("clientRegistrationId", client.getClientRegistration().getRegistrationId());

                            if (accessToken != null) {
                                data.put("accessTokenValue", accessToken.getTokenValue());
                                data.put("accessTokenIssuedAt", accessToken.getIssuedAt());
                                data.put("accessTokenExpiresAt", accessToken.getExpiresAt());
                                data.put("accessTokenScopes", accessToken.getScopes());
                            }

                            if (refreshToken != null) {
                                data.put("refreshTokenValue", refreshToken.getTokenValue());
                                data.put("refreshTokenIssuedAt", refreshToken.getIssuedAt());
                            }
                        }
                    }
                    return Mono.just(data);
                })
                .defaultIfEmpty(data);
    }


}