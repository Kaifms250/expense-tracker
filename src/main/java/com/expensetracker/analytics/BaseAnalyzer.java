package com.expensetracker.analytics;

import com.expensetracker.model.Category;
import com.expensetracker.model.CategorySpending;

import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

/**
 * Base class providing shared analytics helpers.
 * Inheritance: concrete analyzers extend this class.
 */
public abstract class BaseAnalyzer<T> implements Analyzer<T> {

    protected List<CategorySpending> buildCategorySpending(AnalysisContext context) {
        YearMonth month = context.getMonth();
        YearMonth previous = month.minusMonths(1);
        List<CategorySpending> result = new ArrayList<>();

        for (Category category : context.getCategories()) {
            double current = context.totalForCategoryAndMonth(category.getId(), month);
            double prev = context.totalForCategoryAndMonth(category.getId(), previous);
            if (current > 0 || prev > 0) {
                result.add(new CategorySpending(
                        category.getId(), category.getName(), current, prev));
            }
        }
        return result;
    }

    protected Category findCategoryByName(AnalysisContext context, String name) {
        return context.getCategories().stream()
                .filter(c -> c.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
    }
}
