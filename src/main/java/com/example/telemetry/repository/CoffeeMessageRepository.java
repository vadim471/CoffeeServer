package com.example.telemetry.repository;

import com.example.telemetry.model.CoffeeMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CoffeeMessageRepository extends JpaRepository<CoffeeMessage, Long> {
    Optional<CoffeeMessage> findByMessage(String message);
}