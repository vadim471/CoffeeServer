package com.example.telemetry.config;

import com.example.telemetry.model.ChinaMessage;
import com.example.telemetry.model.CoffeeMessage;
import com.example.telemetry.model.CoffeeOrder;
import com.example.telemetry.model.Task;
import com.example.telemetry.repository.ChinaMessageRepository;
import com.example.telemetry.repository.CoffeeMessageRepository;
import com.example.telemetry.repository.CoffeeOrderRepository;
import com.example.telemetry.service.MessageSender;
import com.example.telemetry.service.ResponseService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;


import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.Optional;


@Configuration
public class TcpServerConfig {
    @Value("${tcp.server.port}")
    private int port;

    @Value("${tcp.china_server.port}")
    private int chinaPort;

    @Value("${china_server_ip}")
    private String chinaIp;

    private final ResponseService responseService;
    private final CoffeeMessageRepository coffeeMessageRepository;
    private final ChinaMessageRepository chinaMessageRepository;
    private final CoffeeOrderRepository coffeeOrderRepository;
    private MessageSender messageSender;
    private ServerSocket serverSocket;
    private static final Logger logger = LoggerFactory.getLogger(TcpServerConfig.class);
    private int hb_counter = 0;

    @Autowired
    public TcpServerConfig(ResponseService responseService, CoffeeMessageRepository coffeeMessageRepository, ChinaMessageRepository chinaMessageRepository, CoffeeOrderRepository coffeeOrderRepository) {
        this.responseService = responseService;
        this.coffeeMessageRepository = coffeeMessageRepository;
        this.chinaMessageRepository = chinaMessageRepository;
        this.coffeeOrderRepository = coffeeOrderRepository;
    }

    @PostConstruct
    public void startServer() {
        new Thread(this::runServer).start();
    }

    private void runServer() {
        try {
            serverSocket = new ServerSocket(port);
            System.out.println("TCP server started on port " + port);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Accepted connection from " + clientSocket.getInetAddress());
                handleClient(clientSocket, responseService);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleClient(Socket clientSocket, ResponseService responseService) {

        try (InputStream in = clientSocket.getInputStream();
             OutputStream out = clientSocket.getOutputStream();
             Socket chinaSocket = new Socket(chinaIp, chinaPort);
             InputStream chinaIn = chinaSocket.getInputStream();
             OutputStream chinaOut = chinaSocket.getOutputStream()) {

            Thread forwardThread = new Thread(() -> {
                try {
                    //messageSender = new MessageSender(out);

                    byte[] header = new byte[4];
                    byte[] buffer = new byte[1024];
                    int bytesRead;
                    while ((bytesRead = in.read(buffer)) != -1) {

                        System.arraycopy(buffer, 0, header, 0, 4);
                        getChinaHeader(header);
                        int bodyLength = getPacketSize(header);
                        byte[] bodyBuffer = new byte[bodyLength];

                        System.arraycopy(buffer, 12, bodyBuffer, 0, bodyLength);

                        String jsonBody = new String(bodyBuffer, StandardCharsets.UTF_8);
                        System.out.println("Received from machine to server: " + jsonBody);

                        ObjectNode body = (ObjectNode) responseService.getObjectMapper().readTree(jsonBody);
                        Task task = new Task(body.get("cmd").asText(), body);
                        byte[] responseFrame = responseService.processTelemetry(task);

                        if (!Objects.equals(body.get("cmd").asText(), "hb")) {
                            logger.info(jsonBody);
                            logger.info("hb : " + hb_counter);
                        } else {
                            hb_counter++;
                        }


                        if (jsonBody.contains("PayType")) {
                            saveCoffeeOrder(jsonBody);
                        } else {
                            saveCoffeeMessage(jsonBody);
                        }

                        chinaOut.write(buffer, 0, bytesRead);
                        chinaOut.flush();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });

            Thread reverseThread = new Thread(() -> {
                try {
                    byte[] responseHeader = new byte[4];
                    byte[] responseBuffer = new byte[1024];
                    int bytesRead;
                    while ((bytesRead = chinaIn.read(responseBuffer)) != -1) {

                        System.arraycopy(responseBuffer, 0, responseHeader, 0, 4);
                        getChinaHeader(responseHeader);
                        int responseBodyLength = getPacketSize(responseHeader);
                        byte[] responseBodyBuffer = new byte[responseBodyLength];

                        System.arraycopy(responseBuffer, 12, responseBodyBuffer, 0, responseBodyLength);

                        String responseJsonBody = new String(responseBodyBuffer, StandardCharsets.UTF_8);
                        System.out.println("Received from server to machine: " + responseJsonBody);

                        /*
                        ObjectNode body = (ObjectNode) responseService.getObjectMapper().readTree(responseJsonBody);
                        Task task = new Task(body.get("cmd").asText(), body);

                        if (!Objects.equals(body.get("cmd").asText(), "hb")) {
                            logger.info(responseJsonBody);
                            logger.info("hb : " + hb_counter);
                        } else {
                            hb_counter++;
                        }
                         */

                        saveChinaMessage(responseJsonBody);

                        out.write(responseBuffer, 0, bytesRead);
                        out.flush();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });

            forwardThread.start();
            reverseThread.start();

            forwardThread.join();
            reverseThread.join();


            //messageSender.sendMessage(responseFrame);

    } catch(IOException | InterruptedException e) {
        e.printStackTrace();
    } finally {
        try {
            clientSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}

    private void saveCoffeeOrder(String responseJsonBody) throws JsonProcessingException {
        ObjectNode body = (ObjectNode) responseService.getObjectMapper().readTree(responseJsonBody);
        Optional<CoffeeOrder> existingMessage = coffeeOrderRepository.findByProductName(body.get("nameKey").asText());

        if (existingMessage.isPresent()) {
            CoffeeOrder message = existingMessage.get();
            message.setProductRepeat(message.getProductRepeat() + 1);
            coffeeOrderRepository.save(message);
        } else {
            CoffeeOrder newMessage = new CoffeeOrder();
            newMessage.setProductName(body.get("nameKey").asText());
            newMessage.setProductAmount(body.get("ProductAmount").asInt() / 100);
            newMessage.setProductRepeat(1);
            coffeeOrderRepository.save(newMessage);
        }
    }

    private void saveChinaMessage(String jsonBody) {
    Optional<ChinaMessage> existingMessage = chinaMessageRepository.findByMessage(jsonBody);

    if (existingMessage.isPresent()) {
        ChinaMessage message = existingMessage.get();
        message.setRepeatCount(message.getRepeatCount() + 1);
        chinaMessageRepository.save(message);
    } else {
        ChinaMessage newMessage = new ChinaMessage();
        newMessage.setMessage(jsonBody);
        newMessage.setRepeatCount(1);
        chinaMessageRepository.save(newMessage);
    }
}

private void saveCoffeeMessage(String jsonBody) {
    Optional<CoffeeMessage> existingMessage = coffeeMessageRepository.findByMessage(jsonBody);

    if (existingMessage.isPresent()) {
        CoffeeMessage message = existingMessage.get();
        message.setRepeatCount(message.getRepeatCount() + 1);
        coffeeMessageRepository.save(message);
    } else {
        CoffeeMessage newMessage = new CoffeeMessage();
        newMessage.setMessage(jsonBody.length() > 250 ? jsonBody.substring(0, 250) : jsonBody);
        newMessage.setRepeatCount(1);
        coffeeMessageRepository.save(newMessage);
    }
}

public void sendCommandToMachine(byte[] data) throws IOException {
    if (messageSender != null) {
        messageSender.sendMessage(data);
    } else {
        System.out.println("No active connections!");
    }
}

private void getChinaHeader(byte[] buffer) {
    for (int i = 0; i < buffer.length; i++)
        buffer[i] = (byte) (buffer[i] - 48);
}

private int getPacketSize(byte[] headerBuffer) {
    ByteBuffer byteBuffer = ByteBuffer.wrap(headerBuffer);
    byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
    return byteBuffer.getInt() - 12; //12 - size of header
}
}