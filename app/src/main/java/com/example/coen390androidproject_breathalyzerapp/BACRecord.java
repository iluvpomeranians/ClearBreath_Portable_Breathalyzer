package com.example.coen390androidproject_breathalyzerapp;

public class BACRecord {

    // GETTER FILE FOR ANYTHING BAC

    private double bacValue;
    private String timestamp;

    public BACRecord(double bacValue, String timestamp) {
        this.bacValue = bacValue;
        this.timestamp = timestamp;
    }

    public double getBacValue() {
        return bacValue;
    }

    public String getTimestamp() {
        return timestamp;
    }
}
