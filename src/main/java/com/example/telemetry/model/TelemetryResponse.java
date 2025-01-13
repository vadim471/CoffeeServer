package com.example.telemetry.model;

import java.util.Map;

public class TelemetryResponse {
    private boolean success;
    private String message;
    private Map<String, Object> data;
    private byte[] responseBytes;

    public TelemetryResponse(boolean success, String message, Map<String, Object> data, byte[] responseBytes) {
        this.success = success;
        this.message = message;
        this.data = data;
        this.responseBytes = responseBytes;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public byte[] getResponseBytes() {
        return responseBytes;
    }

    public Map<String, Object> getData() {
        return data;
    }

    public static TelemetryResponse success(byte[] responseBytes, Map<String, Object> data) {
        return new TelemetryResponse(true, null, data, responseBytes);
    }

    public static TelemetryResponse failure(String message, byte[] responseBytes) {
        return new TelemetryResponse(false, message, null, responseBytes);
    }
}