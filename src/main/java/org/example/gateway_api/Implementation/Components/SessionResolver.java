package org.example.gateway_api.Implementation.Components;

import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebSession;
import reactor.core.publisher.Mono;

@Component
public class SessionResolver {

    public Mono<WebSession> resolveSession(ServerWebExchange exchange) {
        return exchange.getSession();
    }
}
