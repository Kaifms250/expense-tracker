package com.expensetracker.analytics;

import com.expensetracker.model.Category;
import com.expensetracker.model.MonthlyTrendPoint;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Detects spending trends across months and consecutive category increases.
 */
public class TrendAnalyzer extends BaseAnalyzer<TrendAnalysisResult> {
    private static final int TREND_MONTHS = 6;
    private static final DateTimeFormatter MONTH_LABEL = DateTimeFormatter.ofPattern("MMM yyyy");

    @Override
    public TrendAnalysisResult analyze(AnalysisContext context) {
        YearMonth month = context.getMonth();
        List<MonthlyTrendPoint> trend = buildMonthlyTrend(context, month);
        Map<String, Integer> consecutive = detectConsecutiveIncreases(context, month);
        return new TrendAnalysisResult(trend, consecutive);
    }

    private List<MonthlyTrendPoint> buildMonthlyTrend(AnalysisContext context, YearMonth month) {
        List<MonthlyTrendPoint> points = new ArrayList<>();
        for (int i = TREND_MONTHS - 1; i >= 0; i--) {
            YearMonth target = month.minusMonths(i);
            double total = context.totalForMonth(target);
            points.add(new MonthlyTrendPoint(target.format(MONTH_LABEL), total));
        }
        return points;
    }

    private Map<String, Integer> detectConsecutiveIncreases(AnalysisContext context, YearMonth month) {
        Map<String, Integer> result = new HashMap<>();
        for (Category category : context.getCategories()) {
            int streak = countConsecutiveIncreases(context, category.getId(), month);
            if (streak >= 2) {
                result.put(category.getName(), streak);
            }
        }
        return result;
    }

    private int countConsecutiveIncreases(AnalysisContext context, String categoryId, YearMonth month) {
        int streak = 0;
        YearMonth current = month;
        while (true) {
            double thisMonth = context.totalForCategoryAndMonth(categoryId, current);
            double lastMonth = context.totalForCategoryAndMonth(categoryId, current.minusMonths(1));
            if (lastMonth > 0 && thisMonth > lastMonth) {
                streak++;
                current = current.minusMonths(1);
            } else {
                break;
            }
        }
        return streak;
    }
}
