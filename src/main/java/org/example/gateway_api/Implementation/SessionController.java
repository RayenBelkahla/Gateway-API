package org.example.gateway_api.Implementation;


import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/authorize")
public class SessionController {
    @GetMapping ("/user")
    public Mono<OidcUser> authorize(@AuthenticationPrincipal OidcUser oidcUser) {
        return Mono.just(oidcUser);
    }


}
