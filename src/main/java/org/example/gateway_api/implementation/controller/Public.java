package org.example.gateway_api.implementation.controller;


import org.example.gateway_api.implementation.service.AppVersionService;
import org.example.gateway_api.implementation.service.HeadersValidation;
import org.example.gateway_api.implementation.service.SessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import java.util.Map;

@RestController
@RequestMapping("/session")
public class Public {
    public final HeadersValidation headerValidator;
    public final SessionService sessionService;
    public final AppVersionService appVersionService;

    @Autowired
    public Public(SessionService sessionService, HeadersValidation headerValidator, AppVersionService appVersionService) {
        this.sessionService = sessionService;
        this.headerValidator = headerValidator;
        this.appVersionService = appVersionService;
    }
    @GetMapping("/device")
    public Mono<String> getDeviceId(ServerWebExchange exchange) {
        return sessionService.includeDeviceId(exchange);
    }

    @GetMapping("/app-headers")
    public Mono<Map<String, Object>> getHeaders(ServerWebExchange exchange) {
        return headerValidator.buildHeaderData(exchange);
    }

    @GetMapping("/attributes")
    public Mono<Map<String, Object>> getMainAttributes(ServerWebExchange exchange) {
        return sessionService.getSessionAttributes(exchange);
    }
}