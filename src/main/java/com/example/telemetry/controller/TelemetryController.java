package com.example.telemetry.controller;

import com.example.telemetry.model.TelemetryData;
import com.example.telemetry.repository.TelemetryDataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * Класс для отображения заказов из БД (страница в браузере)
 */
@Controller
public class TelemetryController {
    private final TelemetryDataRepository telemetryDataRepository;

    @Autowired
    public TelemetryController(TelemetryDataRepository telemetryDataRepository) {
        this.telemetryDataRepository = telemetryDataRepository;
    }

    @GetMapping("/")
    public String index(Model model) {
        List<TelemetryData> telemetryDataList = telemetryDataRepository.findAll();

        model.addAttribute("telemetryDataList", telemetryDataList);
        return "index";
    }


}
