package com.example.telemetry.generator;

import com.example.telemetry.handler.CommandHandler;
import com.example.telemetry.model.Task;
import com.example.telemetry.requestHandler.RemoteHandler;
import com.example.telemetry.requestHandler.UpgradeRecipeHandler;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import static com.example.telemetry.generator.ResponseGenerator.generateSessionId;
import static com.example.telemetry.generator.ResponseGenerator.formatDate;

@Component
public class RequestGenerator {
    private final ObjectMapper objectMapper =                                       new ObjectMapper();
    private static final Logger logger =                                            LoggerFactory.getLogger(ResponseGenerator.class);
    private final Map<String, CommandHandler> commandHandlers =                     new HashMap<>();

    @Value("${dirForDownload}")
    private String dirForDownload;

    @Autowired
    public RequestGenerator() {
        commandHandlers.put("upgrade", new UpgradeRecipeHandler());
        commandHandlers.put("remote", new RemoteHandler());
    }

    public byte[] processTelemetry(String cmd, int vmcNumber, Object params){
        try {
            CommandHandler handler = commandHandlers.get(cmd);
            String responseBody = handler.handle(vmcNumber, params);
            if (responseBody == null)
                return null;
            int responseLength = responseBody.getBytes(StandardCharsets.UTF_8).length;
            byte[] responseHeader = generateHeader(responseLength);
            return createFrame(responseHeader, responseBody);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return null;
    }

    public Task generateTaskFromResponseBytes(byte[] array) {
        try {
            String body = new String(array, StandardCharsets.UTF_8);
            ObjectNode objectNode = (ObjectNode) objectMapper.readTree(body);
            String cmd = objectNode.get("cmd").asText();
            return new Task(cmd, objectNode);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * @param frameSize - размер сгенерированного тела JSON
     * @return Head fram'а
     */
    private byte[] generateHeader(int frameSize){
        int firstByteValue = frameSize + 48 + 12;
        byte firstByte = (byte) firstByteValue;

        byte[] header = new byte[12];
        header[0] = firstByte;

        for (int i = 1; i < header.length; i++){
            header[i] = '0';
        }

        return header;
    }

    /**
     * Метод для конечной генерации frame, который будет отправлен на вендинговый аппарат.
     * @param header - заголовок frame
     * @param body - тело, JSON
     */
    private byte[] createFrame(byte[] header, String body) {
        byte[] bodyBytes = body.getBytes(StandardCharsets.UTF_8);

        byte[] frame = new byte[header.length + bodyBytes.length];

        System.arraycopy(header, 0, frame, 0, header.length);

        System.arraycopy(bodyBytes, 0, frame, header.length, bodyBytes.length);

        return frame;
    }

    //implements redirect in handler
    private ObjectNode handlerPriceSet(ObjectNode jsonNode) {
        ObjectNode response = objectMapper.createObjectNode();
        int vmcNumber = jsonNode.get("vmc_no").asInt();
        response.put("cmd", "priceset");
        response.put("vmc_no", vmcNumber);
        response.put("session_id", generateSessionId(vmcNumber));
        return response;
    }

    private ObjectNode handlerUpload(ObjectNode jsonNode) {
        ObjectNode response = objectMapper.createObjectNode();
        int vmcNumber = jsonNode.get("vmc_no").asInt();
        response.put("cmd", "upload");
        response.put("vmc_no", vmcNumber);
        response.put("session_id", generateSessionId(vmcNumber));
        response.put("date", formatDate());
        response.put("folder", ""); //check dir
        response.put("dir", dirForDownload);
        return response;
    }

}

