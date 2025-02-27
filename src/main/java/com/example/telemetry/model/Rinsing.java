package com.example.telemetry.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import java.time.LocalDateTime;

@Entity
public class Rinsing {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String rinsingCode;
    private LocalDateTime dateTime;
    private boolean isOK;
    private int vmcNumber;
    private String cUid;
    private LocalDateTime dateFrame;

    public void setDateFrame(LocalDateTime dateFrame) {
        this.dateFrame = dateFrame;
    }

    public LocalDateTime getDateFrame() {
        return dateFrame;
    }

    public Rinsing() {
    }

    public Rinsing(String rinsingCode, LocalDateTime dateTime, boolean isOK, int vmcNumber, String cUid) {
        this.rinsingCode = rinsingCode;
        this.dateTime = dateTime;
        this.isOK = isOK;
        this.vmcNumber = vmcNumber;
        this.cUid = cUid;
        this.dateFrame = LocalDateTime.now();
    }

    public void setRinsingCode(String rinsingCode) {
        this.rinsingCode = rinsingCode;
    }

    public void setDateTime(LocalDateTime dateTime) {
        this.dateTime = dateTime;
    }

    public void setOK(boolean OK) {
        isOK = OK;
    }

    public void setVmcNumber(int vmcNumber) {
        this.vmcNumber = vmcNumber;
    }

    public void setcUid(String cUid) {
        this.cUid = cUid;
    }

    public String getRinsingCode() {
        return rinsingCode;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public boolean isOK() {
        return isOK;
    }

    public int getVmcNumber() {
        return vmcNumber;
    }

    public String getcUid() {
        return cUid;
    }
}
