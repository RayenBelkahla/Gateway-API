package org.example.gateway_api.Implementation;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api")
public class UserController {

    @GetMapping("/user")
    public Mono<OidcUser> getUser(@AuthenticationPrincipal OidcUser oidcUser) {
        return Mono.justOrEmpty(oidcUser);
    }
    @GetMapping("/admin")
    public String getAdmin() {
        return "Admin access granted!";
    }
}