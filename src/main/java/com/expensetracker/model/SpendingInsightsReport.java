package com.expensetracker.model;

import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

public class SpendingInsightsReport {
    private YearMonth month;
    private String summary;
    private double totalSpent;
    private double previousMonthTotal;
    private double changeFromPreviousMonth;
    private double changePercent;
    private double totalBudget;
    private double budgetUtilizationPercent;
    private double potentialMonthlySavings;
    private double monthlyIncome;
    private String highestCategory;
    private String lowestCategory;
    private String fastestGrowingCategory;
    private List<Insight> insights = new ArrayList<>();
    private List<CategorySpending> categoryBreakdown = new ArrayList<>();
    private List<MonthlyTrendPoint> trendData = new ArrayList<>();

    public YearMonth getMonth() {
        return month;
    }

    public void setMonth(YearMonth month) {
        this.month = month;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public double getTotalSpent() {
        return totalSpent;
    }

    public void setTotalSpent(double totalSpent) {
        this.totalSpent = totalSpent;
    }

    public double getPreviousMonthTotal() {
        return previousMonthTotal;
    }

    public void setPreviousMonthTotal(double previousMonthTotal) {
        this.previousMonthTotal = previousMonthTotal;
    }

    public double getChangeFromPreviousMonth() {
        return changeFromPreviousMonth;
    }

    public void setChangeFromPreviousMonth(double changeFromPreviousMonth) {
        this.changeFromPreviousMonth = changeFromPreviousMonth;
    }

    public double getChangePercent() {
        return changePercent;
    }

    public void setChangePercent(double changePercent) {
        this.changePercent = changePercent;
    }

    public double getTotalBudget() {
        return totalBudget;
    }

    public void setTotalBudget(double totalBudget) {
        this.totalBudget = totalBudget;
    }

    public double getBudgetUtilizationPercent() {
        return budgetUtilizationPercent;
    }

    public void setBudgetUtilizationPercent(double budgetUtilizationPercent) {
        this.budgetUtilizationPercent = budgetUtilizationPercent;
    }

    public double getPotentialMonthlySavings() {
        return potentialMonthlySavings;
    }

    public void setPotentialMonthlySavings(double potentialMonthlySavings) {
        this.potentialMonthlySavings = potentialMonthlySavings;
    }

    public double getMonthlyIncome() {
        return monthlyIncome;
    }

    public void setMonthlyIncome(double monthlyIncome) {
        this.monthlyIncome = monthlyIncome;
    }

    public String getHighestCategory() {
        return highestCategory;
    }

    public void setHighestCategory(String highestCategory) {
        this.highestCategory = highestCategory;
    }

    public String getLowestCategory() {
        return lowestCategory;
    }

    public void setLowestCategory(String lowestCategory) {
        this.lowestCategory = lowestCategory;
    }

    public String getFastestGrowingCategory() {
        return fastestGrowingCategory;
    }

    public void setFastestGrowingCategory(String fastestGrowingCategory) {
        this.fastestGrowingCategory = fastestGrowingCategory;
    }

    public List<Insight> getInsights() {
        return insights;
    }

    public void setInsights(List<Insight> insights) {
        this.insights = insights;
    }

    public List<CategorySpending> getCategoryBreakdown() {
        return categoryBreakdown;
    }

    public void setCategoryBreakdown(List<CategorySpending> categoryBreakdown) {
        this.categoryBreakdown = categoryBreakdown;
    }

    public List<MonthlyTrendPoint> getTrendData() {
        return trendData;
    }

    public void setTrendData(List<MonthlyTrendPoint> trendData) {
        this.trendData = trendData;
    }
}
