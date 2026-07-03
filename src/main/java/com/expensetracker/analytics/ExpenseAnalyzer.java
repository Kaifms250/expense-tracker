package com.expensetracker.analytics;

import com.expensetracker.model.CategorySpending;

import java.time.YearMonth;
import java.util.List;

/**
 * Analyzes expense totals and per-category spending for a given month.
 */
public class ExpenseAnalyzer extends BaseAnalyzer<ExpenseAnalysisResult> {

    @Override
    public ExpenseAnalysisResult analyze(AnalysisContext context) {
        YearMonth month = context.getMonth();
        double totalSpent = context.totalForMonth(month);
        double previousTotal = context.totalForMonth(month.minusMonths(1));
        List<CategorySpending> categorySpending = buildCategorySpending(context);
        return new ExpenseAnalysisResult(totalSpent, previousTotal, categorySpending);
    }
}
