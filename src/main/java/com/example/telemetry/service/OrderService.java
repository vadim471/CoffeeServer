package com.example.telemetry.service;

import com.example.telemetry.model.CoffeeOrder;
import com.example.telemetry.model.TelemetryData;
import com.example.telemetry.repository.CoffeeOrderRepository;
import com.example.telemetry.repository.TelemetryDataRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.example.telemetry.model.MachineInterface.*;

@Service
public class OrderService {
    private final TelemetryDataRepository telemetryDataRepository;
    private final CoffeeOrderRepository coffeeOrderRepository;

    public OrderService(TelemetryDataRepository telemetryDataRepository, CoffeeOrderRepository coffeeOrderRepository) {
        this.telemetryDataRepository = telemetryDataRepository;
        this.coffeeOrderRepository = coffeeOrderRepository;
    }

    public Map<String, Object> getOrdersByDateRange(LocalDateTime startDate, LocalDateTime endDate, int vmcNumber) {
        List<TelemetryData> telemetryData = telemetryDataRepository.findByDateRange(startDate, endDate, vmcNumber);

        List<Map<String, Object>> products = telemetryData.stream().map(data -> {
            CoffeeOrder coffeeOrder = coffeeOrderRepository.findByProductId(data.getProduct_id()).orElse(null);
            Map<String, Object> productInfo = new HashMap<>();
            productInfo.put("OrderNo", data.getOrder_no());
            productInfo.put("DeviceID", getVmcNumber());
            productInfo.put("ProductID", data.getProduct_id());
            productInfo.put("Price", coffeeOrder != null ? coffeeOrder.getProductLastPrice() : null);
            productInfo.put("PayType", data.getPayType());
            productInfo.put("Status", data.getStatus());
            productInfo.put("BuyTime", data.getDate());

            return productInfo;
        }).collect(Collectors.toList());

        Map<String, Object> result = new HashMap<>();
        result.put("deviceid", getVmcNumber());
        result.put("SoftwareVersion", getSoftwareVersion());
        result.put("IoVersion", getIoVersion());
        result.put("products", Collections.singletonList(products));

        return result;


    }
}
