package com.example.telemetry.model;

import com.fasterxml.jackson.annotation.JsonRawValue;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.*;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;

@Entity
public class MachineActivity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int vmcNumber;

    @Column(columnDefinition = "TEXT")
    @JsonRawValue
    private String lastMessage;

    public MachineActivity() {
    }

    private LocalDateTime dateLastActivity;

    public void setVmcNumber(int vmcNumber) {
        this.vmcNumber = vmcNumber;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public void setDateLastActivity(LocalDateTime dateLastActivity) {
        this.dateLastActivity = dateLastActivity;
    }

    public int getVmcNumber() {
        return vmcNumber;
    }

    public String getLastMessage() {
        if (lastMessage == null) {
            return null;
        }
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, Object> jsonMap = objectMapper.readValue(lastMessage, Map.class);
            return objectMapper.writeValueAsString(jsonMap);
        } catch (IOException e) {
            return lastMessage;
        }
    }


    public LocalDateTime getDateLastActivity() {
        return dateLastActivity;
    }

    public MachineActivity(int vmcNumber, String lastMessage, LocalDateTime dateLastActivity) {
        this.vmcNumber = vmcNumber;
        this.lastMessage = lastMessage;
        this.dateLastActivity = dateLastActivity;
    }
}
