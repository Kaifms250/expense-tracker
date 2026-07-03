package com.expensetracker.repository;

import com.expensetracker.model.Category;
import com.expensetracker.storage.JsonFileStorage;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CategoryRepository {
    private final JsonFileStorage<Category> storage;
    private List<Category> categories;

    public CategoryRepository(Path dataDir) {
        this.storage = new JsonFileStorage<>(
                dataDir.resolve("categories.json"),
                JsonFileStorage.listTypeOf(Category.class));
        this.categories = new ArrayList<>(storage.loadAll());
    }

    public List<Category> findAll() {
        return new ArrayList<>(categories);
    }

    public Optional<Category> findById(String id) {
        return categories.stream().filter(c -> c.getId().equals(id)).findFirst();
    }

    public Optional<Category> findByName(String name) {
        return categories.stream()
                .filter(c -> c.getName().equalsIgnoreCase(name))
                .findFirst();
    }

    public Category save(Category category) {
        categories.add(category);
        storage.saveAll(categories);
        return category;
    }

    public boolean delete(String id) {
        boolean removed = categories.removeIf(c -> c.getId().equals(id));
        if (removed) {
            storage.saveAll(categories);
        }
        return removed;
    }

    public void seedDefaultsIfEmpty() {
        if (!categories.isEmpty()) {
            return;
        }
        String[] defaults = {"Food", "Transport", "Rent", "Entertainment", "Shopping", "Utilities", "Health", "Other"};
        for (String name : defaults) {
            categories.add(new Category(name));
        }
        storage.saveAll(categories);
    }
}
