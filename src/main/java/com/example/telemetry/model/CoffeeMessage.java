package com.example.telemetry.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Entity
public class CoffeeMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String message;
    private int repeatCount;
    private LocalDateTime date;

    public CoffeeMessage() {
    }

    public CoffeeMessage(Long id, String message, int repeatCount, LocalDateTime date) {
        this.id = id;
        this.message = message;
        this.repeatCount = repeatCount;
        this.date = date;
    }
    public String getFormattedDate(){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
        return this.date.format(formatter);
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setRepeatCount(int repeatCount) {
        this.repeatCount = repeatCount;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public Long getId() {
        return id;
    }

    public String getMessage() {
        return message;
    }

    public int getRepeatCount() {
        return repeatCount;
    }

    public LocalDateTime getDate() {
        return date;
    }
}
