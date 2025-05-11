package org.example.gateway_api.Implementation.Objects;

public record DeviceInfo(
        String deviceId,
        String channel,
        String os,
        String osVersion,
        String model,
        String modelVersion
) {}
