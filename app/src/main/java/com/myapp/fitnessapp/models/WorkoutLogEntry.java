package com.myapp.fitnessapp.models;

/**
 * Model class representing a workout log entry for a specific day.
 */
public class WorkoutLogEntry {
    // Day of week for this log (e.g., "Monday")
    private String dayName;
    // Number of sets completed
    private int    sets;
    // Number of reps per set
    private int    reps;
    // Weight used during the exercise
    private float  weight;
    // Timestamp when this log was last updated (e.g., ISO string)
    private String updatedAt;

    /**
     * Constructor to create a workout log entry.
     * @param dayName    Name of the day (Monday..Sunday).
     * @param sets       Number of sets performed.
     * @param reps       Number of reps per set.
     * @param weight     Weight used for the exercise.
     * @param updatedAt  Timestamp of last update.
     */
    public WorkoutLogEntry(
            String dayName,
            int    sets,
            int    reps,
            float  weight,
            String updatedAt
    ) {
        this.dayName   = dayName;
        this.sets      = sets;
        this.reps      = reps;
        this.weight    = weight;
        this.updatedAt = updatedAt;
    }

    // Getters and setters for each field

    /** @return the day of week for this log */
    public String getDayName()    { return dayName; }
    /** @param dayName the day of week to set */
    public void   setDayName(String dayName)     { this.dayName = dayName; }

    /** @return number of sets performed */
    public int    getSets()       { return sets; }
    /** @param sets number of sets to set */
    public void   setSets(int sets)               { this.sets = sets; }

    /** @return number of reps per set */
    public int    getReps()       { return reps; }
    /** @param reps number of reps to set */
    public void   setReps(int reps)               { this.reps = reps; }

    /** @return weight used */
    public float  getWeight()     { return weight; }
    /** @param weight weight value to set */
    public void   setWeight(float weight)         { this.weight = weight; }

    /** @return ISO timestamp of last update */
    public String getUpdatedAt()  { return updatedAt; }
    /** @param updatedAt timestamp to set */
    public void   setUpdatedAt(String updatedAt)  { this.updatedAt = updatedAt; }
}
