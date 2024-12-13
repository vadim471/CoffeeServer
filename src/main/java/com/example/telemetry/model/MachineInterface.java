package com.example.telemetry.model;

import com.example.telemetry.generator.ResponseGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class MachineInterface {

    @Value("${tcp.china_server.port}")
    private int proxyPort;

    @Value("${china_server_ip}")
    private String proxyIp;

    private static final Logger logger = LoggerFactory.getLogger(MachineInterface.class);
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final InetAddress machineIp;
    private int machinePort;
    private ResponseGenerator responseGenerator;
    private Socket proxySocket;
    private static int vmcNumber;
    private OutputStream machineOutputStream;
    private InputStream machineInputStream;
    private static String softwareVersion;
    private static String ioVersion;

    public static int getVmcNumber() {
        return vmcNumber;
    }

    public MachineInterface(Socket machineSocket, ResponseGenerator responseGenerator) throws IOException {
        this.machineIp = machineSocket.getInetAddress();
        this.machineOutputStream = machineSocket.getOutputStream();
        this.machineInputStream = machineSocket.getInputStream();
        this.responseGenerator = responseGenerator;
        this.machinePort = machineSocket.getPort();

        MachineRequest machineRequest = getInputBytes(machineSocket.getInputStream());
        byte[] response = handleLogin(machineRequest);
        machineSocket.getOutputStream().write(response, 0, response.length);
        machineSocket.getOutputStream().flush();
    }

    public void handleRequest(Socket socket) {
        new Thread(() -> {
            //TODO involve getInputBytes
            //TODO proxy to China
            try {
                //proxySocket = new Socket(proxyIp, proxyPort);
                //method for sending message to server
                while(true) {
                    OutputStream machineStream = socket.getOutputStream();
                    MachineRequest machineRequest = getInputBytes(socket.getInputStream());
                    byte[] response = processRequest(machineRequest);

                    machineStream.write(response);
                    machineStream.flush();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            //TODO delete socket from this method
            //при

        }).start();
    }

    private Byte[] castToByte(byte[] array) {
        Byte[] bytes = new Byte[array.length];
        for (int i = 0; i < array.length; i++) {
            bytes[i] = array[i];
        }
        return bytes;
    }

    private byte[] castToMiniByte(Byte[] array) {
        byte[] bytes = new byte[array.length];
        for (int i = 0; i < array.length; i++) {
            bytes[i] = array[i];
        }
        return bytes;
    }

    public static String getIoVersion() {
        return ioVersion;
    }

    public static String getSoftwareVersion() {
        return softwareVersion;
    }

    private byte[] handleLogin(MachineRequest machineRequest) {
        Task task = responseGenerator.generateTaskFromBytes(machineRequest.getBody());

        if (task != null) {
            byte[] response = responseGenerator.processTelemetry(task);
            vmcNumber = task.getBody().get("vmc_no").asInt();
            softwareVersion = task.getBody().get("version").asText();
            ioVersion = task.getBody().get("io_version").asText();
            return response;
        }
        return null;
    }

    public CompletableFuture<byte[]> sendToMachineFromController(byte[] array) {

        CompletableFuture<byte[]> arrayBytes = new CompletableFuture<>();
        try {
            //String mIp = machineIp.getHostAddress();
            //Socket clientSocket = new Socket(mIp, machinePort);
            //OutputStream out = clientSocket.getOutputStream();
            machineOutputStream.write(array, 0, array.length);
            machineOutputStream.flush();
            MachineRequest machineRequest = getInputBytes(machineInputStream);
            arrayBytes.complete(machineRequest.getBody());
            return arrayBytes;
        } catch (IOException e) {
            e.printStackTrace();
        }
        arrayBytes.complete(new byte[0]);
        return arrayBytes;
    }

    /*
    private MachineRequest getInputBytes(InputStream inputStream) throws IOException {
        ArrayList<Byte> list = new ArrayList<>();
        byte[] buffer = new byte[4096];
        byte[] header = new byte[4];
        int bytesRead;
        bytesRead = inputStream.read(buffer);
        System.arraycopy(buffer, 0, header, 0, 4);

        getChinaHeader(header);
        int bodyLength = getPacketSize(header);
        list.addAll(Arrays.asList(castToByte(buffer)));
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            list.addAll(Arrays.asList(castToByte(buffer)));
        }

        Byte[] bodyBuffer = list.toArray(new Byte[0]);

        MachineRequest machineRequest = new MachineRequest();
        machineRequest.setHeader(header);
        machineRequest.setBody(castToMiniByte(bodyBuffer));
        return machineRequest;

    }

     */
    private MachineRequest getInputBytes(InputStream inputStream) throws IOException {
        byte[] header = new byte[4];
        byte[] buffer = new byte[1024];
        MachineRequest machineRequest = new MachineRequest();

        int bytesRead;
        while ((bytesRead = inputStream.read(buffer)) != -1) {

            System.arraycopy(buffer, 0, header, 0, 4);

            getChinaHeader(header);
            int bodyLength = getPacketSize(header);
            byte[] bodyBuffer = new byte[bodyLength];

            System.arraycopy(buffer, 12, bodyBuffer, 0, bodyLength);

            //String jsonBody = new String(bodyBuffer, StandardCharsets.UTF_8);

            machineRequest.setHeader(header);
            machineRequest.setBody(bodyBuffer);
            return machineRequest;
        }
        return null;
    }

    /**
     * Метод, который принимает сообщения от аппарата и генерирует ответ на него.
     */
    private byte[] processRequest(MachineRequest machineRequest) {
        try {
            String jsonBody = new String(machineRequest.getBody(), StandardCharsets.UTF_8);

            //exclude may be?? for logging should use another method
            //not depend on implementation
            System.out.println("Received from machine to server: " + jsonBody);
            if (!jsonBody.contains("hb"))
                logger.info(jsonBody);


            ObjectNode body = (ObjectNode) objectMapper.readTree(jsonBody);
            Task task = new Task(body.get("cmd").asText(), body);
            byte[] response = responseGenerator.processTelemetry(task);

            return response;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new byte[0];
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

