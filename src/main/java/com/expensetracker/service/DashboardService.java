package com.expensetracker.service;

import com.expensetracker.analytics.AnalysisContext;
import com.expensetracker.analytics.ReportGenerator;
import com.expensetracker.model.MonthlyAnalyticsDashboard;
import com.expensetracker.model.UserSettings;
import com.expensetracker.repository.BudgetRepository;
import com.expensetracker.repository.CategoryRepository;
import com.expensetracker.repository.ExpenseRepository;
import com.expensetracker.repository.SettingsRepository;

import java.time.YearMonth;

/**
 * Orchestrates generation of the Monthly Analytics Dashboard.
 */
public class DashboardService {
    private final ReportGenerator reportGenerator = new ReportGenerator();
    private final CategoryRepository categoryRepository;
    private final ExpenseRepository expenseRepository;
    private final BudgetRepository budgetRepository;
    private final SettingsRepository settingsRepository;

    public DashboardService(CategoryRepository categoryRepository,
                            ExpenseRepository expenseRepository,
                            BudgetRepository budgetRepository,
                            SettingsRepository settingsRepository) {
        this.categoryRepository = categoryRepository;
        this.expenseRepository = expenseRepository;
        this.budgetRepository = budgetRepository;
        this.settingsRepository = settingsRepository;
    }

    public MonthlyAnalyticsDashboard getDashboard(YearMonth month) {
        UserSettings settings = settingsRepository.getSettings();
        AnalysisContext context = new AnalysisContext(
                month,
                settings.getMonthlyIncome(),
                categoryRepository,
                expenseRepository,
                budgetRepository);
        return reportGenerator.analyze(context);
    }
}
