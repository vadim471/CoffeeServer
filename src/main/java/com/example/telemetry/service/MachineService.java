package com.example.telemetry.service;

import com.example.telemetry.model.Machine;
import com.example.telemetry.model.MachineActivity;
import com.example.telemetry.repository.MachineActivityRepository;
import com.example.telemetry.repository.MachineRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class MachineService {
    private final MachineRepository machineRepository;
    private final MachineActivityRepository machineActivityRepository;

    public MachineService(MachineRepository machineRepository, MachineActivityRepository machineActivityRepository) {
        this.machineRepository = machineRepository;
        this.machineActivityRepository = machineActivityRepository;
    }

    public List<Machine> getListMachines() {
        return machineRepository.findAll();
    }

    public Optional<Machine> getMachineById(int deviceId) {
        return machineRepository.findByVmcNumber(deviceId);
    }

    public List<MachineActivity> getListMachineActivity() {
        return machineActivityRepository.findAll();
    }

    public Optional<MachineActivity> getMachineActivity(int deviceId) {
        return machineActivityRepository.findByVmcNumber(deviceId);
    }
}
