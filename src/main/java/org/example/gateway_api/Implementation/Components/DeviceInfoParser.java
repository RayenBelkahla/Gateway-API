package org.example.gateway_api.Implementation.Components;

import org.example.gateway_api.Implementation.Objects.DeviceInfo;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import ua_parser.Client;
import ua_parser.OS;
import ua_parser.Parser;
import ua_parser.UserAgent;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
public class DeviceInfoParser {

    private final Parser parser;

    public DeviceInfoParser() {
        this.parser = new Parser();
    }

    public Mono<DeviceInfo> extract(ServerWebExchange exchange, String deviceId, String channel) {
        String userAgent = exchange.getRequest().getHeaders().getFirst("User-Agent");
        if (userAgent == null) {
            return Mono.just(new DeviceInfo(deviceId, channel, null, null, null, null));
        }

        Client client = parser.parse(userAgent);
        OS os = client.os;
        UserAgent ua = client.userAgent;

        String osVersion = join(os.major, os.minor, os.patch);
        String browserVersion = join(ua.major, ua.minor, ua.patch);

        return Mono.just(new DeviceInfo(
                deviceId,
                channel,
                os.family,
                osVersion,
                ua.family,
                browserVersion
        ));
    }

    private String join(String... parts) {
        return Arrays.stream(parts)
                .filter(Objects::nonNull)
                .collect(Collectors.joining("."));
    }
}

