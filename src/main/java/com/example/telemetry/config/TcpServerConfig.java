package com.example.telemetry.config;

import com.example.telemetry.model.ChinaMessage;
import com.example.telemetry.model.CoffeeMessage;
import com.example.telemetry.model.CoffeeOrder;
import com.example.telemetry.model.Task;
import com.example.telemetry.repository.ChinaMessageRepository;
import com.example.telemetry.repository.CoffeeMessageRepository;
import com.example.telemetry.repository.CoffeeOrderRepository;
import com.example.telemetry.service.ResponseService;
import com.example.telemetry.service.SocketStreamManager;
import com.example.telemetry.service.TaskManager;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.util.Optional;


@Configuration
public class TcpServerConfig {
    @Value("${tcp.server.port}")
    private int port;

    @Value("${tcp.china_server.port}")
    private int chinaPort;

    @Value("${china_server_ip}")
    private String chinaIp;

    @Value("${inputFrames}")
    private String input;

    @Value("${expectedFrames}")
    private String expected;

    private final TaskManager taskManager;
    private final CoffeeMessageRepository coffeeMessageRepository;
    private final ChinaMessageRepository chinaMessageRepository;
    private final CoffeeOrderRepository coffeeOrderRepository;
    private final ObjectMapper objectMapper =                                       new ObjectMapper();

    //private static MessageSender messageSender;
    private ServerSocket serverSocket;
    private static final Logger logger                                  = LoggerFactory.getLogger(TcpServerConfig.class);
    private int hb_counter                                              = 0; ///< счетчик полученных heartbeat за время, возможно понадобится для тестов
    private final SocketStreamManager socketStreamManager;
    private final ResponseService responseService;

    @Autowired
    public TcpServerConfig(ResponseService responseService, TaskManager manager, CoffeeMessageRepository coffeeMessageRepository, ChinaMessageRepository chinaMessageRepository, CoffeeOrderRepository coffeeOrderRepository, SocketStreamManager streamManager) {
        this.coffeeMessageRepository = coffeeMessageRepository;
        this.chinaMessageRepository = chinaMessageRepository;
        this.coffeeOrderRepository = coffeeOrderRepository;
        this.socketStreamManager = streamManager;
        this.taskManager = manager;
        this.responseService = responseService;
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
                System.out.println(clientSocket.getPort());
                handleClient(clientSocket);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     *
     * @param clientSocket
     * Основной метод для поддержки связи между сервисом, вендинговым аппаратом и китайским сервером (прокси).
     * Все порты, ip-адреса расположены в properties.
     */

    private void handleClient(Socket clientSocket) {

        try (InputStream in = clientSocket.getInputStream();
             OutputStream out = clientSocket.getOutputStream();

             Socket chinaSocket = new Socket(chinaIp, chinaPort);
             InputStream chinaIn = chinaSocket.getInputStream();
             OutputStream chinaOut = chinaSocket.getOutputStream()) {



            Thread forwardThread = new Thread(() -> {
                try {
                    socketStreamManager.setStreams(in, out);

                    byte[] header = new byte[4];
                    byte[] buffer = new byte[2048];

                    int bytesRead;
                    while ((bytesRead = in.read(buffer)) != -1) {

                        System.arraycopy(buffer, 0, header, 0, 4);

                        getChinaHeader(header);
                        int bodyLength = getPacketSize(header);
                        byte[] bodyBuffer = new byte[bodyLength];

                        System.arraycopy(buffer, 12, bodyBuffer, 0, bodyLength);

                        String jsonBody = new String(bodyBuffer, StandardCharsets.UTF_8);
                        System.out.println("Received from machine to server: " + jsonBody);

                        ObjectNode body = (ObjectNode) objectMapper.readTree(jsonBody);
                        Task task = new Task(body.get("cmd").asText(), body);
                        byte[] response = responseService.processTelemetry(task);
                        /*
                        if (response != null)
                            taskManager.addTask(response);

                         */
                        //byte[] responseFrame = responseService.processTelemetry(task);
                        /*
                        if (!Objects.equals(body.get("cmd").asText(), "hb")) {
                            logger.info(jsonBody);
                            if (hb_counter > 0)
                                logger.info("hb : " + hb_counter);
                        } else {
                            hb_counter++;
                        }

                         */

                        if (jsonBody.contains("PayType")) {
                            saveCoffeeOrder(jsonBody);
                        }
                        //saveFrameToFile(header, jsonBody, input); //for creating tests

                        chinaOut.write(buffer, 0, bytesRead); //it's for proxy
                        chinaOut.flush();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });

            Thread reverseThread = new Thread(() -> {
                try {
                    byte[] responseHeader = new byte[4];
                    byte[] responseBuffer = new byte[2048];
                    int bytesRead;

                    while ((bytesRead = chinaIn.read(responseBuffer)) != -1) {

                        System.arraycopy(responseBuffer, 0, responseHeader, 0, 4);

                        getChinaHeader(responseHeader);
                        int responseBodyLength = getPacketSize(responseHeader);
                        byte[] responseBodyBuffer = new byte[responseBodyLength];

                        System.arraycopy(responseBuffer, 12, responseBodyBuffer, 0, responseBodyLength);

                        String responseJsonBody = new String(responseBodyBuffer, StandardCharsets.UTF_8);
                        System.out.println("Received from server to machine: " + responseJsonBody);


                        ObjectNode body = (ObjectNode) objectMapper.readTree(responseJsonBody);
                        Task task = new Task(body.get("cmd").asText(), body);
                        //byte[] response = responseService.processTelemetry(task);
                        //taskManager.addTask(responseBodyBuffer);
                        out.write(responseBuffer, 0, bytesRead);
                        out.flush();

                        //saveFrameToFile(responseHeader, responseJsonBody, expected); //for creating tests

                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });

            forwardThread.start();
            reverseThread.start();

            forwardThread.join();
            reverseThread.join();

        } catch (IOException | InterruptedException e) {
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
        ObjectNode body = (ObjectNode) objectMapper.readTree(responseJsonBody);

        if (body.get("nameKey") != null) {
            Optional<CoffeeOrder> existingMessage = coffeeOrderRepository.findByProductName(body.get("nameKey").asText());

            if (existingMessage.isPresent()) {
                CoffeeOrder message = existingMessage.get();
                message.setProductPriceSumm(message.getProductPriceSumm() + body.get("ProductAmount").asInt() / 100);
                message.setProductRepeat(message.getProductRepeat() + 1);
                coffeeOrderRepository.save(message);
            } else {
                CoffeeOrder newMessage = new CoffeeOrder();
                newMessage.setProductName(body.get("nameKey").asText());
                newMessage.setProductLastPrice(body.get("ProductAmount").asInt() / 100);
                newMessage.setProductPriceSumm(body.get("ProductAmount").asInt() / 100);
                newMessage.setProductRepeat(1);
                coffeeOrderRepository.save(newMessage);
            }
        }
    }

    /*
    public static void sendCommandToMachine(byte[] data) throws IOException {
        if (messageSender != null) {
            //messageSender.sendMessage(data);
        } else {
            System.out.println("No active connections!");
        }
    }

     */

    private void getChinaHeader(byte[] buffer) {
        for (int i = 0; i < buffer.length; i++)
            buffer[i] = (byte) (buffer[i] - 48);
    }

    private int getPacketSize(byte[] headerBuffer) {
        ByteBuffer byteBuffer = ByteBuffer.wrap(headerBuffer);
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        return byteBuffer.getInt() - 12; //12 - size of header
    }

    private void saveFrameToFile(byte[] header, String body, String filename) throws IOException{
        try (FileOutputStream fos = new FileOutputStream(filename, true)){
            fos.write("Header : " .getBytes(StandardCharsets.UTF_8));
            fos.write(header);
            fos.write('\n');
        }

        try (FileWriter writer = new FileWriter(filename, true)) {
            writer.write("Body : " + body + "\n\n");

        }
    }

    @Deprecated
    private void saveChinaMessage(String jsonBody) {
        Optional<ChinaMessage> existingMessage = chinaMessageRepository.findByMessage(jsonBody);

        if (existingMessage.isPresent()) {
            ChinaMessage message = existingMessage.get();
            message.setRepeatCount(message.getRepeatCount() + 1);
            chinaMessageRepository.save(message);
        } else {
            ChinaMessage newMessage = new ChinaMessage();
            newMessage.setMessage(jsonBody.length() > 250 ? jsonBody.substring(0, 250) : jsonBody);
            newMessage.setRepeatCount(1);
            chinaMessageRepository.save(newMessage);
        }
    }

    @Deprecated
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
}