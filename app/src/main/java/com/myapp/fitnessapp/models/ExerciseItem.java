package com.myapp.fitnessapp.models;

import androidx.annotation.NonNull;

public class ExerciseItem {
    private final int id;
    private final String name;
    private final String category;

    // NEW FIELDS
    private int sets = 0;
    private int reps = 0;
    private float weight = 0.0f;

    public ExerciseItem(int id, @NonNull String name, @NonNull String category) {
        this.id = id;
        this.name = name;
        this.category = category;
    }

    public int getId() {
        return id;
    }

    @NonNull
    public String getName() {
        return name;
    }

    @NonNull
    public String getCategory() {
        return category;
    }

    public int getSets() { return sets; }
    public void setSets(int sets) { this.sets = sets; }

    public int getReps() { return reps; }
    public void setReps(int reps) { this.reps = reps; }

    public float getWeight() { return weight; }
    public void setWeight(float weight) { this.weight = weight; }

    @Override
    @NonNull
    public String toString() {
        return name;
    }
}
