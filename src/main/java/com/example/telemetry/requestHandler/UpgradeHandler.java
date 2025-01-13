package com.example.telemetry.requestHandler;

import com.example.telemetry.enums.Upgrade;
import com.example.telemetry.handler.CommandHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import static com.example.telemetry.generator.ResponseGenerator.generateSessionId;

@Component
public class UpgradeHandler implements CommandHandler {

    @Value("${dir}")
    private String dir;

    private final ObjectMapper objectMapper = new ObjectMapper();

   @Override
    public String handle(int vmcNumber, Object params) {
       if (!(Upgrade.contains((String) params))) {
           throw new IllegalArgumentException("Invalid params");
       }

       ObjectNode response = objectMapper.createObjectNode();
       response.put("cmd", "upgrade");
       response.put("type", (String) params);
       response.put("vmc_no", vmcNumber);
       response.put("session_id", generateSessionId(vmcNumber));
       response.put("dir", dir);

       try {
           return objectMapper.writeValueAsString(response);
       } catch (Exception e) {
           throw new RuntimeException("Failed", e);
       }
   }
}