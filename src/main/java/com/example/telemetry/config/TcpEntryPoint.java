package com.example.telemetry.config;


import com.example.telemetry.repository.CoffeeOrderRepository;
import com.example.telemetry.factory.ConnectionFactory;
import com.example.telemetry.manager.ResponseManager;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    @Value("${tcp.china_server.port}")
    private int chinaPort;

    @Value("${china_server_ip}")
    private String chinaIp;

    @Value("${inputFrames}")
    private String input;

    @Value("${expectedFrames}")
    private String expected;

    private final CoffeeOrderRepository coffeeOrderRepository;

    private ServerSocket serverSocket;
    private static final Logger logger                                  = LoggerFactory.getLogger(TcpEntryPoint.class);
    private int hb_counter                                              = 0; ///< счетчик полученных heartbeat за время, возможно понадобится для тестов

    private final ResponseManager responseService;
    private final ConnectionFactory connectionFactory;


    @Autowired
    public TcpEntryPoint(ResponseManager responseService, CoffeeOrderRepository coffeeOrderRepository) {
        this.coffeeOrderRepository = coffeeOrderRepository;
        this.connectionFactory = new ConnectionFactory();
        this.responseService = responseService;

    }

    @PostConstruct
    public void startServer() {
        new Thread( () -> {
            this.runServer(port);
        }).start();
        //new thread for incoming requests

    }

    @PreDestroy
    public void stopServer() {
        connectionFactory.shutDown();
        try {
            if (serverSocket != null) {
                serverSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
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