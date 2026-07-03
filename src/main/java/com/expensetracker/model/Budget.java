package com.expensetracker.model;

import java.util.UUID;

public class Budget {
    private String id;
    private String categoryId;
    private double monthlyLimit;

    public Budget() {
        this.id = UUID.randomUUID().toString();
    }

    public Budget(String categoryId, double monthlyLimit) {
        this();
        this.categoryId = categoryId;
        this.monthlyLimit = monthlyLimit;
    }

    public String getId() {
        return id;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public double getMonthlyLimit() {
        return monthlyLimit;
    }

    public void setMonthlyLimit(double monthlyLimit) {
        this.monthlyLimit = monthlyLimit;
    }
}
