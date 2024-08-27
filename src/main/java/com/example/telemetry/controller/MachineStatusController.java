package com.example.telemetry.controller;

import com.example.telemetry.enums.RemoteOperations;
import com.example.telemetry.model.Task;
import com.example.telemetry.service.ResponseService;
import com.example.telemetry.service.TaskManager;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class MachineStatusController {

    private final TaskManager taskManager;
    private final ResponseService responseService;

    @Autowired
    public MachineStatusController(TaskManager taskManager, ResponseService responseService) {
        this.taskManager = taskManager;
        this.responseService = responseService;
    }

 /*
    @PostMapping("/machinestatus")
    public void getMachineStatus(@RequestBody Map<String, Object> request) {
        try {


            String status = (String) request.get("status");
            MachineStatus machineStatus = MachineStatus.valueOf(status);

            ObjectNode jsonNode = responseService.getObjectMapper().createObjectNode();
            jsonNode.put("cmd", "machinestatus");
            jsonNode.put("vmc_no", (Integer) request.get("vmc_no"));
            jsonNode.put("status", machineStatus.getStatus());

            responseService.processTelemetry(jsonNode);



        } catch (Exception e) {
            e.printStackTrace();
        }

    }
    */


    @PostMapping("/remote")
    public ResponseEntity<Void> getReconfiguration(@RequestBody Map<String, Object> request) {
        try {
            String operation = ((String) request.get("operation")).toUpperCase();
            RemoteOperations remoteOperations = RemoteOperations.valueOf(operation);

            ObjectNode jsonNode = responseService.getObjectMapper().createObjectNode();
            jsonNode.put("cmd", "remote");
            jsonNode.put("vmc_no", 55418);
            jsonNode.put("operation", remoteOperations.getOperation());

            Task task = new Task(jsonNode.get("cmd").asText(), jsonNode);
            taskManager.addTask(task);

            return ResponseEntity.ok().build();
        } catch (Exception e){
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

}
