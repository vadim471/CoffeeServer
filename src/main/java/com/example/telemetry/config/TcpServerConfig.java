package com.example.telemetry.config;



import com.example.telemetry.service.MessageSender;
import com.example.telemetry.service.ResponseService;
import jakarta.annotation.PostConstruct;
import org.apache.logging.log4j.message.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;


import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;


@Configuration
public class TcpServerConfig {
    @Value("${tcp.server.port}")
    private int port;

    private final ResponseService responseService;

    private ServerSocket serverSocket;

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
                MessageSender messageSender = new MessageSender(out);

                byte[] header = new byte[4];
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = in.read(buffer)) != -1) {
                    String inputLine = new String(buffer, 0, bytesRead);
                    //System.out.println("Received: " + inputLine);

                    System.arraycopy(buffer, 0, header, 0, 4);
                    getChinaHeader(header);
                    int bodyLength = getPacketSize(header);
                    byte[] bodyBuffer = new byte[bodyLength];

                    System.arraycopy(buffer, 12, bodyBuffer, 0, bodyLength);

                    byte[] responseData = responseService.processTelemetry(bodyBuffer);
                    messageSender.sendMessage(responseData);
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