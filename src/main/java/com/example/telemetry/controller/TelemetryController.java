package com.example.telemetry.controller;

import com.example.telemetry.model.TelemetryData;
import com.example.telemetry.repository.TelemetryDataRepository;
import com.example.telemetry.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

/**
 * Класс для отображения заказов из БД (страница в браузере)
 */
@Controller
public class TelemetryController {
    private final TelemetryDataRepository telemetryDataRepository;
    private final OrderService orderService;

    @Autowired
    public TelemetryController(TelemetryDataRepository telemetryDataRepository, OrderService orderService) {
        this.telemetryDataRepository = telemetryDataRepository;
        this.orderService = orderService;
    }

    @GetMapping("/")
    public String index(Model model) {
        List<TelemetryData> telemetryDataList = telemetryDataRepository.findAll();

        model.addAttribute("telemetryDataList", telemetryDataList);
        return "index";
    }


    @GetMapping("/order")
    public ResponseEntity<Map<String, Object>> getOrders(
            @RequestParam("deviceid") int vmcNumber,
            @RequestParam("dates") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam("datef") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {

        //if input params date is yyyy-mm-dd - LocalDate
        //startDate = startDate.toLocalDate().atStartOfDay();
        //endDate = endDate.atTime(LocalTime.MAX);
        Map<String, Object> orders = orderService.getOrdersByDateRange(startDate, endDate, vmcNumber);
        return ResponseEntity.ok(orders);
    }




}
