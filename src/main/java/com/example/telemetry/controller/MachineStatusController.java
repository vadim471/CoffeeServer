package com.example.telemetry.controller;

import com.example.telemetry.service.MachineStatus;
import com.example.telemetry.service.ResponseService;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

@Controller
public class MachineStatusController {

    private final ResponseService responseService;

    @Autowired
    public MachineStatusController(ResponseService responseService) {
        this.responseService = responseService;
    }

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
}
