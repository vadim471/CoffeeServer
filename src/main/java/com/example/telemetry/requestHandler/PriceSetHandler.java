package com.example.telemetry.requestHandler;

import com.example.telemetry.handler.CommandHandler;
import com.example.telemetry.model.CoffeeOrder;
import com.example.telemetry.repository.CoffeeOrderRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.example.telemetry.generator.ResponseGenerator.generateSessionId;

@Component
public class PriceSetHandler implements CommandHandler {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final CoffeeOrderRepository coffeeOrderRepository;

    @Autowired
    public PriceSetHandler(CoffeeOrderRepository coffeeOrderRepository) {
        this.coffeeOrderRepository = coffeeOrderRepository;
    }

    @Override
    public String handle(int vmcNumber, Object params) {

//        if (!(Remote.contains((String) params))) {
//            throw new IllegalArgumentException("Invalid params");
//        }


        Map<String, Object> paramMap = (Map<String, Object>) params;
        List<List<Integer>> priceArray = (List<List<Integer>>) paramMap.get("price");

        for (List<Integer> product : priceArray) {
            int productId = product.get(0);
            int productPrice = product.get(1);

            coffeeOrderRepository.findByProductId(productId).ifPresent(coffeeOrder -> {
                coffeeOrder.setProductLastPrice(productPrice);
                coffeeOrderRepository.save(coffeeOrder);
            });
        }

        ObjectNode response = objectMapper.createObjectNode();
        response.put("cmd", "priceset");
        response.put("vmc_no", vmcNumber);
        response.put("session_id", generateSessionId(vmcNumber));
        response.put("notify_url", "");
        response.putPOJO("price", priceArray);

        try {
            return objectMapper.writeValueAsString(response);
        } catch (Exception e) {
            throw new RuntimeException("Failed", e);
        }
    }
}
