package com.visitortracker.model.dto;

public class ExtractedPayment {
    private String name;

    private String PhoneNumber;
    private String category;
    private String serviceType;
    private double amount;
    private String timestamp;

    public ExtractedPayment(String name, String Phone, String serviceType, String category, double amount, String timestamp) {
        this.name = name;
        this.PhoneNumber = Phone;
        this.category = category;
        this.serviceType = serviceType;
        this.amount = amount;
        this.timestamp = timestamp;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
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
    public String getPhoneNumber() {
        return PhoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        PhoneNumber = phoneNumber;
    }
}
