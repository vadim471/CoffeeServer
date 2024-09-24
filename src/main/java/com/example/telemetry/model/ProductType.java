package com.example.telemetry.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

// filled by xls file
@Entity
public class ProductType {

    @Id
    private Integer productId;

    private String typeProduct;
    private String chinaName;
    private String nameKey;
    private String russianName;

    public ProductType() {
    }

    public void setProductId(Integer productId) {
        this.productId = productId;
    }

    public void setTypeProduct(String typeProduct) {
        this.typeProduct = typeProduct;
    }

    public void setChinaName(String chinaName) {
        this.chinaName = chinaName;
    }

    public void setNameKey(String nameKey) {
        this.nameKey = nameKey;
    }

    public void setRussianName(String russianName) {
        this.russianName = russianName;
    }

    public Integer getProductId() {
        return productId;
    }

    public String getTypeProduct() {
        return typeProduct;
    }

    public String getChinaName() {
        return chinaName;
    }

    public String getNameKey() {
        return nameKey;
    }

    public String getRussianName() {
        return russianName;
    }

    public ProductType(Integer productId, String typeProduct, String chinaName, String nameKey, String russianName) {
        this.productId = productId;
        this.typeProduct = typeProduct;
        this.chinaName = chinaName;
        this.nameKey = nameKey;
        this.russianName = russianName;
    }
}