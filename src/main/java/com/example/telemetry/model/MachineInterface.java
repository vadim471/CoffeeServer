package com.example.telemetry.model;

import com.example.telemetry.generator.ResponseGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;

public class MachineInterface {
    private static final Logger logger = LoggerFactory.getLogger(MachineInterface.class);
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final InetAddress machineIp;
    private int machinePort;
    private ResponseGenerator responseGenerator;
    private Socket proxySocket;
    private int proxyPort;
    private String proxyIp;
    private static int vmcNumber;
    private OutputStream machineOutputStream;
    private InputStream machineInputStream;
    private static String softwareVersion;
    private static String ioVersion;
    private volatile boolean isControllerRequest = false;


    public static int getVmcNumber() {
        return vmcNumber;
    }

    private void init() {
        while (true) {
            try {
                this.proxySocket = new Socket(proxyIp, proxyPort);
                System.out.println("Connection success");
                break;
            } catch (IOException e) {
                System.out.println(e.getMessage());
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    break;
                }

            }
        }

    }

    public MachineInterface(Socket machineSocket, ResponseGenerator responseGenerator, String proxyIp, int proxyPort) throws IOException {
        this.machineIp = machineSocket.getInetAddress();
        this.machineOutputStream = machineSocket.getOutputStream();
        this.machineInputStream = machineSocket.getInputStream();
        this.responseGenerator = responseGenerator;
        this.machinePort = machineSocket.getPort();
        this.proxyIp = proxyIp;
        this.proxyPort = proxyPort;

        init();

        MachineRequest machineRequest = getInputBytes(machineSocket.getInputStream());
        byte[] response = handleLogin(machineRequest);

        machineSocket.getOutputStream().write(response, 0, response.length);
        machineSocket.getOutputStream().flush();
    }

    private void sendToProxy(byte[] array) {
        try {
            if (proxySocket == null || proxySocket.isClosed() || !proxySocket.isConnected()) {
                init();
            }

            proxySocket.getOutputStream().write(array);
            proxySocket.getOutputStream().flush();

        } catch (IOException e) {
            System.out.println("Error sending data to proxy: " + e.getMessage());
            try {
                if (proxySocket != null) {
                    proxySocket.close();
                }
            } catch (IOException ex) {
                System.out.println("Error closing proxy connection: " + ex.getMessage());
            }
            init();

        }
    }

    public void handleRequest(Socket socket) {
        new Thread(() -> {
            try {
                //proxySocket = new Socket(proxyIp, proxyPort);
                //method for sending message to server
                while (!socket.isClosed()) {
                    if (!isControllerRequest) {
                        OutputStream machineOutput = socket.getOutputStream();
                        InputStream machineInput = socket.getInputStream();
                        MachineRequest machineRequest = getInputBytes(machineInput);

                        if (machineRequest != null) {
                            byte[] response = processRequest(machineRequest);

                            byte[] machineByteRequest = createFrame(responseGenerator.generateHeader(machineRequest.getBody().length), machineRequest.getBody());
                            sendToProxy(machineByteRequest); //TODO checking server availability

                            machineOutput.write(response);
                            machineOutput.flush();
                        }

                    }
                }
            } catch (IOException e) {
                System.out.println("Client disconnected: " + e.getMessage());
            } finally {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private byte[] createFrame(byte[] header, byte[] body) {

        byte[] frame = new byte[header.length + body.length];

        System.arraycopy(header, 0, frame, 0, header.length);

        System.arraycopy(body, 0, frame, header.length, body.length);

        return frame;
    }

    public static String getIoVersion() {
        return ioVersion;
    }

    public static String getSoftwareVersion() {
        return softwareVersion;
    }

    private byte[] handleLogin(MachineRequest machineRequest) {
        Task task = responseGenerator.generateTaskFromBytes(machineRequest.getBody());

        byte[] machineByteRequest = createFrame(responseGenerator.generateHeader(machineRequest.getBody().length), machineRequest.getBody());
        sendToProxy(machineByteRequest);

        if (task != null) {
            byte[] response = responseGenerator.processTelemetry(task);
            vmcNumber = task.getBody().get("vmc_no").asInt();
            softwareVersion = task.getBody().get("version").asText();
            ioVersion = task.getBody().get("io_version").asText();

            return response;
        }
        return null;
    }

    public CompletableFuture<byte[]> sendToMachineFromController(byte[] array, String expectedFrame) {

        CompletableFuture<byte[]> arrayBytes = new CompletableFuture<>();

        new Thread(() -> {
            try {
                isControllerRequest = true;
                machineOutputStream.write(array, 0, array.length);
                machineOutputStream.flush();
                while (true) {
                    MachineRequest machineResponse = getInputBytes(machineInputStream);
                    if (machineResponse != null) {
                        String responseBody = new String(machineResponse.getBody(), StandardCharsets.UTF_8);
                        if (responseBody.contains(expectedFrame)) {
                            arrayBytes.complete(machineResponse.getBody());
                            break;
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
                arrayBytes.complete(new byte[0]);
            } finally {
                isControllerRequest = false;
            }
        }).start();

        return arrayBytes;
    }


    private MachineRequest getInputBytes(InputStream inputStream) throws IOException {
        ByteArrayOutputStream messageBuffer = new ByteArrayOutputStream();
        byte[] header = new byte[4];
        byte[] buffer = new byte[1024];
        byte[] nullBytes = new byte[8];

        int bytesRead;
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            messageBuffer.write(buffer, 0, bytesRead);

            if (messageBuffer.size() >= 12) {
                byte[] fullMessage = messageBuffer.toByteArray();

                System.arraycopy(fullMessage, 0, header, 0, 4);

                System.arraycopy(fullMessage, 4, nullBytes, 0, 8);
                int bodyLength = getPacketSize(header);

                if (messageBuffer.size() >= bodyLength + 12) {
                    byte[] bodyBuffer = new byte[bodyLength];
                    System.arraycopy(fullMessage, 12, bodyBuffer, 0, bodyLength);

                    MachineRequest machineRequest = new MachineRequest();
                    machineRequest.setHeader(header);
                    machineRequest.setBody(bodyBuffer);

                    messageBuffer.reset();
                    if (fullMessage.length > bodyLength + 12) {
                        messageBuffer.write(fullMessage, bodyLength + 12, fullMessage.length - (bodyLength + 12));
                    }

                    return machineRequest;
                }
            }
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


    private int getPacketSize(byte[] header) {
        int packetSize = 0;
        for (int i = 0; i < header.length; i++) {
            int byteValue = header[i] & 0xFF;

            int value = byteValue - 48;
            packetSize += value * Math.pow(256, i);
        }
        return packetSize - 12;
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


    private int getPacketSize(byte[] headerBuffer) {
        ByteBuffer byteBuffer = ByteBuffer.wrap(headerBuffer);
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        return byteBuffer.getInt() - 12; //12 - size of header
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

     */
}

