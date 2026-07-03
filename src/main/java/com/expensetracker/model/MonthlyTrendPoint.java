package com.expensetracker.model;

public class MonthlyTrendPoint {
    private final String month;
    private final double totalSpent;

    public MonthlyTrendPoint(String month, double totalSpent) {
        this.month = month;
        this.totalSpent = totalSpent;
    }

    public String getMonth() {
        return month;
    }

    public double getTotalSpent() {
        return totalSpent;
    }
}
