package com.example.telemetry.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;


@Entity
public class CoffeeOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long Id;

    private String productName;
    private Integer productLastPrice;
    private Integer productRepeat;
    private Integer productPriceSumm;
    private Integer productId;

    public CoffeeOrder() {
    }

    public void setProductId(Integer productId) {
        this.productId = productId;
    }

    public Integer getProductId() {
        return productId;
    }

    public CoffeeOrder(String productName, Integer productLastPrice, Integer productRepeat, Integer productPriceSumm, Integer productId) {
        this.productName = productName;
        this.productLastPrice = productLastPrice;
        this.productRepeat = productRepeat;
        this.productPriceSumm = productPriceSumm;
        this.productId = productId;
    }

    public void setProductRepeat(Integer productRepeat) {
        this.productRepeat = productRepeat;
    }

    public Integer getProductRepeat() {
        return productRepeat;
    }

    public void setProductPriceSumm(Integer productPriceSumm) {
        this.productPriceSumm = productPriceSumm;
    }

    public Integer getProductPriceSumm() {
        return productPriceSumm;
    }

    public String getProductName() {
        return productName;
    }

    public Integer getProductLastPrice() {
        return productLastPrice;
    }

    public Long getId() {
        return Id;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public void setProductLastPrice(Integer productLastPrice) {
        this.productLastPrice = productLastPrice;
    }
}

