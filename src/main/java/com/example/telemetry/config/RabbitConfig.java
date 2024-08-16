package com.example.telemetry.config;

import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    @Bean
    public Queue telemetryQueue() {
        return new Queue("telemetry", false);
    }
}
