package com.example.telemetry.requestHandler;

import com.example.telemetry.handler.CommandHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Map;

import static com.example.telemetry.generator.ResponseGenerator.generateSessionId;

@Component
public class UploadHandler implements CommandHandler {

    @Value("${dirForUploadRecipe}")
    private String dir;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String handle(int vmcNumber, Object params) {
        ObjectNode response = objectMapper.createObjectNode();
        response.put("cmd", "remote");
        response.put("vmc_no", vmcNumber);
        response.put("session_id", generateSessionId(vmcNumber));
        response.put("notify_url", "");
        if (params instanceof Map<?, ?> paramMap) {
            paramMap.forEach((key, value) -> {
                response.put("operation", key.toString());
                response.put("type", value.toString());
            });
        }
        response.put("date", "");
        response.put("folder","");
        response.put("dir", dir);

        try {
            return objectMapper.writeValueAsString(response);
        } catch (Exception e) {
            throw new RuntimeException("Failed", e);
        }
    }
}
