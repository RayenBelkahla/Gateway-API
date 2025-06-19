package org.example.gateway_api.implementation.component;

import org.example.gateway_api.implementation.objects.AppInfo;
import org.example.gateway_api.implementation.objects.Variables;
import org.example.gateway_api.implementation.service.AppVersionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import java.util.List;
@Component
public class AppVersionsFetch  {
    private final Logger logger = LoggerFactory.getLogger(AppVersionsFetch.class);
    private final WebClient webClient;
    private final AppVersionService versionService;
    private final CacheGatewayTokenManager cacheGatewayTokenManager;

    public AppVersionsFetch(@Qualifier("webClientService") WebClient webClient, AppVersionService versionService, CacheGatewayTokenManager cacheGatewayTokenManager) {
        this.webClient = webClient;
        this.versionService = versionService;
        this.cacheGatewayTokenManager = cacheGatewayTokenManager;

    }
   @Scheduled(initialDelay = 0,fixedRate = 600000)
    public void fetchAppVersions() {
        logger.info("Fetching app versions");
        String path = "/configuration/appversions";
        String gwToken = cacheGatewayTokenManager.getAccessToken(Variables.GW_REGISTRATION_ID)
                .map(OAuth2AccessToken::getTokenValue).block();
        List<AppInfo> appInfo= webClient.get()
                .uri(path)
                .header(Variables.X_APP_ID,Variables.GW_APP_ID)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + gwToken)
                .header(Variables.CHANNEL,"API")
                .retrieve()
                .bodyToFlux(AppInfo.class)
                .collectList()
                .doOnError(e -> logger.error("Failed to load app versions", e))
                .block();
        if (appInfo != null && !appInfo.isEmpty()) {
            versionService.saveAppVersions(appInfo);
            logger.debug("Saved {} app versions into in-memory repo", appInfo.size());
        } else {
            logger.warn("No app versions returned on startup");
        }




    }


}
