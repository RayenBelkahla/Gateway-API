package org.example.gateway_api;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class GatewayApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(GatewayApiApplication.class, args);
    }
    @Value("${spring.data.redis.host}")
    private String redisHost;

    @Value("${spring.data.redis.port}")
    private String redisPort;

    @PostConstruct
    public void log() {
        System.out.println("REDIS HOST: " + redisHost);
        System.out.println("REDIS PORT: " + redisPort);
    }
}
