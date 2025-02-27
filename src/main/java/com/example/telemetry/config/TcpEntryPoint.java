package com.example.telemetry.config;


import com.example.telemetry.manager.MachinesManager;
import com.example.telemetry.repository.CoffeeOrderRepository;
import com.example.telemetry.generator.ResponseGenerator;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;


@Configuration
public class TcpEntryPoint {
    @Value("${tcp.server.port}")
    private int port;

    @Value("${inputFrames}")
    private String input;

    @Value("${expectedFrames}")
    private String expected;

    private ServerSocket serverSocket;

    private final MachinesManager connectionFactory;


    @Autowired
    public TcpEntryPoint(MachinesManager connectionFactory) {
        this.connectionFactory = connectionFactory;

    }

    @PostConstruct
    public void startServer() {
        new Thread( () -> {
            this.runServer(port);
        }).start();
        //new thread for incoming requests

    }

    private void runServer(int port) {
        try {
            serverSocket = new ServerSocket(port);

            System.out.println("TCP server started on port " + port);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Accepted connection from " + clientSocket.getInetAddress());
                System.out.println(clientSocket.getPort());

                connectionFactory.acceptConnection(clientSocket);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}