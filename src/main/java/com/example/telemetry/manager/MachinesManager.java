package com.example.telemetry.manager;

import com.example.telemetry.exceptions.MachineNotFoundException;
import com.example.telemetry.generator.ResponseGenerator;
import com.example.telemetry.model.Machine;
import com.example.telemetry.model.MachineInterface;
import com.example.telemetry.repository.MachineActivityRepository;
import com.example.telemetry.repository.MachineRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.HashMap;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;


@Service
public class MachinesManager {

    @Value("${tcp.china_server.port}")
    private int proxyPort;

    @Value("${china_server_ip}")
    private String proxyIp;

    private static HashMap<InetAddress, MachineInterface> machineMap = new HashMap<>(); //vmcNumber - interface
    private static HashMap<Integer, InetAddress> machineIdToIp = new HashMap<>(); //vmcNumber - ip
    private static HashMap<Integer, MachineInterface> machineInterfaceById = new HashMap<>(); //vmcNumber - interface
    private final ResponseGenerator responseGenerator;
    private final MachineRepository machineRepository;
    private final MachineActivityRepository machineActivityRepository;

    @Autowired
    public MachinesManager(ResponseGenerator responseGenerator, MachineRepository machineRepository, MachineActivityRepository machineActivityRepository) {
        this.responseGenerator = responseGenerator;
        this.machineRepository = machineRepository;
        this.machineActivityRepository = machineActivityRepository;
    }

    public void acceptConnection(Socket clientSocket) throws IOException {
        MachineInterface machineInterface = new MachineInterface(clientSocket, responseGenerator, proxyIp, proxyPort, machineActivityRepository);
        int vmcNumber = machineInterface.getVmcNumber();
        String softwareVersion = machineInterface.getSoftwareVersion();
        String ioVersion = machineInterface.getIoVersion();

        InetAddress clientAddress = clientSocket.getInetAddress();
        machineMap.put(clientAddress, machineInterface);
        machineIdToIp.put(vmcNumber, clientAddress);
        machineInterfaceById.put(vmcNumber, machineInterface);

        Optional<Machine> existingMachine = machineRepository.findByVmcNumber(vmcNumber);

        if (!existingMachine.isPresent()) {
            Machine newMachine = new Machine(vmcNumber, softwareVersion, ioVersion);
            machineRepository.save(newMachine);
        } else {
            Machine existedMachine = existingMachine.get();
            existedMachine.setSoftwareVersion(softwareVersion);
            existedMachine.setIoVersion(ioVersion);
            existedMachine.setActive(true);
            machineRepository.save(existedMachine);
        }

        machineMap.get(clientSocket.getInetAddress()).handleRequest(clientSocket);
    }

    public CompletableFuture<byte[]> handleRequest(InetAddress ip, byte[] request, String expectedFrame) {
        MachineInterface machine = machineMap.get(ip);
        if (machine != null) {
            return machine.sendToMachineFromController(request, expectedFrame);
        }
        return null;
    }

    public static void removeMachineFromMaps(int vmcNmber, InetAddress ip) {
        machineMap.remove(ip);
        machineIdToIp.remove(vmcNmber);
        machineInterfaceById.remove(vmcNmber);
    }

    public InetAddress getInetAddress(int id) throws MachineNotFoundException {
        InetAddress address = machineIdToIp.get(id);
        if (address == null) {
            throw new MachineNotFoundException("Machine with id: " + id + " not found");
        }
        return address;
    }

    public HashMap<Integer, InetAddress> getListActiveMachines() {
        return machineIdToIp;
    }

    public boolean isConnectedMachine(int id) throws MachineNotFoundException {
        boolean isConnected = machineIdToIp.containsKey(id);
        if (!isConnected) {
            throw new MachineNotFoundException("Machine with id: " + id + " not found");
        }
        return true;
    }

    public MachineInterface getMachineInterfaceById(int id) {
        return machineInterfaceById.get(id);

    }
}
