package org.example.gateway_api.Implementation.Controller;

import org.example.gateway_api.Implementation.Objects.AppInfo;
import org.example.gateway_api.Implementation.Service.AppVersionService;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/app-versions")
public class AppVersionsController {
    public final AppVersionService appVersionService;

    public AppVersionsController(AppVersionService appVersionService) {
        this.appVersionService = appVersionService;

    }
    @PostMapping("/save")
    public void save(@RequestBody List<AppInfo> appInfo) {
        appVersionService.saveAppVersions(appInfo);
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
