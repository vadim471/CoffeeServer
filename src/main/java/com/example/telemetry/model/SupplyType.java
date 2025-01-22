package com.example.telemetry.model;

import jakarta.persistence.*;

@Entity
@Table(name = "supplies")
public class SupplyType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "supply_id", nullable = false, unique = true)
    private int supplyId;

    @Column(name = "chinese_name")
    private String chinaName;

    @Column(name = "russian_name")
    private String russianName;

    public void setId(Long id) {
        this.id = id;
    }

    public void setSupplyId(int supplyId) {
        this.supplyId = supplyId;
    }

    public void setChinaName(String chinaName) {
        this.chinaName = chinaName;
    }

    public void setRussianName(String russianName) {
        this.russianName = russianName;
    }

    public Long getId() {
        return id;
    }

    public int getSupplyId() {
        return supplyId;
    }

    public String getChinaName() {
        return chinaName;
    }

    public String getRussianName() {
        return russianName;
    }
}
