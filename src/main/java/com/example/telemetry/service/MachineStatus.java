package com.example.telemetry.service;

public enum MachineStatus {
    MACHINESUPPLY("supply");

    private final String status;

    MachineStatus(String status){
        this.status = status;
    }

    public String getStatus(){
        return status;
    }
}
