package com.example.telemetry.factory;


import com.example.telemetry.model.MachineInterface;
import org.springframework.beans.factory.annotation.Value;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ConnectionFactory {

    @Value("${poolSize}")
    private int poolSize;

    @Value("${tcp.china_server.port}")
    private int proxyPort;

    @Value("${china_server_ip}")
    private String proxyIp;

    private HashMap<InetAddress, MachineInterface> machineMap;

    final ExecutorService executorService;

    public ConnectionFactory() {
        this.executorService = Executors.newFixedThreadPool(2);
    }

    public void acceptConnection(Socket clientSocket) throws IOException {
        if (!machineMap.containsKey(clientSocket.getInetAddress())) {
            machineMap.put(clientSocket.getInetAddress(), new MachineInterface(clientSocket.getInetAddress()));
        }
        //Socket chinaSocket = new Socket(proxyIp, proxyPort);
        machineMap.get(clientSocket.getInetAddress()).handleRequest(clientSocket);
    }

    public void shutDown() {
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
            executorService.shutdownNow();
        }
    }
}
