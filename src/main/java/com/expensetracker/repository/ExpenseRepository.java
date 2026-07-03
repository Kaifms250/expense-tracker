package com.expensetracker.repository;

import com.expensetracker.model.Expense;
import com.expensetracker.storage.JsonFileStorage;

import java.nio.file.Path;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ExpenseRepository {
    private final JsonFileStorage<Expense> storage;
    private List<Expense> expenses;

    public ExpenseRepository(Path dataDir) {
        this.storage = new JsonFileStorage<>(
                dataDir.resolve("expenses.json"),
                JsonFileStorage.listTypeOf(Expense.class));
        this.expenses = new ArrayList<>(storage.loadAll());
    }

    public List<Expense> findAll() {
        return expenses.stream()
                .sorted(Comparator.comparing(Expense::getDate).reversed())
                .collect(Collectors.toList());
    }

    public List<Expense> findByMonth(YearMonth month) {
        return expenses.stream()
                .filter(e -> YearMonth.from(e.getDate()).equals(month))
                .sorted(Comparator.comparing(Expense::getDate).reversed())
                .collect(Collectors.toList());
    }

    public List<Expense> findByCategoryAndMonth(String categoryId, YearMonth month) {
        return expenses.stream()
                .filter(e -> e.getCategoryId().equals(categoryId))
                .filter(e -> YearMonth.from(e.getDate()).equals(month))
                .collect(Collectors.toList());
    }

    public Optional<Expense> findById(String id) {
        return expenses.stream().filter(e -> e.getId().equals(id)).findFirst();
    }

    public Expense save(Expense expense) {
        expenses.add(expense);
        storage.saveAll(expenses);
        return expense;
    }

    public boolean delete(String id) {
        boolean removed = expenses.removeIf(e -> e.getId().equals(id));
        if (removed) {
            storage.saveAll(expenses);
        }
        return removed;
    }

    public double totalForMonth(YearMonth month) {
        return findByMonth(month).stream().mapToDouble(Expense::getAmount).sum();
    }

    public double totalForCategoryAndMonth(String categoryId, YearMonth month) {
        return findByCategoryAndMonth(categoryId, month).stream()
                .mapToDouble(Expense::getAmount)
                .sum();
    }
}
