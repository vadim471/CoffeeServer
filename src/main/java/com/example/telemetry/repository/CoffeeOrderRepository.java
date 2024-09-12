package com.example.telemetry.repository;

import com.example.telemetry.model.CoffeeMessage;
import com.example.telemetry.model.CoffeeOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CoffeeOrderRepository extends JpaRepository<CoffeeOrder, Long> {
    Optional<CoffeeOrder> findByProductName(String message);
}