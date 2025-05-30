package org.example.gateway_api;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import reactor.core.publisher.Hooks;

@SpringBootApplication
public class GatewayApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(GatewayApiApplication.class, args);
    }
    @PostConstruct
    public void enableReactorDebug() {
        // Enable Reactor’s operator‐debug mode
        Hooks.onOperatorDebug();
    }
}
