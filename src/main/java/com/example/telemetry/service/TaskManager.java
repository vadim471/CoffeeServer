package com.example.telemetry.service;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Класс для хранения всех сообщений в очереди (запросы от сервиса или входящие от вендингового аппарата).
 */
@Service
public class TaskManager {
    private final BlockingQueue<byte[]> taskQueue                                            = new LinkedBlockingQueue<>();
    private final SocketStreamManager socketStreamManager;


    @Autowired
    public TaskManager(SocketStreamManager socketStreamManager) {
        this.socketStreamManager = socketStreamManager;

    }

    public void addTask(byte[] byteMessage) {
        try {
            taskQueue.put(byteMessage);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @PostConstruct
    public void init(){
        Thread taskProcessorThread = new Thread(this::processTask);
        taskProcessorThread.setDaemon(true);
        taskProcessorThread.start();
    }

    private void processTask() {
        while (true) {
            try {
                byte[] arrByte = taskQueue.take();
                handleTask(arrByte);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void handleTask(byte[] byteMessage) throws IOException {
        try {
            //byte[] response = responseService.processTelemetry(task);

            if (socketStreamManager.hasStreams()) {
                OutputStream out = socketStreamManager.getOutputStream();
                out.write(byteMessage);
                out.flush();
            }
        } catch (Exception e){
            e.printStackTrace();
        }
    }
}
