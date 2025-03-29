package org.example.gateway_api.Implementation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.URI;

@RestController
@RequestMapping("/redirecting")
public class RedirectController {
    @GetMapping("/secure")
    public Mono<Void> redirectToResource(ServerWebExchange exchange) {
        String resourceServerUrl = "http://localhost:8085/api/secure";

        exchange.getResponse().setStatusCode(HttpStatus.FOUND);
        exchange.getResponse().getHeaders().setLocation(URI.create(resourceServerUrl));
        return exchange.getResponse().setComplete();
    }
}

