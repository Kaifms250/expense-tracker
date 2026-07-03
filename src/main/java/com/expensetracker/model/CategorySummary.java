package com.expensetracker.model;

public class CategorySummary {
    private final String categoryId;
    private final String categoryName;
    private final double spent;
    private final double budgetLimit;
    private final double remaining;

    public CategorySummary(String categoryId, String categoryName, double spent,
                           double budgetLimit, double remaining) {
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.spent = spent;
        this.budgetLimit = budgetLimit;
        this.remaining = remaining;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public double getSpent() {
        return spent;
    }

    public double getBudgetLimit() {
        return budgetLimit;
    }

    public double getRemaining() {
        return remaining;
    }

    public boolean isOverBudget() {
        return budgetLimit > 0 && spent > budgetLimit;
    }
}
