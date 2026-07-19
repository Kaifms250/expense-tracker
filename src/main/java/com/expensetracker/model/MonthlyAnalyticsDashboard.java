package com.expensetracker.model;

import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

public class MonthlyAnalyticsDashboard {
    private YearMonth month;
    private double totalExpenses;
    private double totalIncome;
    private double totalSavings;
    private double netBalance;
    private double budgetUtilizationPercent;
    private double remainingBudget;
    private double totalBudget;
    private double savingsRatePercent;
    private double previousMonthTotal;
    private double changeFromPreviousMonth;
    private double changePercent;
    private String mostFrequentCategory;
    private String highestSpendingCategory;
    private String budgetStatus;
    private String trendSummary;
    private StatisticsResult statistics = new StatisticsResult();
    private List<CategoryAnalyticsItem> topCategories = new ArrayList<>();
    private List<CategoryAnalyticsItem> allCategories = new ArrayList<>();
    private List<MonthlyTrendPoint> monthlyTrend = new ArrayList<>();
    private List<Insight> smartInsights = new ArrayList<>();
    private List<String> recommendations = new ArrayList<>();

    public YearMonth getMonth() { return month; }
    public void setMonth(YearMonth month) { this.month = month; }
    public double getTotalExpenses() { return totalExpenses; }
    public void setTotalExpenses(double v) { this.totalExpenses = v; }
    public double getTotalIncome() { return totalIncome; }
    public void setTotalIncome(double v) { this.totalIncome = v; }
    public double getTotalSavings() { return totalSavings; }
    public void setTotalSavings(double v) { this.totalSavings = v; }
    public double getNetBalance() { return netBalance; }
    public void setNetBalance(double v) { this.netBalance = v; }
    public double getBudgetUtilizationPercent() { return budgetUtilizationPercent; }
    public void setBudgetUtilizationPercent(double v) { this.budgetUtilizationPercent = v; }
    public double getRemainingBudget() { return remainingBudget; }
    public void setRemainingBudget(double v) { this.remainingBudget = v; }
    public double getTotalBudget() { return totalBudget; }
    public void setTotalBudget(double v) { this.totalBudget = v; }
    public double getSavingsRatePercent() { return savingsRatePercent; }
    public void setSavingsRatePercent(double v) { this.savingsRatePercent = v; }
    public double getPreviousMonthTotal() { return previousMonthTotal; }
    public void setPreviousMonthTotal(double v) { this.previousMonthTotal = v; }
    public double getChangeFromPreviousMonth() { return changeFromPreviousMonth; }
    public void setChangeFromPreviousMonth(double v) { this.changeFromPreviousMonth = v; }
    public double getChangePercent() { return changePercent; }
    public void setChangePercent(double v) { this.changePercent = v; }
    public String getMostFrequentCategory() { return mostFrequentCategory; }
    public void setMostFrequentCategory(String v) { this.mostFrequentCategory = v; }
    public String getHighestSpendingCategory() { return highestSpendingCategory; }
    public void setHighestSpendingCategory(String v) { this.highestSpendingCategory = v; }
    public String getBudgetStatus() { return budgetStatus; }
    public void setBudgetStatus(String v) { this.budgetStatus = v; }
    public String getTrendSummary() { return trendSummary; }
    public void setTrendSummary(String v) { this.trendSummary = v; }
    public StatisticsResult getStatistics() { return statistics; }
    public void setStatistics(StatisticsResult v) { this.statistics = v; }
    public List<CategoryAnalyticsItem> getTopCategories() { return topCategories; }
    public void setTopCategories(List<CategoryAnalyticsItem> v) { this.topCategories = v; }
    public List<CategoryAnalyticsItem> getAllCategories() { return allCategories; }
    public void setAllCategories(List<CategoryAnalyticsItem> v) { this.allCategories = v; }
    public List<MonthlyTrendPoint> getMonthlyTrend() { return monthlyTrend; }
    public void setMonthlyTrend(List<MonthlyTrendPoint> v) { this.monthlyTrend = v; }
    public List<Insight> getSmartInsights() { return smartInsights; }
    public void setSmartInsights(List<Insight> v) { this.smartInsights = v; }
    public List<String> getRecommendations() { return recommendations; }
    public void setRecommendations(List<String> v) { this.recommendations = v; }
}
