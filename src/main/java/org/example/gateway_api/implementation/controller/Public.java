package org.example.gateway_api.implementation.controller;

import org.example.gateway_api.implementation.component.DeviceProvisioning;
import org.example.gateway_api.implementation.objects.Channel;
import org.example.gateway_api.implementation.service.AppVersionService;
import org.example.gateway_api.implementation.service.HeadersValidation;
import org.example.gateway_api.implementation.service.SessionService;
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
    private final DeviceProvisioning deviceProvisioning;

    @Autowired
    public Public(SessionService sessionService, HeadersValidation headerValidator, AppVersionService appVersionService, DeviceProvisioning deviceProvisioning ) {
        this.sessionService = sessionService;
        this.headerValidator = headerValidator;
        this.appVersionService = appVersionService;
        this.deviceProvisioning = deviceProvisioning;
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
        return sessionService.handleDeviceInfo(exchange)
                .flatMap(deviceInfo ->
                        headerValidator.buildHeaderData(exchange)
                                .flatMap(headerData -> {
                                    Map<String, Object> sessionData = new HashMap<>(headerData);
                                    sessionData.put("X-Device-Id", deviceInfo.deviceId());
                                    sessionService.setDeviceCookie(exchange,deviceInfo.deviceId());
                                    if (Channel.MOB.toString().equals(sessionData.get("Channel"))) {
                                        String versionKey = headerData.get("X-App-Version-Key").toString();
                                        sessionData.putAll(appVersionService.AppVersionHandling(versionKey));
                                    }
                                    return sessionService.getSession("front", exchange)
                                            .flatMap(sessionMap -> {
                                                sessionData.putAll(sessionMap);
                                                                    return deviceProvisioning
                                                                            .saveDeviceIdInSession(exchange, deviceInfo.deviceId())
                                                                            .thenReturn(sessionData);
                                                                });
                                            })
                );
    }

}