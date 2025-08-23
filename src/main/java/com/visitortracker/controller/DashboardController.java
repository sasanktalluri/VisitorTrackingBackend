package com.visitortracker.controller;

import com.visitortracker.service.DashboardService;
import com.visitortracker.model.dto.VisitorDetailsResponse;
import com.visitortracker.model.dto.DashboardStatsResponse;
import com.visitortracker.model.dto.ServiceRevenueResponse;
import com.visitortracker.model.dto.WeeklyRevenueResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/dashboard")
@CrossOrigin(origins = "http://localhost:3000")
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    @GetMapping("/stats")
    public DashboardStatsResponse getDashboardStats() {
        return dashboardService.getDashboardStats();
    }

    @GetMapping("/service-revenue")
    public List<ServiceRevenueResponse> getServiceRevenue() {
        return dashboardService.getServiceRevenueBreakdown();
    }

    @GetMapping("/weekly-revenue")
    public List<WeeklyRevenueResponse> getWeeklyRevenue() {
        return dashboardService.getWeeklyRevenue();
    }

    @GetMapping("/details/{phone}")
    public VisitorDetailsResponse getVisitorDetails(@PathVariable String phone) {
        return dashboardService.getVisitorDetails(phone);
    }
}
