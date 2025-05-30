package org.example.gateway_api.implementation.controller;

import org.example.gateway_api.implementation.component.DeviceInfoParser;
import org.example.gateway_api.implementation.component.DeviceInfoWebClientCalls;
import org.example.gateway_api.implementation.component.DeviceProvisioning;
import org.example.gateway_api.implementation.objects.Variables;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/register")
public class MobileDeviceRegistrationController {
    private final DeviceInfoParser deviceInfoParser;
    private final DeviceProvisioning deviceProvisioning;
    private final DeviceInfoWebClientCalls deviceInfoWebClientCalls;
    public MobileDeviceRegistrationController(DeviceInfoParser deviceInfoParser, DeviceProvisioning deviceProvisioning, DeviceInfoWebClientCalls deviceInfoWebClientCalls) {
        this.deviceInfoParser = deviceInfoParser;
        this.deviceProvisioning = deviceProvisioning;
        this.deviceInfoWebClientCalls = deviceInfoWebClientCalls;
    }

    @GetMapping("")
    public Mono<String> registerDevice(ServerWebExchange exchange) {
        if(exchange.getRequest().getHeaders().getFirst(Variables.X_APP_VERSION_KEY) != null) {
            return deviceProvisioning.generateDeviceId(exchange).flatMap(
                    deviceId -> deviceInfoParser.createDeviceInfo(deviceId,exchange).flatMap(deviceInfo ->
                            deviceInfoWebClientCalls.register(deviceInfo, Variables.GW_CHANNEL_VALUE).thenReturn(deviceId)
                    ));
        }
        return Mono.empty();
    }
}
