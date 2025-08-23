package com.visitortracker.model.dto;

public class DashboardStatsResponse {
    public double todayRevenue, weekRevenue, monthRevenue;
    public int todayVisitors, weekVisitors, monthVisitors;
    public DashboardStatsResponse(double today, double week, double month, int tv, int wv, int mv) {
        this.todayRevenue = today;
        this.weekRevenue = week;
        this.monthRevenue = month;
        this.todayVisitors = tv;
        this.weekVisitors = wv;
        this.monthVisitors = mv;
    }
}
