package com.expensetracker.analytics;

import com.expensetracker.model.Category;
import com.expensetracker.repository.BudgetRepository;
import com.expensetracker.repository.CategoryRepository;
import com.expensetracker.repository.ExpenseRepository;

import java.time.YearMonth;
import java.util.List;

/**
 * Encapsulates all data needed for analytics in a single context object.
 */
public class AnalysisContext {
    private final YearMonth month;
    private final double monthlyIncome;
    private final CategoryRepository categoryRepository;
    private final ExpenseRepository expenseRepository;
    private final BudgetRepository budgetRepository;

    public AnalysisContext(YearMonth month,
                           double monthlyIncome,
                           CategoryRepository categoryRepository,
                           ExpenseRepository expenseRepository,
                           BudgetRepository budgetRepository) {
        this.month = month;
        this.monthlyIncome = monthlyIncome;
        this.categoryRepository = categoryRepository;
        this.expenseRepository = expenseRepository;
        this.budgetRepository = budgetRepository;
    }

    public YearMonth getMonth() {
        return month;
    }

    public double getMonthlyIncome() {
        return monthlyIncome;
    }

    public CategoryRepository getCategoryRepository() {
        return categoryRepository;
    }

    public ExpenseRepository getExpenseRepository() {
        return expenseRepository;
    }

    public BudgetRepository getBudgetRepository() {
        return budgetRepository;
    }

    public List<Category> getCategories() {
        return categoryRepository.findAll();
    }

    public double totalForMonth(YearMonth targetMonth) {
        return expenseRepository.totalForMonth(targetMonth);
    }

    public double totalForCategoryAndMonth(String categoryId, YearMonth targetMonth) {
        return expenseRepository.totalForCategoryAndMonth(categoryId, targetMonth);
    }
}
