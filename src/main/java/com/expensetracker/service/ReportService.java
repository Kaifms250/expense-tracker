package com.expensetracker.service;

import com.expensetracker.model.Category;
import com.expensetracker.model.CategorySummary;
import com.expensetracker.model.MonthlyReport;
import com.expensetracker.repository.BudgetRepository;
import com.expensetracker.repository.CategoryRepository;
import com.expensetracker.repository.ExpenseRepository;

import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

public class ReportService {
    private final ExpenseRepository expenseRepository;
    private final CategoryRepository categoryRepository;
    private final BudgetRepository budgetRepository;

    public ReportService(ExpenseRepository expenseRepository,
                         CategoryRepository categoryRepository,
                         BudgetRepository budgetRepository) {
        this.expenseRepository = expenseRepository;
        this.categoryRepository = categoryRepository;
        this.budgetRepository = budgetRepository;
    }

    public MonthlyReport generateMonthlyReport(YearMonth month) {
        double totalSpent = expenseRepository.totalForMonth(month);
        double previousMonthTotal = expenseRepository.totalForMonth(month.minusMonths(1));

        List<CategorySummary> summaries = new ArrayList<>();
        for (Category category : categoryRepository.findAll()) {
            double spent = expenseRepository.totalForCategoryAndMonth(category.getId(), month);
            if (spent == 0) {
                continue;
            }
            double budgetLimit = budgetRepository.findByCategoryId(category.getId())
                    .map(b -> b.getMonthlyLimit())
                    .orElse(0.0);
            double remaining = budgetLimit > 0 ? budgetLimit - spent : 0;
            summaries.add(new CategorySummary(
                    category.getId(), category.getName(), spent, budgetLimit, remaining));
        }

        summaries.sort((a, b) -> Double.compare(b.getSpent(), a.getSpent()));
        return new MonthlyReport(month, totalSpent, summaries, previousMonthTotal);
    }
}
