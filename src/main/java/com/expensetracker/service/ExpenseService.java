package com.expensetracker.service;

import com.expensetracker.model.Expense;
import com.expensetracker.repository.CategoryRepository;
import com.expensetracker.repository.ExpenseRepository;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

public class ExpenseService {
    private final ExpenseRepository expenseRepository;
    private final CategoryRepository categoryRepository;

    public ExpenseService(ExpenseRepository expenseRepository, CategoryRepository categoryRepository) {
        this.expenseRepository = expenseRepository;
        this.categoryRepository = categoryRepository;
    }

    public Expense addExpense(double amount, String categoryId, LocalDate date, String description) {
        validateExpense(amount, categoryId, date);
        Expense expense = new Expense(amount, categoryId, date, description != null ? description.trim() : "");
        return expenseRepository.save(expense);
    }

    public List<Expense> getAllExpenses() {
        return expenseRepository.findAll();
    }

    public List<Expense> getExpensesForMonth(YearMonth month) {
        return expenseRepository.findByMonth(month);
    }

    public Optional<Expense> getExpenseById(String id) {
        return expenseRepository.findById(id);
    }

    public boolean deleteExpense(String id) {
        return expenseRepository.delete(id);
    }

    private void validateExpense(double amount, String categoryId, LocalDate date) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero.");
        }
        if (date == null) {
            throw new IllegalArgumentException("Date is required.");
        }
        if (date.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Date cannot be in the future.");
        }
        if (categoryRepository.findById(categoryId).isEmpty()) {
            throw new IllegalArgumentException("Invalid category.");
        }
    }
}
