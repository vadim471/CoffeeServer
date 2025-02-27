package com.example.telemetry.controller;

import com.example.telemetry.exceptions.MachineNotFoundException;
import com.example.telemetry.generator.RequestGenerator;
import com.example.telemetry.manager.MachinesManager;
import com.example.telemetry.model.MachineInterface;
import com.example.telemetry.model.Task;
import com.example.telemetry.model.TelemetryResponse;
import com.example.telemetry.service.MachineService;
import com.example.telemetry.service.SupplyService;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.InetAddress;
import java.util.ArrayList;
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
    private final MachineService machineService;

    @Autowired
    public MachineController(RequestGenerator requestGenerator, MachinesManager machinesManager, SupplyService supplyService, MachineService machineService) {
        this.requestGenerator = requestGenerator;
        this.machinesManager = machinesManager;
        this.supplyService = supplyService;
        this.machineService = machineService;
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
    public ResponseEntity<?> getProductsFromVending(@RequestParam String deviceid) {
        try {
            int deviceId = Integer.parseInt(deviceid);
            InetAddress machineAddress = machinesManager.getInetAddress(deviceId);
            byte[] response = requestGenerator.processTelemetry("upload", deviceId, Map.of("upload", "product")).getResponseBytes();
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
    public ResponseEntity<?> getRecipesFromVending(@RequestParam String deviceid) {
        try {
            int deviceId = Integer.parseInt(deviceid);
            InetAddress machineAddress = machinesManager.getInetAddress(deviceId);
            byte[] response = requestGenerator.processTelemetry("upload", deviceId, Map.of("upload", "recipe")).getResponseBytes();
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
    public ResponseEntity<?> sendRecipe(@RequestParam String deviceid, @RequestBody JsonNode payLoad) {
        try {
            int deviceId = Integer.parseInt(deviceid);
            InetAddress machineAddress = machinesManager.getInetAddress(deviceId);

            JsonNode products = payLoad.get("products");
            String nameZipArchieve = requestGenerator.saveJsonFileAndArchieve(products);

            byte[] response = requestGenerator.processTelemetry("upgrade", deviceId, Map.of("type", "recipe",
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
    public ResponseEntity<?> send(@RequestParam String deviceid) {
        try {
            int deviceId = Integer.parseInt(deviceid);
            InetAddress machineAddress = machinesManager.getInetAddress(deviceId);
            byte[] response = requestGenerator.processTelemetry("upgrade", deviceId, "products").getResponseBytes();

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
    @GetMapping("/supply")
    public CompletableFuture<ResponseEntity<?>> getMachineStatus(@RequestParam String deviceid) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                int deviceId = Integer.parseInt(deviceid);
                if (deviceId == 55418) {
                    return ResponseEntity.ok("Not supported");
                }
                InetAddress machineAddress = machinesManager.getInetAddress(deviceId);
                byte[] response = requestGenerator.processTelemetry("remote", deviceId, "sync").getResponseBytes();

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
    public ResponseEntity<?> addSupply(@RequestParam String deviceid,  @RequestBody JsonNode supplyLoad) {
        try {
            int deviceId = Integer.parseInt(deviceid);
            InetAddress machineAddress = machinesManager.getInetAddress(deviceId);
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
    public ResponseEntity<?> setProductPrice(@RequestParam String deviceid, @RequestBody Map<String, Object> price) {
        try {
            int deviceId = Integer.parseInt(deviceid);
            InetAddress machineAddress = machinesManager.getInetAddress(deviceId);
            List<List<Integer>> priceArray = (List<List<Integer>>) price.get("price");
            Map<String, Object> params = Map.of(
                    "price", priceArray
            );
            TelemetryResponse response = requestGenerator.processTelemetry("priceset", deviceId, params);
            machinesManager.handleRequest(machineAddress, response.getResponseBytes(), "cmd");
            return ResponseEntity.ok("");
        } catch (MachineNotFoundException ex) {
            return ResponseEntity.status(404).body(ex.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Internal server error");
        }
    }

    @PostMapping("/setorder")
    public ResponseEntity<?> makeProduct(@RequestParam String deviceid, @RequestBody Map<String, Object> payload) {
        try {
            int deviceId = Integer.parseInt(deviceid);
            InetAddress machineAddress = machinesManager.getInetAddress(deviceId);
            Map<String, Object> order = (Map<String, Object>) payload.get("order");

            int productId = (Integer) order.get("productId");
            Map<String, Object> params = Map.of(
                    "operation", "products",
                    "productId", productId
            );

            //byte[] response = requestGenerator.processTelemetry("products", deviceid, params).getResponseBytes();
            TelemetryResponse response = requestGenerator.processTelemetry("products", deviceId, params);

            if (!response.isSuccess()) {
                return ResponseEntity.status(400).body(response.getMessage());
            }
            machinesManager.handleRequest(machineAddress, response.getResponseBytes(), "cmd");

            Map<String, String> success = new HashMap<>();
            success.put("Status", "Success");
            return ResponseEntity.status(200).body(success);
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

    @GetMapping("/listMachines")
    public ResponseEntity<?> getListMachines() {
        try {
            return ResponseEntity.ok(new ArrayList<>(machineService.getListMachines()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Internal server error");
        }
    }

    @GetMapping("/getMachineInfoById")
    public ResponseEntity<?> getInfoById(Integer deviceId) {
        try {
            return ResponseEntity.ok(machineService.getMachineById(deviceId));
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Internal server error");
        }
    }

    @GetMapping("/listActiveMachines")
    public ResponseEntity<?> getListActiveMachines() {
        try {

            List<Map<String,Object>> result = new ArrayList<>();
            for (Integer key : machinesManager.getListActiveMachines().keySet()) {
                Map<String, Object> machineInfo = new HashMap<>();
                MachineInterface machineInterface = machinesManager.getMachineInterfaceById(key);
                machineInfo.put("deviceId", key);
                machineInfo.put("info", machineInterface.getConnectInfo());
                result.add(machineInfo);
            }


            return ResponseEntity.status(200).body(result);

        } catch (Exception e) {
            return ResponseEntity.status(500).body("Internal server error");
        }

    }

    @GetMapping("/checkMachineAvailability")
    public ResponseEntity<?> checkMachineAvailability(@RequestParam String deviceid) {
        try {
            int deviceId = Integer.parseInt(deviceid);
            if (machinesManager.isConnectedMachine(deviceId)) {
                MachineInterface machineInterface = machinesManager.getMachineInterfaceById(deviceId);
                return ResponseEntity.status(200).body(machineInterface.getMachineInfo());
            } else
                return null;

        } catch (MachineNotFoundException ex) {
            return ResponseEntity.status(404).body(ex.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Internal server error");
        }
    }

}
