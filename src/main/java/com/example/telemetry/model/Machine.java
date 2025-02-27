package com.example.telemetry.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Machine {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long Id;

    private int vmcNumber;
    private String softwareVersion;
    private String ioVersion;
    private String address;
    private String machineName;
    private boolean isActive;
    private String serialNumber;

    public Machine(int vmcNumber, String softwareVersion, String ioVersion) {
        this.vmcNumber = vmcNumber;
        this.softwareVersion = softwareVersion;
        this.ioVersion = ioVersion;
        this.isActive = true;
    }

    public void setMachineName(String machineName) {
        this.machineName = machineName;
    }

    public void setActive(boolean flag) {
        isActive = flag;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    public String getMachineName() {
        return machineName;
    }

    public boolean isActive() {
        return isActive;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public Machine() {
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setVmcNumber(int vmcNumber) {
        this.vmcNumber = vmcNumber;
    }

    public void setSoftwareVersion(String softwareVersion) {
        this.softwareVersion = softwareVersion;
    }

    public void setIoVersion(String ioVersion) {
        this.ioVersion = ioVersion;
    }

    public int getVmcNumber() {
        return vmcNumber;
    }

    public String getSoftwareVersion() {
        return softwareVersion;
    }

    public String getIoVersion() {
        return ioVersion;
    }

    public String getAddress() {
        return address;
    }
}
