package com.example.telemetry.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import java.time.LocalDateTime;

@Entity
public class Supply {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String russianName;
    private String supplyId;
    private int vmcNumber;
    private LocalDateTime dateFrame;
    private int surplus;

    public void setRussianName(String russianName) {
        this.russianName = russianName;
    }

    public void setSupplyId(String supplyId) {
        this.supplyId = supplyId;
    }

    public void setVmcNumber(int vmcNumber) {
        this.vmcNumber = vmcNumber;
    }

    public void setDateFrame(LocalDateTime dateFrame) {
        this.dateFrame = dateFrame;
    }

    public void setSurplus(int surplus) {
        this.surplus = surplus;
    }

    public String getRussianName() {
        return russianName;
    }

    public String getSupplyId() {
        return supplyId;
    }

    public int getVmcNumber() {
        return vmcNumber;
    }

    public LocalDateTime getDateFrame() {
        return dateFrame;
    }

    public int getSurplus() {
        return surplus;
    }

    public Supply(String russianName, String supplyId, int vmcNumber, LocalDateTime date, int surplus) {
        this.russianName = russianName;
        this.supplyId = supplyId;
        this.vmcNumber = vmcNumber;
        this.dateFrame = date;
        this.surplus = surplus;
    }
}
