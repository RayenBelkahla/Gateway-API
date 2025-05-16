package org.example.gateway_api.Implementation.Components;

import org.example.gateway_api.Implementation.Objects.AppInfo;
import org.example.gateway_api.Implementation.Objects.Variables;
import org.example.gateway_api.Implementation.Service.AppVersionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import java.util.List;
@Component
public class OnStartUpAppVersionsFetch implements ApplicationRunner {
    private final Logger logger = LoggerFactory.getLogger(OnStartUpAppVersionsFetch.class);
    private final WebClient webClient;
    private final AppVersionService versionService;
    public OnStartUpAppVersionsFetch(@Qualifier("webClientService") WebClient webClient, AppVersionService versionService) {
        this.webClient = webClient;
        this.versionService = versionService;
    }
    @Override
    public void run(ApplicationArguments args) {

        String path = "/bankerise-platform/configuration/appversions";

        List<AppInfo> appInfo= webClient.get()
                .uri(path)
                .header(Variables.X_APP_ID,"api_gateway_front")
                .retrieve()
                .bodyToFlux(AppInfo.class)
                .collectList()
                .doOnError(e -> logger.error("Failed to load app versions", e))
                .block();
        if (appInfo != null && !appInfo.isEmpty()) {
            versionService.saveAppVersions(appInfo);
            logger.info("Saved {} app versions into in-memory repo", appInfo.size());
        } else {
            logger.warn("No app versions returned on startup");
        }




    }


}
