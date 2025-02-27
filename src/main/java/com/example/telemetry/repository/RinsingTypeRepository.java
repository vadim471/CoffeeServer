package com.example.telemetry.repository;

import com.example.telemetry.model.RinsingType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RinsingTypeRepository extends JpaRepository<RinsingType, Long> {
    RinsingType findByRinsingCode(String code);
}
