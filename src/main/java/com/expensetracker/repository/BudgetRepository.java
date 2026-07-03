package com.expensetracker.repository;

import com.expensetracker.model.Budget;
import com.expensetracker.storage.JsonFileStorage;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class BudgetRepository {
    private final JsonFileStorage<Budget> storage;
    private List<Budget> budgets;

    public BudgetRepository(Path dataDir) {
        this.storage = new JsonFileStorage<>(
                dataDir.resolve("budgets.json"),
                JsonFileStorage.listTypeOf(Budget.class));
        this.budgets = new ArrayList<>(storage.loadAll());
    }

    public List<Budget> findAll() {
        return new ArrayList<>(budgets);
    }

    public Optional<Budget> findByCategoryId(String categoryId) {
        return budgets.stream()
                .filter(b -> b.getCategoryId().equals(categoryId))
                .findFirst();
    }

    public Budget save(Budget budget) {
        budgets.removeIf(b -> b.getCategoryId().equals(budget.getCategoryId()));
        budgets.add(budget);
        storage.saveAll(budgets);
        return budget;
    }

    public boolean deleteByCategoryId(String categoryId) {
        boolean removed = budgets.removeIf(b -> b.getCategoryId().equals(categoryId));
        if (removed) {
            storage.saveAll(budgets);
        }
        return removed;
    }
}
