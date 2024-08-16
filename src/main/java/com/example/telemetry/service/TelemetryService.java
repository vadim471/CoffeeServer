package com.example.telemetry.service;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class TelemetryService {
    private final RabbitTemplate rabbitTemplate;

    @Autowired
    public TelemetryService(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void processTelemetry(String telemetryData) {
        //may be any action with data (processing or else)
        rabbitTemplate.convertAndSend("telemetry", telemetryData);
    }
}
