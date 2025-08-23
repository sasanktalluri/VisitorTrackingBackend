package com.visitortracker.service;

import com.visitortracker.model.dto.*;
import com.visitortracker.model.*;
import com.visitortracker.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DashboardService {

    @Autowired private VisitorRepository visitorRepo;
    @Autowired private VisitRepository visitRepo;
    @Autowired private PaymentRepository paymentRepo;
    @Autowired private ServiceUsageRepository serviceUsageRepo;

    public DashboardStatsResponse getDashboardStats() {
        LocalDateTime startOfToday = LocalDate.now().atStartOfDay();
        LocalDateTime startOfWeek = LocalDate.now().with(TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY)).atStartOfDay();
        LocalDateTime startOfMonth = LocalDate.now().withDayOfMonth(1).atStartOfDay();

        double todayRevenue = getTotalRevenueSince(startOfToday);
        double weekRevenue = getTotalRevenueSince(startOfWeek);
        double monthRevenue = getTotalRevenueSince(startOfMonth);

        int todayVisitors = getVisitorCountSince(startOfToday);
        int weekVisitors = getVisitorCountSince(startOfWeek);
        int monthVisitors = getVisitorCountSince(startOfMonth);

        return new DashboardStatsResponse(todayRevenue, weekRevenue, monthRevenue, todayVisitors, weekVisitors, monthVisitors);
    }

    private double getTotalRevenueSince(LocalDateTime startTime) {
        return paymentRepo.findAll().stream()
                .filter(p -> p.getPaidAt().isAfter(startTime))
                .mapToDouble(Payment::getAmount)
                .sum();
    }

    private int getVisitorCountSince(LocalDateTime startTime) {
        return (int) visitRepo.findAll().stream()
                .filter(v -> v.getInTime().isAfter(startTime))
                .map(Visit::getVisitorId)
                .distinct()
                .count();
    }

    public List<ServiceRevenueResponse> getServiceRevenueBreakdown() {
        return paymentRepo.findAll().stream()
                .collect(Collectors.groupingBy(Payment::getServiceType,
                        Collectors.summingDouble(Payment::getAmount)))
                .entrySet().stream()
                .map(e -> new ServiceRevenueResponse(e.getKey(), e.getValue()))
                .collect(Collectors.toList());
    }

    public List<WeeklyRevenueResponse> getWeeklyRevenue() {
        Map<LocalDate, Double> dailyRevenue = new LinkedHashMap<>();
        LocalDate today = LocalDate.now();

        for (int i = 6; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            double total = paymentRepo.findAll().stream()
                    .filter(p -> p.getPaidAt().toLocalDate().equals(date))
                    .mapToDouble(Payment::getAmount)
                    .sum();
            dailyRevenue.put(date, total);
        }

        return dailyRevenue.entrySet().stream()
                .map(e -> new WeeklyRevenueResponse(e.getKey().toString(), e.getValue()))
                .collect(Collectors.toList());
    }

    public VisitorDetailsResponse getVisitorDetails(String phone) {
        Visitor visitor = visitorRepo.findByPhoneNumber(phone)
                .orElseThrow(() -> new RuntimeException("Visitor not found"));

        List<Visit> visits = visitRepo.findByVisitorId(visitor.getId());
        List<Payment> payments = paymentRepo.findByVisitorId(visitor.getId());
        List<ServiceUsage> services = serviceUsageRepo.findByVisitorId(visitor.getId());

        return new VisitorDetailsResponse(visitor, visits, payments, services);
    }
}
