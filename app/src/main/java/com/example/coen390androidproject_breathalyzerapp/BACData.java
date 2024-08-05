package com.example.coen390androidproject_breathalyzerapp;

public class BACData {
    private String timestamp;
    private double bacLevel;

    public BACData(String timestamp, double bacLevel) {
        this.timestamp = timestamp;
        this.bacLevel = bacLevel;
    }

    public String getDate() {
        return timestamp.split(" ")[0]; // Assuming timestamp is in "yyyy-MM-dd HH:mm:ss" format
    }

    public String getTime() {
        return timestamp.split(" ")[1]; // Assuming timestamp is in "yyyy-MM-dd HH:mm:ss" format
    }

    public double getBacLevel() {
        return bacLevel;
    }
}


