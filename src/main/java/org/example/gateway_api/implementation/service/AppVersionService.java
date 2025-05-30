package org.example.gateway_api.implementation.service;
import org.example.gateway_api.implementation.objects.Status;
import org.example.gateway_api.implementation.objects.AppInfo;
import org.example.gateway_api.implementation.objects.Variables;
import org.example.gateway_api.implementation.repo.InMemoryAppVersionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.List;

@Service // responsible for handling mobile app versions
public class AppVersionService {
    Logger logger = LoggerFactory.getLogger(AppVersionService.class);
    private final InMemoryAppVersionRepository repository;
    public AppVersionService(InMemoryAppVersionRepository repository) {
        this.repository = repository;
        }
    public void saveAppVersions(List<AppInfo> jsonData) {
        try {

            jsonData.forEach(repository::save);
            logger.debug("Stored all versions in memory.");
        } catch (Exception e) {
            logger.error("Error storing app versions: {}", e.getMessage());
        }
    }

    public List<AppInfo> getAllAppVersions() {
        return repository.findAll();
    }

    public AppInfo getAppVersionByKey(String versionKey) {
        return repository.findByVersionKey(versionKey);
    }
    public HashMap<String,Object> AppVersionHandling(String versionKey) {
        AppInfo appInfo = getAppVersionByKey(versionKey);
        HashMap<String,Object> headerData = new HashMap<>();
        if(appInfo != null) {
            if(appInfo.status().equals(Status.BLOCKING.toString())) {
                logger.warn("A client requested access using a deprecated app version -- Access Blocked");
                 headerData.put(Variables.X_APP_VERSION_BLOCKED, "True");
                 String infoText = appInfo.informationText();
                 headerData.put(Variables.NOTIFICATION_TEXT, infoText);
                 return headerData;
            }
            headerData.put(Variables.X_APP_VERSION_NUMBER,appInfo.versionNumber());
            headerData.put(Variables.X_APP_VERSION_KEY,appInfo.versionKey());
            if(appInfo.status().equals(Status.ACTIVE_NOTIFYING.toString())) {
                headerData.put(Variables.X_APP_VERSION_BLOCKED, "False");
                String infoText = appInfo.informationText();
                headerData.put(Variables.NOTIFICATION_TEXT, infoText);
            }
            return headerData;
        }
        headerData.put(Variables.X_APP_VERSION_BLOCKED, "True");
        headerData.put(Variables.NOTIFICATION_TEXT,"Unsupported app version");
        System.out.println(headerData);
        return headerData;
    }
    public void clearAppVersions() {
        repository.clearAll();
    }


}

