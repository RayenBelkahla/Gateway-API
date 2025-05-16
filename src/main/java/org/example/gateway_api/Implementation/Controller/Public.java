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
    @GetMapping("/attributes")
    public Mono<Map<String, Object>> getSessionAttributes(ServerWebExchange exchange) {
        return sessionService.includeDeviceId(exchange)
                .flatMap(deviceId ->
                        headerValidator.buildHeaderData(exchange)
                                .flatMap(headerData -> {
                                    // start with X-Device-Id + headerData
                                    Map<String, Object> sessionData = new HashMap<>(headerData);
                                    sessionData.put("X-Device-Id", deviceId);

                                    // mobile-only version handling
                                    if (Channel.MOB.toString().equals(sessionData.get("Channel"))) {
                                        String versionKey = headerData.get("X-App-Version-Key").toString();
                                        sessionData.putAll(appVersionService.AppVersionHandling(versionKey));
                                    }

                                    // now fetch the “front” session
                                    return sessionService.getSession("front", exchange)
                                            .flatMap(sessionMap -> {
                                                sessionData.putAll(sessionMap);

                                                // fetch the gateway token
                                                return sessionService.getGwToken()
                                                        .flatMap(token -> {
                                                            sessionData.put("GwTokenValue", token);

                                                            // finally fetch deviceInfo and return the completed map
                                                            return sessionService.getDeviceInfo(exchange)
                                                                    .map(deviceData -> {
                                                                        sessionData.putAll(deviceData);
                                                                        return sessionData;
                                                                    });
                                                        });
                                            });
                                })
                );
    }

}