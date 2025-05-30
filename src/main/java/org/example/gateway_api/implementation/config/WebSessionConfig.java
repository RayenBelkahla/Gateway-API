package org.example.gateway_api.implementation.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.server.session.CookieWebSessionIdResolver;
import org.springframework.web.server.session.WebSessionIdResolver;

@Configuration
public class WebSessionConfig {
    Logger logger = LoggerFactory.getLogger(WebSessionConfig.class);
    @Bean
    public WebSessionIdResolver webSessionIdResolver() {
        logger.debug("Generating Session ID on authentication");
        CookieWebSessionIdResolver resolver = new CookieWebSessionIdResolver();
        resolver.setCookieName("SESSION_ID");
        return resolver;

    }

}
