package com.example.telemetry.controller;

import com.example.telemetry.generator.RequestGenerator;
import com.example.telemetry.manager.MachinesManager;
import com.example.telemetry.model.Task;
import com.example.telemetry.model.TelemetryResponse;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.net.InetAddress;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Класс, созданный для отправки запросов на вендинговый аппарат. Должен использоваться Rabbit'ом для обработки входящих
 * запросов от ОПТИМы
 */
@RestController
public class MachineController {

    private final RequestGenerator requestGenerator;
    private final MachinesManager machinesManager;

    @Autowired
    public MachineController(RequestGenerator requestGenerator, MachinesManager machinesManager) {
        this.requestGenerator = requestGenerator;
        this.machinesManager = machinesManager;
    }

    /**
     * first point API
      * @return HTTPStatus
     */
    @GetMapping("/ping")
    public HttpStatus ping() {
        return HttpStatus.resolve(200);
    }

    /**
     * second point API
     * for deliver drinks from vending
     * @param deviceid
     */
    @GetMapping("/getproducts")
    public void getProductsFromVending(@RequestParam int deviceid) {
        try {
            InetAddress machineAddress = machinesManager.getInetAddress(deviceid);
            byte[] response = requestGenerator.processTelemetry("upload", deviceid, Map.of("upload", "product")).getResponseBytes();
            CompletableFuture<byte[]> bytes = machinesManager.handleRequest(machineAddress, response);

            /*
            if (bytes != null) {
                bytes.whenComplete((res, error) -> {
                    Task responseTask = requestGenerator.generateTaskFromResponseBytes(res);
                    System.out.println("FROM CONTROLLER!!!11" + responseTask.getBody());
                });
            }
             */

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * third point API
     * for deliver recipes from vending
     * @param deviceid
     */
    @GetMapping("/getrecipes")
    public void getRecipesFromVending(@RequestParam int deviceid) {
        try {
            InetAddress machineAddress = machinesManager.getInetAddress(deviceid);
            byte[] response = requestGenerator.processTelemetry("upload", deviceid, Map.of("upload", "recipe")).getResponseBytes();
            CompletableFuture<byte[]> bytes = machinesManager.handleRequest(machineAddress, response);

            /*
            if (bytes != null) {
                bytes.whenComplete((res, error) -> {
                    Task responseTask = requestGenerator.generateTaskFromResponseBytes(res);
                    System.out.println("FROM CONTROLLER!!!11" + responseTask.getBody());
                });
            }
             */

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * forth point API
     *
     * @param deviceid
     */
    @PostMapping("/setrecipes")
    public void sendRecipe(@RequestParam int deviceid, @RequestBody JsonNode payLoad) {
        try {
            InetAddress machineAddress = machinesManager.getInetAddress(deviceid);

            JsonNode products = payLoad.get("products");
            String nameZipArchieve = requestGenerator.saveJsonFileAndArchieve(products);

            byte[] response = requestGenerator.processTelemetry("upgrade", deviceid, Map.of("type", "recipe",
                                                                                                    "zipDir", nameZipArchieve)).getResponseBytes();

            CompletableFuture<byte[]> bytes = machinesManager.handleRequest(machineAddress, response);
            if (bytes != null) {
                 bytes.whenComplete((res, error) -> {
                    System.out.println(res);
                 });
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @PostMapping("/setproducts")
    public void send(@RequestParam int deviceid) {
        try {
            InetAddress machineAddress = machinesManager.getInetAddress(deviceid);
            byte[] response = requestGenerator.processTelemetry("upgrade", deviceid, "products").getResponseBytes();

            CompletableFuture<byte[]> bytes = machinesManager.handleRequest(machineAddress, response);
            if (bytes != null) {
                bytes.whenComplete((res, error) -> {
                    System.out.println(res);
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * point 5 API
     * @param deviceid
     */
    @PostMapping("/supply")
    public void getMachineStatus(@RequestParam int deviceid) {
        try {
            InetAddress machineAddress = machinesManager.getInetAddress(deviceid);
            byte[] response = requestGenerator.processTelemetry("remote", deviceid, "sync").getResponseBytes();

            CompletableFuture<byte[]> bytes = machinesManager.handleRequest(machineAddress, response);
                if (bytes != null) {
                bytes.whenComplete((res, error) -> {
                    Task responseTask = requestGenerator.generateTaskFromResponseBytes(res);
                    System.out.println("FROM CONTROLLER!!!11" + responseTask.getBody());
                });
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 8 point API
     * for creating order
     * @param deviceid
     * @param price
     */
    @PostMapping("/setprice")
    public void setProductPrice(@RequestParam int deviceid, @RequestBody Map<String, Object> price) {
        try {
            InetAddress machineAddress = machinesManager.getInetAddress(deviceid);
            List<List<Integer>> priceArray = (List<List<Integer>>) price.get("price");
            Map<String, Object> params = Map.of(
                    "price", priceArray
            );
            TelemetryResponse response = requestGenerator.processTelemetry("priceset", deviceid, params);
            machinesManager.handleRequest(machineAddress, response.getResponseBytes());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @PostMapping("/setorder")
    public ResponseEntity<?> makeProduct(@RequestParam int deviceid, @RequestBody Map<String, Object> payload) {
        try {
            InetAddress machineAddress = machinesManager.getInetAddress(deviceid);
            Map<String, Object> order = (Map<String, Object>) payload.get("order");

            int productId = (Integer) order.get("productId");
            int price = (Integer) order.get("price");
            Map<String, Object> params = Map.of(
                    "operation", "products",
                    "productId", productId,
                    "price", price
            );

            //byte[] response = requestGenerator.processTelemetry("products", deviceid, params).getResponseBytes();
            TelemetryResponse response = requestGenerator.processTelemetry("products", deviceid, params);

            if (!response.isSuccess()) {
                if ("PRICE_MISMATCH".equals(response.getMessage())) {
                    return ResponseEntity.status(402).body(response.getMessage());
                }
                return ResponseEntity.status(400).body(response.getResponseBytes());
            }
            machinesManager.handleRequest(machineAddress, response.getResponseBytes());
            return ResponseEntity.status(200).body("success");
            //CompletableFuture<byte[]> bytes =
//            if (bytes != null) {
//                bytes.whenComplete((res, error) -> {
//                    Task responseTask = requestGenerator.generateTaskFromResponseBytes(res);
//                    System.out.println("FROM CONTROLLER!!!11" + responseTask.getBody());
//                });
//            }

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Internal server error");
        }
    }

}
