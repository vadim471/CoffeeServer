package com.example.telemetry.service;

import com.example.telemetry.config.TcpServerConfig;
import com.example.telemetry.model.Task;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

@Service
public class TaskManager {
    private final BlockingQueue<Task> taskQueue = new LinkedBlockingQueue<>();
    private final ResponseService responseService;
    private final TcpServerConfig tcpServerConfig;

    @Autowired
    public TaskManager(ResponseService responseService, TcpServerConfig tcpServerConfig) {
        this.responseService = responseService;
        this.tcpServerConfig = tcpServerConfig;
    }

    public void addTask(Task task) {
        try {
            taskQueue.put(task);
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
                Task task = taskQueue.take();
                handleTask(task);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void handleTask(Task task) throws IOException {
        try {
            byte[] response = responseService.processTelemetry(task);

            tcpServerConfig.sendCommandToMachine(response);
        } catch (Exception e){
            e.printStackTrace();
        }
    }
}
