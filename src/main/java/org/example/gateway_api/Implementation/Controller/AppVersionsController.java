package org.example.gateway_api.Implementation.Controller;

import org.example.gateway_api.Implementation.Objects.AppInfo;
import org.example.gateway_api.Implementation.Service.AppVersionService;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
@RestController
@RequestMapping("/app-versions")
public class AppVersionsController {
    public final AppVersionService appVersionService;

    public AppVersionsController(AppVersionService appVersionService) {
        this.appVersionService = appVersionService;

    }
    @SuppressWarnings("JvmTaintAnalysis")
    @PostMapping("/save")
    public void saveAppVersions(@RequestBody String versionsJson) {
        appVersionService.saveAppVersions(versionsJson);
        System.out.println("saved app versions");
    }

    @GetMapping("/all")
    public Mono<AppInfo[]> getAllAppVersions() {
        return Mono.justOrEmpty(appVersionService.getAllAppVersions().toArray(new AppInfo[0]));
    }
    @GetMapping("/clear")
    public Mono<Void> clearApp() {
        appVersionService.clearAppVersions();
        return Mono.empty();
    }
}
