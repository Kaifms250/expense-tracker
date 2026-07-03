package com.expensetracker.service;

import com.expensetracker.model.Goal;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Service for generating intelligent recommendations for savings goals.
 * Provides personalized messages based on goal status and progress.
 */
public class GoalRecommendationService {

    private static final double EPSILON = 0.001;
    private static final double AHEAD_OF_SCHEDULE_PROGRESS_THRESHOLD = 50.0; // Less than 50%
    private static final double AHEAD_OF_SCHEDULE_TIME_THRESHOLD = 25.0; // Less than 25% elapsed

    private final GoalProgressTracker progressTracker;
    private final GoalCalculator goalCalculator;

    /**
     * Creates a new GoalRecommendationService with the specified tracker and calculator.
     *
     * @param progressTracker the progress tracker for status determination
     * @param goalCalculator  the calculator for computing metrics
     */
    public GoalRecommendationService(GoalProgressTracker progressTracker, GoalCalculator goalCalculator) {
        this.progressTracker = progressTracker;
        this.goalCalculator = goalCalculator;
    }

    /**
     * Gets a recommendation message for the specified goal.
     *
     * Priority order:
     * 1. Achieved (highest priority)
     * 2. Behind Schedule
     * 3. Ahead of Schedule
     * 4. On Track (lowest priority)
     *
     * Only generates recommendations when remainingAmount > 0.
     *
     * @param goal the goal to get a recommendation for
     * @return the recommendation message, or empty string if no recommendation applicable
     */
    public String getRecommendation(Goal goal) {
        if (goal == null) {
            return "";
        }

        // Don't generate recommendations if no remaining amount
        if (goal.getRemainingAmount() <= EPSILON) {
            return getAchievedMessage(goal);
        }

        // Determine ahead of schedule first (special case)
        if (isAheadOfSchedule(goal)) {
            return getAheadOfScheduleMessage(goal);
        }

        // Check status-based priorities
        Goal.GoalStatus status = progressTracker.determineStatus(goal);

        switch (status) {
            case ACHIEVED:
                return getAchievedMessage(goal);
            case BEHIND_SCHEDULE:
                return getBehindScheduleMessage(goal);
            case ON_TRACK:
            default:
                return getOnTrackMessage(goal);
        }
    }

    /**
     * Generates a message for goals that are on track.
     * Format: "Save ₹X/month to achieve your goal on time"
     *
     * @param goal the goal to generate message for
     * @return the on-track message
     */
    public String getOnTrackMessage(Goal goal) {
        if (goal == null) {
            return "";
        }

        double requiredMonthly = goal.getRequiredMonthlySavings();

        // Only show message if there's something to save
        if (requiredMonthly <= EPSILON) {
            return getAchievedMessage(goal);
        }

        return String.format("Save ₹%.2f/month to achieve your goal on time", requiredMonthly);
    }

    /**
     * Generates a message for goals that are behind schedule.
     * Format: "Increase your monthly savings by ₹X to stay on track"
     *
     * @param goal the goal to generate message for
     * @return the behind schedule message
     */
    public String getBehindScheduleMessage(Goal goal) {
        if (goal == null) {
            return "";
        }

        // Calculate how much more needs to be saved monthly
        double requiredMonthly = goal.getRequiredMonthlySavings();
        double expectedMonthly = progressTracker.calculateExpectedMonthlySavings(goal);

        // Amount that needs to be added to current pace to get back on track
        double additionalNeeded = requiredMonthly - expectedMonthly;

        // If already on track, don't show behind schedule message
        if (additionalNeeded <= EPSILON) {
            return getOnTrackMessage(goal);
        }

        // Only show message if additional needed is significant
        if (additionalNeeded < 0.01) {
            return getOnTrackMessage(goal);
        }

        return String.format("Increase your monthly savings by ₹%.2f to stay on track", additionalNeeded);
    }

    /**
     * Generates a congratulatory message for achieved goals.
     * Format: "Congratulations! You've achieved your [name] goal"
     *
     * @param goal the achieved goal
     * @return the achievement message
     */
    public String getAchievedMessage(Goal goal) {
        if (goal == null) {
            return "";
        }

        String goalName = goal.getName();
        if (goalName == null || goalName.isBlank()) {
            goalName = "Savings";
        }

        return String.format("Congratulations! You've achieved your %s goal", goalName);
    }

    /**
     * Generates a message for goals that are ahead of schedule.
     * This applies when progress is less than 50% complete but less than 25% of time has elapsed.
     * Format: "You are ahead of schedule. Consider increasing your target amount or reducing monthly savings."
     *
     * @param goal the ahead-of-schedule goal
     * @return the ahead of schedule message
     */
    public String getAheadOfScheduleMessage(Goal goal) {
        if (goal == null) {
            return "";
        }

        return "You are ahead of schedule. Consider increasing your target amount or reducing monthly savings.";
    }

    /**
     * Checks if a goal is ahead of schedule.
     * A goal is ahead of schedule when BOTH conditions are met:
     * 1. Percentage Complete < 50%
     * 2. Percentage Time Elapsed < 25%
     *
     * @param goal the goal to check
     * @return true if ahead of schedule, false otherwise
     */
    public boolean isAheadOfSchedule(Goal goal) {
        if (goal == null) {
            return false;
        }

        // If goal is achieved, it's not "ahead of schedule"
        if (goal.getRemainingAmount() <= EPSILON) {
            return false;
        }

        double progressPercent = goal.getProgressPercent();

        // Condition 1: Progress < 50%
        if (progressPercent >= AHEAD_OF_SCHEDULE_PROGRESS_THRESHOLD) {
            return false;
        }

        // Condition 2: Time Elapsed < 25%
        double timeElapsedPercent = calculateTimeElapsedPercent(goal);
        if (timeElapsedPercent >= AHEAD_OF_SCHEDULE_TIME_THRESHOLD) {
            return false;
        }

        return true;
    }

    /**
     * Calculates the percentage of time that has elapsed since goal creation.
     *
     * @param goal the goal to calculate time elapsed for
     * @return percentage of time elapsed (0-100)
     */
    public double calculateTimeElapsedPercent(Goal goal) {
        if (goal == null) {
            return 100.0; // All time elapsed if goal is null
        }

        LocalDate createdAt = goal.getCreatedAt();
        LocalDate targetDate = goal.getTargetDate();

        if (createdAt == null || targetDate == null) {
            return 100.0;
        }

        LocalDate now = LocalDate.now();

        // If target date is in the past, 100% time has elapsed
        if (now.isAfter(targetDate) || now.isEqual(targetDate)) {
            return 100.0;
        }

        // If created date is after target date (invalid), return 100%
        if (createdAt.isAfter(targetDate)) {
            return 100.0;
        }

        long totalDays = ChronoUnit.DAYS.between(createdAt, targetDate);
        long elapsedDays = ChronoUnit.DAYS.between(createdAt, now);

        if (totalDays <= 0) {
            return 100.0;
        }

        return (elapsedDays * 100.0) / totalDays;
    }

    /**
     * Gets the minimum monthly savings recommendation.
     * Returns the required monthly savings if on track,
     * or the adjusted amount needed to get back on track.
     *
     * @param goal the goal to get recommendation for
     * @return recommended monthly savings amount
     */
    public double getRecommendedMonthlySavings(Goal goal) {
        if (goal == null) {
            return 0.0;
        }

        double requiredMonthly = goal.getRequiredMonthlySavings();
        double expectedMonthly = progressTracker.calculateExpectedMonthlySavings(goal);

        // If behind schedule, recommend the additional amount
        if (expectedMonthly < requiredMonthly * GoalProgressTracker.ON_TRACK_THRESHOLD) {
            return requiredMonthly;
        }

        // If on track, recommend what's required
        return requiredMonthly;
    }
}