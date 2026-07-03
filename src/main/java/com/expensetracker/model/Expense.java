package com.expensetracker.model;

import java.time.LocalDate;
import java.util.UUID;

public class Expense {
    private String id;
    private double amount;
    private String categoryId;
    private LocalDate date;
    private String description;

    public Expense() {
        this.id = UUID.randomUUID().toString();
    }

    public Expense(double amount, String categoryId, LocalDate date, String description) {
        this();
        this.amount = amount;
        this.categoryId = categoryId;
        this.date = date;
        this.description = description;
    }

    public String getId() {
        return id;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
