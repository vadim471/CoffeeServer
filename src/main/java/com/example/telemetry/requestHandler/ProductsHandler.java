package com.example.telemetry.requestHandler;

import com.example.telemetry.enums.Remote;
import com.example.telemetry.handler.CommandHandler;
import com.example.telemetry.model.CoffeeOrder;
import com.example.telemetry.repository.CoffeeOrderRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;

import static com.example.telemetry.generator.ResponseGenerator.generateSessionId;

@Component
public class ProductsHandler implements CommandHandler {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final CoffeeOrderRepository coffeeOrderRepository;

    @Autowired
    public ProductsHandler(CoffeeOrderRepository coffeeOrderRepository) {
        this.coffeeOrderRepository = coffeeOrderRepository;
    }

    @Override
    public String handle(int vmcNumber, Object params) {

//        if (!(Remote.contains((String) params))) {
//            throw new IllegalArgumentException("Invalid params");
//        }


        Map<String, Object> paramMap = (Map<String, Object>) params;
        String operation = (String) paramMap.get("operation");
        int productId = (Integer) paramMap.get("productId");

        Optional<CoffeeOrder> product = coffeeOrderRepository.findByProductId(productId);

        if(product.isPresent()) {
            ObjectNode response = objectMapper.createObjectNode();
            response.put("cmd", "remote");
            response.put("vmc_no", vmcNumber);
            response.put("session_id", generateSessionId(vmcNumber));
            response.put("notify_url", "");
            response.put("operation", operation);
            response.put("product_id", productId);
            try {
                return objectMapper.writeValueAsString(response);
            } catch (Exception e) {
                throw new RuntimeException("Failed", e);
            }
        } else {
            return("Failed: no product with id " + productId);
        }




    }
}
