package com.example.telemetry.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


@Entity
public class TelemetryData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long Id;

    private Integer product_id;
    private String nameKey;
    private Integer productAmount;
    private String payType;
    private LocalDateTime date; //from timestamp Format：%Y%m%d%H%M%S
    private int vmc_number;
    private String order_no;
    private String status;

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }

    public void setOrder_no(String order_no) {
        this.order_no = order_no;
    }

    public String getOrder_no() {
        return order_no;
    }

    public TelemetryData(){
    }

    public void setVmc_number(int vmc_number) {
        this.vmc_number = vmc_number;
    }

    public int getVmc_number() {
        return vmc_number;
    }

    public TelemetryData(Integer product_id, String nameKey, Integer productAmount, String payType, LocalDateTime date, int vmcNumber, String order_no, String status){
        this.product_id = product_id;
        this.nameKey = nameKey;
        this.productAmount = productAmount;
        this.payType = payType;
        this.date = date;
        this.vmc_number = vmcNumber;
        this.order_no = order_no;
        this.status = status;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public String getFormattedDate(){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
        return this.date.format(formatter);
    }

    public Integer getProduct_id() {
        return product_id;
    }

    public void setProduct_id(Integer product_id) {
        this.product_id = product_id;
    }

    public void setProductAmount(Integer productAmount) {
        this.productAmount = productAmount;
    }

    public void setPayType(String payType) {
        this.payType = payType;
    }

    public void setNameKey(String nameKey) {
        this.nameKey = nameKey;
    }

    public Integer getProductAmount() {
        return productAmount;
    }

    public String getPayType() {
        return payType;
    }

    public String getNameKey() {
        return nameKey;
    }
}

