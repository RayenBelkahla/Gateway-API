package org.example.gateway_api.implementation.component;
import org.example.gateway_api.implementation.objects.DeviceInfo;
import org.example.gateway_api.implementation.objects.Variables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
@Component
public class DeviceInfoWebClientCalls {

    private final Logger logger = LoggerFactory.getLogger(DeviceInfoWebClientCalls.class);
    private final WebClient webClient;
    private final CacheGatewayTokenManager cacheGatewayTokenManager;
    public DeviceInfoWebClientCalls(WebClient webClient, CacheGatewayTokenManager cacheGatewayTokenManager) {
            this.webClient = webClient;
        this.cacheGatewayTokenManager = cacheGatewayTokenManager;
    }
    public Mono<DeviceInfo> register(DeviceInfo info,  String channel)
    {   return cacheGatewayTokenManager.getAccessToken(Variables.GW_REGISTRATION_ID).flatMap(token ->
            webClient.post()
            .uri("/configuration/gw/device")
            .headers(h -> {
                h.set(HttpHeaders.AUTHORIZATION, "Bearer " + token.getTokenValue());
                h.set(Variables.X_APP_ID, Variables.GW_APP_ID);
                h.set(Variables.DI_CHANNEL, channel);
            })
            .bodyValue(info)
            .exchangeToMono(postResp -> {
                HttpStatusCode postStatus = postResp.statusCode();
                if (postStatus == HttpStatus.CREATED) {
                    logger.info("Device successfully saved in RS: {}", info.deviceId());
                    return Mono.just(info);
                } else if (postStatus == HttpStatus.CONFLICT) {
                    logger.warn("Device Exists, status: {}", postStatus.value());
                }else {logger.error("Failed to save device: {}", postStatus.value());}
                return Mono.empty();
            }));
    }
    public Mono<DeviceInfo> fetch (String deviceId, String channel) {
        return cacheGatewayTokenManager.getAccessToken(Variables.GW_REGISTRATION_ID).flatMap(token ->
                webClient.get()
                    .uri(uri -> uri.path("/configuration/gw/device/{id}")
                            .build(deviceId))
                    .headers(h -> {
                        h.set(Variables.X_APP_ID, Variables.GW_APP_ID);
                        h.set(HttpHeaders.AUTHORIZATION, "Bearer " + token.getTokenValue());
                        h.set(Variables.DI_CHANNEL, channel);
                    })
                    .exchangeToMono(response ->{
                        HttpStatusCode statusCode = response.statusCode();
                        if (statusCode == HttpStatus.OK) {
                            logger.info("Device successfully fetched from RS: {}", deviceId);
                            return response.bodyToMono(DeviceInfo.class);
                        }
                        return Mono.empty();
                    }));}
    }



