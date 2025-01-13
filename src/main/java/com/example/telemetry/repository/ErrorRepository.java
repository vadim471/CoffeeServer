package com.example.telemetry.repository;

import com.example.telemetry.model.Error;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ErrorRepository extends JpaRepository<Error, Long> {
    Optional<Error> findByVmcNumberAndFaultCode(int vmcNumber, int faultCode);
}
