package com.example.coen390androidproject_breathalyzerapp;

public class BACRecord {
    private double bac;
    private String timestamp;

    public BACRecord(double bac, String timestamp) {
        this.bac = bac;
        this.timestamp = timestamp;
    }

    public double getBac() {
        return bac;
    }

    public String getTimestamp() {
        return timestamp;
    }
}
