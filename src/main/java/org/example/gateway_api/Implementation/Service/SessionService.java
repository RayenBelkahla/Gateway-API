package org.example.gateway_api.Implementation.Service;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebSession;
import reactor.core.publisher.Mono;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class SessionService {
    public final ReactiveOAuth2AuthorizedClientManager authorizedClientManager;
    public SessionService(ReactiveOAuth2AuthorizedClientManager authorizedClientManager) {
        this.authorizedClientManager = authorizedClientManager;
    }

    public Mono<WebSession> getSessionAttributes(ServerWebExchange exchange) {
        return exchange.getSession();
    }

    public Mono<Map<String, Object>> getSession(String clientRegistrationId, ServerWebExchange exchange) {
        Map<String, Object> data = new HashMap<>();

        return ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication)
                .flatMap(authentication -> {
                    OAuth2AuthorizeRequest authorizeRequest = OAuth2AuthorizeRequest
                            .withClientRegistrationId(clientRegistrationId)
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
                        data.put("refreshToken", client.getRefreshToken());
                        if (accessToken != null) {
                            data.put("accessTokenValue", accessToken.getTokenValue());
                            data.put("accessTokenExpiresAt", accessToken.getExpiresAt());
                        }
                    }
                    return data;
                })
                .defaultIfEmpty(data);
    }
    public Mono<String> verifyDeviceId(ServerWebExchange exchange) {
        HttpCookie deviceIdValue = exchange.getRequest().getCookies().getFirst("x-device-id");
        System.out.println("extracted cookie data" + exchange.getRequest().getCookies());
        ResponseCookie responseCookie;
        if (deviceIdValue != null) {
            responseCookie = ResponseCookie.from("x-device-id", deviceIdValue.getValue())
                    .httpOnly(true)
                    .sameSite("Lax")
                    .path("/")
                    .maxAge(36000000)
                    .build();
        }
        else
        {
            UUID uuid = UUID.randomUUID();
            responseCookie = ResponseCookie.from("x-device-id", uuid.toString())
                    .httpOnly(true)
                    .sameSite("Lax")
                    .path("/")
                    .maxAge(36000000)
                    .build();
        }
        exchange.getResponse().getHeaders().add(HttpHeaders.SET_COOKIE, responseCookie.toString());
        return Mono.just(responseCookie.getValue());
    }


}