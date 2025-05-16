package org.example.gateway_api.Implementation.Service;

import org.example.gateway_api.Implementation.Components.CustomHeadersManipulation;
import org.example.gateway_api.Implementation.Enum.Channel;
import org.example.gateway_api.Implementation.Objects.Variables;
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
        String appIdHeader = exchange.getRequest().getHeaders().getFirst(Variables.X_APP_ID);
        String appId = customHeadersManipulation.determineAppId(exchange);
        if (appIdHeader == null) {
             headerData.put(Variables.X_APP_ID, appId);
        }
        else headerData.put(Variables.X_APP_ID, appIdHeader);

        Channel channel = customHeadersManipulation.determineChannel(exchange);
        headerData.put(Variables.CHANNEL, channel.name());

        if (channel == Channel.MOB) {
            String version = customHeadersManipulation.extractAppVersionKey(exchange);
            headerData.put(Variables.X_APP_VERSION_KEY, version);
        }
        return Mono.justOrEmpty(headerData);
    }
}
