package com.expensetracker.service;

import com.expensetracker.model.MonthlyAnalyticsDashboard;
import com.expensetracker.repository.BudgetRepository;
import com.expensetracker.repository.CategoryRepository;
import com.expensetracker.repository.ExpenseRepository;
import com.expensetracker.repository.SettingsRepository;

import java.time.YearMonth;

/**
 * Facade for all analytics operations exposed to the application layer.
 */
public class AnalyticsService {
    private final DashboardService dashboardService;

    public AnalyticsService(CategoryRepository categoryRepository,
                              ExpenseRepository expenseRepository,
                              BudgetRepository budgetRepository,
                              SettingsRepository settingsRepository) {
        this.dashboardService = new DashboardService(
                categoryRepository, expenseRepository, budgetRepository, settingsRepository);
    }

    public MonthlyAnalyticsDashboard getMonthlyDashboard(YearMonth month) {
        return dashboardService.getDashboard(month);
    }
}
