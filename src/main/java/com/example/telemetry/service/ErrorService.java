package com.example.telemetry.service;

import com.example.telemetry.manager.MachinesManager;
import com.example.telemetry.model.Error;
import com.example.telemetry.model.MachineInterface;
import com.example.telemetry.repository.ErrorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ErrorService {
    private final ErrorRepository errorRepository;

    @Autowired
    private final MachinesManager machinesManager;

    public ErrorService(ErrorRepository errorRepository, MachinesManager machinesManager) {
        this.machinesManager = machinesManager;
        this.errorRepository = errorRepository;
    }

    public Map<String, Object> getErrors(int deviceId) {
        List<Error> errors = errorRepository.findByVmcNumber(deviceId);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        MachineInterface machineInterface = machinesManager.getMachineInterfaceById(deviceId);

        List<Map<String, Object>> errorResult = errors.stream().map(error -> {
            Map<String, Object> errorInfo = new HashMap<>();
            errorInfo.put("DeviceID", error.getVmcNumber());
            errorInfo.put("FaultyCode", error.getFaultCode());
            errorInfo.put("FaultyInfo", error.getFaultInfo());
            errorInfo.put("FaultyState", error.getFaultyState());
            errorInfo.put("OccuredTime", error.getOccuredTime().format(formatter));
            errorInfo.put("ClearTime", error.getClearTime());
            errorInfo.put("FaultDuration", error.getFaultDuration());

            return errorInfo;
        }).collect(Collectors.toList());

        Map<String, Object> result = new HashMap<>();

        if (machineInterface != null) {
            result.putAll(machineInterface.getMachineInfo());
        }
        result.put("products", errorResult);

        return result;
    }

    public Map<String, Object> getErrorsByDateRange(int deviceId, LocalDateTime startDate, LocalDateTime endDate) {
        List<Error> errors = errorRepository.findByDateRange(deviceId, startDate, endDate);

        MachineInterface machineInterface = machinesManager.getMachineInterfaceById(deviceId);


        List<Map<String, Object>> errorResult = errors.stream().map(error -> {
            Map<String, Object> errorInfo = new HashMap<>();
            errorInfo.put("DeviceID", error.getVmcNumber());
            errorInfo.put("FaultyCode", error.getFaultCode());
            errorInfo.put("FaultyInfo", error.getFaultInfo());
            errorInfo.put("FaultyState", error.getFaultyState());
            errorInfo.put("OccuredTime", error.getOccuredTime());
            errorInfo.put("ClearTime", error.getClearTime());
            errorInfo.put("FaultDuration", error.getFaultDuration());

            return errorInfo;
        }).collect(Collectors.toList());

        Map<String, Object> result = new HashMap<>();

        if (machineInterface != null) {
            result.putAll(machineInterface.getMachineInfo());
        }

        result.put("products", errorResult);

        return result;
    }
}
