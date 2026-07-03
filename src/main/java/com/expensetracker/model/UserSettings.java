package com.expensetracker.model;

public class UserSettings {
    private double monthlyIncome;
    private String userName;

    public UserSettings() {
        this.monthlyIncome = 0;
        this.userName = "User";
    }

    public double getMonthlyIncome() {
        return monthlyIncome;
    }

    public void setMonthlyIncome(double monthlyIncome) {
        this.monthlyIncome = monthlyIncome;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
}
