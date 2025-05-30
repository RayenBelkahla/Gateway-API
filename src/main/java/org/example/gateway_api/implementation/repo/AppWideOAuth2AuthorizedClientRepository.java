package org.example.gateway_api.implementation.repo;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientService;
import reactor.core.publisher.Mono;
import java.util.HashMap;
import java.util.Map;

public class AppWideOAuth2AuthorizedClientRepository implements ReactiveOAuth2AuthorizedClientService {

    private final Map<String, OAuth2AuthorizedClient> authorizedClients = new HashMap<>();


    @Override
    public < T extends OAuth2AuthorizedClient> Mono<T> loadAuthorizedClient(String clientRegistrationId, String principalName) {
        //noinspection unchecked
        return Mono.justOrEmpty((T) this.authorizedClients.get(clientRegistrationId));
    }

    @Override
    public Mono<Void> saveAuthorizedClient(OAuth2AuthorizedClient authorizedClient, Authentication principal) {
        return Mono.fromRunnable(() -> this.authorizedClients.put(authorizedClient.getClientRegistration().getRegistrationId(), authorizedClient));
    }

    @Override
    public Mono<Void> removeAuthorizedClient(String clientRegistrationId, String principalName) {
        return Mono.fromRunnable(() ->this.authorizedClients.remove(clientRegistrationId));
    }
}
