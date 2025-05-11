package org.example.gateway_api.Implementation.Components;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
public class DeviceProvisioning {

    private static final Logger logger = LoggerFactory.getLogger(DeviceProvisioning.class);
    private static final String DEVICE_ID = "X-Device-Id";
    private static final String APP_VERSION_KEY = "X-App-Version-Key";

    // main method
    public Mono<String> addDeviceId(ServerWebExchange exchange) {
        if (isInvalidMobileRequest(exchange)) {
            exchange.getResponse().setStatusCode(HttpStatusCode.valueOf(400));
            return Mono.empty();
        }

        String deviceId = resolveDeviceId(exchange);
        setDeviceIdCookie(exchange, deviceId);
        return saveDeviceIdInSession(exchange, deviceId);
    }

    private boolean isInvalidMobileRequest(ServerWebExchange exchange) {
        return exchange.getRequest().getCookies().getFirst(DEVICE_ID) == null
                && exchange.getRequest().getHeaders().containsKey(APP_VERSION_KEY);
    }

    public String retrieveDeviceId(ServerWebExchange exchange) {
        HttpCookie deviceIdCookie = exchange.getRequest().getCookies().getFirst(DEVICE_ID);
        if (deviceIdCookie != null) {
            logger.debug("Device ID found in cookie: {}", deviceIdCookie.getValue());
            return deviceIdCookie.getValue();
        }
        return null;
    }

    public String resolveDeviceId(ServerWebExchange exchange) {
        if (retrieveDeviceId(exchange) != null) {
            return retrieveDeviceId(exchange);
        }

        String generatedId = generateDeviceId();
        logger.info("Generated new Device ID: {}", generatedId);
        return generatedId;
    }

    private void setDeviceIdCookie(ServerWebExchange exchange, String deviceId) {
        ResponseCookie cookie = ResponseCookie.from(DEVICE_ID, deviceId)
                .httpOnly(true)
                .sameSite("Lax")
                .path("/")
                .maxAge(36000000)
                .build();
        exchange.getResponse().getHeaders().add(HttpHeaders.SET_COOKIE, cookie.toString());
        logger.debug("Set-Cookie header added for Device ID");
    }

    private Mono<String> saveDeviceIdInSession(ServerWebExchange exchange, String deviceId) {
        return exchange.getSession()
                .doOnNext(session -> {
                    session.getAttributes().put(DEVICE_ID, deviceId);
                    logger.debug("Device ID stored in session: {}", deviceId);
                })
                .thenReturn(deviceId);
    }
    private String generateDeviceId() {
        logger.info("Generating new Id.");
        return UUID.randomUUID().toString();
    }

}
