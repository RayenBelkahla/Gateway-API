package org.example.gateway_api.Implementation.Config;

import org.example.gateway_api.Implementation.Repo.AppWideOAuth2AuthorizedClientRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.client.*;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultReactiveOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.web.server.ServerOAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.client.web.server.WebSessionServerOAuth2AuthorizedClientRepository;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.context.WebSessionServerSecurityContextRepository;


@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {
    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(exchanges -> exchanges
                        .pathMatchers("/configuration/**").permitAll()
                        .pathMatchers("/device-management/**").permitAll()
                        .pathMatchers("/app-versions/**").permitAll()
                        .anyExchange().authenticated()
                )
                .oauth2Client(Customizer.withDefaults())
                .oauth2Login(oauth2 -> oauth2
                        .authenticationSuccessHandler((webFilterExchange, authentication) -> {
                            var response = webFilterExchange.getExchange().getResponse();
                            return response.setComplete();
                        })
                )
                // required for clean keycloak authentication by returning redirect-uri
                /*.exceptionHandling(e -> e
                        .authenticationEntryPoint((exchange, ex) -> {

                            if(!exchange.getRequest().getHeaders().containsKey("X-App-Version-Key"))
                            {
                                 String originalUri = exchange.getRequest().getURI().toString();
                                return exchange.getSession().flatMap(session -> {
                                    session.getAttributes().put("REDIRECT_URI", originalUri);
                                    exchange.getResponse().setStatusCode(HttpStatus.SEE_OTHER);
                                    exchange.getResponse().getHeaders().setLocation(URI.create("/authorization/redirect-uri"));
                                    return exchange.getResponse().setComplete();
                                });
                            }
                            return Mono.empty();

                        })
                )*/
                .securityContextRepository(new WebSessionServerSecurityContextRepository())
                .build();
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
