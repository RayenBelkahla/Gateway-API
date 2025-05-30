package org.example.gateway_api.implementation.repo;

import org.example.gateway_api.implementation.objects.AppInfo;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
@Component
public class InMemoryAppVersionRepository {
    Map<String, AppInfo> versionMap = new ConcurrentHashMap<>();

    public void save(AppInfo appVersion) {
        versionMap.put(appVersion.versionKey(), appVersion);
    }

     public List<AppInfo> findAll() {
        return new ArrayList<>(versionMap.values());
    }

     public AppInfo findByVersionKey(String versionKey) {
        return versionMap.get(versionKey);
    }

    public void clearAll() {
        versionMap.clear();
    }
}
