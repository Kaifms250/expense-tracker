package com.expensetracker.service;

import com.expensetracker.model.Goal;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Utility class for performing all goal-related calculations.
 * Handles progress percentages, required savings rates, and completion estimates.
 */
public class GoalCalculator {
    private static final double DAYS_IN_MONTH = 30.0;
    private static final double EPSILON = 0.001;

    /**
     * Calculates the progress percentage toward the goal.
     *
     * @param goal the goal to calculate progress for
     * @return percentage from 0 to 100 (capped at 100)
     */
    public double calculateProgressPercent(Goal goal) {
        if (goal.getTargetAmount() <= 0) {
            return 0.0;
        }

        double percent = (goal.getCurrentSavings() / goal.getTargetAmount()) * 100.0;
        return Math.min(percent, 100.0);
    }

    /**
     * Calculates the remaining amount to save.
     *
     * @param goal the goal to calculate remaining amount for
     * @return remaining amount (can be negative if over-saved)
     */
    public double calculateRemainingAmount(Goal goal) {
        return goal.getTargetAmount() - goal.getCurrentSavings();
    }

    /**
     * Calculates the required monthly savings to reach the goal on time.
     *
     * @param goal the goal to calculate required monthly savings for
     * @return required monthly savings amount
     */
    public double calculateRequiredMonthlySavings(Goal goal) {
        double remaining = calculateRemainingAmount(goal);
        if (remaining <= 0) {
            return 0.0;
        }

        LocalDate targetDate = goal.getTargetDate();
        if (targetDate == null) {
            return remaining; // No deadline, need to save all at once
        }

        long monthsRemaining = calculateMonthsRemaining(targetDate);
        if (monthsRemaining <= 0) {
            return remaining; // Already at deadline, need all remaining
        }

        return remaining / monthsRemaining;
    }

    /**
     * Calculates the daily savings target based on required monthly savings.
     *
     * @param goal the goal to calculate daily savings target for
     * @return daily savings amount
     */
    public double calculateDailySavingsTarget(Goal goal) {
        double monthlyRequired = calculateRequiredMonthlySavings(goal);
        return monthlyRequired / DAYS_IN_MONTH;
    }

    /**
     * Calculates the number of days remaining until the target date.
     *
     * @param goal the goal to calculate days remaining for
     * @return number of days remaining (0 if target date has passed)
     */
    public int calculateDaysRemaining(Goal goal) {
        LocalDate targetDate = goal.getTargetDate();
        if (targetDate == null) {
            // If no target date, use duration if available
            if (goal.getTargetDurationMonths() != null && goal.getCreatedAt() != null) {
                LocalDate calculatedTarget = goal.getCreatedAt()
                        .plusMonths(goal.getTargetDurationMonths());
                long days = ChronoUnit.DAYS.between(LocalDate.now(), calculatedTarget);
                return (int) Math.max(0, days);
            }
            return 0;
        }

        long days = ChronoUnit.DAYS.between(LocalDate.now(), targetDate);
        return (int) Math.max(0, days);
    }

    /**
     * Calculates the estimated completion date based on current progress and savings rate.
     *
     * @param goal the goal to calculate estimated completion for
     * @return estimated completion date (or target date if on track)
     */
    public LocalDate calculateEstimatedCompletionDate(Goal goal) {
        LocalDate targetDate = goal.getTargetDate();
        if (targetDate == null) {
            return null;
        }

        // If already achieved or on track, return target date
        if (goal.getCurrentSavings() >= goal.getTargetAmount()) {
            return targetDate;
        }

        // If past target date, return target date
        if (LocalDate.now().isAfter(targetDate) || LocalDate.now().isEqual(targetDate)) {
            return targetDate;
        }

        double dailyTarget = calculateDailySavingsTarget(goal);
        if (dailyTarget <= EPSILON) {
            return targetDate;
        }

        double remaining = calculateRemainingAmount(goal);
        if (remaining <= 0) {
            return targetDate;
        }

        long additionalDays = (long) Math.ceil(remaining / dailyTarget);
        LocalDate estimatedDate = LocalDate.now().plusDays(additionalDays);

        // Return whichever is earlier: estimated or target
        return estimatedDate.isBefore(targetDate) ? estimatedDate : targetDate;
    }

    /**
     * Calculates all derived fields for a goal and sets them.
     *
     * @param goal the goal to update with calculated values
     */
    public void calculateAll(Goal goal) {
        goal.setProgressPercent(calculateProgressPercent(goal));
        goal.setRemainingAmount(calculateRemainingAmount(goal));
        goal.setRequiredMonthlySavings(calculateRequiredMonthlySavings(goal));
        goal.setDailySavingsTarget(calculateDailySavingsTarget(goal));
        goal.setDaysRemaining(calculateDaysRemaining(goal));
        goal.setEstimatedCompletionDate(calculateEstimatedCompletionDate(goal));
    }

    /**
     * Calculates the number of full months remaining until the target date.
     *
     * @param targetDate the target date
     * @return number of full months remaining, minimum 1
     */
    private long calculateMonthsRemaining(LocalDate targetDate) {
        LocalDate today = LocalDate.now();
        long totalDays = ChronoUnit.DAYS.between(today, targetDate);
        return Math.max(1, totalDays / 30);
    }

    /**
     * Validates that a goal has valid calculation parameters.
     *
     * @param goal the goal to validate
     * @throws IllegalArgumentException if validation fails
     */
    public void validateForCalculation(Goal goal) {
        if (goal.getTargetAmount() <= 0) {
            throw new IllegalArgumentException("Target amount must be greater than zero for calculations.");
        }
        if (goal.getTargetDate() == null && goal.getTargetDurationMonths() == null) {
            throw new IllegalArgumentException("Target date or duration is required for calculations.");
        }
    }
}