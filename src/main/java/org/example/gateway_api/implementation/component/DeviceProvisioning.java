package org.example.gateway_api.implementation.component;

import org.example.gateway_api.implementation.objects.Variables;
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
    public Mono<String> getDeviceId(ServerWebExchange exchange) {
        if (isInvalidMobileRequest(exchange)) {
            exchange.getResponse().setStatusCode(HttpStatusCode.valueOf(400));
            return Mono.empty();
        }
       return retrieveDeviceIdFromCookie(exchange);
    }

    private Boolean isInvalidMobileRequest(ServerWebExchange exchange) {
        return exchange.getRequest().getCookies().getFirst(Variables.X_DEVICE_ID) == null
                && exchange.getRequest().getHeaders().containsKey(Variables.X_APP_VERSION_KEY);
    }

    public Mono<String> retrieveDeviceIdFromCookie(ServerWebExchange exchange) {
        HttpCookie deviceIdCookie = exchange.getRequest().getCookies().getFirst(Variables.X_DEVICE_ID);
        if (deviceIdCookie != null) {
            logger.debug("Device ID found in cookie: {}", deviceIdCookie.getValue());

            return Mono.just(deviceIdCookie.getValue());
        }
        return Mono.empty();
    }

    public void setDeviceIdCookie(ServerWebExchange exchange, String deviceId) {
        {
            logger.debug("Set-Cookie header added for Device ID");
            ResponseCookie cookie = ResponseCookie.from(Variables.X_DEVICE_ID, deviceId)
                .httpOnly(true)
                .sameSite("Lax")
                .domain("")
                .path("/")
                .maxAge(36000000)
                .build();
            exchange.getResponse().getHeaders().add(HttpHeaders.SET_COOKIE, cookie.toString());}
    }

    public Mono<Void> saveDeviceIdInSession(ServerWebExchange exchange, String deviceId) {
        return exchange.getSession()
                .doOnNext(session -> {
                    session.getAttributes().put(Variables.X_DEVICE_ID, deviceId);
                    logger.debug("Device ID stored in session: {}", deviceId);
                }).then();
    }
    public Mono<String> generateDeviceId(ServerWebExchange exchange) {
        logger.debug("Generating new Id.");
        String deviceId = UUID.randomUUID().toString();
        setDeviceIdCookie(exchange, deviceId);
        return Mono.just(deviceId);

    }
    public Mono<String> addDeviceId(ServerWebExchange exchange) {
        return getDeviceId(exchange).flatMap(id ->{
            setDeviceIdCookie(exchange,id);
            return Mono.just(id);
        });
    }

}


