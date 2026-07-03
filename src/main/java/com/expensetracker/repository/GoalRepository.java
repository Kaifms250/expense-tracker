package com.expensetracker.repository;

import com.expensetracker.model.Goal;
import com.expensetracker.storage.JsonFileStorage;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Repository for persisting and retrieving Goal entities from JSON storage.
 * Uses the existing JsonFileStorage infrastructure for goals.json.
 */
public class GoalRepository {
    private static final Logger logger = Logger.getLogger(GoalRepository.class.getName());

    private final JsonFileStorage<Goal> storage;
    private List<Goal> goals;

    /**
     * Creates a new GoalRepository that manages goals.json in the specified data directory.
     *
     * @param dataDir the directory where goals.json will be stored
     */
    public GoalRepository(Path dataDir) {
        this.storage = new JsonFileStorage<>(
                dataDir.resolve("goals.json"),
                JsonFileStorage.listTypeOf(Goal.class));
        this.goals = loadGoals();
    }

    /**
     * Loads all goals from storage, handling file not found and corruption.
     *
     * @return list of goals, or empty list if loading fails
     */
    private List<Goal> loadGoals() {
        try {
            List<Goal> loadedGoals = storage.loadAll();
            return new ArrayList<>(loadedGoals);
        } catch (Exception e) {
            logger.log(Level.WARNING, "Failed to load goals from storage, starting with empty list", e);
            return new ArrayList<>();
        }
    }

    /**
     * Retrieves all goals, sorted by creation date (newest first).
     *
     * @return list of all goals sorted by createdAt descending
     */
    public List<Goal> findAll() {
        return goals.stream()
                .sorted(Comparator.comparing(Goal::getCreatedAt).reversed())
                .collect(Collectors.toList());
    }

    /**
     * Finds a goal by its unique identifier.
     *
     * @param id the goal ID to search for
     * @return Optional containing the goal if found, empty otherwise
     */
    public Optional<Goal> findById(String id) {
        return goals.stream()
                .filter(g -> g.getId().equals(id))
                .findFirst();
    }

    /**
     * Saves a new goal to the repository.
     *
     * @param goal the goal to save
     * @return the saved goal
     */
    public Goal save(Goal goal) {
        goals.add(goal);
        persistGoals();
        return goal;
    }

    /**
     * Updates an existing goal in the repository.
     *
     * @param goal the goal with updated values
     * @return the updated goal
     * @throws IllegalArgumentException if goal does not exist
     */
    public Goal update(Goal goal) {
        Optional<Goal> existingGoal = findById(goal.getId());
        if (existingGoal.isEmpty()) {
            throw new IllegalArgumentException("Goal not found: " + goal.getId());
        }

        int index = goals.indexOf(existingGoal.get());
        goals.set(index, goal);
        persistGoals();
        return goal;
    }

    /**
     * Deletes a goal by its ID.
     *
     * @param id the ID of the goal to delete
     * @return true if a goal was deleted, false if no goal with that ID existed
     */
    public boolean delete(String id) {
        boolean removed = goals.removeIf(g -> g.getId().equals(id));
        if (removed) {
            persistGoals();
        }
        return removed;
    }

    /**
     * Finds all goals with a specific status.
     *
     * @param status the status to filter by
     * @return list of goals with the specified status
     */
    public List<Goal> findByStatus(Goal.GoalStatus status) {
        return goals.stream()
                .filter(g -> g.getStatus() == status)
                .sorted(Comparator.comparing(Goal::getCreatedAt).reversed())
                .collect(Collectors.toList());
    }

    /**
     * Persists all goals to the JSON file.
     * Logs an error if saving fails but does not throw.
     */
    private void persistGoals() {
        try {
            storage.saveAll(goals);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to save goals to storage", e);
        }
    }

    /**
     * Returns the total number of goals in the repository.
     *
     * @return the count of all goals
     */
    public int count() {
        return goals.size();
    }
}