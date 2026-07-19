package com.expensetracker.model;

public class CategoryAnalyticsItem {
    private String categoryId;
    private String categoryName;
    private double spent;
    private double percentOfTotal;
    private int transactionCount;
    private double budgetLimit;
    private double remainingBudget;
    private double budgetUtilizationPercent;

    public CategoryAnalyticsItem() {
    }

    public CategoryAnalyticsItem(String categoryId, String categoryName, double spent,
                                 double percentOfTotal, int transactionCount) {
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.spent = spent;
        this.percentOfTotal = percentOfTotal;
        this.transactionCount = transactionCount;
    }

    public String getCategoryId() { return categoryId; }
    public void setCategoryId(String categoryId) { this.categoryId = categoryId; }
    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
    public double getSpent() { return spent; }
    public void setSpent(double spent) { this.spent = spent; }
    public double getPercentOfTotal() { return percentOfTotal; }
    public void setPercentOfTotal(double percentOfTotal) { this.percentOfTotal = percentOfTotal; }
    public int getTransactionCount() { return transactionCount; }
    public void setTransactionCount(int transactionCount) { this.transactionCount = transactionCount; }
    public double getBudgetLimit() { return budgetLimit; }
    public void setBudgetLimit(double budgetLimit) { this.budgetLimit = budgetLimit; }
    public double getRemainingBudget() { return remainingBudget; }
    public void setRemainingBudget(double remainingBudget) { this.remainingBudget = remainingBudget; }
    public double getBudgetUtilizationPercent() { return budgetUtilizationPercent; }
    public void setBudgetUtilizationPercent(double budgetUtilizationPercent) {
        this.budgetUtilizationPercent = budgetUtilizationPercent;
    }
}
