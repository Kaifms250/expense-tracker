package com.expensetracker.analytics;

import com.expensetracker.model.CategoryAnalyticsItem;
import com.expensetracker.model.Insight;
import com.expensetracker.model.InsightSeverity;
import com.expensetracker.model.InsightType;
import com.expensetracker.model.MonthlyAnalyticsDashboard;
import com.expensetracker.model.StatisticsResult;
import com.expensetracker.utils.CurrencyUtil;
import com.expensetracker.utils.PercentageUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Assembles the Monthly Analytics Dashboard report from all analyzer outputs.
 */
public class ReportGenerator extends BaseAnalyzer<MonthlyAnalyticsDashboard> {

    private final ExpenseAnalyzer expenseAnalyzer = new ExpenseAnalyzer();
    private final TrendAnalyzer trendAnalyzer = new TrendAnalyzer();
    private final CategoryAnalyzer categoryAnalyzer = new CategoryAnalyzer();
    private final StatisticsCalculator statisticsCalculator = new StatisticsCalculator();

    @Override
    public MonthlyAnalyticsDashboard analyze(AnalysisContext context) {
        ExpenseAnalysisResult expenseResult = expenseAnalyzer.analyze(context);
        TrendAnalysisResult trendResult = trendAnalyzer.analyze(context);
        List<CategoryAnalyticsItem> categories = categoryAnalyzer.analyze(context);
        StatisticsResult stats = statisticsCalculator.analyze(context);

        MonthlyAnalyticsDashboard dashboard = new MonthlyAnalyticsDashboard();
        dashboard.setMonth(context.getMonth());

        double totalExpenses = expenseResult.getTotalSpent();
        double income = context.getMonthlyIncome();
        double savings = income > 0 ? Math.max(income - totalExpenses, 0) : 0;

        dashboard.setTotalExpenses(totalExpenses);
        dashboard.setTotalIncome(income);
        dashboard.setTotalSavings(savings);
        dashboard.setNetBalance(income - totalExpenses);
        dashboard.setSavingsRatePercent(income > 0 ? PercentageUtil.of(savings, income) : 0);

        dashboard.setPreviousMonthTotal(expenseResult.getPreviousMonthTotal());
        dashboard.setChangeFromPreviousMonth(expenseResult.getChangeAmount());
        dashboard.setChangePercent(expenseResult.getChangePercent());

        double totalBudget = 0;
        double totalBudgetSpent = 0;
        for (CategoryAnalyticsItem cat : categories) {
            if (cat.getBudgetLimit() > 0) {
                totalBudget += cat.getBudgetLimit();
                totalBudgetSpent += cat.getSpent();
            }
        }
        dashboard.setTotalBudget(totalBudget);
        dashboard.setRemainingBudget(Math.max(totalBudget - totalBudgetSpent, 0));
        dashboard.setBudgetUtilizationPercent(PercentageUtil.of(totalBudgetSpent, totalBudget));
        dashboard.setBudgetStatus(resolveBudgetStatus(dashboard.getBudgetUtilizationPercent()));

        dashboard.setStatistics(stats);
        dashboard.setAllCategories(categories);
        dashboard.setTopCategories(categoryAnalyzer.topN(categories, 5));
        dashboard.setMonthlyTrend(trendResult.getMonthlyTrend());

        if (!categories.isEmpty()) {
            dashboard.setHighestSpendingCategory(categories.get(0).getCategoryName());
        }
        dashboard.setMostFrequentCategory(categoryAnalyzer.findMostFrequentCategory(categories));

        dashboard.setTrendSummary(buildTrendSummary(expenseResult));
        dashboard.setSmartInsights(buildSmartInsights(context, dashboard, expenseResult, stats, categories));
        dashboard.setRecommendations(buildRecommendations(dashboard, categories));

        return dashboard;
    }

    private String resolveBudgetStatus(double utilization) {
        if (utilization <= 0) return "No Budget";
        if (utilization > 100) return "Critical";
        if (utilization >= 80) return "Warning";
        if (utilization >= 50) return "Good";
        return "Excellent";
    }

    private String buildTrendSummary(ExpenseAnalysisResult expense) {
        if (expense.getTotalSpent() == 0) {
            return "No spending recorded this month.";
        }
        if (expense.getPreviousMonthTotal() == 0) {
            return String.format("You spent %s this month.", CurrencyUtil.format(expense.getTotalSpent()));
        }
        if (expense.getChangeAmount() > 0) {
            return String.format("Spending increased by %s (%.0f%%) compared to last month.",
                    CurrencyUtil.format(expense.getChangeAmount()), Math.abs(expense.getChangePercent()));
        }
        if (expense.getChangeAmount() < 0) {
            return String.format("Spending decreased by %s (%.0f%%) compared to last month.",
                    CurrencyUtil.format(Math.abs(expense.getChangeAmount())), Math.abs(expense.getChangePercent()));
        }
        return "Spending is unchanged compared to last month.";
    }

    private List<Insight> buildSmartInsights(AnalysisContext context,
                                               MonthlyAnalyticsDashboard dashboard,
                                               ExpenseAnalysisResult expense,
                                               StatisticsResult stats,
                                               List<CategoryAnalyticsItem> categories) {
        List<Insight> insights = new ArrayList<>();

        if (!categories.isEmpty()) {
            insights.add(new Insight(InsightType.CATEGORY, InsightSeverity.INFO,
                    "Highest spending category this month: " + categories.get(0).getCategoryName() + ".",
                    categories.get(0).getCategoryName(), categories.get(0).getSpent()));
        }

        if (expense.getPreviousMonthTotal() > 0) {
            InsightSeverity sev = expense.getChangeAmount() > 0 ? InsightSeverity.WARNING : InsightSeverity.SUCCESS;
            String dir = expense.getChangeAmount() >= 0 ? "increased" : "decreased";
            insights.add(new Insight(InsightType.TREND, sev,
                    String.format("Spending %s by %s compared to previous month.", dir,
                            CurrencyUtil.format(Math.abs(expense.getChangeAmount())))));
        }

        if (stats.getLargestTransaction() > 0) {
            insights.add(new Insight(InsightType.CATEGORY, InsightSeverity.INFO,
                    String.format("Largest single transaction: %s (%s).",
                            CurrencyUtil.format(stats.getLargestTransaction()),
                            stats.getLargestTransactionCategory())));
        }

        if (stats.getMostActiveSpendingDay() != null) {
            insights.add(new Insight(InsightType.TREND, InsightSeverity.INFO,
                    String.format("Most active spending day: %s (%d transactions, %s).",
                            stats.getMostActiveSpendingDay(),
                            stats.getMostActiveDayCount(),
                            CurrencyUtil.format(stats.getMostActiveDayTotal()))));
        }

        if (dashboard.getBudgetUtilizationPercent() >= 80) {
            insights.add(new Insight(InsightType.BUDGET, InsightSeverity.WARNING,
                    String.format("Budget warning: %.0f%% of your total budget has been utilized.",
                            dashboard.getBudgetUtilizationPercent())));
        }

        for (CategoryAnalyticsItem cat : categories) {
            if (cat.getBudgetLimit() > 0 && cat.getBudgetUtilizationPercent() >= 80) {
                insights.add(new Insight(InsightType.BUDGET, InsightSeverity.WARNING,
                        String.format("%s budget is %.0f%% utilized — only %s remaining.",
                                cat.getCategoryName(),
                                cat.getBudgetUtilizationPercent(),
                                CurrencyUtil.format(cat.getRemainingBudget())),
                        cat.getCategoryName(), cat.getBudgetUtilizationPercent()));
            }
        }

        insights.add(new Insight(InsightType.SUMMARY, InsightSeverity.INFO, dashboard.getTrendSummary()));
        return insights;
    }

    private List<String> buildRecommendations(MonthlyAnalyticsDashboard dashboard,
                                              List<CategoryAnalyticsItem> categories) {
        List<String> recs = new ArrayList<>();

        if (dashboard.getSavingsRatePercent() < 20 && dashboard.getTotalIncome() > 0) {
            recs.add("Try to save at least 20% of your income. Consider reducing discretionary spending.");
        }

        CategoryAnalyticsItem top = categories.isEmpty() ? null : categories.get(0);
        if (top != null && top.getPercentOfTotal() > 40) {
            recs.add(String.format("%s accounts for %.0f%% of spending — review for unnecessary expenses.",
                    top.getCategoryName(), top.getPercentOfTotal()));
        }

        if (dashboard.getBudgetUtilizationPercent() > 100) {
            recs.add("You've exceeded your overall budget. Prioritize essential categories next month.");
        } else if (dashboard.getBudgetUtilizationPercent() >= 80) {
            recs.add("You're approaching your budget limit. Monitor spending for the rest of the month.");
        }

        if (dashboard.getChangePercent() > 15) {
            recs.add("Spending rose significantly this month. Compare with last month's top categories.");
        }

        if (recs.isEmpty() && dashboard.getTotalExpenses() > 0) {
            recs.add("Your spending patterns look healthy. Keep tracking to maintain financial awareness.");
        }

        return recs;
    }
}
