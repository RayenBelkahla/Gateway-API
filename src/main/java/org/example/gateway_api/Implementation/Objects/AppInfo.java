package org.example.gateway_api.Implementation.Objects;

public record AppInfo(
        String appName,
        String audience,
        String versionKey,
        String versionNumber,
        String status,
        String oauthClientId,
        String platform,
        String informationText,
        long lastModifiedDate,
        long createdDate
){}