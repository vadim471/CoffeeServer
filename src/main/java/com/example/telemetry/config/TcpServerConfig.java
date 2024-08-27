package com.example.telemetry.config;



import com.example.telemetry.model.Task;
import com.example.telemetry.service.MessageSender;
import com.example.telemetry.service.ResponseService;
import com.example.telemetry.service.TaskManager;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.annotation.PostConstruct;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
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


@Configuration
public class TcpServerConfig {
    @Value("${tcp.server.port}")
    private int port;

    private final ResponseService responseService;
    private MessageSender messageSender;
    private ServerSocket serverSocket;
    private static final Logger logger = LogManager.getLogger(TcpServerConfig.class);

    @Autowired
    public TcpServerConfig(ResponseService responseService){
        this.responseService = responseService;
    }

    @PostConstruct
    public void startServer() {
        new Thread(this :: runServer).start();
    }

    private void runServer() {
        try {
            serverSocket = new ServerSocket(port);
            System.out.println("TCP server started on port " + port);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Accepted connection from " + clientSocket.getInetAddress());
                System.out.println(System.getProperty("log4j.configurationFile"));
                handleClient(clientSocket, responseService);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleClient(Socket clientSocket, ResponseService responseService) {
        new Thread(() -> {
            try (InputStream in = clientSocket.getInputStream();
                 OutputStream out = clientSocket.getOutputStream()) {
                 messageSender = new MessageSender(out);

                byte[] header = new byte[4];
                byte[] buffer = new byte[1024];

                while ((in.read(buffer)) != -1) {

                    System.arraycopy(buffer, 0, header, 0, 4);
                    getChinaHeader(header);
                    int bodyLength = getPacketSize(header);
                    byte[] bodyBuffer = new byte[bodyLength];

                    System.arraycopy(buffer, 12, bodyBuffer, 0, bodyLength);

                    String jsonBody = new String(bodyBuffer, StandardCharsets.UTF_8);
                    System.out.println("Received: " + jsonBody);
                    ObjectNode body = (ObjectNode) responseService.getObjectMapper().readTree(jsonBody);

                    Task task = new Task(body.get("cmd").asText(), body);

                    if (!Objects.equals(body.get("cmd").asText(), "hb"))
                        logger.info(jsonBody);

                    byte[] responseFrame = responseService.processTelemetry(task);
                    messageSender.sendMessage(responseFrame);
                }


            } catch (IOException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    clientSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public void sendCommandToMachine(byte[] data) throws IOException {
        if (messageSender != null) {
            messageSender.sendMessage(data);
        } else {
            System.out.println("No active connections!");
        }
    }
    private void getChinaHeader(byte[] buffer){
        for (int i = 0; i < buffer.length; i++)
            buffer[i] = (byte) (buffer[i] - 48);
    }

    private int getPacketSize(byte[] headerBuffer){
        ByteBuffer byteBuffer = ByteBuffer.wrap(headerBuffer);
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        return byteBuffer.getInt() - 12; //12 - size of header
    }
}