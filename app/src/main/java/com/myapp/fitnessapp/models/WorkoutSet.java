package com.myapp.fitnessapp.models;

/**
 * Model representing an individual workout set performed by a user.
 */
public class WorkoutSet {
    // Unique ID for this set (primary key in DB)
    private int    id;
    // Foreign key to the exercise this set belongs to
    private int    exerciseId;
    // Email of the user who performed this set
    private String userEmail;
    // The sequential number of this set (1-based)
    private int    setNumber;
    // Number of repetitions completed in this set
    private int    reps;
    // Weight used in this set (e.g., in kilograms or pounds)
    private float  weight;

    /**
     * Constructor to create a WorkoutSet entry.
     * @param id           Unique ID from database.
     * @param exerciseId   ID of the exercise performed.
     * @param userEmail    Email of the user performing the set.
     * @param setNumber    Order of the set within the workout.
     * @param reps         Number of repetitions completed.
     * @param weight       Weight used for this set.
     */
    public WorkoutSet(int id,
                      int exerciseId,
                      String userEmail,
                      int setNumber,
                      int reps,
                      float weight) {
        this.id          = id;
        this.exerciseId  = exerciseId;
        this.userEmail   = userEmail;
        this.setNumber   = setNumber;
        this.reps        = reps;
        this.weight      = weight;
    }

    // Getters and setters for each field

    /** @return unique set ID */
    public int getId() { return id; }
    /** @param id unique set ID to set */
    public void setId(int id) { this.id = id; }

    /** @return associated exercise ID */
    public int getExerciseId() { return exerciseId; }
    /** @param exerciseId associated exercise ID to set */
    public void setExerciseId(int exerciseId) { this.exerciseId = exerciseId; }

    /** @return email of the user who performed this set */
    public String getUserEmail() { return userEmail; }
    /** @param userEmail email of the user to set */
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }

    /** @return set sequence number within workout */
    public int getSetNumber() { return setNumber; }
    /** @param setNumber sequence number to set */
    public void setSetNumber(int setNumber) { this.setNumber = setNumber; }

    /** @return number of reps completed */
    public int getReps() { return reps; }
    /** @param reps number of reps to set */
    public void setReps(int reps) { this.reps = reps; }

    /** @return weight used in this set */
    public float getWeight() { return weight; }
    /** @param weight weight value to set */
    public void setWeight(float weight) { this.weight = weight; }
}
