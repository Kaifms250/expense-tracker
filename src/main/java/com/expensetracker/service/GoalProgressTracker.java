package com.expensetracker.service;

import com.expensetracker.model.Goal;

/**
 * Service for tracking and determining the status of savings goals.
 * Evaluates goal progress against required savings pace to determine
 * if a goal is on track, behind schedule, or achieved.
 */
public class GoalProgressTracker {

    private static final double ON_TRACK_THRESHOLD = 0.90; // 90% of required pace
    private static final double EPSILON = 0.001; // For floating-point comparison

    private final GoalCalculator goalCalculator;

    /**
     * Creates a new GoalProgressTracker with the specified calculator.
     *
     * @param goalCalculator the calculator for computing goal metrics
     */
    public GoalProgressTracker(GoalCalculator goalCalculator) {
        this.goalCalculator = goalCalculator;
    }

    /**
     * Determines the current status of a goal based on progress and savings pace.
     *
     * Priority:
     * 1. ACHIEVED - if progress >= 100%
     * 2. BEHIND_SCHEDULE - if Expected Monthly Savings < Required Monthly Savings * 0.9
     * 3. ON_TRACK - otherwise (Expected >= Required * 0.9)
     *
     * @param goal the goal to evaluate
     * @return the determined status (ON_TRACK, BEHIND_SCHEDULE, or ACHIEVED)
     */
    public Goal.GoalStatus determineStatus(Goal goal) {
        if (goal == null) {
            throw new IllegalArgumentException("Goal cannot be null");
        }

        // Check for achieved first
        if (isAchieved(goal)) {
            return Goal.GoalStatus.ACHIEVED;
        }

        // Determine on track vs behind schedule
        if (isOnTrack(goal)) {
            return Goal.GoalStatus.ON_TRACK;
        } else {
            return Goal.GoalStatus.BEHIND_SCHEDULE;
        }
    }

    /**
     * Checks if a goal is on track.
     * A goal is on track when Expected Monthly Savings >= Required Monthly Savings * 0.9.
     *
     * Expected Monthly Savings = (Target - Current) / Remaining months
     *
     * @param goal the goal to check
     * @return true if on track, false otherwise
     */
    public boolean isOnTrack(Goal goal) {
        if (goal == null) {
            return false;
        }

        double expectedMonthlySavings = calculateExpectedMonthlySavings(goal);
        double requiredMonthlySavings = goal.getRequiredMonthlySavings();

        // Handle edge case where required is zero (already achieved or invalid)
        if (Math.abs(requiredMonthlySavings) < EPSILON) {
            return true; // No required savings means we're on track
        }

        return expectedMonthlySavings >= requiredMonthlySavings * ON_TRACK_THRESHOLD;
    }

    /**
     * Checks if a goal is behind schedule.
     * A goal is behind schedule when Expected Monthly Savings < Required Monthly Savings * 0.9.
     *
     * @param goal the goal to check
     * @return true if behind schedule, false otherwise
     */
    public boolean isBehindSchedule(Goal goal) {
        if (goal == null) {
            return false;
        }

        // Behind schedule means not achieved and not on track
        if (isAchieved(goal)) {
            return false;
        }

        return !isOnTrack(goal);
    }

    /**
     * Checks if a goal has been achieved.
     * A goal is achieved when progress >= 100%.
     *
     * @param goal the goal to check
     * @return true if achieved, false otherwise
     */
    public boolean isAchieved(Goal goal) {
        if (goal == null) {
            return false;
        }

        return goal.getProgressPercent() >= 100.0 - EPSILON;
    }

    /**
     * Calculates the progress rate as a percentage of required pace.
     * A value of 1.0 (100%) means exactly on pace.
     * Values above 1.0 mean ahead of schedule.
     * Values below 1.0 mean behind schedule.
     *
     * @param goal the goal to calculate progress rate for
     * @return progress rate as a multiplier of required pace
     */
    public double calculateProgressRate(Goal goal) {
        if (goal == null) {
            return 0.0;
        }

        double expectedMonthlySavings = calculateExpectedMonthlySavings(goal);
        double requiredMonthlySavings = goal.getRequiredMonthlySavings();

        if (Math.abs(requiredMonthlySavings) < EPSILON) {
            // If no required savings (achieved or invalid), return 1.0
            return 1.0;
        }

        return expectedMonthlySavings / requiredMonthlySavings;
    }

    /**
     * Calculates the expected monthly savings based on current progress and remaining time.
     * This represents the savings rate needed to meet the goal on time given current progress.
     *
     * Expected Monthly Savings = (Target - Current) / Remaining months
     *
     * @param goal the goal to calculate expected savings for
     * @return expected monthly savings amount
     */
    public double calculateExpectedMonthlySavings(Goal goal) {
        if (goal == null) {
            return 0.0;
        }

        double remainingAmount = goal.getRemainingAmount();
        int daysRemaining = goal.getDaysRemaining();

        // If no remaining amount, no savings needed
        if (remainingAmount <= 0) {
            return 0.0;
        }

        // If no time remaining, can't calculate
        if (daysRemaining <= 0) {
            return remainingAmount; // Need all remaining at once
        }

        // Convert days to months (using 30 days per month)
        double monthsRemaining = daysRemaining / 30.0;

        if (monthsRemaining < 1.0) {
            // Less than a month remaining
            return remainingAmount;
        }

        return remainingAmount / monthsRemaining;
    }

    /**
     * Updates the status of a goal based on current progress.
     *
     * @param goal the goal to update
     */
    public void updateGoalStatus(Goal goal) {
        if (goal == null) {
            throw new IllegalArgumentException("Goal cannot be null");
        }

        goal.setStatus(determineStatus(goal));
    }
}