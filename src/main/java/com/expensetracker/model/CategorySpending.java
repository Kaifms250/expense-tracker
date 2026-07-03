package com.expensetracker.model;

public class CategorySpending {
    private final String categoryId;
    private final String categoryName;
    private final double currentMonth;
    private final double previousMonth;
    private final double changeAmount;
    private final double changePercent;

    public CategorySpending(String categoryId, String categoryName,
                            double currentMonth, double previousMonth) {
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.currentMonth = currentMonth;
        this.previousMonth = previousMonth;
        this.changeAmount = currentMonth - previousMonth;
        this.changePercent = previousMonth > 0
                ? ((currentMonth - previousMonth) / previousMonth) * 100
                : (currentMonth > 0 ? 100 : 0);
    }

    public String getCategoryId() {
        return categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public double getCurrentMonth() {
        return currentMonth;
    }

    public double getPreviousMonth() {
        return previousMonth;
    }

    public double getChangeAmount() {
        return changeAmount;
    }

    public double getChangePercent() {
        return changePercent;
    }

    public boolean isIncreasing() {
        return changeAmount > 0;
    }
}
