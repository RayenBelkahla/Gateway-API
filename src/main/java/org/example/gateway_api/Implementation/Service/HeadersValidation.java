package org.example.gateway_api.Implementation.Service;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ServerWebExchange;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
@Service
public class HeadersValidation {
    private static final Logger logger = LoggerFactory.getLogger(HeadersValidation.class);
    public Map<String, Object> filter(ServerWebExchange exchange) {
        String appVersionKey = exchange.getRequest().getHeaders().getFirst("X-App-Version-Key");
        String referer = exchange.getRequest().getHeaders().getFirst("Referer");
        String host = exchange.getRequest().getHeaders().getFirst("Host");
        Map<String, Object> headerData = new HashMap<>();
        if (referer != null) {
            headerData.put("X-App-Id", referer);
        } else if (host != null) {
            headerData.put("X-App-Id", host);
        } else {
            System.out.println("Unable to identify source of request");
            logger.warn("Unable to identify source of request.{}", exchange.getRequest().getURI());
            return null;
        }
        if (appVersionKey != null) {
            logger.debug("Mobile version detected.");
            headerData.put("Channel", "MOBILE");
            headerData.put("X-App-Version-Key", appVersionKey);
        } else {
            logger.debug("Web version detected.");
            headerData.put("Channel", "WEB");
        }
        return headerData;
    }
}
