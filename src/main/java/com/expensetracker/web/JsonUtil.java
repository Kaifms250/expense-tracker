package com.expensetracker.web;

import com.expensetracker.storage.LocalDateAdapter;
import com.expensetracker.storage.YearMonthAdapter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public final class JsonUtil {
    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .registerTypeAdapter(java.time.LocalDate.class, new LocalDateAdapter())
            .registerTypeAdapter(java.time.YearMonth.class, new YearMonthAdapter())
            .create();

    private JsonUtil() {
    }

    public static Gson gson() {
        return GSON;
    }
}
