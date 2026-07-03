package com.expensetracker.analytics;

import com.expensetracker.model.Category;
import com.expensetracker.model.CategorySpending;

import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Result object produced by ExpenseAnalyzer.
 */
public class ExpenseAnalysisResult {
    private final double totalSpent;
    private final double previousMonthTotal;
    private final double changeAmount;
    private final double changePercent;
    private final List<CategorySpending> categorySpending;
    private final CategorySpending highest;
    private final CategorySpending lowest;
    private final CategorySpending fastestGrowing;

    public ExpenseAnalysisResult(double totalSpent,
                                 double previousMonthTotal,
                                 List<CategorySpending> categorySpending) {
        this.totalSpent = totalSpent;
        this.previousMonthTotal = previousMonthTotal;
        this.changeAmount = totalSpent - previousMonthTotal;
        this.changePercent = previousMonthTotal > 0
                ? (changeAmount / previousMonthTotal) * 100
                : (totalSpent > 0 ? 100 : 0);
        this.categorySpending = categorySpending;
        this.highest = findHighest(categorySpending);
        this.lowest = findLowest(categorySpending);
        this.fastestGrowing = findFastestGrowing(categorySpending);
    }

    private CategorySpending findHighest(List<CategorySpending> list) {
        return list.stream()
                .filter(c -> c.getCurrentMonth() > 0)
                .max(Comparator.comparingDouble(CategorySpending::getCurrentMonth))
                .orElse(null);
    }

    private CategorySpending findLowest(List<CategorySpending> list) {
        return list.stream()
                .filter(c -> c.getCurrentMonth() > 0)
                .min(Comparator.comparingDouble(CategorySpending::getCurrentMonth))
                .orElse(null);
    }

    private CategorySpending findFastestGrowing(List<CategorySpending> list) {
        return list.stream()
                .filter(c -> c.getPreviousMonth() > 0 && c.isIncreasing())
                .max(Comparator.comparingDouble(CategorySpending::getChangePercent))
                .orElse(null);
    }

    public double getTotalSpent() {
        return totalSpent;
    }

    public double getPreviousMonthTotal() {
        return previousMonthTotal;
    }

    public double getChangeAmount() {
        return changeAmount;
    }

    public double getChangePercent() {
        return changePercent;
    }

    public List<CategorySpending> getCategorySpending() {
        return categorySpending;
    }

    public CategorySpending getHighest() {
        return highest;
    }

    public CategorySpending getLowest() {
        return lowest;
    }

    public CategorySpending getFastestGrowing() {
        return fastestGrowing;
    }
}
