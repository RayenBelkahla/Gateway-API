package org.example.gateway_api.implementation.component;

import org.example.gateway_api.implementation.objects.Channel;
import org.example.gateway_api.implementation.objects.Variables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

@Component
public class CustomHeadersManipulation {
    private static final Logger logger = LoggerFactory.getLogger(CustomHeadersManipulation.class);
    private static final String X_APP_VERSION_KEY = Variables.X_APP_VERSION_KEY;
    private static final String REFERER = Variables.REFERER;
    private static final String HOST = Variables.HOST;

    public String extractAppVersionKey(ServerWebExchange exchange) {
        return exchange.getRequest().getHeaders().getFirst(X_APP_VERSION_KEY);
    }

    public String extractReferer(ServerWebExchange exchange) {
        return exchange.getRequest().getHeaders().getFirst(REFERER);
    }

    public String extractHost(ServerWebExchange exchange) {
        return exchange.getRequest().getHeaders().getFirst(HOST);
    }

    public String determineAppId(ServerWebExchange exchange) {
        String referer = extractReferer(exchange);
        String host = extractHost(exchange);

        if (referer != null) {
            return referer;
        } else if (host != null) {
            return host;
        } else {
            logger.warn("Unable to identify source of request: {}", exchange.getRequest().getURI());
            return null;
        }
    }

    public Channel determineChannel(ServerWebExchange exchange) {
        String appVersionKey = extractAppVersionKey(exchange);
        return appVersionKey != null ? Channel.MOB : Channel.WEB;
    }
}
