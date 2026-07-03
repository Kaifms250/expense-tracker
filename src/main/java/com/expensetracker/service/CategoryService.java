package com.expensetracker.service;

import com.expensetracker.model.Category;
import com.expensetracker.repository.CategoryRepository;

import java.util.List;
import java.util.Optional;

public class CategoryService {
    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public void initializeDefaults() {
        categoryRepository.seedDefaultsIfEmpty();
    }

    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    public Optional<Category> getCategoryById(String id) {
        return categoryRepository.findById(id);
    }

    public Category addCategory(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Category name cannot be empty.");
        }
        if (categoryRepository.findByName(name.trim()).isPresent()) {
            throw new IllegalArgumentException("Category already exists: " + name);
        }
        return categoryRepository.save(new Category(name.trim()));
    }

    public boolean deleteCategory(String id) {
        return categoryRepository.delete(id);
    }
}
