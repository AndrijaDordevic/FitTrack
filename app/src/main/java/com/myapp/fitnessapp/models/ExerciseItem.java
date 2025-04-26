package com.myapp.fitnessapp.models;

import androidx.annotation.NonNull;

/** Simple POJO to hold an exercise entry for the Spinner/RecyclerView */
public class ExerciseItem {
    private final int id;
    private final String name;
    private final String category;

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

    @Override
    @NonNull
    public String toString() {
        // Used by ArrayAdapter or Spinner to display the label
        return name;
    }
}
