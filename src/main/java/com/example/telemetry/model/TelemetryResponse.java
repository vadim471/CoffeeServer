package com.example.telemetry.model;


public class TelemetryResponse {
    private boolean success;
    private String message;
    //private Map<String, Object> data;
    private byte[] responseBytes;

    public TelemetryResponse(boolean success, String message, byte[] responseBytes) {
        this.success = success;
        this.message = message;
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

    public static TelemetryResponse success(byte[] responseBytes, String message) {
        return new TelemetryResponse(true, message, responseBytes);
    }

    public static TelemetryResponse failure(String message, byte[] responseBytes) {
        return new TelemetryResponse(false, message, responseBytes);
    }
}