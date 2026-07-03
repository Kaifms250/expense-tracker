package com.expensetracker.model;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Goal entity representing a savings goal with target amount, current savings,
 * and deadline for tracking financial objectives.
 */
public class Goal {
    /**
     * Enum representing the possible statuses of a goal.
     */
    public enum GoalStatus {
        ON_TRACK("On Track"),
        BEHIND_SCHEDULE("Behind Schedule"),
        ACHIEVED("Achieved");

        private final String displayName;

        GoalStatus(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    // Core fields
    private String id;
    private String name;
    private double targetAmount;
    private double currentSavings;
    private LocalDate targetDate;
    private Integer targetDurationMonths;
    private String icon;
    private GoalStatus status;

    // Metadata
    private LocalDate createdAt;

    // Calculated fields (transient, not persisted)
    private double progressPercent;
    private double remainingAmount;
    private double requiredMonthlySavings;
    private double dailySavingsTarget;
    private int daysRemaining;
    private LocalDate estimatedCompletionDate;

    // Icon mappings for goal name auto-detection
    private static final Map<String, String> ICON_MAPPINGS = new HashMap<>();

    static {
        ICON_MAPPINGS.put("laptop", "💻");
        ICON_MAPPINGS.put("computer", "🖥️");
        ICON_MAPPINGS.put("trip", "✈️");
        ICON_MAPPINGS.put("travel", "✈️");
        ICON_MAPPINGS.put("vacation", "🌴");
        ICON_MAPPINGS.put("emergency fund", "🛡️");
        ICON_MAPPINGS.put("emergency", "🛡️");
        ICON_MAPPINGS.put("car", "🚗");
        ICON_MAPPINGS.put("vehicle", "🚗");
        ICON_MAPPINGS.put("house", "🏠");
        ICON_MAPPINGS.put("home", "🏠");
        ICON_MAPPINGS.put("wedding", "💒");
        ICON_MAPPINGS.put("education", "📚");
        ICON_MAPPINGS.put("school", "📚");
        ICON_MAPPINGS.put("college", "🎓");
        ICON_MAPPINGS.put("retirement", "🏖️");
        ICON_MAPPINGS.put("phone", "📱");
        ICON_MAPPINGS.put("bike", "🚲");
        ICON_MAPPINGS.put("motorcycle", "🏍️");
    }

    /**
     * Default constructor - creates a new goal with a unique ID.
     */
    public Goal() {
        this.id = UUID.randomUUID().toString();
        this.createdAt = LocalDate.now();
        this.currentSavings = 0.0;
        this.status = GoalStatus.ON_TRACK;
    }

    /**
     * Parameterized constructor with required fields.
     *
     * @param name         the goal name
     * @param targetAmount the target amount to save
     * @param targetDate   the target date to achieve the goal
     */
    public Goal(String name, double targetAmount, LocalDate targetDate) {
        this();
        this.name = name;
        this.targetAmount = targetAmount;
        this.targetDate = targetDate;
        this.icon = generateIcon(name);
    }

    /**
     * Parameterized constructor with required fields using duration.
     *
     * @param name               the goal name
     * @param targetAmount       the target amount to save
     * @param targetDurationMonths the number of months to achieve the goal
     */
    public Goal(String name, double targetAmount, int targetDurationMonths) {
        this();
        this.name = name;
        this.targetAmount = targetAmount;
        this.targetDurationMonths = targetDurationMonths;
        this.targetDate = this.createdAt.plusMonths(targetDurationMonths);
        this.icon = generateIcon(name);
    }

    /**
     * Parameterized constructor with all optional fields.
     *
     * @param name                 the goal name
     * @param targetAmount         the target amount to save
     * @param currentSavings       the current savings amount
     * @param targetDate           the target date to achieve the goal
     * @param targetDurationMonths the number of months to achieve the goal
     */
    public Goal(String name, double targetAmount, double currentSavings,
                LocalDate targetDate, Integer targetDurationMonths) {
        this();
        this.name = name;
        this.targetAmount = targetAmount;
        this.currentSavings = currentSavings;
        this.targetDate = targetDate;
        this.targetDurationMonths = targetDurationMonths;
        if (targetDate == null && targetDurationMonths != null) {
            this.targetDate = this.createdAt.plusMonths(targetDurationMonths);
        }
        this.icon = generateIcon(name);
    }

    // Core field getters and setters

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getTargetAmount() {
        return targetAmount;
    }

    public void setTargetAmount(double targetAmount) {
        this.targetAmount = targetAmount;
    }

    public double getCurrentSavings() {
        return currentSavings;
    }

    public void setCurrentSavings(double currentSavings) {
        this.currentSavings = currentSavings;
    }

    public LocalDate getTargetDate() {
        return targetDate;
    }

    public void setTargetDate(LocalDate targetDate) {
        this.targetDate = targetDate;
    }

    public Integer getTargetDurationMonths() {
        return targetDurationMonths;
    }

    public void setTargetDurationMonths(Integer targetDurationMonths) {
        this.targetDurationMonths = targetDurationMonths;
    }

    public String getIcon() {
        return icon != null ? icon : generateIcon(name);
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public GoalStatus getStatus() {
        return status;
    }

    public void setStatus(GoalStatus status) {
        this.status = status;
    }

    public LocalDate getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDate createdAt) {
        this.createdAt = createdAt;
    }

    // Calculated field getters and setters

    public double getProgressPercent() {
        return progressPercent;
    }

    public void setProgressPercent(double progressPercent) {
        this.progressPercent = progressPercent;
    }

    public double getRemainingAmount() {
        return remainingAmount;
    }

    public void setRemainingAmount(double remainingAmount) {
        this.remainingAmount = remainingAmount;
    }

    public double getRequiredMonthlySavings() {
        return requiredMonthlySavings;
    }

    public void setRequiredMonthlySavings(double requiredMonthlySavings) {
        this.requiredMonthlySavings = requiredMonthlySavings;
    }

    public double getDailySavingsTarget() {
        return dailySavingsTarget;
    }

    public void setDailySavingsTarget(double dailySavingsTarget) {
        this.dailySavingsTarget = dailySavingsTarget;
    }

    public int getDaysRemaining() {
        return daysRemaining;
    }

    public void setDaysRemaining(int daysRemaining) {
        this.daysRemaining = daysRemaining;
    }

    public LocalDate getEstimatedCompletionDate() {
        return estimatedCompletionDate;
    }

    public void setEstimatedCompletionDate(LocalDate estimatedCompletionDate) {
        this.estimatedCompletionDate = estimatedCompletionDate;
    }

    /**
     * Generates an icon based on the goal name using predefined mappings.
     *
     * @param name the goal name
     * @return the corresponding emoji icon, or default money bag if no match
     */
    public String generateIcon(String name) {
        if (name == null || name.isBlank()) {
            return "💰";
        }

        String lowerName = name.toLowerCase();
        for (Map.Entry<String, String> entry : ICON_MAPPINGS.entrySet()) {
            if (lowerName.contains(entry.getKey())) {
                return entry.getValue();
            }
        }
        return "💰";
    }

    /**
     * Validates that the goal has required fields populated.
     *
     * @throws IllegalArgumentException if validation fails
     */
    public void validate() {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Goal name is required.");
        }
        if (targetAmount <= 0) {
            throw new IllegalArgumentException("Target amount must be greater than zero.");
        }
        if (targetDate == null && targetDurationMonths == null) {
            throw new IllegalArgumentException("Either target date or duration is required.");
        }
        if (currentSavings > targetAmount) {
            throw new IllegalArgumentException("Current savings cannot exceed target amount.");
        }
    }

    @Override
    public String toString() {
        return "Goal{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", targetAmount=" + targetAmount +
                ", currentSavings=" + currentSavings +
                ", targetDate=" + targetDate +
                ", status=" + status +
                '}';
    }
}