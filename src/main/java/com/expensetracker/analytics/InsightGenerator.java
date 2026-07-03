package com.expensetracker.analytics;

import com.expensetracker.model.Budget;
import com.expensetracker.model.Category;
import com.expensetracker.model.CategorySpending;
import com.expensetracker.model.Expense;
import com.expensetracker.model.Insight;
import com.expensetracker.model.InsightSeverity;
import com.expensetracker.model.InsightType;
import com.expensetracker.utils.CurrencyUtil;
import com.expensetracker.utils.PercentageUtil;

import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Generates personalized spending insights from analysis results.
 */
public class InsightGenerator extends BaseAnalyzer<Void> {

    public List<Insight> generate(AnalysisContext context,
                                  ExpenseAnalysisResult expenseResult,
                                  TrendAnalysisResult trendResult) {
        List<Insight> insights = new ArrayList<>();

        addOverallTrendInsights(insights, expenseResult);
        addCategoryInsights(insights, expenseResult);
        addBudgetInsights(insights, context);
        addConsecutiveTrendInsights(insights, trendResult);
        addIncomeInsights(insights, context, expenseResult);
        addSavingsInsights(insights, context, expenseResult);

        insights.sort(Comparator.comparingInt(i -> severityOrder(i.getSeverity())));
        return insights;
    }

    private int severityOrder(InsightSeverity severity) {
        return switch (severity) {
            case WARNING -> 0;
            case INFO -> 1;
            case SUCCESS -> 2;
        };
    }

    private void addOverallTrendInsights(List<Insight> insights, ExpenseAnalysisResult result) {
        if (result.getPreviousMonthTotal() == 0 && result.getTotalSpent() == 0) {
            return;
        }
        double pct = result.getChangePercent();
        if (result.getChangeAmount() > 0) {
            insights.add(new Insight(
                    InsightType.TREND, InsightSeverity.WARNING,
                    String.format("Total spending increased by %s (%.0f%%) compared to last month.",
                            CurrencyUtil.format(result.getChangeAmount()), Math.abs(pct)),
                    null, result.getChangeAmount()));
        } else if (result.getChangeAmount() < 0) {
            insights.add(new Insight(
                    InsightType.TREND, InsightSeverity.SUCCESS,
                    String.format("Great job! Total spending decreased by %s (%.0f%%) compared to last month.",
                            CurrencyUtil.format(Math.abs(result.getChangeAmount())), Math.abs(pct)),
                    null, Math.abs(result.getChangeAmount())));
        } else {
            insights.add(new Insight(
                    InsightType.TREND, InsightSeverity.INFO,
                    "Total spending is unchanged compared to last month."));
        }
    }

    private void addCategoryInsights(List<Insight> insights, ExpenseAnalysisResult result) {
        CategorySpending highest = result.getHighest();
        if (highest != null) {
            insights.add(new Insight(
                    InsightType.CATEGORY, InsightSeverity.INFO,
                    String.format("Most expensive category this month: %s (%s).",
                            highest.getCategoryName(), CurrencyUtil.format(highest.getCurrentMonth())),
                    highest.getCategoryName(), highest.getCurrentMonth()));
        }

        CategorySpending fastest = result.getFastestGrowing();
        if (fastest != null && fastest.getChangePercent() >= 10) {
            insights.add(new Insight(
                    InsightType.CATEGORY, InsightSeverity.WARNING,
                    String.format("%s expenses increased by %.0f%% compared to last month.",
                            fastest.getCategoryName(), fastest.getChangePercent()),
                    fastest.getCategoryName(), fastest.getChangePercent()));
        }

        for (CategorySpending cs : result.getCategorySpending()) {
            if (cs.getPreviousMonth() > 0 && cs.getChangePercent() >= 15) {
                boolean alreadyCovered = fastest != null
                        && fastest.getCategoryName().equals(cs.getCategoryName());
                if (!alreadyCovered) {
                    insights.add(new Insight(
                            InsightType.CATEGORY, InsightSeverity.WARNING,
                            String.format("%s expenses increased by %.0f%% this month.",
                                    cs.getCategoryName(), cs.getChangePercent()),
                            cs.getCategoryName(), cs.getChangePercent()));
                }
            }
        }
    }

    private void addBudgetInsights(List<Insight> insights, AnalysisContext context) {
        YearMonth month = context.getMonth();
        for (Budget budget : context.getBudgetRepository().findAll()) {
            String categoryName = context.getCategoryRepository()
                    .findById(budget.getCategoryId())
                    .map(Category::getName)
                    .orElse("Unknown");
            double spent = context.totalForCategoryAndMonth(budget.getCategoryId(), month);
            double limit = budget.getMonthlyLimit();
            double utilization = PercentageUtil.of(spent, limit);

            if (spent > limit) {
                double over = spent - limit;
                insights.add(new Insight(
                        InsightType.BUDGET, InsightSeverity.WARNING,
                        String.format("%s spending exceeded the budget by %s (%.0f%% utilized).",
                                categoryName, CurrencyUtil.format(over), utilization),
                        categoryName, over));
            } else if (limit > 0 && utilization >= 80) {
                insights.add(new Insight(
                        InsightType.BUDGET, InsightSeverity.INFO,
                        String.format("%s budget is %.0f%% utilized — %s remaining.",
                                categoryName, utilization, CurrencyUtil.format(limit - spent)),
                        categoryName, utilization));
            }
        }
    }

    private void addConsecutiveTrendInsights(List<Insight> insights, TrendAnalysisResult trendResult) {
        trendResult.getConsecutiveIncreases().forEach((category, months) -> {
            if (months >= 3) {
                insights.add(new Insight(
                        InsightType.TREND, InsightSeverity.WARNING,
                        String.format("%s expenses increased for %d consecutive months — upward trend detected.",
                                category, months),
                        category, months));
            } else if (months == 2) {
                insights.add(new Insight(
                        InsightType.TREND, InsightSeverity.INFO,
                        String.format("%s expenses show an upward trend for 2 consecutive months.",
                                category),
                        category, months));
            }
        });
    }

    private void addIncomeInsights(List<Insight> insights, AnalysisContext context,
                                   ExpenseAnalysisResult result) {
        double income = context.getMonthlyIncome();
        if (income <= 0) {
            return;
        }
        for (CategorySpending cs : result.getCategorySpending()) {
            if (cs.getCurrentMonth() <= 0) {
                continue;
            }
            double pctOfIncome = PercentageUtil.of(cs.getCurrentMonth(), income);
            if (pctOfIncome >= 30) {
                insights.add(new Insight(
                        InsightType.CATEGORY, InsightSeverity.WARNING,
                        String.format("You spent %.0f%% of your income on %s this month.",
                                pctOfIncome, cs.getCategoryName()),
                        cs.getCategoryName(), pctOfIncome));
            }
        }
    }

    private void addSavingsInsights(List<Insight> insights, AnalysisContext context,
                                    ExpenseAnalysisResult result) {
        double totalSavings = 0;

        Category food = findCategoryByName(context, "Food");
        if (food != null) {
            double foodSpent = context.totalForCategoryAndMonth(food.getId(), context.getMonth());
            double deliverySpent = estimateDeliverySpending(context, food.getId());
            if (deliverySpent > 0) {
                double saveAmount = deliverySpent * 0.20;
                totalSavings += saveAmount;
                insights.add(new Insight(
                        InsightType.SAVINGS, InsightSeverity.INFO,
                        String.format("Reducing food delivery expenses by 20%% could save %s monthly.",
                                CurrencyUtil.format(saveAmount)),
                        "Food", saveAmount));
            } else if (foodSpent > 0) {
                double saveAmount = foodSpent * 0.15;
                totalSavings += saveAmount;
                insights.add(new Insight(
                        InsightType.SAVINGS, InsightSeverity.INFO,
                        String.format("Cutting %s expenses by 15%% could save %s per month.",
                                food.getName(), CurrencyUtil.format(saveAmount)),
                        food.getName(), saveAmount));
            }
        }

        if (totalSavings > 0) {
            insights.add(new Insight(
                    InsightType.SAVINGS, InsightSeverity.SUCCESS,
                    String.format("Potential monthly savings opportunity: %s.",
                            CurrencyUtil.format(totalSavings)),
                    null, totalSavings));
        }
    }

    private double estimateDeliverySpending(AnalysisContext context, String foodCategoryId) {
        YearMonth month = context.getMonth();
        return context.getExpenseRepository().findByCategoryAndMonth(foodCategoryId, month).stream()
                .filter(this::isDeliveryExpense)
                .mapToDouble(Expense::getAmount)
                .sum();
    }

    private boolean isDeliveryExpense(Expense expense) {
        String desc = expense.getDescription().toLowerCase();
        return desc.contains("delivery") || desc.contains("swiggy")
                || desc.contains("zomato") || desc.contains("uber eats")
                || desc.contains("food delivery");
    }

    @Override
    public Void analyze(AnalysisContext context) {
        throw new UnsupportedOperationException("Use generate() instead");
    }
}
