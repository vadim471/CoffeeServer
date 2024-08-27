package com.example.telemetry.enums;

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
