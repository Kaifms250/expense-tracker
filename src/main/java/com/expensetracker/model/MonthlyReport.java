package com.expensetracker.model;

import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

public class MonthlyReport {
    private final YearMonth month;
    private final double totalSpent;
    private final List<CategorySummary> categorySummaries;
    private final double previousMonthTotal;
    private final double changeFromPreviousMonth;

    public MonthlyReport(YearMonth month, double totalSpent, List<CategorySummary> categorySummaries,
                         double previousMonthTotal) {
        this.month = month;
        this.totalSpent = totalSpent;
        this.categorySummaries = new ArrayList<>(categorySummaries);
        this.previousMonthTotal = previousMonthTotal;
        this.changeFromPreviousMonth = totalSpent - previousMonthTotal;
    }

    public YearMonth getMonth() {
        return month;
    }

    public double getTotalSpent() {
        return totalSpent;
    }

    public List<CategorySummary> getCategorySummaries() {
        return categorySummaries;
    }

    public double getPreviousMonthTotal() {
        return previousMonthTotal;
    }

    public double getChangeFromPreviousMonth() {
        return changeFromPreviousMonth;
    }
}
