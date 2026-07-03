package com.expensetracker.analytics;

/**
 * Abstraction for all analyzers in the analytics engine.
 * Polymorphism: each analyzer implements analyze() differently.
 */
public interface Analyzer<T> {
    T analyze(AnalysisContext context);
}
