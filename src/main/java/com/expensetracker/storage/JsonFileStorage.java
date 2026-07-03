package com.expensetracker.storage;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class JsonFileStorage<T> {
    private final Path filePath;
    private final Gson gson;
    private final Type listType;

    public JsonFileStorage(Path filePath, Type listType) {
        this.filePath = filePath;
        this.listType = listType;
        this.gson = new GsonBuilder()
                .setPrettyPrinting()
                .registerTypeAdapter(java.time.LocalDate.class, new LocalDateAdapter())
                .create();
    }

    public List<T> loadAll() {
        if (!Files.exists(filePath)) {
            return new ArrayList<>();
        }
        try {
            String json = Files.readString(filePath);
            if (json.isBlank()) {
                return new ArrayList<>();
            }
            List<T> items = gson.fromJson(json, listType);
            return items != null ? items : new ArrayList<>();
        } catch (IOException e) {
            throw new RuntimeException("Failed to load data from " + filePath, e);
        }
    }

    public void saveAll(List<T> items) {
        try {
            Files.createDirectories(filePath.getParent());
            Files.writeString(filePath, gson.toJson(items));
        } catch (IOException e) {
            throw new RuntimeException("Failed to save data to " + filePath, e);
        }
    }

    public static <T> Type listTypeOf(Class<T> clazz) {
        return TypeToken.getParameterized(List.class, clazz).getType();
    }
}
