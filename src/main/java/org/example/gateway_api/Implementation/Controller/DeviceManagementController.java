package org.example.gateway_api.Implementation.Controller;

import org.example.gateway_api.Implementation.Objects.DeviceInfo;
import org.example.gateway_api.Implementation.Service.SessionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/device-management")
public class DeviceManagementController {
    Logger logger = LoggerFactory.getLogger(DeviceManagementController.class);
    private final SessionService sessionService;

    public DeviceManagementController(SessionService sessionService)
        {this.sessionService = sessionService;}


    @GetMapping("/create/device")
    public Mono<DeviceInfo> createDeviceInfo(ServerWebExchange exchange){
        return sessionService.createDeviceInfo(exchange);
    }

    @GetMapping("/save/device")
    public Mono<DeviceInfo> saveDeviceInfo(@RequestBody String info, ServerWebExchange exchange) {
        return sessionService.parseDeviceInfo(info)
                .flatMap(deviceInfo -> sessionService.saveDeviceInfoInSession(deviceInfo, exchange))
                .onErrorMap(e -> {
                    logger.error("Failed to parse DeviceInfo", e);
                    return new RuntimeException("Failed to parse DeviceInfo: " + e.getMessage(), e);
                });

    }

}
