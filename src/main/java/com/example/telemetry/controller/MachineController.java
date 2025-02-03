package com.example.telemetry.controller;

import com.example.telemetry.exceptions.MachineNotFoundException;
import com.example.telemetry.generator.RequestGenerator;
import com.example.telemetry.manager.MachinesManager;
import com.example.telemetry.model.Task;
import com.example.telemetry.model.TelemetryResponse;
import com.example.telemetry.service.SupplyService;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Класс, созданный для отправки запросов на вендинговый аппарат.
 * запросов от ОПТИМы
 */
@RestController
public class MachineController {

    private final RequestGenerator requestGenerator;
    private final MachinesManager machinesManager;
    private final SupplyService supplyService;

    @Autowired
    public MachineController(RequestGenerator requestGenerator, MachinesManager machinesManager, SupplyService supplyService) {
        this.requestGenerator = requestGenerator;
        this.machinesManager = machinesManager;
        this.supplyService = supplyService;
    }

    /**
     * first point API
     *
     * @return HTTPStatus
     */
    @GetMapping("/ping")
    public ResponseEntity<?> ping() {
        Map<String, String> okStatus = new HashMap<>();
        okStatus.put("Status", "OK");
        return ResponseEntity.status(200).body(okStatus);
    }

    /**
     * second point API
     * for deliver drinks from vending
     *
     * @param deviceid
     */
    @GetMapping("/getproducts")
    public ResponseEntity<?> getProductsFromVending(@RequestParam int deviceid) {
        try {
            InetAddress machineAddress = machinesManager.getInetAddress(deviceid);
            byte[] response = requestGenerator.processTelemetry("upload", deviceid, Map.of("upload", "product")).getResponseBytes();
            CompletableFuture<byte[]> bytes = machinesManager.handleRequest(machineAddress, response, "cmd");

            /*
            if (bytes != null) {
                bytes.whenComplete((res, error) -> {
                    Task responseTask = requestGenerator.generateTaskFromResponseBytes(res);
                    System.out.println("FROM CONTROLLER!!!11" + responseTask.getBody());
                });
            }
             */
            return ResponseEntity.status(404).body("Not supported");

        } catch (MachineNotFoundException ex) {
            return ResponseEntity.status(404).body(ex.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Internal server error");
        }
    }

    /**
     * third point API
     * for deliver recipes from vending
     *
     * @param deviceid
     */
    @GetMapping("/getrecipes")
    public ResponseEntity<?> getRecipesFromVending(@RequestParam int deviceid) {
        try {
            InetAddress machineAddress = machinesManager.getInetAddress(deviceid);
            byte[] response = requestGenerator.processTelemetry("upload", deviceid, Map.of("upload", "recipe")).getResponseBytes();
            CompletableFuture<byte[]> bytes = machinesManager.handleRequest(machineAddress, response, "cmd");

            /*
            if (bytes != null) {
                bytes.whenComplete((res, error) -> {
                    Task responseTask = requestGenerator.generateTaskFromResponseBytes(res);
                    System.out.println("FROM CONTROLLER!!!11" + responseTask.getBody());
                });
            }
             */
            return ResponseEntity.status(404).body("Not supported");

        } catch (MachineNotFoundException ex) {
            return ResponseEntity.status(404).body(ex.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Internal server error");
        }
    }

    /**
     * forth point API
     *
     * @param deviceid
     */
    @PostMapping("/setrecipes")
    public ResponseEntity<?> sendRecipe(@RequestParam int deviceid, @RequestBody JsonNode payLoad) {
        try {
            InetAddress machineAddress = machinesManager.getInetAddress(deviceid);

            JsonNode products = payLoad.get("products");
            String nameZipArchieve = requestGenerator.saveJsonFileAndArchieve(products);

            byte[] response = requestGenerator.processTelemetry("upgrade", deviceid, Map.of("type", "recipe",
                    "zipDir", nameZipArchieve)).getResponseBytes();

            CompletableFuture<byte[]> bytes = machinesManager.handleRequest(machineAddress, response, "cmd");
            if (bytes != null) {
                bytes.whenComplete((res, error) -> {
                    System.out.println(res);
                });
            }
            return ResponseEntity.ok("");
        } catch (MachineNotFoundException ex) {
            return ResponseEntity.status(404).body(ex.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Internal server error");
        }
    }

    @PostMapping("/setproducts")
    public ResponseEntity<?> send(@RequestParam int deviceid) {
        try {
            InetAddress machineAddress = machinesManager.getInetAddress(deviceid);
            byte[] response = requestGenerator.processTelemetry("upgrade", deviceid, "products").getResponseBytes();

            CompletableFuture<byte[]> bytes = machinesManager.handleRequest(machineAddress, response, "cmd");
            if (bytes != null) {
                bytes.whenComplete((res, error) -> {
                    System.out.println(res);
                });
            }
            return ResponseEntity.ok("");
        } catch (MachineNotFoundException ex) {
            return ResponseEntity.status(404).body(ex.getMessage());
        }
    }

    /**
     * point 5 API
     *
     * @param deviceid
     */
    @PostMapping("/supply")
    public CompletableFuture<ResponseEntity<?>> getMachineStatus(@RequestParam int deviceid) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                InetAddress machineAddress = machinesManager.getInetAddress(deviceid);
                byte[] response = requestGenerator.processTelemetry("remote", deviceid, "sync").getResponseBytes();

                CompletableFuture<byte[]> bytes = machinesManager.handleRequest(machineAddress, response, "supply");

                if (bytes == null) {
                    return ResponseEntity.status(500).body("Null Response Exception");
                }

                return bytes.thenApply(res -> {
                    Task responseTask = requestGenerator.generateTaskFromResponseBytes(res);
                    Map<String, Object> supplyStatus = supplyService.getSupplyStatus(responseTask.getBody());
                    System.out.println("FROM CONTROLLER!!!11" + responseTask.getBody());
                    return ResponseEntity.ok(supplyStatus);
                }).exceptionally(e -> {
                    e.printStackTrace();

                    return null; //TODO WTF cant return ResponseEntity.status(500).body("server");
                }).join();

            } catch (MachineNotFoundException e) {
                return ResponseEntity.status(404).body(e.getMessage());
            }
        });
    }

    @PostMapping("/fillsupply")
    public ResponseEntity<?> addSupply(@RequestParam int deviceid,  @RequestBody JsonNode supplyLoad) {
        try {
            InetAddress machineAddress = machinesManager.getInetAddress(deviceid);
            //TODO
            return ResponseEntity.ok("");
        } catch (MachineNotFoundException ex) {
            return ResponseEntity.status(404).body(ex.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Internal server error " + e.getMessage());
        }
    }

    /**
     * 8 point API
     * for creating order
     *
     * @param deviceid
     * @param price
     */
    @PostMapping("/setprice")
    public ResponseEntity<?> setProductPrice(@RequestParam int deviceid, @RequestBody Map<String, Object> price) {
        try {
            InetAddress machineAddress = machinesManager.getInetAddress(deviceid);
            List<List<Integer>> priceArray = (List<List<Integer>>) price.get("price");
            Map<String, Object> params = Map.of(
                    "price", priceArray
            );
            TelemetryResponse response = requestGenerator.processTelemetry("priceset", deviceid, params);
            machinesManager.handleRequest(machineAddress, response.getResponseBytes(), "cmd");
            return ResponseEntity.ok("");
        } catch (MachineNotFoundException ex) {
            return ResponseEntity.status(404).body(ex.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Internal server error");
        }
    }

    @PostMapping("/setorder")
    public ResponseEntity<?> makeProduct(@RequestParam int deviceid, @RequestBody Map<String, Object> payload) throws MachineNotFoundException {
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
                return ResponseEntity.status(402).body(response.getMessage());
            }
            machinesManager.handleRequest(machineAddress, response.getResponseBytes(), "cmd");
            return ResponseEntity.status(200).body("success");
            //CompletableFuture<byte[]> bytes =
//            if (bytes != null) {
//                bytes.whenComplete((res, error) -> {
//                    Task responseTask = requestGenerator.generateTaskFromResponseBytes(res);
//                    System.out.println("FROM CONTROLLER!!!11" + responseTask.getBody());
//                });
//            }

        } catch (MachineNotFoundException ex) {
            return ResponseEntity.status(404).body(ex.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Internal server error");
        }
    }

}
