package com.visitortracker.model.dto;

public class TransactionResponse {
    private String visitorName;
    private String serviceType;
    private double amount;
    private String timestamp;


    public TransactionResponse(String visitorName, String serviceType, double amount, String timestamp) {
        this.visitorName = visitorName;
        this.serviceType = serviceType;
        this.amount = amount;
        this.timestamp = timestamp;
    }

    public String getVisitorName() {
        return visitorName;
    }

    public void setVisitorName(String visitorName) {
        this.visitorName = visitorName;
    }

    public String getServiceType() {
        return serviceType;
    }

    public void setServiceType(String serviceType) {
        this.serviceType = serviceType;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    // constructor, getters, setters
}
