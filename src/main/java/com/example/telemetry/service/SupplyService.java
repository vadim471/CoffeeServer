package com.example.telemetry.service;

import com.example.telemetry.manager.MachinesManager;
import com.example.telemetry.model.MachineInterface;
import com.example.telemetry.model.Supply;
import com.example.telemetry.model.SupplyType;
import com.example.telemetry.repository.SupplyRepository;
import com.example.telemetry.repository.SupplyTypeRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;


@Service
public class SupplyService {

    private final SupplyTypeRepository supplyTypeRepository;
    //private final ObjectMapper objectMapper = new ObjectMapper();
    private final MachinesManager machinesManager;
    private final SupplyRepository supplyRepository;


    @Autowired
    public SupplyService(SupplyTypeRepository supplyTypeRepository, MachinesManager machinesManager, SupplyRepository supplyRepository) {
        this.supplyTypeRepository = supplyTypeRepository;
        this.machinesManager = machinesManager;
        this.supplyRepository = supplyRepository;
    }

    private String getNameBySupplyId(int supplyId) {
        SupplyType supplyType = supplyTypeRepository.findBySupplyId(supplyId);
        if (supplyType != null)
            return supplyType.getRussianName();
        return "Supply Id not found";
    }

    public Map<String, Object> getSupplyStatus(ObjectNode jsonBody) {
        Map<String, Object> result = new HashMap<>();
        List<Supply> supplyList = new ArrayList<>();
        List<Map<String, Object>> products = new ArrayList<>();
        JsonNode supplyNode = jsonBody.get("supply");
        int vmcNumber = jsonBody.get("vmc_no").asInt();

        try {
            if (supplyNode != null) {
                int deviceId = jsonBody.get("vmc_no").asInt();
                MachineInterface machineInterface = machinesManager.getMachineInterfaceById(deviceId);

                Iterator<String> fieldNames = supplyNode.fieldNames();

                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
                LocalDateTime date = LocalDateTime.parse(jsonBody.get("time").asText(), formatter);

                while (fieldNames.hasNext()) {
                    String supplyId = fieldNames.next();
                    String surplus = supplyNode.get(supplyId).asText();
                    String russianName = getNameBySupplyId(Integer.parseInt(supplyId));
                    Map<String, Object> product = new HashMap<>();

                    product.put("SupplyID", supplyId);
                    product.put("SupplyName", russianName);
                    product.put("Surplus", surplus);
                    product.put("UploadTime", date);

                    products.add(product);

                    Supply supply = new Supply(russianName, supplyId, vmcNumber, date, Integer.parseInt(surplus));
                    supplyList.add(supply);
                }
                result.putAll(machineInterface.getMachineInfo());
            }
            result.put("products", products);

        } catch(Exception e){
            e.printStackTrace();
        }
        supplyRepository.saveAll(supplyList);
        return result;
    }
}
