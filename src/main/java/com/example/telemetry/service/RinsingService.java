package com.example.telemetry.service;

import com.example.telemetry.manager.MachinesManager;
import com.example.telemetry.model.MachineInterface;
import com.example.telemetry.model.Rinsing;
import com.example.telemetry.model.RinsingType;
import com.example.telemetry.repository.RinsingRepository;
import com.example.telemetry.repository.RinsingTypeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class RinsingService {
    private final RinsingRepository rinsingRepository;
    private final MachinesManager machinesManager;
    private final RinsingTypeRepository rinsingTypeRepository;

    @Autowired
    public RinsingService(RinsingRepository rinsingRepository, MachinesManager machinesManager, RinsingTypeRepository rinsingTypeRepository) {
        this.rinsingRepository = rinsingRepository;
        this.machinesManager = machinesManager;
        this.rinsingTypeRepository = rinsingTypeRepository;
    }

    private String getDescriptionByCode(String code) {
        RinsingType rinsingType = rinsingTypeRepository.findByRinsingCode(code);
        if (rinsingType != null) {
            return rinsingType.getRinsingDescription();
        }
        return "Rinsing description not found";
    }

    public Map<String, Object> getRinsingRecords(int deviceId) {
        List<Rinsing> rinsings = rinsingRepository.findByVmcNumber(deviceId);

        MachineInterface machineInterface = machinesManager.getMachineInterfaceById(deviceId);

        List<Map<String, Object>> rinsingResult = rinsings.stream().map(rinsing -> {
            Map<String, Object> rinsingInfo = new HashMap<>();
            rinsingInfo.put("RinsingCode", rinsing.getRinsingCode());
            rinsingInfo.put("Time", rinsing.getDateTime());
            rinsingInfo.put("CUid", rinsing.getcUid());
            rinsingInfo.put("isOK", rinsing.isOK());
            rinsingInfo.put("RinsingDescription", getDescriptionByCode(rinsing.getRinsingCode()));

            return rinsingInfo;
        }).toList();

        Map<String, Object> result = new HashMap<>();

        if (machineInterface != null) {
            result.putAll(machineInterface.getMachineInfo());
        }
        result.put("products", rinsingResult);

        return result;
    }

    public Map<String, Object> getRinsingRecordsByDateRange(int deviceId, LocalDateTime startDate, LocalDateTime endDate) {
        List<Rinsing> rinsings = rinsingRepository.findByDateRange(deviceId, startDate, endDate);

        MachineInterface machineInterface = machinesManager.getMachineInterfaceById(deviceId);

        List<Map<String, Object>> rinsingResult = rinsings.stream().map(rinsing -> {
            Map<String, Object> rinsingInfo = new HashMap<>();
            rinsingInfo.put("RinsingCode", rinsing.getRinsingCode());
            rinsingInfo.put("Time", rinsing.getDateTime());
            rinsingInfo.put("CUid", rinsing.getcUid());
            rinsingInfo.put("IsOk", rinsing.isOK());
            rinsingInfo.put("RinsingDescription", getDescriptionByCode(rinsing.getRinsingCode()));

            return rinsingInfo;
        }).toList();

        Map<String, Object> result = new HashMap<>();

        if (machineInterface != null) {
            result.putAll(machineInterface.getMachineInfo());
        }
        result.put("products", rinsingResult);

        return result;
    }
}
