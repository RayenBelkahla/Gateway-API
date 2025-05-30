package org.example.gateway_api.implementation.objects;
public record DeviceInfo(
        String deviceId,
        String channel,
        String platform,
        String osVersion,
        String model,
        String modelVersion
)implements java.io.Serializable { }

