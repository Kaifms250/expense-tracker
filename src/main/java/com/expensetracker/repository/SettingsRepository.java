package com.expensetracker.repository;

import com.expensetracker.model.UserSettings;
import com.expensetracker.web.JsonUtil;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class SettingsRepository {
    private final Path filePath;
    private UserSettings settings;

    public SettingsRepository(Path dataDir) {
        this.filePath = dataDir.resolve("settings.json");
        this.settings = loadSettings();
    }

    private UserSettings loadSettings() {
        if (!Files.exists(filePath)) {
            return new UserSettings();
        }
        try {
            String json = Files.readString(filePath);
            if (json.isBlank()) {
                return new UserSettings();
            }
            UserSettings loaded = JsonUtil.gson().fromJson(json, UserSettings.class);
            return loaded != null ? loaded : new UserSettings();
        } catch (IOException e) {
            throw new RuntimeException("Failed to load settings from " + filePath, e);
        }
    }

    public UserSettings getSettings() {
        return settings;
    }

    public UserSettings save(UserSettings newSettings) {
        this.settings = newSettings;
        try {
            Files.createDirectories(filePath.getParent());
            Files.writeString(filePath, JsonUtil.gson().toJson(settings));
        } catch (IOException e) {
            throw new RuntimeException("Failed to save settings to " + filePath, e);
        }
        return settings;
    }
}
