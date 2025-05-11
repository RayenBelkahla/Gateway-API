package org.example.gateway_api.Implementation.Service;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.gateway_api.Implementation.Enum.Status;
import org.example.gateway_api.Implementation.Objects.AppInfo;
import org.example.gateway_api.Implementation.Repo.InMemoryAppVersionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.List;

@Service // responsible for handling mobile app versions
public class AppVersionService {
    Logger logger = LoggerFactory.getLogger(AppVersionService.class);
    private final InMemoryAppVersionRepository repository;
    private final ObjectMapper objectMapper;

    public AppVersionService(InMemoryAppVersionRepository repository) {
        this.repository = repository;
        this.objectMapper = new ObjectMapper();
        }

    public void saveAppVersions(String jsonData) {
        try {
            List<AppInfo> versions = objectMapper.readValue(jsonData, new TypeReference<>() {
            });
            versions.forEach(repository::save);
            logger.info("Stored all versions in memory.");
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
                 headerData.put("X-App-Version-Blocked", "True");
                 String infoText = appInfo.informationText();
                 headerData.put("NotificationText", infoText);
                 return headerData;
            }
            headerData.put("X-App-Version-Number",appInfo.versionNumber());
            headerData.put("X-App-Version-Key",appInfo.versionKey());
            if(appInfo.status().equals(Status.ACTIVE_NOTIFYING.toString())) {
                headerData.put("X-App-Version-Blocked", "False");
                String infoText = appInfo.informationText();
                headerData.put("NotificationText", infoText);
            }
            return headerData;
        }
        headerData.put("X-App-Version-Blocked", "True");
        headerData.put("NotificationText","Unsupported app version");
        System.out.println(headerData);
        return headerData;
    }
    public void clearAppVersions() {
        repository.clearAll();
    }


}

