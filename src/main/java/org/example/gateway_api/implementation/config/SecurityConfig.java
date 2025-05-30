package org.example.gateway_api.implementation.config;

import org.example.gateway_api.implementation.repo.AppWideOAuth2AuthorizedClientRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.client.*;
import org.springframework.security.oauth2.client.oidc.web.server.logout.OidcClientInitiatedServerLogoutSuccessHandler;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultReactiveOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.web.server.ServerOAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.client.web.server.WebSessionServerOAuth2AuthorizedClientRepository;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.logout.ServerLogoutSuccessHandler;
import org.springframework.security.web.server.context.WebSessionServerSecurityContextRepository;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {
    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http, ReactiveClientRegistrationRepository clientRegistrationRepository) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(exchanges -> exchanges
                        .pathMatchers("/configuration/**").permitAll()
                        .pathMatchers("/device-management/**").permitAll()
                        .pathMatchers("/register").permitAll()
                        .anyExchange().authenticated()
                )
                .oauth2Client(Customizer.withDefaults())
                .oauth2Login(oauth2 -> oauth2
                        .authenticationSuccessHandler((webFilterExchange, authentication) -> {
                            var response = webFilterExchange.getExchange().getResponse();
                            return response.setComplete();
                        })
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessHandler(oidcLogoutSuccessHandler(clientRegistrationRepository)))
                .securityContextRepository(new WebSessionServerSecurityContextRepository())
                .build();
    }
    private ServerLogoutSuccessHandler oidcLogoutSuccessHandler(
            ReactiveClientRegistrationRepository clientRegistrations) {

        var handler = new OidcClientInitiatedServerLogoutSuccessHandler(clientRegistrations);
        handler.setPostLogoutRedirectUri("{baseUrl}/logout/success");
        return handler;
    }
    @Bean
    ServerOAuth2AuthorizedClientRepository authorizedClientRepository()
    {
        return new WebSessionServerOAuth2AuthorizedClientRepository();
    }
    @Bean
    public ReactiveOAuth2AuthorizedClientManager authorizedClientManager(
            ReactiveClientRegistrationRepository clientRegistrationRepository,
            ServerOAuth2AuthorizedClientRepository authorizedClientRepository) {

        ReactiveOAuth2AuthorizedClientProvider authorizedClientProvider =
                ReactiveOAuth2AuthorizedClientProviderBuilder.builder()
                        .authorizationCode()
                        .refreshToken()
                        .clientCredentials()
                        .build();

        DefaultReactiveOAuth2AuthorizedClientManager authorizedClientManager =
                new DefaultReactiveOAuth2AuthorizedClientManager(
                        clientRegistrationRepository, authorizedClientRepository);
        authorizedClientManager.setAuthorizedClientProvider(authorizedClientProvider);

        return authorizedClientManager;
    }

    @Bean
    public ReactiveOAuth2AuthorizedClientService authorizedClientService() {
        return new AppWideOAuth2AuthorizedClientRepository();
    }
    @Bean
    public ReactiveOAuth2AuthorizedClientManager serviceAuthorizedClientManager(
            ReactiveClientRegistrationRepository clients,
            ReactiveOAuth2AuthorizedClientService clientService
    ) {
        var mgr = new AuthorizedClientServiceReactiveOAuth2AuthorizedClientManager(
                clients,
                clientService
        );

        mgr.setAuthorizedClientProvider(
                ReactiveOAuth2AuthorizedClientProviderBuilder.builder()
                        .clientCredentials()
                        .build()
        );
        return mgr;
    }

}
