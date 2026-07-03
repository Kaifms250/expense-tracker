package com.expensetracker.utils;

import java.text.NumberFormat;
import java.util.Locale;

public final class CurrencyUtil {
    private static final NumberFormat FORMATTER = NumberFormat.getCurrencyInstance(new Locale("en", "IN"));

    private CurrencyUtil() {
    }

    public static String format(double amount) {
        return FORMATTER.format(amount);
    }
}
