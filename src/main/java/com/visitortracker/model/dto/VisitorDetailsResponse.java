package com.visitortracker.model.dto;
import com.visitortracker.model.*;

import java.util.List;

public class VisitorDetailsResponse {
    public String name;
    public String phoneNumber;
    public String address;
    public List<Visit> visits;
    public List<Payment> payments;
    public List<ServiceUsage> services;

    public VisitorDetailsResponse(Visitor v, List<Visit> visits, List<Payment> payments, List<ServiceUsage> services) {
        this.name = v.getName();
        this.phoneNumber = v.getPhoneNumber();
        this.address = v.getAddress();
        this.visits = visits;
        this.payments = payments;
        this.services = services;
    }
}