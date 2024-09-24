package com.example.telemetry.repository;

import com.example.telemetry.model.ChinaMessage;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ChinaMessageRepository extends JpaRepository<ChinaMessage, Long> {
    Optional<ChinaMessage> findByMessage(String message);
}