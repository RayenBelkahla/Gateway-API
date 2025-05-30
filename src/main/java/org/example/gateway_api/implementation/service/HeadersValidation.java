package org.example.gateway_api.implementation.service;

import org.example.gateway_api.implementation.component.CustomHeadersManipulation;
import org.example.gateway_api.implementation.objects.Channel;
import org.example.gateway_api.implementation.objects.Variables;
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
        if (appIdHeader != null) {
             headerData.put(Variables.X_APP_ID, appIdHeader);
        }
        else {
            headerData.put(Variables.X_APP_ID, Variables.REGISTRATION_ID);
        }

        Channel channel = customHeadersManipulation.determineChannel(exchange);
        headerData.put(Variables.CHANNEL, channel.name());

        if (channel == Channel.MOB) {
            String version = customHeadersManipulation.extractAppVersionKey(exchange);
            headerData.put(Variables.X_APP_VERSION_KEY, version);
        }
        return exchange.getSession().flatMap(session -> {
            for (Map.Entry<String, Object> entry : headerData.entrySet()) {
                session.getAttributes().put(entry.getKey(), entry.getValue());
            }
            return Mono.justOrEmpty(headerData);
        });

    }
}
