package com.example.telemetry.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import java.time.LocalDateTime;

@Entity
public class Error {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long Id;

    private int vmcNumber;
    private int faultCode;
    private String faultInfo;
    private String faultyState;
    private LocalDateTime occuredTime;
    private LocalDateTime clearTime;
    private LocalDateTime faultDuration;

    public Error() {
    }

    public void setId(Long id) {
        Id = id;
    }

    public void setVmcNumber(int vmcNumber) {
        this.vmcNumber = vmcNumber;
    }

    public void setFaultCode(int faultCode) {
        this.faultCode = faultCode;
    }

    public void setFaultInfo(String faultInfo) {
        this.faultInfo = faultInfo;
    }

    public void setFaultyState(String faultyState) {
        this.faultyState = faultyState;
    }

    public void setOccuredTime(LocalDateTime occuredTime) {
        this.occuredTime = occuredTime;
    }

    public void setClearTime(LocalDateTime clearTime) {
        this.clearTime = clearTime;
    }

    public void setFaultDuration(LocalDateTime faultDuration) {
        this.faultDuration = faultDuration;
    }

    public Long getId() {
        return Id;
    }

    public int getVmcNumber() {
        return vmcNumber;
    }

    public int getFaultCode() {
        return faultCode;
    }

    public String getFaultInfo() {
        return faultInfo;
    }

    public String getFaultyState() {
        return faultyState;
    }

    public LocalDateTime getOccuredTime() {
        return occuredTime;
    }

    public LocalDateTime getClearTime() {
        return clearTime;
    }

    public LocalDateTime getFaultDuration() {
        return faultDuration;
    }
}

