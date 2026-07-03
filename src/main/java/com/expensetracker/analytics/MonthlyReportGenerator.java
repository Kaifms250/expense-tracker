package com.expensetracker.analytics;

import com.expensetracker.model.Budget;
import com.expensetracker.model.Category;
import com.expensetracker.model.Insight;
import com.expensetracker.model.InsightSeverity;
import com.expensetracker.model.InsightType;
import com.expensetracker.model.SpendingInsightsReport;
import com.expensetracker.utils.CurrencyUtil;
import com.expensetracker.utils.PercentageUtil;

import java.time.YearMonth;
import java.util.List;

/**
 * Assembles the final monthly insights report from all analysis components.
 */
public class MonthlyReportGenerator extends BaseAnalyzer<SpendingInsightsReport> {

    private final ExpenseAnalyzer expenseAnalyzer = new ExpenseAnalyzer();
    private final TrendAnalyzer trendAnalyzer = new TrendAnalyzer();
    private final InsightGenerator insightGenerator = new InsightGenerator();

    @Override
    public SpendingInsightsReport analyze(AnalysisContext context) {
        ExpenseAnalysisResult expenseResult = expenseAnalyzer.analyze(context);
        TrendAnalysisResult trendResult = trendAnalyzer.analyze(context);
        List<Insight> insights = insightGenerator.generate(context, expenseResult, trendResult);

        SpendingInsightsReport report = new SpendingInsightsReport();
        report.setMonth(context.getMonth());
        report.setTotalSpent(expenseResult.getTotalSpent());
        report.setPreviousMonthTotal(expenseResult.getPreviousMonthTotal());
        report.setChangeFromPreviousMonth(expenseResult.getChangeAmount());
        report.setChangePercent(expenseResult.getChangePercent());
        report.setMonthlyIncome(context.getMonthlyIncome());
        report.setCategoryBreakdown(expenseResult.getCategorySpending());
        report.setTrendData(trendResult.getMonthlyTrend());
        report.setInsights(insights);

        if (expenseResult.getHighest() != null) {
            report.setHighestCategory(expenseResult.getHighest().getCategoryName());
        }
        if (expenseResult.getLowest() != null) {
            report.setLowestCategory(expenseResult.getLowest().getCategoryName());
        }
        if (expenseResult.getFastestGrowing() != null) {
            report.setFastestGrowingCategory(expenseResult.getFastestGrowing().getCategoryName());
        }

        double totalBudget = 0;
        double totalBudgetSpent = 0;
        for (Budget budget : context.getBudgetRepository().findAll()) {
            totalBudget += budget.getMonthlyLimit();
            totalBudgetSpent += context.totalForCategoryAndMonth(
                    budget.getCategoryId(), context.getMonth());
        }
        report.setTotalBudget(totalBudget);
        report.setBudgetUtilizationPercent(PercentageUtil.of(totalBudgetSpent, totalBudget));

        double potentialSavings = insights.stream()
                .filter(i -> i.getType() == InsightType.SAVINGS)
                .filter(i -> !i.getMessage().startsWith("Potential monthly savings"))
                .mapToDouble(Insight::getValue)
                .sum();
        report.setPotentialMonthlySavings(potentialSavings);

        report.setSummary(buildSummary(context, expenseResult, insights));
        insights.add(0, new Insight(InsightType.SUMMARY, InsightSeverity.INFO, report.getSummary()));

        return report;
    }

    private String buildSummary(AnalysisContext context,
                                ExpenseAnalysisResult expense,
                                List<Insight> insights) {
        if (expense.getTotalSpent() == 0) {
            return "No expenses recorded this month. Start tracking to unlock personalized insights.";
        }

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("You spent %s this month", CurrencyUtil.format(expense.getTotalSpent())));

        if (expense.getPreviousMonthTotal() > 0) {
            if (expense.getChangeAmount() > 0) {
                sb.append(String.format(", up %s from last month", CurrencyUtil.format(expense.getChangeAmount())));
            } else if (expense.getChangeAmount() < 0) {
                sb.append(String.format(", down %s from last month", CurrencyUtil.format(Math.abs(expense.getChangeAmount()))));
            }
        }
        sb.append(". ");

        if (expense.getHighest() != null) {
            sb.append(String.format("%s was your top category. ", expense.getHighest().getCategoryName()));
        }

        long warnings = insights.stream().filter(i -> i.getSeverity() == InsightSeverity.WARNING).count();
        if (warnings > 0) {
            sb.append(String.format("%d areas need attention. ", warnings));
        }

        double savings = insights.stream()
                .filter(i -> i.getType() == InsightType.SAVINGS)
                .mapToDouble(Insight::getValue)
                .max()
                .orElse(0);
        if (savings > 0) {
            sb.append(String.format("You could potentially save up to %s per month.", CurrencyUtil.format(savings)));
        }

        return sb.toString().trim();
    }
}
