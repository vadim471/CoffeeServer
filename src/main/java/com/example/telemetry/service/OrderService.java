package com.example.telemetry.service;

import com.example.telemetry.manager.MachinesManager;
import com.example.telemetry.model.CoffeeOrder;
import com.example.telemetry.model.MachineInterface;
import com.example.telemetry.model.ProductType;
import com.example.telemetry.model.TelemetryData;
import com.example.telemetry.repository.CoffeeOrderRepository;
import com.example.telemetry.repository.ProductTypeRepository;
import com.example.telemetry.repository.TelemetryDataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;


@Service
public class OrderService {
    private final TelemetryDataRepository telemetryDataRepository;
    private final CoffeeOrderRepository coffeeOrderRepository;
    private final ProductTypeRepository productTypeRepository;

    @Autowired
    private final MachinesManager machinesManager;

    public OrderService(TelemetryDataRepository telemetryDataRepository, CoffeeOrderRepository coffeeOrderRepository, ProductTypeRepository productTypeRepository, MachinesManager machinesManager) {
        this.telemetryDataRepository = telemetryDataRepository;
        this.coffeeOrderRepository = coffeeOrderRepository;
        this.productTypeRepository = productTypeRepository;
        this.machinesManager = machinesManager;
    }

    private String getProductNameById(int productId) {
        ProductType productType = productTypeRepository.findByProductId(productId);
        if (productType != null) {
            return productType.getRussianName();
        }
        return "Product not found";
    }

    public List<CoffeeOrder> getCoffeeNames() {
        List<CoffeeOrder> coffeeOrders = coffeeOrderRepository.findAll();

        coffeeOrders.forEach( coffee -> coffee.setProductName(getProductNameById(coffee.getProductId())));
        return coffeeOrders;
    }

    public Map<String, Object> getOrdersByDateRange(LocalDateTime startDate, LocalDateTime endDate, int deviceId) {
        List<TelemetryData> telemetryData = telemetryDataRepository.findByDateRange(startDate, endDate, deviceId);

        MachineInterface machineInterface = machinesManager.getMachineInterfaceById(deviceId);

        List<Map<String, Object>> products = telemetryData.stream().map(data -> {
            Map<String, Object> productInfo = new HashMap<>();
            productInfo.put("OrderNo", data.getOrder_no());
            productInfo.put("ProductName", getProductNameById(data.getProduct_id()));
            productInfo.put("DeviceID", deviceId);
            productInfo.put("ProductID", data.getProduct_id());
            productInfo.put("Price", data.getProductAmount());
            productInfo.put("PayType", data.getPayType());
            productInfo.put("Status", data.getStatus());
            productInfo.put("BuyTime", data.getDate());

            return productInfo;
        }).collect(Collectors.toList());

        Map<String, Object> result = new HashMap<>();

        if (machineInterface != null) {
            result.putAll(machineInterface.getMachineInfo());
        }

        result.put("products", products);

        return result;
    }

    public Map<String, Object> getOrders(int deviceId) {
        List<TelemetryData> telemetryData = telemetryDataRepository.findByVmcNumber(deviceId);

        MachineInterface machineInterface = machinesManager.getMachineInterfaceById(deviceId);

        List<Map<String, Object>> products = telemetryData.stream().map(data -> {
            CoffeeOrder coffeeOrder = coffeeOrderRepository.findByProductId(data.getProduct_id()).orElse(null);
            Map<String, Object> productInfo = new HashMap<>();
            productInfo.put("OrderNo", data.getOrder_no());
            productInfo.put("ProductName",getProductNameById(data.getProduct_id()));
            productInfo.put("DeviceID", deviceId);
            productInfo.put("ProductID", data.getProduct_id());
            productInfo.put("Price", coffeeOrder != null ? coffeeOrder.getProductLastPrice() : null);
            productInfo.put("PayType", data.getPayType());
            productInfo.put("Status", data.getStatus());
            productInfo.put("BuyTime", data.getDate());

            return productInfo;
        }).collect(Collectors.toList());

        Map<String, Object> result = new HashMap<>();

        if (machineInterface != null) {
            result.putAll(machineInterface.getMachineInfo());
        }

        result.put("products", products);

        return result;
    }
}
