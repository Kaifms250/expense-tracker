package com.expensetracker.analytics;

import com.expensetracker.model.MonthlyTrendPoint;

import java.util.List;
import java.util.Map;

/**
 * Result object produced by TrendAnalyzer.
 */
public class TrendAnalysisResult {
    private final List<MonthlyTrendPoint> monthlyTrend;
    private final Map<String, Integer> consecutiveIncreases;

    public TrendAnalysisResult(List<MonthlyTrendPoint> monthlyTrend,
                               Map<String, Integer> consecutiveIncreases) {
        this.monthlyTrend = monthlyTrend;
        this.consecutiveIncreases = consecutiveIncreases;
    }

    public List<MonthlyTrendPoint> getMonthlyTrend() {
        return monthlyTrend;
    }

    public Map<String, Integer> getConsecutiveIncreases() {
        return consecutiveIncreases;
    }

    public int getConsecutiveMonths(String categoryName) {
        return consecutiveIncreases.getOrDefault(categoryName, 0);
    }
}
