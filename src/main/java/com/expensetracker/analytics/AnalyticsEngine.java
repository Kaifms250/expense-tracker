package com.expensetracker.analytics;

import com.expensetracker.model.SpendingInsightsReport;
import com.expensetracker.repository.BudgetRepository;
import com.expensetracker.repository.CategoryRepository;
import com.expensetracker.repository.ExpenseRepository;

import java.time.YearMonth;

/**
 * Facade for the analytics engine — coordinates all analyzers.
 */
public class AnalyticsEngine {
    private final MonthlyReportGenerator reportGenerator = new MonthlyReportGenerator();

    public SpendingInsightsReport generateInsights(YearMonth month,
                                                   double monthlyIncome,
                                                   CategoryRepository categoryRepository,
                                                   ExpenseRepository expenseRepository,
                                                   BudgetRepository budgetRepository) {
        AnalysisContext context = new AnalysisContext(
                month, monthlyIncome, categoryRepository, expenseRepository, budgetRepository);
        return reportGenerator.analyze(context);
    }
}
