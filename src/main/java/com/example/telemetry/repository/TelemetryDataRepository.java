package com.example.telemetry.repository;

import com.example.telemetry.model.TelemetryData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface TelemetryDataRepository extends JpaRepository<TelemetryData, Long> {

    Optional<TelemetryData> findByDate(LocalDateTime date);
}
