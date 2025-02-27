package com.example.telemetry.generator;


import com.example.telemetry.model.*;
import com.example.telemetry.model.Error;
import com.example.telemetry.repository.CoffeeOrderRepository;
import com.example.telemetry.repository.ErrorRepository;
import com.example.telemetry.repository.RinsingRepository;
import com.example.telemetry.repository.TelemetryDataRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;

@Service
public class ResponseGenerator {
    private final ObjectMapper objectMapper =                                       new ObjectMapper();
    private static final Logger logger =                                            LoggerFactory.getLogger(ResponseGenerator.class);
    private final Map<String, Function<ObjectNode, ObjectNode>> commandHandlers =   new HashMap<>();
    private final TelemetryDataRepository telemetryDataRepository;
    private final CoffeeOrderRepository coffeeOrderRepository;
    private final ErrorRepository errorRepository;
    private final RinsingRepository rinsingRepository;

    @Autowired
    public ResponseGenerator(TelemetryDataRepository telemetryDataRepository, CoffeeOrderRepository coffeeOrderRepository, ErrorRepository errorRepository, RinsingRepository rinsingRepository) throws JsonProcessingException {
        commandHandlers.put("hb", this :: handlerHeartbeat);
        commandHandlers.put("login", this :: handlerLogin);
        commandHandlers.put("machinestatus", this :: handlerMachineStatus);
        commandHandlers.put("productdone", this :: handlerProductCompletion);
        commandHandlers.put("error", this :: handlerError);
        commandHandlers.put("rinsingrecord", this :: handleRinsing);
        commandHandlers.put("remote", this :: handlerRemote);

        this.telemetryDataRepository = telemetryDataRepository;
        this.coffeeOrderRepository = coffeeOrderRepository;
        this.errorRepository = errorRepository;
        this.rinsingRepository = rinsingRepository;
    }

    public Task generateTaskFromBytes(byte[] array) {
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

    public byte[] processTelemetry(Task task){
        try {
            String responseBody = generateResponseBody(task);
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

    /**
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
                return objectMapper.writeValueAsString(response);
            } else {
                logger.info("Unknown command: " + cmd);
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
    public static byte[] generateHeader(int frameSize) {
        int headerSize = 12;
        int total = frameSize + headerSize;
        int baseValue = 48;
        int maxByteValue = 256;


        byte[] header = new byte[headerSize];

        for (int i = 0; i < header.length; i++) {
            header[i] = (byte) baseValue;
        }

        int remainingSize = total;

        for (int i = 0; i < 4; i++) {
            if (remainingSize > 0) {
                int value = remainingSize % maxByteValue;
                header[i] = (byte) (baseValue + value);
                remainingSize = (remainingSize + 48) / maxByteValue;
            }
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
        int vmcNumber = jsonNode.get("vmc_no").asInt();
        response.put("cmd", "remote");
        response.put("vmc_no", vmcNumber);
        response.put("session_id", generateSessionId(vmcNumber));
        response.put("notify_url", "");
        response.put("operation", jsonNode.get("operation").asText());
        return response;
    }

    private ObjectNode handlerMachineStatus(ObjectNode jsonNode){
        ObjectNode response = objectMapper.createObjectNode();
        response.put("status", jsonNode.get("status").asText());
        response.put("vmc_no", jsonNode.get("vmc_no").asInt());
        response.put("cmd", "machinestatus");
        return response;
    }

    private ObjectNode handlerProductCompletion(ObjectNode jsonNode) {

        if (!Objects.equals(jsonNode.get("PayType").asText(), "test")) {
            saveTelemetryData(jsonNode);
            saveCoffeeOrder(jsonNode);
        }
        ObjectNode response = objectMapper.createObjectNode();
        response.put("cmd", "productdone_r");
        response.put("vmc_no", jsonNode.get("vmc_no").asInt());
        response.put("order_no", jsonNode.get("order_no").asText());
        response.put("isOk", true);
        return response;
    }

    private ObjectNode handlerError(ObjectNode jsonNode) {
        saveErrorMessage(jsonNode);
        ObjectNode response = objectMapper.createObjectNode();
        response.put("cmd", "error_r");
        response.put("vmc_no", jsonNode.get("vmc_no").asInt());
        return response;
    }

    private ObjectNode handleRinsing(ObjectNode jsonNode){
        saveRinsingMessage(jsonNode);
        ObjectNode response = objectMapper.createObjectNode();
        response.put("cmd", "rinsingrecord_r");
        response.put("c_uid", jsonNode.get("c_uid").asText());
        response.put("vmc_no", jsonNode.get("vmc_no").asInt());
        return response;
    }

    public static String formatDate(){
        LocalDateTime date = LocalDateTime.now();
        return date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    public LocalDateTime parseFrameDate(String frameDate) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        LocalDate frameDateTime = LocalDate.parse(frameDate, formatter);
        return frameDateTime.atStartOfDay();
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
        int vmcNumber = jsonNode.get("vmc_no").asInt();
        String orderNumber = jsonNode.get("order_no").asText();
        boolean isOK = jsonNode.get("isok").asBoolean();
        String status = isOK ? "success" : "failed";
        String canisterIds = jsonNode.get("canisterIds").asText();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        LocalDateTime date = LocalDateTime.parse(timestamp, formatter);

        Optional<TelemetryData> existingData = telemetryDataRepository.findByDate(date);

        if (existingData.isPresent())
            return;


        TelemetryData data = new TelemetryData(productId, nameKey, productAmount, payType, date, vmcNumber, orderNumber, status, canisterIds);
        telemetryDataRepository.save(data);
    }

    private void saveCoffeeOrder(ObjectNode body) {
        if (body.get("ProductId") != null) {
            Optional<CoffeeOrder> existingMessage = coffeeOrderRepository.findByProductId(body.get("ProductId").asInt());

            if (existingMessage.isPresent()) {
                CoffeeOrder message = existingMessage.get();
                message.setProductPriceSumm(message.getProductPriceSumm() + body.get("ProductAmount").asInt() / 100);
                message.setProductRepeat(message.getProductRepeat() + 1);
                message.setProductLastPrice(body.get("ProductAmount").asInt() / 100);
                //message.setProductName(body.get("ProductName").asText());
                coffeeOrderRepository.save(message);
            } else {
                CoffeeOrder newMessage = new CoffeeOrder();
                newMessage.setProductId(body.get("ProductId").asInt());
                newMessage.setProductName(body.get("ProductName").asText());
                newMessage.setProductLastPrice(body.get("ProductAmount").asInt() / 100);
                newMessage.setProductPriceSumm(body.get("ProductAmount").asInt() / 100);
                newMessage.setProductRepeat(1);
                coffeeOrderRepository.save(newMessage);
            }
        }
    }

    private void saveErrorMessage(ObjectNode jsonNode) {
        String errorStringCode = jsonNode.get("error_code").asText();
        String errorDescription = jsonNode.get("error_description").asText();
        Boolean isFatalError = jsonNode.get("is_fatal_error").asBoolean();
        Boolean isSet = jsonNode.get("isSet").asBoolean();
        int vmcNumber = jsonNode.get("vmc_no").asInt();
        Boolean pessistanceError = jsonNode.get("is_pessistance_error").asBoolean();
        LocalDateTime occuredTime = LocalDateTime.now();

        String frameDate = jsonNode.get("datetime").asText();
        LocalDateTime frameDateTime = parseFrameDate(frameDate);

        Error error = new Error(vmcNumber, extractFaultCode(errorStringCode), errorDescription, "verstehe niht", occuredTime, frameDateTime);

        errorRepository.save(error);
    }

    private void saveRinsingMessage(ObjectNode jsonNode) {
        String cUid = jsonNode.get("c_uid").asText();
        boolean isOk = jsonNode.get("isok").asBoolean();
        String rinsingCode = jsonNode.get("rinsing_code").asText();
        int vmcNumber = jsonNode.get("vmc_no").asInt();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        LocalDateTime date = LocalDateTime.parse(jsonNode.get("timestamp").asText(), formatter);

        Rinsing rinsing = new Rinsing(rinsingCode, date, isOk, vmcNumber, cUid);

        rinsingRepository.save(rinsing);
    }


    public static String generateSessionId(int vmcNumber) {

        LocalDateTime dateTime = LocalDateTime.now().plusHours(3);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        String formattedDateTime = dateTime.format(formatter);
        Random randomInt = new Random();
        String randomPart = String.format("%05d", randomInt.nextInt(100000));
        return formattedDateTime + vmcNumber + randomPart;
    }

    private void updateErrorClearTime(int vmcNumber, int faultCode, LocalDateTime clearTime) {
        //TODO
    }

    private String extractFaultCode(String faultCode) {
        return faultCode.split(":")[1];
    }
}
