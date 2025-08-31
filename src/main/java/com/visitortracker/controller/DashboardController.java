package com.visitortracker.controller;

import com.visitortracker.model.Visitor;
import com.visitortracker.model.dto.*;
import com.visitortracker.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/dashboard")
@PreAuthorize("hasRole('ADMIN')")
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
    public ResponseEntity<?> getVisitorDetails(@PathVariable String phone) {
        try {
            VisitorDetailsResponse response = dashboardService.getVisitorDetails(phone);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Visitor not found");
        }
    }
    @GetMapping("/transactions")
    public List<TransactionResponse> getTransactions(@RequestParam String period) {
        return dashboardService.getTransactionsForPeriod(period);
    }

}