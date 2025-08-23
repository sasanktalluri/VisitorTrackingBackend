package com.visitortracker.model.dto;

public class WeeklyRevenueResponse {
    public String date;
    public double amount;
    public WeeklyRevenueResponse(String date, double amount) {
        this.date = date;
        this.amount = amount;
    }
}
