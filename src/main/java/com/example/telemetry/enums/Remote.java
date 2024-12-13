package com.example.telemetry.enums;

//5 point
public enum Remote {
    SYNC("sync");

    private final String value;

    Remote(String value) {
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
