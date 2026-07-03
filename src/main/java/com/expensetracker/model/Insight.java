package com.expensetracker.model;

public class Insight {
    private InsightType type;
    private InsightSeverity severity;
    private String message;
    private String categoryName;
    private double value;

    public Insight() {
    }

    public Insight(InsightType type, InsightSeverity severity, String message) {
        this.type = type;
        this.severity = severity;
        this.message = message;
    }

    public Insight(InsightType type, InsightSeverity severity, String message,
                   String categoryName, double value) {
        this(type, severity, message);
        this.categoryName = categoryName;
        this.value = value;
    }

    public InsightType getType() {
        return type;
    }

    public InsightSeverity getSeverity() {
        return severity;
    }

    public String getMessage() {
        return message;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public double getValue() {
        return value;
    }
}
