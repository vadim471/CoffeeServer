package com.example.telemetry.controller;

import com.example.telemetry.model.TelemetryData;
import com.example.telemetry.repository.TelemetryDataRepository;
import com.example.telemetry.service.ErrorService;
import com.example.telemetry.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Класс для отображения заказов из БД (страница в браузере)
 */
@Controller
public class TelemetryController {
    private final TelemetryDataRepository telemetryDataRepository;
    private final OrderService orderService;
    private final ErrorService errorService;

    @Autowired
    public TelemetryController(TelemetryDataRepository telemetryDataRepository, OrderService orderService, ErrorService errorService) {
        this.telemetryDataRepository = telemetryDataRepository;
        this.orderService = orderService;
        this.errorService = errorService;
    }

    @GetMapping("/")
    public String index(Model model) {
        List<TelemetryData> telemetryDataList = telemetryDataRepository.findAll();

        model.addAttribute("telemetryDataList", telemetryDataList);
        return "index";
    }


    /**
     * 4 point API
     * for extract orders
     *
     * @param deviceid
     * @param startDate
     * @param endDate
     */
    @GetMapping("/order")
    public ResponseEntity<Map<String, Object>> getOrders(
            @RequestParam("deviceid") int deviceid,
            @RequestParam("dates") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam("datef") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {

        //if input params date is yyyy-mm-dd - LocalDate
        //startDate = startDate.toLocalDate().atStartOfDay();
        //endDate = endDate.atTime(LocalTime.MAX);
        Map<String, Object> orders = orderService.getOrdersByDateRange(startDate, endDate, deviceid);
        return ResponseEntity.ok(orders);
    }

    /**
     * 7 point API
     * for extract errors
     *
     * @param deviceid
     */
    @GetMapping("/faulty")
    public ResponseEntity<Map<String, Object>> getErrors(
            @RequestParam int deviceid,
            @RequestParam(required = false)@DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false)@DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {

        Map<String, Object> errors;

        if (startDate != null && endDate != null) {
            errors = errorService.getErrorsByDateRange(deviceid, startDate, endDate);
        } else {
            errors = errorService.getErrors(deviceid);
        }
        return ResponseEntity.ok(errors);
    }




}
