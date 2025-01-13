package com.example.telemetry.generator;

import com.example.telemetry.handler.CommandHandler;
import com.example.telemetry.model.Task;
import com.example.telemetry.model.TelemetryResponse;
import com.example.telemetry.requestHandler.PriceSetHandler;
import com.example.telemetry.requestHandler.ProductsHandler;
import com.example.telemetry.requestHandler.RemoteHandler;
import com.example.telemetry.requestHandler.UpgradeHandler;
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

import static com.example.telemetry.generator.ResponseGenerator.*;

@Component
public class RequestGenerator {
    private final ObjectMapper objectMapper =                                       new ObjectMapper();
    private static final Logger logger =                                            LoggerFactory.getLogger(ResponseGenerator.class);
    private final Map<String, CommandHandler> commandHandlers =                     new HashMap<>();

    @Value("${dirForDownload}")
    private String dirForDownload;

    @Autowired
    public RequestGenerator(ProductsHandler productsHandler,
                            UpgradeHandler upgradeHandler,
                            RemoteHandler remoteHandler,
                            PriceSetHandler priceSetHandler) {
        commandHandlers.put("upgrade", upgradeHandler);
        commandHandlers.put("remote", remoteHandler);
        commandHandlers.put("products", productsHandler);
        commandHandlers.put("priceset", priceSetHandler);
    }

    public TelemetryResponse processTelemetry(String cmd, int vmcNumber, Object params) {
        try {
            CommandHandler handler = commandHandlers.get(cmd);
            String responseBody = handler.handle(vmcNumber, params);

            //TODO move this to controller method "makeProduct"
            if ("PRICE_MISMATCH".equals(responseBody)) {
                return TelemetryResponse.failure(responseBody, null);
            }

            byte[] successBytes = createFrame(generateHeader(responseBody.length()), responseBody);
            return TelemetryResponse.success(successBytes, Map.of("message", "Order processed successfully"));
        } catch (Exception e) {
            return TelemetryResponse.failure("Internal server error: " + e.getMessage(), null);
        }
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
}

