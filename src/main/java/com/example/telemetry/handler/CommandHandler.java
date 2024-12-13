package com.example.telemetry.handler;

public interface CommandHandler {
    String handle(int vmcNumber, Object params);
}
