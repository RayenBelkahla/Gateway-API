package org.example.gateway_api.Implementation;

import org.example.gateway_api.Implementation.Service.HeadersValidation;
import org.example.gateway_api.Implementation.Service.SessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebSession;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/authorization")
public class Public {
    public final HeadersValidation headerValidator;
    public final SessionService sessionService;
    @Autowired
    public Public(SessionService sessionService, HeadersValidation headerValidator) {
        this.sessionService = sessionService;
        this.headerValidator = headerValidator;
    }
    @GetMapping("/redirect-uri")
    public Mono<Map<String,Object>> redirectUri(ServerWebExchange exchange)
    {   Map<String,Object> data = new HashMap<>();
        data.put("redirectUri",exchange.getSession().flatMap(
                session -> Mono.justOrEmpty(session.getAttribute("redirectUri"))
        ));
        return Mono.just(data);


    }
    @GetMapping("/device")
    public Mono<String> testing(ServerWebExchange exchange) {
         return sessionService.verifyDeviceId(exchange);
    }
    @GetMapping("/get/{attribute}")
    public Mono<Map<String, Object>> getSession(@PathVariable String attribute , ServerWebExchange exchange) {
        return sessionService.getSession(attribute, exchange);
    }

    @GetMapping("/headers")
    public Mono<Map<String, Object>> getHeaders(ServerWebExchange exchange) {
        return Mono.just(headerValidator.filter(exchange));
    }
    @GetMapping("/session")
    public Mono<WebSession> getSessionAttributes(ServerWebExchange exchange) {
        return sessionService.getSessionAttributes(exchange);
    }


}
