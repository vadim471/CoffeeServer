package com.example.telemetry.controller;


import com.example.telemetry.service.ErrorService;
import com.example.telemetry.service.OrderService;
import com.example.telemetry.service.RinsingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

import java.util.Map;

@RestController
public class TelemetryController {

    private final OrderService orderService;
    private final ErrorService errorService;
    private final RinsingService rinsingService;

    @Autowired
    public TelemetryController(OrderService orderService, ErrorService errorService, RinsingService rinsingService) {
        this.orderService = orderService;
        this.errorService = errorService;
        this.rinsingService = rinsingService;
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
            @RequestParam String deviceid,
            @RequestParam(value = "dates", required = false)@DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(value = "datef", required = false)@DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {

        //if input params date is yyyy-mm-dd - LocalDate
        //startDate = startDate.toLocalDate().atStartOfDay();
        //endDate = endDate.atTime(LocalTime.MAX);
        Map<String, Object> orders;
        int deviceId = Integer.parseInt(deviceid);
        if (startDate != null && endDate != null) {
            orders = orderService.getOrdersByDateRange(startDate, endDate, deviceId);
        } else {
            orders = orderService.getOrders(deviceId);
        }
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
            @RequestParam String deviceid,
            @RequestParam(value = "dates", required = false)@DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(value = "datef", required = false)@DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {

        Map<String, Object> errors;
        int deviceId = Integer.parseInt(deviceid);
        if (startDate != null && endDate != null) {
            errors = errorService.getErrorsByDateRange(deviceId, startDate, endDate);
        } else {
            errors = errorService.getErrors(deviceId);
        }
        return ResponseEntity.ok(errors);
    }

    @GetMapping("/rinsing")
    public ResponseEntity<?> getRinsingRecord(
            @RequestParam String deviceid,
            @RequestParam(value = "dates", required = false)@DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(value = "datef", required = false)@DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        Map<String, Object> rinsingRecords;
        int deviceId = Integer.parseInt(deviceid);

        if (startDate != null && endDate != null) {
            rinsingRecords = rinsingService.getRinsingRecordsByDateRange(deviceId, startDate, endDate);
        } else {
            rinsingRecords = rinsingService.getRinsingRecords(deviceId);
        }
        return ResponseEntity.ok(rinsingRecords);

    }

//    @GetMapping("/supply")
//    public ResponseEntity<?> getSupplyRecord(
//            @RequestParam String deviceid,
//            @RequestParam(value = "dates", required = false)@DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
//            @RequestParam(value = "datef", required = false)@DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
//
//
//    }

    @GetMapping("/getNameCoffeeOrders")
    public ResponseEntity<?> getNameCoffeeOrders() {
        try {
            return ResponseEntity.ok(orderService.getCoffeeNames());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Internal server error");
        }
    }
}
