package com.example.telemetry.requestHandler;

import com.example.telemetry.enums.Remote;
import com.example.telemetry.handler.CommandHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import static com.example.telemetry.generator.ResponseGenerator.generateSessionId;

//"2017-05-01 02:13:50"

public class RemoteHandler implements CommandHandler {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String handle(int vmcNumber, Object params) {

        if (!(Remote.contains((String) params))) {
            throw new IllegalArgumentException("Invalid params");
        }

        ObjectNode response = objectMapper.createObjectNode();
        response.put("cmd", "remote");
        response.put("operation", (String) params);
        response.put("vmc_no", vmcNumber);
        response.put("session_id", generateSessionId(vmcNumber));

        try {
            return objectMapper.writeValueAsString(response);
        } catch (Exception e) {
            throw new RuntimeException("Failed", e);
        }
    }

}
