package com.example.telemetry.service;

import com.example.telemetry.model.Error;
import com.example.telemetry.repository.ErrorRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.example.telemetry.model.MachineInterface.*;

@Service
public class ErrorService {
    private final ErrorRepository errorRepository;

    public ErrorService(ErrorRepository errorRepository) {
        this.errorRepository = errorRepository;
    }

    public Map<String, Object> getErrors(int deviceId) {
        List<Error> errors = errorRepository.findByVmcNumber(deviceId);

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
        result.put("deviceid", getVmcNumber());
        result.put("SoftwareVersion", getSoftwareVersion());
        result.put("IoVersion", getIoVersion());
        result.put("products", Collections.singletonList(errorResult));

        return result;
    }

    public Map<String, Object> getErrorsByDateRange(int deviceId, LocalDateTime startDate, LocalDateTime endDate) {
        List<Error> errors = errorRepository.findByDateRange(deviceId, startDate, endDate);

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
        result.put("deviceid", getVmcNumber());
        result.put("SoftwareVersion", getSoftwareVersion());
        result.put("IoVersion", getIoVersion());
        result.put("products", Collections.singletonList(errors));

        return result;
    }
}
