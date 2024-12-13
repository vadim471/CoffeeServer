package com.example.telemetry.repository;

import com.example.telemetry.model.TelemetryData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TelemetryDataRepository extends JpaRepository<TelemetryData, Long> {

    Optional<TelemetryData> findByDate(LocalDateTime date);

    @Query("SELECT t FROM TelemetryData t WHERE t.date BETWEEN :dates AND :datef AND t.vmc_number = :deviceid ORDER BY t.date ASC, t.vmc_number ASC")
    List<TelemetryData> findByDateRange(@Param("dates") LocalDateTime startDate,
                                        @Param("datef") LocalDateTime endDate,
                                        @Param("deviceid") int deviceid);
}
