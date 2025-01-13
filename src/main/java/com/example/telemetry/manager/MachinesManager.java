package com.example.telemetry.manager;

import com.example.telemetry.generator.ResponseGenerator;
import com.example.telemetry.model.MachineInterface;
import jakarta.annotation.PostConstruct;
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

    @Value("${tcp.china_server.port}")
    private int proxyPort;

    @Value("${china_server_ip}")
    private String proxyIp;

    private HashMap<InetAddress, MachineInterface> machineMap = new HashMap<>();
    private HashMap<Integer, InetAddress> machineIdToIp = new HashMap<>();
    private final ResponseGenerator responseGenerator;

    @Autowired
    public MachinesManager(ResponseGenerator responseGenerator) {
        this.responseGenerator = responseGenerator;
    }

    public void acceptConnection(Socket clientSocket) throws IOException {
        if (!machineMap.containsKey(clientSocket.getInetAddress())) {
            MachineInterface machineInterface =  new MachineInterface(clientSocket, responseGenerator, proxyIp, proxyPort);
            InetAddress clientAddress = clientSocket.getInetAddress();
            machineMap.put(clientAddress, machineInterface);
            machineIdToIp.put(MachineInterface.getVmcNumber(), clientAddress);
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

    public InetAddress getInetAddress(int id) {
        return machineIdToIp.get(id);
    }
}
