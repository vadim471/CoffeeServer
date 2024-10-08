package com.example.telemetry.service;


import com.example.telemetry.model.Task;
import com.example.telemetry.model.TelemetryData;
import com.example.telemetry.repository.TelemetryDataRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

@Service
public class ResponseService {
    private final ObjectMapper objectMapper =                                       new ObjectMapper();
    private static final Logger LOGGER =                                            LoggerFactory.getLogger(ResponseService.class);
    private final Map<String, Function<ObjectNode, ObjectNode>> commandHandlers =   new HashMap<>();
    private final TelemetryDataRepository telemetryDataRepository;

    @Autowired
    public ResponseService(TelemetryDataRepository telemetryDataRepository) {
        commandHandlers.put("hb", this :: handlerHeartbeat);
        commandHandlers.put("login", this :: handlerLogin);
        //commandHandlers.put("machinestatus", this :: handlerMachineStatus);
        commandHandlers.put("productdone", this :: handlerProductCompletion);
        commandHandlers.put("error", this :: handlerError);
        commandHandlers.put("rinsingrecord", this :: handleRinsing);
        commandHandlers.put("remote", this :: handlerRemote);
        this.telemetryDataRepository = telemetryDataRepository;
    }

    public byte[] processTelemetry(Task task){
        try {
            String responseBody = generateResponseBody(task);
            if (responseBody == null)
                return null;
            //System.out.println(responseBody);
            int responseLength = responseBody.getBytes(StandardCharsets.UTF_8).length;
            byte[] responseHeader = generateHeader(responseLength);
            return createFrame(responseHeader, responseBody);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return null;
    }

    /**
     *
     * @param task - класс, содержащий команду (cmd) и JSON tree.
     * @return String, JSON тело будущего fram'а
     * Метод ищет в Map подходящий хендлер согласно пришедшему cmd и передает его в метод, составляющий JSON tree ответ
     */
    public String generateResponseBody(Task task){
        try {
            ObjectNode jsonNode = task.getBody();

            String cmd = task.getCommand();

            Function<ObjectNode, ObjectNode> receiveBody = commandHandlers.get(cmd);
            if (receiveBody != null) {
                ObjectNode response = receiveBody.apply(jsonNode);
                //System.out.println("My response: " + receiveBody);
                return objectMapper.writeValueAsString(response);
            } else {
                LOGGER.info("Unknown command: " + cmd);
                return null;
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return null;
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

    /**
     * @param jsonNode - JSON дерево, поступившее от аппарата, из которого достаются нужные данные (номер машины, номер заказа, тип команды)
     *                 для генерации ответа
     * @return JSON дерево, согласно API документации аппарата
     */
    private ObjectNode handlerHeartbeat(ObjectNode jsonNode){
        ObjectNode response = objectMapper.createObjectNode();
        response.put("cmd", "hb");
        response.put("vmc_no", jsonNode.get("vmc_no").asInt());
        return response;
    }

    private ObjectNode handlerLogin(ObjectNode jsonNode){
        ObjectNode response = objectMapper.createObjectNode();
        response.put("cmd", "login_r");
        response.put("vmc_no", jsonNode.get("vmc_no").asInt());
        response.put("carrier_code", "RU-RU-00391");
        response.put("ret",0);
        response.put("date_time", formatDate());
        response.put("server_list", "10.9.2.86");
        return response;
    }

    private ObjectNode handlerRemote(ObjectNode jsonNode){
        ObjectNode response = objectMapper.createObjectNode();
        response.put("cmd", "remote");
        response.put("vmc_no", jsonNode.get("vmc_no").asInt());
        response.put("session_id", "202408261843585541821673");
        response.put("operation", jsonNode.get("operation").asText());
        return response;
    }

    private ObjectNode handlerMachineStatus(ObjectNode jsonNode){
        ObjectNode response = objectMapper.createObjectNode();
        response.put("status", jsonNode.get("status").asText());
        response.put("cmd", "machinestatus");
        response.put("vmc_no", jsonNode.get("vmc_no").asInt());
        return response;
    }

    private ObjectNode handlerProductCompletion(ObjectNode jsonNode){
        if (!Objects.equals(jsonNode.get("PayType").asText(), "test"))
            saveTelemetryData(jsonNode);
        ObjectNode response = objectMapper.createObjectNode();
        response.put("cmd", "productdone_r");
        response.put("vmc_no", jsonNode.get("vmc_no").asInt());
        response.put("order_no", jsonNode.get("order_no").asText());
        response.put("isOk", true);
        return response;
    }

    private ObjectNode handlerError(ObjectNode jsonNode){
        ObjectNode response = objectMapper.createObjectNode();
        response.put("cmd", "error_r");
        response.put("vmc_no", jsonNode.get("vmc_no").asInt());
        return response;
    }

    private ObjectNode handleRinsing(ObjectNode jsonNode){
        ObjectNode response = objectMapper.createObjectNode();
        response.put("cmd", "rinsingrecord_r");
        response.put("c_uid", jsonNode.get("c_uid").asText());
        response.put("vmc_no", jsonNode.get("vmc_no").asInt());
        return response;
    }

    private String formatDate(){
        LocalDateTime date = LocalDateTime.now();
        return date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    public ObjectMapper getObjectMapper(){
        return objectMapper;
    }

    private void saveTelemetryData(ObjectNode jsonNode){
        String nameKey = jsonNode.get("nameKey").asText();
        Integer productId = jsonNode.get("ProductId").asInt();
        String payType = jsonNode.get("PayType").asText();
        Integer productAmount = jsonNode.get("ProductAmount").asInt() / 100;
        String timestamp = jsonNode.get("timestamp").asText();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        LocalDateTime date = LocalDateTime.parse(timestamp, formatter);

        Optional<TelemetryData> existingData = telemetryDataRepository.findByDate(date);

        if (existingData.isPresent())
            return;


        TelemetryData data = new TelemetryData(productId, nameKey, productAmount, payType, date);
        telemetryDataRepository.save(data);
    }
}
