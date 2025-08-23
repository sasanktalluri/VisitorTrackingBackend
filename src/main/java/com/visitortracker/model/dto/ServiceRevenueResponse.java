package com.visitortracker.model.dto;

public class ServiceRevenueResponse {
    public String serviceType;
    public double total;
    public ServiceRevenueResponse(String serviceType, double total) {
        this.serviceType = serviceType;
        this.total = total;
    }
}

