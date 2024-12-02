package com.example.telemetry.factory;

import com.example.telemetry.manager.ResponseManager;
import com.example.telemetry.model.MachineInterface;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.net.Socket;

public class MachineInterfaceFactory {
    private final ResponseManager responseManager;

    @Autowired
    public MachineInterfaceFactory(ResponseManager responseManager) {
        this.responseManager = responseManager;
    }


    public MachineInterface create(Socket clientSocket) throws IOException {
        return new MachineInterface(clientSocket.getInetAddress());
    }
}
