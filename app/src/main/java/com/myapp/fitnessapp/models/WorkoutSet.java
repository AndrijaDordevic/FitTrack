package com.myapp.fitnessapp.models;

public class WorkoutSet {
    private int    id;
    private int    exerciseId;
    private String userEmail;   // new!
    private int    setNumber;
    private int    reps;
    private float  weight;

    public WorkoutSet(int id,
                      int exerciseId,
                      String userEmail,   // new param
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

    // --- getters & setters ---

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getExerciseId() { return exerciseId; }
    public void setExerciseId(int exerciseId) { this.exerciseId = exerciseId; }

    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }

    public int getSetNumber() { return setNumber; }
    public void setSetNumber(int setNumber) { this.setNumber = setNumber; }

    public int getReps() { return reps; }
    public void setReps(int reps) { this.reps = reps; }

    public float getWeight() { return weight; }
    public void setWeight(float weight) { this.weight = weight; }
}
