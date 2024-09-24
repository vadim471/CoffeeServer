package com.example.telemetry.enums;

public enum RemoteOperations {
    SYNC("sync");

    private final String operation;

    RemoteOperations(String operation){
        this.operation = operation;
    }

    public String getOperation(){
        return operation;
    }
}
