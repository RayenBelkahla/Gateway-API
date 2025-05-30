package org.example.gateway_api.implementation.controller;
import org.example.gateway_api.implementation.component.CacheGatewayTokenManager;
import org.example.gateway_api.implementation.service.SessionService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebSession;
import reactor.core.publisher.Mono;
import java.util.Map;

@RestController
@RequestMapping("/authorization")
public class SessionController {
    public final SessionService sessionService;
    public final CacheGatewayTokenManager cacheManager;
    public SessionController(SessionService sessionService,CacheGatewayTokenManager cacheManager) {
        this.sessionService = sessionService;
        this.cacheManager = cacheManager;
    }
    @GetMapping("/user")
    public Mono<OAuth2AuthenticatedPrincipal> getAuthenticatedPrincipal(@AuthenticationPrincipal OAuth2AuthenticatedPrincipal principal) {
        return Mono.justOrEmpty(principal);
    }

    @GetMapping("/session-data")
    public Mono<WebSession> getSessionAttributes(ServerWebExchange exchange) {
        return sessionService.getMainSessionAttributes(exchange);
    }
    @GetMapping("/get/{clientRegId}")
    public Mono<Map<String, Object>> getSession(@PathVariable String clientRegId, ServerWebExchange exchange) {
        return sessionService.getSession(clientRegId, exchange);
    }

}
