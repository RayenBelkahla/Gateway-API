package org.example.gateway_api.implementation.component;

import org.example.gateway_api.implementation.objects.DeviceInfo;
import org.example.gateway_api.implementation.objects.Variables;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class DeviceVerificationHandling {
    private final DeviceProvisioning deviceProvisioning;
    private final DeviceInfoWebClientCalls webClientCalls;
    private final OAuthSession oAuthSession;
    private final DeviceInfoParser deviceInfoParser;

    public DeviceVerificationHandling(DeviceProvisioning deviceProvisioning,
                                      DeviceInfoWebClientCalls webClientCalls,
                                      OAuthSession oAuthSession,
                                      DeviceInfoParser deviceInfoParser) {
        this.deviceProvisioning = deviceProvisioning;
        this.webClientCalls     = webClientCalls;
        this.oAuthSession = oAuthSession;
        this.deviceInfoParser = deviceInfoParser;
    }

    public Mono<DeviceInfo> verifyDeviceExistence(ServerWebExchange exchange) {
        return deviceProvisioning.getDeviceId(exchange)
                .flatMap(this::lookUpDevice)
                .switchIfEmpty(registerDevice(exchange));
    }
    private Mono<DeviceInfo> lookUpDevice(String deviceId) {
        return webClientCalls.fetch(deviceId, Variables.GW_CHANNEL_VALUE);

    }

    private Mono<DeviceInfo> registerDevice(ServerWebExchange exchange) {
        return deviceProvisioning.generateDeviceId(exchange)
                .flatMap(newId ->
                        deviceInfoParser.createDeviceInfo(newId, exchange)
                )
                .flatMap(deviceInfo ->
                        webClientCalls.register(deviceInfo, Variables.GW_CHANNEL_VALUE)
                )
                .flatMap(deviceInfo ->
                        oAuthSession.saveDeviceInfoInSession(deviceInfo, exchange)
                                .then(Mono.fromRunnable(() ->
                                        deviceProvisioning.setDeviceIdCookie(exchange, deviceInfo.deviceId())
                                ))
                                .thenReturn(deviceInfo)
                );
    }
}
