package com.example.telemetry.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class RinsingType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    public RinsingType() {
    }

    private String rinsingCode;
    private String rinsingDescription;

    public RinsingType(String rinsingCode, String rinsingDescription) {
        this.rinsingCode = rinsingCode;
        this.rinsingDescription = rinsingDescription;
    }

    public void setRinsingCode(String rinsingCode) {
        this.rinsingCode = rinsingCode;
    }

    public void setRinsingDescription(String rinsingDescription) {
        this.rinsingDescription = rinsingDescription;
    }

    public String getRinsingCode() {
        return rinsingCode;
    }

    public String getRinsingDescription() {
        return rinsingDescription;
    }
}
