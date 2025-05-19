package com.myapp.fitnessapp.models;

import androidx.annotation.NonNull;

public class ExerciseItem {
    // Unique identifier for the exercise
    private final int id;
    // Display name of the exercise
    private final String name;
    // Category (e.g., "Legs", "Back") for filtering
    private final String category;

    // NEW: Fields to track workout details per day
    private int sets = 0;
    private int reps = 0;
    private float weight = 0.0f;

    /**
     * Constructor to create an ExerciseItem.
     * @param id       Unique ID from database (or -1 for Rest).
     * @param name     Display name of the exercise.
     * @param category Category label for grouping/filtering.
     */
    public ExerciseItem(int id, @NonNull String name, @NonNull String category) {
        this.id = id;
        this.name = name;
        this.category = category;
    }

    /** @return unique exercise ID */
    public int getId() {
        return id;
    }

    /** @return exercise display name */
    @NonNull
    public String getName() {
        return name;
    }

    /** @return exercise category */
    @NonNull
    public String getCategory() {
        return category;
    }

    /** @return number of sets user has entered */
    public int getSets() {
        return sets;
    }
    /** @param sets number of sets to record */
    public void setSets(int sets) {
        this.sets = sets;
    }

    /** @return number of reps per set */
    public int getReps() {
        return reps;
    }
    /** @param reps number of reps to record */
    public void setReps(int reps) {
        this.reps = reps;
    }

    /** @return weight used in exercise */
    public float getWeight() {
        return weight;
    }
    /** @param weight weight value to record */
    public void setWeight(float weight) {
        this.weight = weight;
    }

    /**
     * toString returns the exercise name, useful for lists.
     */
    @Override
    @NonNull
    public String toString() {
        return name;
    }
}
