package com.example.telemetry.model;

import com.vladmihalcea.hibernate.type.interval.PostgreSQLIntervalType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import org.hibernate.annotations.Type;


import java.time.Duration;
import java.time.LocalDate;
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
    private LocalDateTime frameDateTime;

    public void setFrameDateTime(LocalDateTime frameDateTime) {
        this.frameDateTime = frameDateTime;
    }

    public LocalDateTime getFrameDateTime() {
        return frameDateTime;
    }

    @Type(PostgreSQLIntervalType.class)
    private Duration faultDuration;

    public Error() {
    }

    public Error(int vmcNumber, int faultCode, String faultInfo,
                 String faultyState, LocalDateTime occuredTime, LocalDateTime frameDateTime) {
        this.vmcNumber = vmcNumber;
        this.faultCode = faultCode;
        this.faultInfo = faultInfo;
        this.faultyState = faultyState;
        this.occuredTime = occuredTime;
        this.frameDateTime = frameDateTime;
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
        if (this.occuredTime != null) {
            this.faultDuration = Duration.between(this.occuredTime, clearTime);
        }
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

    public Duration getFaultDuration() {
        return faultDuration;
    }
}

