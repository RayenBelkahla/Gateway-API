package org.example.gateway_api.Implementation.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
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
    private static final Logger logger = LoggerFactory.getLogger(SessionService.class);

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
                        if (accessToken != null) {
                            data.put("accessTokenValue", accessToken.getTokenValue());
                            data.put("accessTokenExpiresAt", accessToken.getExpiresAt());
                        }
                    }
                    exchange.getRequest().getHeaders().forEach((key,values) ->
                    {
                        for (String value:values)
                            data.put(key,value);
                    });
                    return data;
                })
                .defaultIfEmpty(data);
    }
    public Mono<String> verifyDeviceId(ServerWebExchange exchange) {
        HttpCookie deviceIdValue = exchange.getRequest().getCookies().getFirst("X-Device-Id");

        HttpCookie responseCookie;
        if (deviceIdValue != null) {
            logger.debug(" Including Device ID in Set-Cookie header.");
            responseCookie = ResponseCookie.from("X-Device-Id", deviceIdValue.getValue())
                    .httpOnly(true)
                    .sameSite("Lax")
                    .path("/")
                    .maxAge(36000000)
                    .build();
        }
        else if( exchange.getRequest().getHeaders().containsKey("X-App-Version-Key")) {
            exchange.getResponse().setStatusCode(HttpStatusCode.valueOf(400));
            return Mono.empty();
        }
        else
        {
            responseCookie = generateDeviceId();
            deviceIdValue = responseCookie;
        }
        exchange.getResponse().getHeaders().add(HttpHeaders.SET_COOKIE, responseCookie.toString());
        logger.debug("Saving Device ID in session : {}", deviceIdValue.getValue());
        HttpCookie finalDeviceIdValue = deviceIdValue;
        return exchange.getSession()
                .doOnNext(session -> session.getAttributes().put("X-Device-Id", finalDeviceIdValue.getValue()))
                .thenReturn(deviceIdValue.getValue());
    }

    public HttpCookie generateDeviceId(){
            HttpCookie responseCookie;
            logger.debug("Generating new Device ID.");
            UUID uuid = UUID.randomUUID();
            logger.debug("Generating X-Device-Id Set-Cookie header.");
            responseCookie = ResponseCookie.from("X-Device-Id", uuid.toString())
                    .httpOnly(true)
                    .sameSite("Lax")
                    .path("/")
                    .maxAge(36000000)
                    .build();

        return responseCookie;
    }
}