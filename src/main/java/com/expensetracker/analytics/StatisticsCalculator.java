package com.expensetracker.analytics;

import com.expensetracker.model.Category;
import com.expensetracker.model.Expense;
import com.expensetracker.model.StatisticsResult;

import java.time.DayOfWeek;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Computes statistical metrics from raw expense transactions.
 */
public class StatisticsCalculator extends BaseAnalyzer<StatisticsResult> {

    @Override
    public StatisticsResult analyze(AnalysisContext context) {
        YearMonth month = context.getMonth();
        List<Expense> expenses = context.getExpenseRepository().findByMonth(month);
        StatisticsResult stats = new StatisticsResult();
        stats.setTotalTransactions(expenses.size());

        if (expenses.isEmpty()) {
            return stats;
        }

        double total = expenses.stream().mapToDouble(Expense::getAmount).sum();
        stats.setMonthlyAverageSpending(total);

        Expense largest = expenses.stream()
                .max(Comparator.comparingDouble(Expense::getAmount))
                .orElse(null);
        Expense smallest = expenses.stream()
                .min(Comparator.comparingDouble(Expense::getAmount))
                .orElse(null);

        if (largest != null) {
            stats.setHighestExpense(largest.getAmount());
            stats.setLargestTransaction(largest.getAmount());
            stats.setLargestTransactionDescription(
                    largest.getDescription() != null && !largest.getDescription().isBlank()
                            ? largest.getDescription() : "No description");
            stats.setLargestTransactionCategory(resolveCategoryName(context, largest.getCategoryId()));
        }
        if (smallest != null) {
            stats.setLowestExpense(smallest.getAmount());
        }

        stats.setAverageExpensePerTransaction(total / expenses.size());

        int daysInMonth = month.lengthOfMonth();
        int daysWithSpending = (int) expenses.stream().map(Expense::getDate).distinct().count();
        int activeDays = Math.max(daysWithSpending, 1);
        stats.setDailyAverageSpending(total / activeDays);

        int weeksInMonth = Math.max((daysInMonth + 6) / 7, 1);
        stats.setWeeklyAverageSpending(total / weeksInMonth);

        computeMostActiveDay(expenses, stats);
        return stats;
    }

    private void computeMostActiveDay(List<Expense> expenses, StatisticsResult stats) {
        Map<DayOfWeek, Double> dayTotals = new HashMap<>();
        Map<DayOfWeek, Integer> dayCounts = new HashMap<>();

        for (Expense expense : expenses) {
            DayOfWeek dow = expense.getDate().getDayOfWeek();
            dayTotals.merge(dow, expense.getAmount(), Double::sum);
            dayCounts.merge(dow, 1, Integer::sum);
        }

        DayOfWeek mostActive = dayTotals.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);

        if (mostActive != null) {
            stats.setMostActiveSpendingDay(
                    mostActive.getDisplayName(TextStyle.FULL, Locale.ENGLISH));
            stats.setMostActiveDayTotal(dayTotals.get(mostActive));
            stats.setMostActiveDayCount(dayCounts.get(mostActive));
        }
    }

    private String resolveCategoryName(AnalysisContext context, String categoryId) {
        return context.getCategoryRepository().findById(categoryId)
                .map(Category::getName)
                .orElse("Unknown");
    }
}
