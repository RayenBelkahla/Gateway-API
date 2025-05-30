package org.example.gateway_api.implementation.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.gateway_api.implementation.component.*;
import org.example.gateway_api.implementation.objects.DeviceInfo;
import org.example.gateway_api.implementation.objects.Variables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebSession;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import java.util.Map;

@Service
public class SessionService {
    private final OAuthSession oAuthSessionService;
    private final DeviceProvisioning deviceProvisioning;
    private final SessionResolver sessionResolver;
    private final Logger logger = LoggerFactory.getLogger(SessionService.class);
    private final DeviceVerificationHandling deviceVerificationComponent;
    private final ObjectMapper mapper = new ObjectMapper();

    @Autowired
    public SessionService(OAuthSession oAuthSessionService,
                          DeviceProvisioning deviceProvisioning,
                          SessionResolver sessionResolver,
                          DeviceVerificationHandling deviceVerificationComponent) {
        this.oAuthSessionService = oAuthSessionService;
        this.deviceProvisioning = deviceProvisioning;
        this.sessionResolver = sessionResolver;
        this.deviceVerificationComponent = deviceVerificationComponent;
    }


    public Mono<String> getGwToken() {
        return oAuthSessionService.getGwToken();
    }

    public Mono<Map<String, Object>> getSession(String clientId, ServerWebExchange exchange) {

        return oAuthSessionService.getSession(clientId, exchange);
    }

    public Mono<DeviceInfo> parseDeviceInfo(String rawJson) {
        return Mono.fromCallable(() -> {
            try {
                JsonNode jsonNode = mapper.readTree(rawJson);
                if (!jsonNode.hasNonNull(Variables.DEVICE_ID)) {
                    throw new IllegalArgumentException("Missing field: deviceId");
                }
                if (!jsonNode.hasNonNull(Variables.DI_CHANNEL)) {
                    throw new IllegalArgumentException("Missing field: channel");
                }
                String deviceId = jsonNode.get(Variables.DEVICE_ID).asText();
                String platform = jsonNode.hasNonNull("platform" )
                        ? jsonNode.get(Variables.PLATFORM).asText()
                        : jsonNode.path(Variables.OS).asText();
                String osVersion = jsonNode.path(Variables.OS_VERSION).asText();
                String model = jsonNode.path(Variables.MODEL).asText();
                String modelVersion = jsonNode.path(Variables.MODEL_VERSION).asText();
                String channel = jsonNode.get(Variables.DI_CHANNEL).asText();

                return new DeviceInfo(deviceId, channel, platform, osVersion, model, modelVersion);
            } catch (Exception e) {
                logger.error("Error parsing device info: {}", e.getMessage());
                return null;
            }
        }).subscribeOn(Schedulers.boundedElastic());
    }
    public Mono<WebSession> getMainSessionAttributes(ServerWebExchange exchange) {
        return sessionResolver.resolveSession(exchange);
    }


    public Mono<Map<String, Object>> getDeviceInfo(ServerWebExchange exchange) {
        return oAuthSessionService.getDeviceInfo(exchange);
    }
    public Mono<String> includeDeviceId(ServerWebExchange exchange) {
        return deviceProvisioning.getDeviceId(exchange);
    }

    public Mono<DeviceInfo> handleDeviceInfo(ServerWebExchange exchange) {
        return exchange.getSession()
                .flatMap(session -> {
                    DeviceInfo cached = session.getAttribute("DEVICE-INFO");
                    if (cached != null) {
                        return Mono.just(cached);
                    }
                    return deviceVerificationComponent.verifyDeviceExistence(exchange);
                });
    }

    public Mono<Void> saveDeviceIdInSession(ServerWebExchange exchange, String deviceId) {
        return deviceProvisioning.saveDeviceIdInSession(exchange,deviceId);
    }
    public void setDeviceCookie (ServerWebExchange exchange,String deviceId) {
        deviceProvisioning.setDeviceIdCookie(exchange,deviceId);
    }

}