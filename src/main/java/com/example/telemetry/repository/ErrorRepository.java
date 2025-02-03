package com.example.telemetry.repository;

import com.example.telemetry.model.Error;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ErrorRepository extends JpaRepository<Error, Long> {

    List<Error> findByVmcNumber(int vmcNumber);

    @Query("SELECT t FROM Error t WHERE t.occuredTime BETWEEN :dates AND :datef AND t.vmcNumber = :deviceid ORDER BY t.occuredTime")
    List<Error> findByDateRange(@Param("deviceid") int deviceid,
                                @Param("dates") LocalDateTime startDate,
                                @Param("datef") LocalDateTime endDate
                                );
}
