package com.expensetracker.service;

import com.expensetracker.analytics.AnalyticsEngine;
import com.expensetracker.model.SpendingInsightsReport;
import com.expensetracker.model.UserSettings;
import com.expensetracker.repository.BudgetRepository;
import com.expensetracker.repository.CategoryRepository;
import com.expensetracker.repository.ExpenseRepository;
import com.expensetracker.repository.SettingsRepository;

import java.time.YearMonth;

public class InsightsService {
    private final AnalyticsEngine analyticsEngine;
    private final CategoryRepository categoryRepository;
    private final ExpenseRepository expenseRepository;
    private final BudgetRepository budgetRepository;
    private final SettingsRepository settingsRepository;

    public InsightsService(CategoryRepository categoryRepository,
                           ExpenseRepository expenseRepository,
                           BudgetRepository budgetRepository,
                           SettingsRepository settingsRepository) {
        this.analyticsEngine = new AnalyticsEngine();
        this.categoryRepository = categoryRepository;
        this.expenseRepository = expenseRepository;
        this.budgetRepository = budgetRepository;
        this.settingsRepository = settingsRepository;
    }

    public SpendingInsightsReport getInsights(YearMonth month) {
        UserSettings settings = settingsRepository.getSettings();
        return analyticsEngine.generateInsights(
                month,
                settings.getMonthlyIncome(),
                categoryRepository,
                expenseRepository,
                budgetRepository);
    }

    public UserSettings getSettings() {
        return settingsRepository.getSettings();
    }

    public UserSettings updateSettings(double monthlyIncome, String userName) {
        if (monthlyIncome < 0) {
            throw new IllegalArgumentException("Monthly income cannot be negative.");
        }
        UserSettings settings = settingsRepository.getSettings();
        settings.setMonthlyIncome(monthlyIncome);
        if (userName != null && !userName.isBlank()) {
            settings.setUserName(userName.trim());
        }
        return settingsRepository.save(settings);
    }
}
