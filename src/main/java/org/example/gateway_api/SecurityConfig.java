package org.example.gateway_api;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.logout.ServerLogoutSuccessHandler;
import org.springframework.security.web.server.authentication.logout.WebSessionServerLogoutHandler;


@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {
    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(exchanges -> exchanges
                        .pathMatchers("/api/admin").authenticated()
                        .pathMatchers("/api/user").authenticated()
                        .anyExchange().authenticated())
                .oauth2Client(Customizer.withDefaults());
        http.oauth2Login(oauth2 -> oauth2
                .authenticationSuccessHandler((webFilterExchange, authentication) -> webFilterExchange.getExchange().getResponse().setComplete())
        )
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
}
