package com.example.telemetry.repository;

import com.example.telemetry.model.MachineActivity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MachineActivityRepository extends JpaRepository<MachineActivity, Long> {
    Optional<MachineActivity> findByVmcNumber(int vmcNumber);
}
