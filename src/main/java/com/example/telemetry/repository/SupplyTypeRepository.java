package com.example.telemetry.repository;

import com.example.telemetry.model.SupplyType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SupplyTypeRepository extends JpaRepository<SupplyType, Long> {
    SupplyType findBySupplyId(int supplyId);
}
