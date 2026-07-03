package com.expensetracker.model;

import java.util.List;

/**
 * Data Transfer Object for aggregating goal statistics across all goals.
 * Used by the dashboard to display a summary of all savings goals.
 */
public class GoalSummary {
    private int activeGoalsCount;
    private double totalTargetAmount;
    private double totalSavedAmount;
    private double overallProgressPercent;

    /**
     * Default constructor.
     *
     * @param activeGoalsCount       number of active (non-achieved) goals
     * @param totalTargetAmount      sum of target amounts for all active goals
     * @param totalSavedAmount       sum of current savings for all active goals
     * @param overallProgressPercent overall progress percentage (0-100)
     */
    public GoalSummary(int activeGoalsCount, double totalTargetAmount,
                       double totalSavedAmount, double overallProgressPercent) {
        this.activeGoalsCount = activeGoalsCount;
        this.totalTargetAmount = totalTargetAmount;
        this.totalSavedAmount = totalSavedAmount;
        this.overallProgressPercent = overallProgressPercent;
    }

    public int getActiveGoalsCount() {
        return activeGoalsCount;
    }

    public void setActiveGoalsCount(int activeGoalsCount) {
        this.activeGoalsCount = activeGoalsCount;
    }

    public double getTotalTargetAmount() {
        return totalTargetAmount;
    }

    public void setTotalTargetAmount(double totalTargetAmount) {
        this.totalTargetAmount = totalTargetAmount;
    }

    public double getTotalSavedAmount() {
        return totalSavedAmount;
    }

    public void setTotalSavedAmount(double totalSavedAmount) {
        this.totalSavedAmount = totalSavedAmount;
    }

    public double getOverallProgressPercent() {
        return overallProgressPercent;
    }

    public void setOverallProgressPercent(double overallProgressPercent) {
        this.overallProgressPercent = overallProgressPercent;
    }

    /**
     * Static factory method to create a GoalSummary from a list of goals.
     * Only considers active (non-achieved) goals.
     *
     * @param goals the list of goals to aggregate
     * @return GoalSummary with calculated aggregate statistics
     */
    public static GoalSummary fromGoals(List<Goal> goals) {
        int activeGoalsCount = 0;
        double totalTargetAmount = 0.0;
        double totalSavedAmount = 0.0;

        for (Goal goal : goals) {
            // Only count active goals (not achieved)
            if (goal.getStatus() != Goal.GoalStatus.ACHIEVED) {
                activeGoalsCount++;
                totalTargetAmount += goal.getTargetAmount();
                totalSavedAmount += goal.getCurrentSavings();
            }
        }

        double overallProgressPercent = 0.0;
        if (totalTargetAmount > 0) {
            overallProgressPercent = (totalSavedAmount / totalTargetAmount) * 100.0;
        }

        return new GoalSummary(activeGoalsCount, totalTargetAmount,
                totalSavedAmount, overallProgressPercent);
    }

    @Override
    public String toString() {
        return "GoalSummary{" +
                "activeGoalsCount=" + activeGoalsCount +
                ", totalTargetAmount=" + totalTargetAmount +
                ", totalSavedAmount=" + totalSavedAmount +
                ", overallProgressPercent=" + overallProgressPercent +
                '}';
    }
}