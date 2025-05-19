package com.myapp.fitnessapp.models;

/**
 * Model class representing a single meal log entry.
 */
public class MealLogEntry {
    // Unique identifier in the database
    private int id;
    // Email of the user who logged this meal
    private String userEmail;
    // Date of the entry in yyyy-MM-dd format
    private String date;
    // Type of meal (e.g., breakfast, lunch)
    private String mealType;
    // Description or name of the food eaten
    private String food;
    // Nutritional values: calories, protein (g), carbs (g), fat (g)
    private int calories;
    private float protein;
    private float carbs;
    private float fat;

    /**
     * Constructor to create a meal log entry.
     * @param id         Database ID of the entry.
     * @param userEmail  User's email associated with this entry.
     * @param date       Date string, e.g., "2025-05-19".
     * @param mealType   Meal category, like "Breakfast".
     * @param food       Food description, e.g., "Oatmeal".
     * @param calories   Total calories consumed.
     * @param protein    Protein grams.
     * @param carbs      Carbohydrate grams.
     * @param fat        Fat grams.
     */
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

    /** @return unique entry ID */
    public int getId() {
        return id;
    }

    /** @return user's email for this entry */
    public String getUserEmail() {
        return userEmail;
    }

    /** @return date of the meal log */
    public String getDate() {
        return date;
    }

    /** @return meal type/category */
    public String getMealType() {
        return mealType;
    }

    /** @return food description */
    public String getFood() {
        return food;
    }

    /** @return calorie count */
    public int getCalories() {
        return calories;
    }

    /** @return protein in grams */
    public float getProtein() {
        return protein;
    }

    /** @return carbohydrates in grams */
    public float getCarbs() {
        return carbs;
    }

    /** @return fat in grams */
    public float getFat() {
        return fat;
    }
}
