package com.expensetracker.model;

public class StatisticsResult {
    private double highestExpense;
    private double lowestExpense;
    private double averageExpensePerTransaction;
    private double dailyAverageSpending;
    private double weeklyAverageSpending;
    private double monthlyAverageSpending;
    private double largestTransaction;
    private String largestTransactionDescription;
    private String largestTransactionCategory;
    private int totalTransactions;
    private String mostActiveSpendingDay;
    private double mostActiveDayTotal;
    private int mostActiveDayCount;

    public double getHighestExpense() { return highestExpense; }
    public void setHighestExpense(double highestExpense) { this.highestExpense = highestExpense; }
    public double getLowestExpense() { return lowestExpense; }
    public void setLowestExpense(double lowestExpense) { this.lowestExpense = lowestExpense; }
    public double getAverageExpensePerTransaction() { return averageExpensePerTransaction; }
    public void setAverageExpensePerTransaction(double v) { this.averageExpensePerTransaction = v; }
    public double getDailyAverageSpending() { return dailyAverageSpending; }
    public void setDailyAverageSpending(double v) { this.dailyAverageSpending = v; }
    public double getWeeklyAverageSpending() { return weeklyAverageSpending; }
    public void setWeeklyAverageSpending(double v) { this.weeklyAverageSpending = v; }
    public double getMonthlyAverageSpending() { return monthlyAverageSpending; }
    public void setMonthlyAverageSpending(double v) { this.monthlyAverageSpending = v; }
    public double getLargestTransaction() { return largestTransaction; }
    public void setLargestTransaction(double v) { this.largestTransaction = v; }
    public String getLargestTransactionDescription() { return largestTransactionDescription; }
    public void setLargestTransactionDescription(String v) { this.largestTransactionDescription = v; }
    public String getLargestTransactionCategory() { return largestTransactionCategory; }
    public void setLargestTransactionCategory(String v) { this.largestTransactionCategory = v; }
    public int getTotalTransactions() { return totalTransactions; }
    public void setTotalTransactions(int v) { this.totalTransactions = v; }
    public String getMostActiveSpendingDay() { return mostActiveSpendingDay; }
    public void setMostActiveSpendingDay(String v) { this.mostActiveSpendingDay = v; }
    public double getMostActiveDayTotal() { return mostActiveDayTotal; }
    public void setMostActiveDayTotal(double v) { this.mostActiveDayTotal = v; }
    public int getMostActiveDayCount() { return mostActiveDayCount; }
    public void setMostActiveDayCount(int v) { this.mostActiveDayCount = v; }
}
