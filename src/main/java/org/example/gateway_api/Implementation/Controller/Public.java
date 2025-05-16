package org.example.gateway_api.Implementation.Controller;

import org.example.gateway_api.Implementation.Enum.Channel;
import org.example.gateway_api.Implementation.Service.AppVersionService;
import org.example.gateway_api.Implementation.Service.HeadersValidation;
import org.example.gateway_api.Implementation.Service.SessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/configuration")
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
    @GetMapping("/attributes/{clientId}")
    public Mono<Map<String, Object>> getSessionAttributes(@PathVariable String clientId, ServerWebExchange exchange) {
        return sessionService.includeDeviceId(exchange)
                .flatMap(deviceId -> {
                    Map<String, Object> sessionData = new HashMap<>();
                    sessionData.put("X-Device-Id", deviceId);

                    return headerValidator.buildHeaderData(exchange)
                            .flatMap(headerData -> {
                                sessionData.putAll(headerData);
                                if(sessionData.get("Channel").equals(Channel.MOB.toString())) {
                                    String versionKey = headerData.get("X-App-Version-Key").toString();
                                    sessionData.putAll(appVersionService.AppVersionHandling(versionKey));
                                }
                                return sessionService.getSession(clientId, exchange)
                                        .flatMap(sessionMap -> {
                                            sessionData.putAll(sessionMap);
                                            // map the token into the same map and return the map
                                            return sessionService.getGwToken()
                                                    .map(token -> {
                                                        sessionData.put("GwTokenValue", token);
                                                        return sessionData;
                                                    });
                                        });
                            });
                });
    }

}