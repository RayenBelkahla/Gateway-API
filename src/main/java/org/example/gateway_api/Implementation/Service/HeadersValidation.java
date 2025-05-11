package org.example.gateway_api.Implementation.Service;

import org.example.gateway_api.Implementation.Components.CustomHeadersManipulation;
import org.example.gateway_api.Implementation.Enum.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@Service
public class HeadersValidation {
    public CustomHeadersManipulation customHeadersManipulation;

    public HeadersValidation(CustomHeadersManipulation customHeadersManipulation) {
        this.customHeadersManipulation = customHeadersManipulation;
    }



    public Mono<Map<String, Object>> buildHeaderData(ServerWebExchange exchange) {
        Map<String, Object> headerData = new HashMap<>();

        String appId = customHeadersManipulation.determineAppId(exchange);
        if (appId == null) {
            return null;
        }

        headerData.put("X-App-Id", appId);

        Channel channel = customHeadersManipulation.determineChannel(exchange);
        headerData.put("Channel", channel.name());

        if (channel == Channel.MOBILE) {
            String version = customHeadersManipulation.extractAppVersionKey(exchange);
            headerData.put("X-App-Version-Key", version);
        }
        return Mono.justOrEmpty(headerData);
    }
}
