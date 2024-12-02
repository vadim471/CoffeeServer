package com.example.telemetry.model;

import com.example.telemetry.manager.ResponseManager;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.persistence.Tuple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.util.function.Tuple2;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

public class MachineInterface {

    public Queue<byte[]> queue;
    private static final Logger logger = LoggerFactory.getLogger(MachineInterface.class);
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final InetAddress machineIp;

    @Autowired
    private ResponseManager responseManager;

    public MachineInterface(InetAddress ip) throws IOException {

        this.queue = new ConcurrentLinkedQueue<>();
        this.machineIp = ip;

    }

    public void handleRequest(Socket socket) {
        new Thread(() -> {
            //TODO involve getInputBytes
            //TODO proxy to China
            processRequest(socket);
        }).start();
    }

    private MachineRequest getInputBytes(Socket socket) throws IOException {
        ArrayList<byte[]> list = new ArrayList<>();
        byte[] buffer = new byte[4096];
        byte[] header = new byte[4];
        int bytesRead;
        bytesRead = socket.getInputStream().read(buffer);
        System.arraycopy(buffer, 0, header, 0, 4);

        getChinaHeader(header);
        int bodyLength = getPacketSize(header);
        Collections.addAll(list, buffer);
        while ((bytesRead = socket.getInputStream().read(buffer)) != -1) {
            Collections.addAll(list, buffer);
        }

        byte[] bodyBuffer = new byte[bodyLength];

        System.arraycopy(list, 12, bodyBuffer, 0, bodyLength);

        MachineRequest machineRequest = new MachineRequest();
        machineRequest.setHeader(header);
        machineRequest.setBody(bodyBuffer);
        return machineRequest;
    }

    /**
     * Метод, который принимает сообщения от аппарата и генерирует ответ на него.
     */
    private void processRequest(Socket socket) {
        try {
            byte[] header = new byte[4];
            byte[] buffer = new byte[2048];
            int bytesRead;
            InputStream inputStream = socket.getInputStream();
            OutputStream outputStream = socket.getOutputStream();
            while ((bytesRead = inputStream.read(buffer)) != -1) {

                System.arraycopy(buffer, 0, header, 0, 4);

                getChinaHeader(header);
                int bodyLength = getPacketSize(header);
                byte[] bodyBuffer = new byte[bodyLength];

                System.arraycopy(buffer, 12, bodyBuffer, 0, bodyLength);

                String jsonBody = new String(bodyBuffer, StandardCharsets.UTF_8);

                //exclude may be?? for logging should use another method
                //not depend on implementation
                System.out.println("Received from server to machine: " + jsonBody);
                if (!jsonBody.contains("hb"))
                    logger.info(jsonBody);

                //
                ObjectNode body = (ObjectNode) objectMapper.readTree(jsonBody);
                Task task = new Task(body.get("cmd").asText(), body);
                byte[] response = responseManager.processTelemetry(task);
                //addRequest(response);
                outputStream.write(response, 0, response.length);
                outputStream.flush();

                //add to queue
                //add proxy to china
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Метод, который отправляет сообщения от сервера до аппарата.
     */
    private void handleReverseThread() {

    }

    public void addRequest(byte[] request) {
        queue.add(request);
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

    private void proxySend(OutputStream proxyOut, byte[] responseBuffer, int bytesWrite) throws IOException {
        proxyOut.write(responseBuffer, 0, bytesWrite);
        proxyOut.flush();
    }

    /*
    private byte[] proxyGet(InputStream proxyIn) {

    }

     */
}

class Manager {
    private final Map<String, MachineInterface> machineInterfaceMap = new HashMap<>();

    public void addMachine(String id, MachineInterface machineInterface) {
        machineInterfaceMap.put(id, machineInterface);
    }

    public void handleRequest(String id, byte[] request) {
        MachineInterface machine = machineInterfaceMap.get(id);
        if (machine != null) {
            machine.addRequest(request);
        }
    }

}
