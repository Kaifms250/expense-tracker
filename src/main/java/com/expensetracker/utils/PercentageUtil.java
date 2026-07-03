package com.expensetracker.utils;

public final class PercentageUtil {
    private PercentageUtil() {
    }

    public static double of(double part, double total) {
        if (total <= 0) {
            return 0;
        }
        return (part / total) * 100;
    }

    public static double change(double current, double previous) {
        if (previous <= 0) {
            return current > 0 ? 100 : 0;
        }
        return ((current - previous) / previous) * 100;
    }
}
