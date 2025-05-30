package org.example.gateway_api.implementation.objects;

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