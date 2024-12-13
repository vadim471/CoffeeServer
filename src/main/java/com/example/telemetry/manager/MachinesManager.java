package com.example.telemetry.manager;

import com.example.telemetry.generator.ResponseGenerator;
import com.example.telemetry.model.MachineInterface;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.InetAddress;

import java.net.Socket;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;


@Service
public class MachinesManager {

    @Value("${poolSize}")
    private int poolSize;

    private HashMap<InetAddress, MachineInterface> machineMap = new HashMap<>();

    private final ResponseGenerator responseGenerator;

    @Autowired
    public MachinesManager(ResponseGenerator responseGenerator) {
        this.responseGenerator = responseGenerator;
    }

    public void acceptConnection(Socket clientSocket) throws IOException {
        if (!machineMap.containsKey(clientSocket.getInetAddress())) {
            machineMap.put(clientSocket.getInetAddress(), new MachineInterface(clientSocket, responseGenerator));
        }
        machineMap.get(clientSocket.getInetAddress()).handleRequest(clientSocket);
    }

    public CompletableFuture<byte[]> handleRequest(InetAddress ip, byte[] request) {
        MachineInterface machine = machineMap.get(ip);
       //int vmc_no = machineMap.get(id).getType();
        if (machine != null) {
            return machine.sendToMachineFromController(request);
        }
        return null;
    }

    public int getVmcNumber(InetAddress ip) {
        MachineInterface machine = machineMap.get(ip);
        return machine.getVmcNumber();
    }
}
