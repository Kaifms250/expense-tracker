package com.expensetracker.analytics;

import com.expensetracker.model.Budget;
import com.expensetracker.model.Category;
import com.expensetracker.model.CategoryAnalyticsItem;
import com.expensetracker.model.Expense;

import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Analyzes category-level spending patterns and budget utilization.
 */
public class CategoryAnalyzer extends BaseAnalyzer<List<CategoryAnalyticsItem>> {

    @Override
    public List<CategoryAnalyticsItem> analyze(AnalysisContext context) {
        YearMonth month = context.getMonth();
        List<Expense> expenses = context.getExpenseRepository().findByMonth(month);
        double totalSpent = expenses.stream().mapToDouble(Expense::getAmount).sum();

        Map<String, Integer> txCountByCategory = new HashMap<>();
        for (Expense expense : expenses) {
            txCountByCategory.merge(expense.getCategoryId(), 1, Integer::sum);
        }

        List<CategoryAnalyticsItem> items = new ArrayList<>();
        for (Category category : context.getCategories()) {
            double spent = context.totalForCategoryAndMonth(category.getId(), month);
            if (spent <= 0 && !txCountByCategory.containsKey(category.getId())) {
                continue;
            }

            CategoryAnalyticsItem item = new CategoryAnalyticsItem(
                    category.getId(),
                    category.getName(),
                    spent,
                    totalSpent > 0 ? (spent / totalSpent) * 100 : 0,
                    txCountByCategory.getOrDefault(category.getId(), 0));

            context.getBudgetRepository().findByCategoryId(category.getId()).ifPresent(budget -> {
                item.setBudgetLimit(budget.getMonthlyLimit());
                item.setRemainingBudget(Math.max(budget.getMonthlyLimit() - spent, 0));
                item.setBudgetUtilizationPercent(
                        budget.getMonthlyLimit() > 0
                                ? (spent / budget.getMonthlyLimit()) * 100 : 0);
            });

            items.add(item);
        }

        items.sort(Comparator.comparingDouble(CategoryAnalyticsItem::getSpent).reversed());
        return items;
    }

    public String findMostFrequentCategory(List<CategoryAnalyticsItem> items) {
        return items.stream()
                .max(Comparator.comparingInt(CategoryAnalyticsItem::getTransactionCount))
                .map(CategoryAnalyticsItem::getCategoryName)
                .orElse(null);
    }

    public List<CategoryAnalyticsItem> topN(List<CategoryAnalyticsItem> items, int n) {
        return items.stream().limit(n).collect(Collectors.toList());
    }
}
