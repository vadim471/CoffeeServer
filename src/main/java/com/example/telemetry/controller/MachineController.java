package com.example.telemetry.controller;

import com.example.telemetry.generator.RequestGenerator;
import com.example.telemetry.manager.MachinesManager;
import com.example.telemetry.model.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.net.InetAddress;
import java.util.concurrent.CompletableFuture;

/**
 * Класс, созданный для отправки запросов на вендинговый аппарат. Должен использоваться Rabbit'ом для обработки входящих
 * запросов от ОПТИМы
 */
@RestController
public class MachineController {

    private final RequestGenerator requestGenerator;
    private final MachinesManager machinesManager;

    @Autowired
    public MachineController(RequestGenerator requestGenerator, MachinesManager machinesManager) {
        this.requestGenerator = requestGenerator;
        this.machinesManager = machinesManager;
    }

    @GetMapping("/ping")
    public HttpStatus ping() {
        return HttpStatus.resolve(200);
    }

    //Can exclude cmd
    //4 point part for recipe
    @PostMapping("/setrecipes")
    public void sendRecipe(@RequestParam String ip) {
        try {
            InetAddress machineAddress = InetAddress.getByName(ip);
            int vmcNumber = machinesManager.getVmcNumber(machineAddress);
            byte[] response = requestGenerator.processTelemetry("upgrade", vmcNumber, "recipe");

            CompletableFuture<byte[]> bytes = machinesManager.handleRequest(machineAddress, response);
            if (bytes != null) {
                 bytes.whenComplete((res, error) -> {
                    System.out.println(res);
                 });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @PostMapping("/setproducts")
    public void send(@RequestParam String ip) {
        try {
            InetAddress machineAddress = InetAddress.getByName(ip);
            int vmcNumber = machinesManager.getVmcNumber(machineAddress);
            byte[] response = requestGenerator.processTelemetry("upgrade", vmcNumber, "recipe");

            CompletableFuture<byte[]> bytes = machinesManager.handleRequest(machineAddress, response);
            if (bytes != null) {
                bytes.whenComplete((res, error) -> {
                    System.out.println(res);
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @PostMapping("/supply")
    public void getMachineStatus(@RequestParam String ip) {
        try {
            InetAddress machineAddress = InetAddress.getByName(ip);
            int vmcNumber = machinesManager.getVmcNumber(machineAddress);
            byte[] response = requestGenerator.processTelemetry("remote", vmcNumber, "sync");

            CompletableFuture<byte[]> bytes = machinesManager.handleRequest(machineAddress, response);
                if (bytes != null) {
                bytes.whenComplete((res, error) -> {
                    Task responseTask = requestGenerator.generateTaskFromResponseBytes(res);
                    System.out.println("FROM CONTROLLER!!!11" + responseTask.getBody());
                });
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


/*
    @PostMapping("/remote")
    public ResponseEntity<Void> getReconfiguration(@RequestBody Map<String, Object> request) {
        try {
            String operation = ((String) request.get("operation")).toUpperCase();
            RemoteOperations remoteOperations = RemoteOperations.valueOf(operation);

            ObjectNode jsonNode = responseGenerator.getObjectMapper().createObjectNode();
            jsonNode.put("cmd", "remote");
            jsonNode.put("vmc_no", 55418);
            jsonNode.put("operation", remoteOperations.getOperation());

            Task task = new Task(jsonNode.get("cmd").asText(), jsonNode);
            //taskManager.addTask(task); rewrite on byte array

            return ResponseEntity.ok().build();
        } catch (Exception e){
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

  */

}
