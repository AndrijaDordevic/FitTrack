package com.myapp.fitnessapp.models;

public class MealLogEntry {
    private int id;
    private String userEmail;
    private String date;
    private String mealType;
    private String food;
    private int calories;
    private float protein;
    private float carbs;
    private float fat;

    public MealLogEntry(int id,
                        String userEmail,
                        String date,
                        String mealType,
                        String food,
                        int calories,
                        float protein,
                        float carbs,
                        float fat) {
        this.id = id;
        this.userEmail = userEmail;
        this.date = date;
        this.mealType = mealType;
        this.food = food;
        this.calories = calories;
        this.protein = protein;
        this.carbs = carbs;
        this.fat = fat;
    }

    public int getId() {
        return id;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public String getDate() {
        return date;
    }

    public String getMealType() {
        return mealType;
    }

    public String getFood() {
        return food;
    }

    public int getCalories() {
        return calories;
    }

    public float getProtein() {
        return protein;
    }

    public float getCarbs() {
        return carbs;
    }

    public float getFat() {
        return fat;
    }
}
