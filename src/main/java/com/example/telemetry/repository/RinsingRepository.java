package com.example.telemetry.repository;

import com.example.telemetry.model.Rinsing;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface RinsingRepository extends JpaRepository<Rinsing, Long> {

    @Query("SELECT t FROM Rinsing t WHERE t.dateTime BETWEEN :dates AND :datef AND t.vmcNumber = :deviceid ORDER BY t.dateTime")
    List<Rinsing> findByDateRange(@Param("deviceid") int deviceid,
                                  @Param("dates") LocalDateTime startDate,
                                  @Param("datef") LocalDateTime endDate);

    List<Rinsing> findByVmcNumber(int vmcNumber);
}
