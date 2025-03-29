package org.example.gateway_api.Implementation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Map;

@Component
public class TokenFilter implements GatewayFilter {

    private static final Logger logger = LoggerFactory.getLogger(TokenFilter.class);
    private static final String AUTHORIZED_CLIENTS_ATTRIBUTE_NAME = "org.springframework.security.oauth2.client.web.server.WebSessionServerOAuth2AuthorizedClientRepository.AUTHORIZED_CLIENTS";
    private static final String CLIENT_REGISTRATION_ID = "keycloak";
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        logger.info("Starting TokenFilter execution for path: {}", exchange.getRequest().getPath());

        Mono<ServerWebExchange> exchangeMono = ReactiveSecurityContextHolder.getContext()

                .doOnNext(ctx -> logger.debug("Security context found"))
                .doOnError(e -> logger.error("Error getting security context", e))

                .map(SecurityContext::getAuthentication)
                .filter(authentication -> authentication instanceof OAuth2AuthenticationToken)

                .doOnNext(auth -> logger.debug("Authentication token found: {}", auth.getName()))

                .flatMap(authenticationToken -> exchange.getSession()

                        .doOnNext(session -> logger.debug("Session found"))

                        .flatMap(session -> {
                            Map<String, OAuth2AuthorizedClient> authorizedClients =
                                    session.getAttribute(AUTHORIZED_CLIENTS_ATTRIBUTE_NAME);

                            if (authorizedClients != null && authorizedClients.containsKey(CLIENT_REGISTRATION_ID)) {
                                OAuth2AuthorizedClient authorizedClient = authorizedClients.get(CLIENT_REGISTRATION_ID);
                                OAuth2AccessToken accessToken = authorizedClient.getAccessToken();

                                if (accessToken != null) {
                                    String tokenValue = accessToken.getTokenValue();
                                    logger.info("Authorization Filter - Retrieved accessToken: {}", tokenValue);

                                    org.springframework.http.server.reactive.ServerHttpRequest request = exchange.getRequest().mutate()
                                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenValue)
                                            .build();
                                    ServerWebExchange mutatedExchange = exchange.mutate()
                                            .request(request)
                                            .build();

                                    return Mono.just(mutatedExchange);
                                }
                            }

                            logger.warn("Unable to add token to request");
                            return Mono.just(exchange);
                        })
                )
                .defaultIfEmpty(exchange)
                .doOnSuccess(ex -> logger.debug("Exchange processing succeeded"))
                .doOnError(e -> logger.error("Error processing exchange", e));

        return exchangeMono.flatMap(chain::filter);
    }
    }