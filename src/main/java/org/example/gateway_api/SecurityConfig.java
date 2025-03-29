package org.example.gateway_api;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;

import org.springframework.security.oauth2.client.web.server.ServerOAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.client.web.server.WebSessionServerOAuth2AuthorizedClientRepository;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.logout.ServerLogoutSuccessHandler;
import org.springframework.security.web.server.authentication.logout.WebSessionServerLogoutHandler;
import reactor.core.publisher.Mono;

import java.net.URI;


@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {
    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(exchanges -> exchanges
                        .pathMatchers("authorize/**").authenticated()
                        .anyExchange().authenticated())
                .oauth2Client(Customizer.withDefaults());
        http.oauth2Login(oauth2 -> oauth2
                .authenticationSuccessHandler((webFilterExchange, authentication) ->
                        Mono.fromRunnable(() -> webFilterExchange.getExchange().getResponse().setStatusCode(HttpStatus.FOUND))
                                .then(Mono.fromRunnable(() -> webFilterExchange.getExchange().getResponse().getHeaders().setLocation(URI.create("/authorize/user"))))
                                .then(webFilterExchange.getExchange().getResponse().setComplete())))
                .logout(logout -> logout
                        .logoutSuccessHandler(logoutSuccessHandler())
                );
        return http.build();
    }
    @Bean
    public ServerLogoutSuccessHandler logoutSuccessHandler() {
        WebSessionServerLogoutHandler logoutHandler = new WebSessionServerLogoutHandler();
        return logoutHandler::logout;
    }
    @Bean
    ServerOAuth2AuthorizedClientRepository authorizedClientRepository()
    {
        return new WebSessionServerOAuth2AuthorizedClientRepository();
    }


}
