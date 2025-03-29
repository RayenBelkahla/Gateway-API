package org.example.gateway_api.Implementation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Routing {

    public final TokenFilter tokenFilter;
    @Autowired
    public Routing(TokenFilter tokenFilter) {
        this.tokenFilter = tokenFilter;
    }
    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("redirect", r -> r.path("/redirect")
                        .filters(f -> f.filter(tokenFilter)
                                .rewritePath("/redirect", "/api/secure"))
                        .uri("http://localhost:9901"))
                .build();
    }
}
