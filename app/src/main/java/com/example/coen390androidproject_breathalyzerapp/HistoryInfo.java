package com.example.coen390androidproject_breathalyzerapp;

public class HistoryInfo {
    private int accountId;
    private String timeStamp;
    private int historyValue;

    public HistoryInfo(int accountId, String timeStamp, int historyValue) {
        this.accountId = accountId;
        this.timeStamp = timeStamp;
        this.historyValue = historyValue;
    }

    public int getAccountId() {
        return accountId;
    }

    public void setAccountId(int accountId) {
        this.accountId = accountId;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }

    public int getHistoryValue() {
        return historyValue;
    }

    public void setHistoryValue(int historyValue) {
        this.historyValue = historyValue;
    }
}
