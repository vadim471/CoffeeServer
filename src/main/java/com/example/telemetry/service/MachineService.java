package com.example.telemetry.service;

import com.example.telemetry.model.Machine;
import com.example.telemetry.repository.MachineRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class MachineService {
    private final MachineRepository machineRepository;

    public MachineService(MachineRepository machineRepository) {
        this.machineRepository = machineRepository;
    }

    public List<Machine> getListMachines() {
        return machineRepository.findAll();
    }

    public Optional<Machine> getMachineById(int deviceId) {
        return machineRepository.findByVmcNumber(deviceId);
    }
}
