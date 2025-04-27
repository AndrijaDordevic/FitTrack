package com.myapp.fitnessapp.models;

public class WorkoutLogEntry {
    private String dayName;
    private int    sets;
    private int    reps;
    private float  weight;
    private String updatedAt;   // if you want to display the date it was logged

    public WorkoutLogEntry(
            String dayName,
            int    sets,
            int    reps,
            float  weight
            // optional, only if you need it
    ) {
        this.dayName   = dayName;
        this.sets      = sets;
        this.reps      = reps;
        this.weight    = weight;
        this.updatedAt = updatedAt;
    }

    // --- getters & setters ---

    public String getDayName()    { return dayName; }
    public void   setDayName(String dayName)     { this.dayName = dayName; }

    public int    getSets()       { return sets; }
    public void   setSets(int sets)               { this.sets = sets; }

    public int    getReps()       { return reps; }
    public void   setReps(int reps)               { this.reps = reps; }

    public float  getWeight()     { return weight; }
    public void   setWeight(float weight)         { this.weight = weight; }

    public String getUpdatedAt()  { return updatedAt; }
    public void   setUpdatedAt(String updatedAt)  { this.updatedAt = updatedAt; }
}
