package com.expensetracker.service;

import com.expensetracker.model.Goal;
import com.expensetracker.model.GoalSummary;
import com.expensetracker.repository.GoalRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Service layer for managing savings goals.
 * Coordinates repository, calculator, and progress tracking operations.
 */
public class GoalService {
    private final GoalRepository goalRepository;
    private final GoalCalculator goalCalculator;
    private final GoalProgressTracker progressTracker;
    private final GoalRecommendationService recommendationService;

    /**
     * Creates a new GoalService with the specified repository and calculator.
     *
     * @param goalRepository the repository for goal persistence
     * @param goalCalculator the calculator for goal metrics
     */
    public GoalService(GoalRepository goalRepository, GoalCalculator goalCalculator) {
        this(goalRepository, goalCalculator,
             new GoalProgressTracker(goalCalculator),
             new GoalRecommendationService(
                 new GoalProgressTracker(goalCalculator),
                 goalCalculator));
    }

    /**
     * Creates a new GoalService with all dependencies.
     *
     * @param goalRepository        the repository for goal persistence
     * @param goalCalculator        the calculator for goal metrics
     * @param progressTracker       the tracker for goal status
     * @param recommendationService the service for generating recommendations
     */
    public GoalService(GoalRepository goalRepository, GoalCalculator goalCalculator,
                       GoalProgressTracker progressTracker, GoalRecommendationService recommendationService) {
        this.goalRepository = goalRepository;
        this.goalCalculator = goalCalculator;
        this.progressTracker = progressTracker;
        this.recommendationService = recommendationService;
    }

    /**
     * Creates a new savings goal with the specified parameters.
     *
     * @param name                 the goal name (1-100 characters, non-empty)
     * @param targetAmount         the target amount to save (> 0)
     * @param currentSavings       the current savings amount (0 to targetAmount)
     * @param targetDate           the target date (must be in the future)
     * @param targetDurationMonths the duration in months (1-600, optional)
     * @return the created goal with calculated fields
     * @throws IllegalArgumentException if validation fails
     */
    public Goal createGoal(String name, double targetAmount, double currentSavings,
                           LocalDate targetDate, Integer targetDurationMonths) {
        // Validate inputs
        validateGoalInputs(name, targetAmount, currentSavings, targetDate, targetDurationMonths);

        // Create goal
        Goal goal;
        if (targetDate != null) {
            goal = new Goal(name, targetAmount, targetDate);
        } else if (targetDurationMonths != null) {
            goal = new Goal(name, targetAmount, targetDurationMonths);
        } else {
            goal = new Goal(name, targetAmount, currentSavings, targetDate, targetDurationMonths);
        }

        // Set current savings if provided and different from default
        if (currentSavings > 0) {
            goal.setCurrentSavings(currentSavings);
        }

        // Validate goal
        goal.validate();

        // Calculate all derived fields and update status
        updateCalculatedFields(goal);

        // Save and return
        return goalRepository.save(goal);
    }

    /**
     * Creates a goal with minimal parameters.
     *
     * @param name         the goal name
     * @param targetAmount the target amount
     * @param targetDate   the target date
     * @return the created goal
     */
    public Goal createGoal(String name, double targetAmount, LocalDate targetDate) {
        return createGoal(name, targetAmount, 0.0, targetDate, null);
    }

    /**
     * Creates a goal with duration instead of specific date.
     *
     * @param name               the goal name
     * @param targetAmount       the target amount
     * @param currentSavings     the current savings
     * @param targetDurationMonths the duration in months
     * @return the created goal
     */
    public Goal createGoalWithDuration(String name, double targetAmount,
                                       double currentSavings, int targetDurationMonths) {
        return createGoal(name, targetAmount, currentSavings, null, targetDurationMonths);
    }

    /**
     * Retrieves all goals with calculated fields.
     *
     * @return list of all goals sorted by creation date (newest first)
     */
    public List<Goal> getAllGoals() {
        List<Goal> goals = goalRepository.findAll();
        for (Goal goal : goals) {
            goalCalculator.calculateAll(goal);
        }
        return goals;
    }

    /**
     * Retrieves a specific goal by ID with calculated fields.
     *
     * @param id the goal ID
     * @return Optional containing the goal if found
     */
    public Optional<Goal> getGoalById(String id) {
        Optional<Goal> goal = goalRepository.findById(id);
        goal.ifPresent(goalCalculator::calculateAll);
        return goal;
    }

    /**
     * Adds savings to an existing goal.
     *
     * @param goalId  the ID of the goal
     * @param amount  the amount to add (must be > 0)
     * @return the updated goal
     * @throws IllegalArgumentException if goal not found or amount invalid
     */
    public Goal addSavings(String goalId, double amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero.");
        }

        Goal goal = goalRepository.findById(goalId)
                .orElseThrow(() -> new IllegalArgumentException("Goal not found: " + goalId));

        double newSavings = goal.getCurrentSavings() + amount;
        if (newSavings > goal.getTargetAmount()) {
            newSavings = goal.getTargetAmount(); // Cap at target
        }

        goal.setCurrentSavings(newSavings);

        // Recalculate all derived fields and update status
        updateCalculatedFields(goal);

        return goalRepository.update(goal);
    }

    /**
     * Updates an existing goal.
     *
     * @param goal the goal with updated values
     * @return the updated goal
     * @throws IllegalArgumentException if goal validation fails
     */
    public Goal updateGoal(Goal goal) {
        // Validate the goal
        goal.validate();

        // Recalculate all derived fields and update status
        updateCalculatedFields(goal);

        // Save and return
        return goalRepository.update(goal);
    }

    /**
     * Updates the target amount for a goal.
     *
     * @param goalId        the ID of the goal
     * @param targetAmount  the new target amount (must be > current savings)
     * @return the updated goal
     */
    public Goal updateTargetAmount(String goalId, double targetAmount) {
        Goal goal = goalRepository.findById(goalId)
                .orElseThrow(() -> new IllegalArgumentException("Goal not found: " + goalId));

        if (targetAmount <= 0) {
            throw new IllegalArgumentException("Target amount must be greater than zero.");
        }

        if (targetAmount <= goal.getCurrentSavings()) {
            throw new IllegalArgumentException("Target amount must exceed current savings.");
        }

        goal.setTargetAmount(targetAmount);
        return updateGoal(goal);
    }

    /**
     * Updates the current savings for a goal.
     *
     * @param goalId        the ID of the goal
     * @param currentSavings the new current savings amount
     * @return the updated goal
     */
    public Goal updateCurrentSavings(String goalId, double currentSavings) {
        Goal goal = goalRepository.findById(goalId)
                .orElseThrow(() -> new IllegalArgumentException("Goal not found: " + goalId));

        if (currentSavings < 0) {
            throw new IllegalArgumentException("Current savings cannot be negative.");
        }

        if (currentSavings > goal.getTargetAmount()) {
            throw new IllegalArgumentException("Current savings cannot exceed target amount.");
        }

        goal.setCurrentSavings(currentSavings);
        return updateGoal(goal);
    }

    /**
     * Updates the target date for a goal.
     *
     * @param goalId    the ID of the goal
     * @param targetDate the new target date (must be in the future)
     * @return the updated goal
     */
    public Goal updateTargetDate(String goalId, LocalDate targetDate) {
        Goal goal = goalRepository.findById(goalId)
                .orElseThrow(() -> new IllegalArgumentException("Goal not found: " + goalId));

        if (targetDate == null) {
            throw new IllegalArgumentException("Target date is required.");
        }

        if (targetDate.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Target date must be in the future.");
        }

        goal.setTargetDate(targetDate);
        goal.setTargetDurationMonths(null);
        return updateGoal(goal);
    }

    /**
     * Deletes a goal by its ID.
     *
     * @param id the ID of the goal to delete
     * @return true if deleted, false if not found
     */
    public boolean deleteGoal(String id) {
        return goalRepository.delete(id);
    }

    /**
     * Gets aggregate statistics for all goals.
     *
     * @return GoalSummary with aggregate statistics
     */
    public GoalSummary getGoalSummary() {
        List<Goal> goals = goalRepository.findAll();
        return GoalSummary.fromGoals(goals);
    }

    /**
     * Gets all goals with a specific status.
     *
     * @param status the status to filter by
     * @return list of goals with the specified status
     */
    public List<Goal> getGoalsByStatus(Goal.GoalStatus status) {
        List<Goal> goals = goalRepository.findByStatus(status);
        for (Goal goal : goals) {
            goalCalculator.calculateAll(goal);
        }
        return goals;
    }

    /**
     * Updates all calculated fields for a goal and determines its status.
     * This method should be called after any goal creation or modification.
     *
     * @param goal the goal to update
     */
    public void updateCalculatedFields(Goal goal) {
        if (goal == null) {
            throw new IllegalArgumentException("Goal cannot be null");
        }

        // Calculate all derived fields using the calculator
        goalCalculator.calculateAll(goal);

        // Determine and set the goal status
        if (progressTracker != null) {
            progressTracker.updateGoalStatus(goal);
        }
    }

    /**
     * Gets a recommendation message for a specific goal.
     *
     * @param goalId the ID of the goal
     * @return the recommendation message, or empty string if goal not found
     */
    public String getRecommendation(String goalId) {
        if (goalId == null || goalId.isBlank()) {
            return "";
        }

        return goalRepository.findById(goalId)
                .map(goal -> {
                    updateCalculatedFields(goal);
                    return recommendationService.getRecommendation(goal);
                })
                .orElse("");
    }

    /**
     * Validates goal creation inputs.
     *
     * @throws IllegalArgumentException if validation fails
     */
    private void validateGoalInputs(String name, double targetAmount, double currentSavings,
                                    LocalDate targetDate, Integer targetDurationMonths) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Goal name is required and cannot be empty.");
        }

        if (name.trim().length() > 100) {
            throw new IllegalArgumentException("Goal name must be 100 characters or less.");
        }

        if (targetAmount <= 0) {
            throw new IllegalArgumentException("Target amount must be greater than zero.");
        }

        if (currentSavings < 0) {
            throw new IllegalArgumentException("Current savings cannot be negative.");
        }

        if (currentSavings > targetAmount) {
            throw new IllegalArgumentException("Current savings cannot exceed target amount.");
        }

        if (targetDate == null && targetDurationMonths == null) {
            throw new IllegalArgumentException("Either target date or duration is required.");
        }

        if (targetDate != null && targetDate.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Target date must be in the future.");
        }

        if (targetDurationMonths != null && targetDurationMonths <= 0) {
            throw new IllegalArgumentException("Duration must be greater than zero months.");
        }

        if (targetDurationMonths != null && targetDurationMonths > 600) {
            throw new IllegalArgumentException("Duration cannot exceed 600 months (50 years).");
        }
    }
}