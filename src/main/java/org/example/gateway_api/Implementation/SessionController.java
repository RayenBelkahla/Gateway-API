package org.example.gateway_api.Implementation;
import org.example.gateway_api.Implementation.Service.SessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebSession;
import reactor.core.publisher.Mono;
import java.util.Map;

@RestController
@RequestMapping("/authorize")
public class SessionController {

    public final SessionService sessionService;
    @Autowired
    public SessionController(SessionService sessionService) {
        this.sessionService = sessionService;
    }
    @GetMapping("/user")
    public Mono<OAuth2AuthenticatedPrincipal> getAuthenticatedPrincipal(@AuthenticationPrincipal OAuth2AuthenticatedPrincipal principal) {
        return Mono.justOrEmpty(principal);
    }

    @GetMapping("/session-data")
    public Mono<WebSession> getSessionAttributes(ServerWebExchange exchange) {
        return sessionService.getSessionAttributes(exchange);
    }
    @GetMapping("/get/{attribute}")
    public Mono<Map<String, Object>> getSession(@PathVariable String attribute ,ServerWebExchange exchange) {
        return sessionService.getSession(attribute, exchange);
    }
}
