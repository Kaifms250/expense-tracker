package com.expensetracker.service;

import com.expensetracker.model.Budget;
import com.expensetracker.model.Category;
import com.expensetracker.repository.BudgetRepository;
import com.expensetracker.repository.CategoryRepository;
import com.expensetracker.repository.ExpenseRepository;

import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

public class BudgetService {
    private final BudgetRepository budgetRepository;
    private final CategoryRepository categoryRepository;
    private final ExpenseRepository expenseRepository;

    public BudgetService(BudgetRepository budgetRepository,
                         CategoryRepository categoryRepository,
                         ExpenseRepository expenseRepository) {
        this.budgetRepository = budgetRepository;
        this.categoryRepository = categoryRepository;
        this.expenseRepository = expenseRepository;
    }

    public Budget setBudget(String categoryId, double monthlyLimit) {
        if (monthlyLimit <= 0) {
            throw new IllegalArgumentException("Budget limit must be greater than zero.");
        }
        if (categoryRepository.findById(categoryId).isEmpty()) {
            throw new IllegalArgumentException("Invalid category.");
        }
        return budgetRepository.save(new Budget(categoryId, monthlyLimit));
    }

    public List<Budget> getAllBudgets() {
        return budgetRepository.findAll();
    }

    public boolean removeBudget(String categoryId) {
        return budgetRepository.deleteByCategoryId(categoryId);
    }

    public List<BudgetStatus> getBudgetStatusForMonth(YearMonth month) {
        List<BudgetStatus> statuses = new ArrayList<>();
        for (Budget budget : budgetRepository.findAll()) {
            Category category = categoryRepository.findById(budget.getCategoryId()).orElse(null);
            if (category == null) {
                continue;
            }
            double spent = expenseRepository.totalForCategoryAndMonth(budget.getCategoryId(), month);
            statuses.add(new BudgetStatus(category.getName(), budget.getMonthlyLimit(), spent));
        }
        return statuses;
    }

    public record BudgetStatus(String categoryName, double limit, double spent) {
        public double remaining() {
            return limit - spent;
        }

        public boolean isOverBudget() {
            return spent > limit;
        }

        public double percentUsed() {
            return limit > 0 ? (spent / limit) * 100 : 0;
        }
    }
}
