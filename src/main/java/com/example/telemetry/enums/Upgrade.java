package com.example.telemetry.enums;

//
public enum Upgrade {
    RECIPE("recipe");

    private final String value;

    Upgrade(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static boolean contains(String value) {
        for (Remote item : Remote.values()) {
            if (item.getValue().equals(value)) {
                return true;
            }
        }
        return false;
    }
}
