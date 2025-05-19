package com.myapp.fitnessapp.database;

import android.content.Context;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase;
import android.content.ContentValues;
import android.database.Cursor;
import com.myapp.fitnessapp.models.ExerciseItem;
import com.myapp.fitnessapp.models.MealLogEntry;
import com.myapp.fitnessapp.models.WorkoutLogEntry;
import com.myapp.fitnessapp.models.WorkoutSet;
import androidx.annotation.NonNull;
import android.util.Log;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Database helper class for the Fitness App.
 * Manages creation, upgrading, and common CRUD operations on the SQLite database.
 */
public class DBHelper extends SQLiteOpenHelper {
    private static final String TAG = "DBHelper";
    private static final String DATABASE_NAME = "FitnessApp.db";
    private static final int DATABASE_VERSION = 26;

    // ----- Table and column definitions -----
    // Users table
    private static final String TABLE_USERS       = "users";
    private static final String COLUMN_ID         = "id";
    private static final String COLUMN_EMAIL      = "email";
    private static final String COLUMN_USERNAME   = "username";
    private static final String COLUMN_PASSWORD   = "password";
    private static final String COLUMN_FULL_NAME  = "full_name";
    private static final String COLUMN_AGE        = "age";
    private static final String COLUMN_IMAGE_URI  = "image_uri";
    private static final String COLUMN_HEIGHT_CM  = "height_cm";
    private static final String COLUMN_WEIGHT_KG  = "weight_kg";

    // Exercises table
    private static final String TABLE_EXERCISES    = "exercises";
    private static final String COLUMN_EX_ID       = "id";
    private static final String COLUMN_EX_NAME     = "name";
    private static final String COLUMN_EX_CATEGORY = "category";
    private static final String COLUMN_EX_TIPS     = "tips";

    // Day plans linking users to exercises per day
    private static final String TABLE_DAY_PLAN    = "day_plans";
    private static final String COL_DAY_NAME      = "day_name";
    private static final String COL_EX_ID         = "exercise_id";
    private static final String COL_USER_EMAIL    = "user_email";

    /**
     * Constructor
     * @param context Application context
     */
    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * Enable foreign key support when configuring the database.
     */
    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }

    /**
     * Create tables and initial data when the database is first created.
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create users table
        db.execSQL("CREATE TABLE IF NOT EXISTS users (" +
                "id            INTEGER PRIMARY KEY AUTOINCREMENT," +
                "username      TEXT," +
                "email         TEXT UNIQUE NOT NULL," +
                "password      TEXT," +
                "full_name     TEXT," +
                "age           INTEGER," +
                "image_uri     TEXT," +
                "height_cm     REAL," +
                "weight_kg     REAL" +
                ")");

        // Create exercises table
        db.execSQL(
                "CREATE TABLE IF NOT EXISTS " + TABLE_EXERCISES + " (" +
                        COLUMN_EX_ID       + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        COLUMN_EX_NAME     + " TEXT NOT NULL, " +
                        COLUMN_EX_CATEGORY + " TEXT NOT NULL, " +
                        COLUMN_EX_TIPS     + " TEXT" +
                        ")"
        );

        // Insert starter exercises data
        db.execSQL(
                "INSERT INTO exercises (name, category, tips) VALUES " +
                        // Biceps
                        "('Bicep Curl',           'Bicep',   'Keep elbows tucked and use slow controlled motion')," +
                        "('Hammer Curl',          'Bicep',   'Use a neutral grip to target the brachialis muscle')," +
                        "('Preacher Curl',        'Bicep',   'Rest arms fully on the pad for isolation')," +
                        "('Concentration Curl',   'Bicep',   'Keep your elbow still and squeeze at the top')," +
                        "('Incline Dumbbell Curl','Bicep',   'Lean back to stretch the bicep through full range')," +
                        "('Zottman Curl',         'Bicep',   'Rotate wrists at the top for dual muscle activation')," +
                        //Triceps
                        "('Tricep Dips',          'Tricep',  'Keep your body close to the bench and elbows tight')," +
                        "('Overhead Extension',   'Tricep',  'Use both hands and keep elbows pointing forward')," +
                        "('Close-Grip Bench',     'Tricep',  'Keep elbows close to sides and grip narrower than shoulders')," +
                        "('Tricep Pushdown',      'Tricep',  'Use full range and avoid leaning too far forward')," +
                        "('Skull Crusher',        'Tricep',  'Lower the bar to your forehead and keep elbows fixed')," +
                        "('Diamond Push-Up',      'Tricep',  'Form a diamond with hands and keep elbows tight')," +
                        //Legs
                        "('Squat',                'Legs',    'Keep knees behind toes and chest upright')," +
                        "('Lunge',                'Legs',    'Step far enough forward to form 90° angles')," +
                        "('Leg Press',            'Legs',    'Do not lock knees and keep feet flat')," +
                        "('Bulgarian Split Squat','Legs',    'Balance with rear foot elevated and go deep')," +
                        "('Deadlift',             'Legs',    'Keep back straight and lift with legs and hips')," +
                        "('Leg Extension',        'Legs',    'Use controlled movement, avoid locking knees')," +
                        "('Leg Curl',             'Legs',    'Keep hips down and squeeze at the top')," +
                        "('Calf Raise',           'Legs',    'Use full range and pause at the top')," +
                        //Shoulders
                        "('Shoulder Press',       'Shoulders','Keep core tight and avoid arching your back')," +
                        "('Lateral Raise',        'Shoulders','Lift to shoulder height, lead with elbows')," +
                        "('Front Raise',          'Shoulders','Raise weights just above parallel, control the descent')," +
                        "('Arnold Press',         'Shoulders','Rotate palms during the lift for full shoulder activation')," +
                        "('Reverse Fly',          'Shoulders','Bend forward slightly and squeeze shoulder blades')," +
                        "('Upright Row',          'Shoulders','Keep hands close and elbows higher than wrists')," +
                        //Chest
                        "('Bench Press',          'Chest',   'Lower bar to mid-chest, maintain shoulder stability')," +
                        "('Push-Up',              'Chest',   'Keep your body in a straight line and lower to 90° elbows')," +
                        "('Incline Press',        'Chest',   'Set bench at 30-45°, target upper chest')," +
                        "('Dumbbell Fly',         'Chest',   'Slight bend in elbows, stretch but don’t overextend')," +
                        "('Chest Dip',            'Chest',   'Lean forward slightly to engage chest more')," +
                        "('Cable Crossover',      'Chest',   'Bring hands together with slight bend in elbows')," +
                        //Abs
                        "('Plank',                'Abs',     'Keep your back flat and core tight throughout')," +
                        "('Crunch',               'Abs',     'Lift with your abs, not your neck or momentum')," +
                        "('Russian Twist',        'Abs',     'Twist through the torso, keep feet up for more challenge')," +
                        "('Leg Raise',            'Abs',     'Keep legs straight and avoid arching your back')," +
                        "('Bicycle Crunch',       'Abs',     'Twist fully and touch opposite elbow to knee')," +
                        "('Mountain Climber',     'Abs',     'Keep core tight and move quickly with control')," +
                        "('Hanging Knee Raise',   'Abs',     'Lift knees slowly and don’t swing')," +
                        //Back
                        "('Pull-Up',              'Back',    'Start from full hang, lead with chest and pull to chin')," +
                        "('Bent-Over Row',        'Back',    'Keep back flat and row towards your waist')," +
                        "('Lat Pulldown',         'Back',    'Pull bar to chest level and squeeze lats')," +
                        "('Seated Cable Row',     'Back',    'Sit upright and pull elbows straight back')," +
                        "('T-Bar Row',            'Back',    'Keep torso at 45° and drive through the elbows')," +
                        "('Single-Arm Row',       'Back',    'Support yourself and row in a controlled motion')," +
                        "('Hyperextension',       'Back',    'Avoid overextending, lift to a neutral spine');"

        );

        // Create mapping table for user-specific exercises
        db.execSQL(
                "CREATE TABLE IF NOT EXISTS user_exercises (" +
                        "  user_email   TEXT    NOT NULL," +
                        "  exercise_id  INTEGER NOT NULL," +
                        "  name         TEXT    NOT NULL," +
                        "  category     TEXT    NOT NULL," +
                        "  tips         TEXT," +
                        "  PRIMARY KEY(user_email,exercise_id)," +
                        "  FOREIGN KEY(exercise_id) REFERENCES exercises(id) ON DELETE CASCADE," +
                        "  FOREIGN KEY(user_email)   REFERENCES users(email) ON DELETE CASCADE" +
                        ")"
        );

        // Create day plans table
        db.execSQL(
                "CREATE TABLE IF NOT EXISTS " + TABLE_DAY_PLAN + " (" +
                        COL_USER_EMAIL + " TEXT    NOT NULL, " +
                        COL_DAY_NAME   + " TEXT    NOT NULL, " +
                        COL_EX_ID      + " INTEGER NOT NULL, " +
                        "name           TEXT, " +
                        "PRIMARY KEY(" + COL_USER_EMAIL + ","+ COL_DAY_NAME + ","+ COL_EX_ID + "), " +
                        "FOREIGN KEY(" + COL_EX_ID + ")     REFERENCES exercises(id)    ON DELETE CASCADE, " +
                        "FOREIGN KEY(" + COL_USER_EMAIL + ") REFERENCES users(email) ON DELETE CASCADE" +
                        ")"
        );

        // Create workout logs table
        db.execSQL(
                "CREATE TABLE IF NOT EXISTS workout_logs (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "user_email TEXT NOT NULL, " +
                        "exercise_id INTEGER NOT NULL, " +
                        "day_name TEXT NOT NULL, " +
                        "sets INTEGER, " +
                        "reps INTEGER, " +
                        "weight REAL, " +
                        "updated_at TEXT, " +
                        "FOREIGN KEY(exercise_id) REFERENCES exercises(id) ON DELETE CASCADE, " +
                        "FOREIGN KEY(user_email) REFERENCES users(email) ON DELETE CASCADE" +
                        ")"
        );

        // Create workout sets table
        db.execSQL(
                "CREATE TABLE IF NOT EXISTS workout_sets (" +
                        "id           INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "exercise_id  INTEGER NOT NULL, " +
                        "user_email   TEXT    NOT NULL, " +
                        "set_number   INTEGER NOT NULL, " +
                        "reps         INTEGER, " +
                        "weight       REAL, " +
                        "day_name     TEXT, " +
                        "FOREIGN KEY(exercise_id) REFERENCES exercises(id) ON DELETE CASCADE, " +
                        "FOREIGN KEY(user_email)    REFERENCES users(email) ON DELETE CASCADE" +
                        ")"
        );

        // Trigger: after inserting a new workout set, add to workout_logs
        db.execSQL(
                "CREATE TRIGGER IF NOT EXISTS trg_workout_sets_after_insert " +
                        "AFTER INSERT ON workout_sets " +
                        "BEGIN " +
                        "  INSERT INTO workout_logs(user_email,exercise_id,day_name,sets,reps,weight,updated_at) " +
                        "  VALUES (NEW.user_email,NEW.exercise_id,NEW.day_name,1,NEW.reps,NEW.weight,date('now')); " +
                        "END;"
        );

        // Trigger: after updating a workout set, add to workout_logs
        db.execSQL(
                "CREATE TRIGGER IF NOT EXISTS trg_workout_sets_after_update " +
                        "AFTER UPDATE ON workout_sets " +
                        "BEGIN " +
                        "  INSERT INTO workout_logs(user_email,exercise_id,day_name,sets,reps,weight,updated_at) " +
                        "  VALUES (NEW.user_email,NEW.exercise_id,NEW.day_name,1,NEW.reps,NEW.weight,date('now')); " +
                        "END;"
        );

        // Create nutrition logs table
        db.execSQL("CREATE TABLE IF NOT EXISTS nutrition_logs (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "user_email TEXT NOT NULL, " +
                "date TEXT NOT NULL, " +
                "meal_type TEXT NOT NULL, " +
                "food TEXT NOT NULL, " +
                "calories INTEGER, " +
                "protein REAL, " +
                "carbs REAL, " +
                "fat REAL, " +
                "FOREIGN KEY(user_email) REFERENCES users(email) ON DELETE CASCADE" +
                ")");
    }

    /**
     * Handle database upgrades by dropping and recreating all tables.
     * @param db SQLiteDatabase instance
     * @param oldVersion previous DB version
     * @param newVersion new DB version
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Disable foreign keys to avoid issues during drop
        db.execSQL("PRAGMA foreign_keys=OFF");

        // Drop all tables in dependency order
        db.execSQL("DROP TABLE IF EXISTS day_plans");
        db.execSQL("DROP TABLE IF EXISTS workout_sets");
        db.execSQL("DROP TABLE IF EXISTS workout_logs");
        db.execSQL("DROP TABLE IF EXISTS nutrition_logs");
        db.execSQL("DROP TABLE IF EXISTS user_exercises");
        db.execSQL("DROP TABLE IF EXISTS exercises");
        db.execSQL("DROP TABLE IF EXISTS users");

        // Recreate schema
        onCreate(db);

        // Re-enable foreign keys
        db.execSQL("PRAGMA foreign_keys=ON");
    }

    /**
     * Insert a new user into the database.
     * @return true if insertion was successful
     */
    public boolean addUser(String email, String username, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_EMAIL, email);
        values.put(COLUMN_USERNAME, username);
        values.put(COLUMN_PASSWORD, password);
        long result = db.insert(TABLE_USERS, null, values);
        db.close();
        return result != -1;
    }

    /**
     * Verify user credentials for login.
     * @return true if a matching user exists
     */
    public boolean checkUser(String email, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] columns = { COLUMN_ID };
        String selection = COLUMN_EMAIL + " = ? AND " + COLUMN_PASSWORD + " = ?";
        String[] selectionArgs = { email, password };
        Cursor cursor = db.query(TABLE_USERS, columns, selection, selectionArgs, null, null, null);
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        db.close();
        return exists;
    }

    /**
     * Update user profile information.
     * @return true if update affected any row
     */
    public boolean updateProfile(String email, String fullName, int age,
                                 String imageUri, double heightCm, double weightKg) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_FULL_NAME, fullName);
        values.put(COLUMN_AGE, age);
        values.put(COLUMN_IMAGE_URI, imageUri);
        values.put(COLUMN_HEIGHT_CM, heightCm);
        values.put(COLUMN_WEIGHT_KG, weightKg);
        int rows = db.update(
                TABLE_USERS,
                values,
                COLUMN_EMAIL + " = ?",
                new String[]{ email }
        );
        db.close();
        return rows > 0;
    }

    /**
     * Retrieve all exercises, including tips.
     */
    public Cursor getAllExercisesWithTips() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_EXERCISES, null);
    }

    /**
     * Query exercises filtered by category.
     */
    public Cursor getExercisesByCategoryWithTips(String category) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_EXERCISES + " WHERE " + COLUMN_EX_CATEGORY + " = ?", new String[]{category});
    }

    /**
     * Save or update a user's daily exercise plan in a transaction.
     */
    public void saveDayPlan(String userEmail, String dayName, List<Integer> exerciseIds) {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            // Remove existing entries for this user and day
            db.delete(TABLE_DAY_PLAN,
                    COL_USER_EMAIL + " = ? AND " + COL_DAY_NAME + " = ?",
                    new String[]{ userEmail, dayName });

            // Insert new plan entries
            ContentValues cv = new ContentValues();
            for (Integer exId : exerciseIds) {
                cv.clear();
                cv.put(COL_USER_EMAIL, userEmail);
                cv.put(COL_DAY_NAME,   dayName);
                cv.put(COL_EX_ID,      exId);
                db.insert(TABLE_DAY_PLAN, null, cv);
            }
            db.setTransactionSuccessful();  // mark as successful
        } finally {
            db.endTransaction();            // end transaction
        }
    }

    /**
     * Retrieves the list of exercise IDs saved for a specific day plan.
     *
     * @param userEmail the email of the user whose plan to fetch
     * @param dayName   the name of the day (e.g., "Monday")
     * @return List of exercise IDs for the given day; empty if none or on error
     */
    @NonNull
    public List<Integer> getDayPlan(String userEmail, String dayName) {
        // Prepare result container
        List<Integer> result = new ArrayList<>();

        // 1) Prevent null binding in SQL by returning empty on null inputs
        if (userEmail == null || dayName == null) {
            Log.w(TAG, "getDayPlan called with null: userEmail="
                    + userEmail + " dayName=" + dayName);
            return result;  // Return empty list instead of crashing
        }

        // Open database for reading
        SQLiteDatabase db = getReadableDatabase();

        // Query the day plan table for the given user and day
        Cursor c = db.query(
                TABLE_DAY_PLAN,                     // Table name
                new String[]{ COL_EX_ID },         // Columns to return (exercise ID)
                COL_USER_EMAIL + " = ? AND " + COL_DAY_NAME + " = ?",  // Selection clause
                new String[]{ userEmail, dayName },// Selection args
                null, null, null                   // No grouping, filtering, or sorting
        );

        // Iterate cursor and collect exercise IDs
        while (c != null && c.moveToNext()) {
            // getInt throws if column missing
            result.add(c.getInt(c.getColumnIndexOrThrow(COL_EX_ID)));
        }

        // Close cursor to release resources
        if (c != null) c.close();

        return result;
    }

    /**
     * Updates the name of the workout plan for a specific user and day.
     *
     * @param userEmail the email of the user
     * @param dayName   the name of the day
     * @param planName  the new name to assign to the plan
     */
    public void savePlanName(String userEmail, String dayName, String planName) {
        // Open database for writing
        SQLiteDatabase db = getWritableDatabase();

        // Prepare new values for update
        ContentValues cv = new ContentValues();
        cv.put("name", planName);

        // Perform update on day plan table
        db.update(
                TABLE_DAY_PLAN,                   // Table to update
                cv,                               // New values
                COL_USER_EMAIL + " = ? AND " + COL_DAY_NAME + " = ?",  // Selection clause
                new String[]{ userEmail, dayName }// Selection args
        );
        // Note: db not closed here as per typical SQLiteOpenHelper usage (optional)
    }

    /**
     * Retrieves the custom name of a saved plan for a given user and day.
     *
     * @param userEmail the email of the user
     * @param dayName   the name of the day
     * @return the plan name if exists; empty string otherwise
     */
    public String getPlanName(String userEmail, String dayName) {
        // Open database for reading
        SQLiteDatabase db = getReadableDatabase();

        // Query only the 'name' column, limit to 1 result
        Cursor c = db.query(
                TABLE_DAY_PLAN,                   // Table name
                new String[]{ "name" },         // Column to return
                COL_USER_EMAIL + " = ? AND " + COL_DAY_NAME + " = ?",  // Selection clause
                new String[]{ userEmail, dayName },// Selection args
                null, null,
                "1"                              // Limit to 1 row
        );

        String name = "";  // Default to empty
        // If there's a result, fetch the name
        if (c != null && c.moveToFirst()) {
            name = c.getString(c.getColumnIndexOrThrow("name"));
            c.close();  // Close cursor after use
        }
        return name;
    }

    /**
     * Inserts or replaces a workout set record in the database.
     *
     * @param exerciseId the exercise's unique ID
     * @param setNumber  the set number within the exercise
     * @param reps       number of repetitions performed
     * @param weight     weight used in this set
     * @param userEmail  the email of the user
     * @param dayName    the day name for this workout
     * @return the row ID of the inserted or replaced record, or -1 on error
     */
    public long insertWorkoutSet(int exerciseId, int setNumber, int reps, float weight, String userEmail, String dayName) {
        // Open database for writing
        SQLiteDatabase db = getWritableDatabase();

        // Bundle values for insertion
        ContentValues cv = new ContentValues();
        cv.put("exercise_id", exerciseId);
        cv.put("user_email",   userEmail);
        cv.put("set_number",   setNumber);
        cv.put("reps",         reps);
        cv.put("weight",       weight);
        cv.put("day_name",     dayName);

        // Insert or replace on conflict, e.g., same exercise_id+set_number+user_email
        long result = db.insertWithOnConflict(
                "workout_sets",                  // Table name
                null,                             // Null column hack
                cv,                               // Values
                SQLiteDatabase.CONFLICT_REPLACE  // Conflict algorithm
        );

        // Close DB to free resources
        db.close();
        return result;
    }

    /**
     * Fetches all workout sets for a specific exercise and user, ordered by set number.
     *
     * @param exerciseId the ID of the exercise
     * @param userEmail  the email of the user
     * @return List of WorkoutSet objects; empty if none found
     */
    public List<WorkoutSet> getWorkoutSetsForExercise(int exerciseId, String userEmail) {
        List<WorkoutSet> out = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();

        // Query all columns for matching exercise and user, sort ascending by set number
        Cursor c = db.query(
                "workout_sets",                  // Table name
                new String[]{"id","exercise_id","user_email","set_number","reps","weight"},
                "exercise_id = ? AND user_email = ?",  // Selection
                new String[]{String.valueOf(exerciseId), userEmail},  // Args
                null, null,
                "set_number ASC"                // Order by set_number
        );

        // Iterate and map each row to a WorkoutSet instance
        if (c != null) {
            while (c.moveToNext()) {
                out.add(new WorkoutSet(
                        c.getInt(c.getColumnIndexOrThrow("id")),            // Unique PK
                        c.getInt(c.getColumnIndexOrThrow("exercise_id")),  // Exercise FK
                        c.getString(c.getColumnIndexOrThrow("user_email")),// User reference
                        c.getInt(c.getColumnIndexOrThrow("set_number")),   // Set sequence
                        c.getInt(c.getColumnIndexOrThrow("reps")),         // Repetition count
                        c.getFloat(c.getColumnIndexOrThrow("weight"))      // Weight used
                ));
            }
            c.close();  // Don't forget to close cursor
        }
        return out;
    }

    /**
     * Checks whether a specific workout set exists for a given exercise, set number, and user.
     *
     * @param exerciseId the exercise ID to look up
     * @param setNumber  the set number within that exercise
     * @param userEmail  the user's email
     * @return true if such a set exists; false otherwise
     */
    public boolean hasWorkoutSet(int exerciseId, int setNumber, String userEmail) {
        SQLiteDatabase db = getReadableDatabase();

        // Query only the primary key 'id' to check existence
        Cursor c = db.query(
                "workout_sets",
                new String[]{"id"},
                "exercise_id = ? AND set_number = ? AND user_email = ?",
                new String[]{String.valueOf(exerciseId), String.valueOf(setNumber), userEmail},
                null, null,
                null
        );

        boolean exists = (c != null && c.moveToFirst());  // Cursor.moveToFirst returns false if empty
        if (c != null) c.close();  // Close cursor
        return exists;
    }

    /**
     * Updates reps and weight (and optionally day name) for an existing workout set record.
     *
     * @param exerciseId the ID of the exercise to update
     * @param setNumber  the set number
     * @param reps       new repetition count
     * @param weight     new weight value
     * @param userEmail  the user's email
     * @param dayName    the updated day name (if plan moved to another day)
     * @return number of rows affected (should be 1 if successful)
     */
    public int updateWorkoutSet(int exerciseId, int setNumber, int reps, float weight, String userEmail, String dayName) {
        SQLiteDatabase db = getWritableDatabase();

        // Prepare updated values
        ContentValues cv = new ContentValues();
        cv.put("reps", reps);
        cv.put("weight", weight);
        cv.put("day_name", dayName);

        // Execute update statement
        int rows = db.update(
                "workout_sets",                  // Table to update
                cv,                               // New values
                "exercise_id = ? AND set_number = ? AND user_email = ?",  // Selection
                new String[]{String.valueOf(exerciseId), String.valueOf(setNumber), userEmail}
        );
        db.close();  // Close DB
        return rows; // Should be 1 if record existed
    }

    /**
     * Deletes a specific workout set record from the database.
     *
     * @param exerciseId the exercise ID of the set to delete
     * @param setNumber  the set number to delete
     * @param userEmail  the user's email associated with the set
     * @return number of rows deleted (1 if successful, 0 if not found)
     */
    public int deleteWorkoutSet(int exerciseId, int setNumber, String userEmail) {
        SQLiteDatabase db = this.getWritableDatabase();

        // Perform delete based on exerciseId, setNumber, and userEmail
        int rows = db.delete(
                "workout_sets",                  // Table name
                "exercise_id = ? AND set_number = ? AND user_email = ?",  // Where clause
                new String[]{
                        String.valueOf(exerciseId),
                        String.valueOf(setNumber),
                        userEmail
                }
        );
        db.close();  // Close DB connection
        return rows;
    }
    /**
     * Saves a single workout log entry for a user and exercise.
     *
     * @param userEmail  email of the user
     * @param exerciseId ID of the exercise being logged
     * @param dayName    name of the day (e.g., "Monday")
     * @param sets       number of sets performed
     * @param reps       number of reps per set
     * @param weight     weight used
     */
    public void saveLogEntry(String userEmail, int exerciseId, String dayName, int sets, int reps, float weight) {
        // Open the database in writable mode
        SQLiteDatabase db = this.getWritableDatabase();

        // Prepare the column-value mappings for insertion
        ContentValues values = new ContentValues();
        values.put("user_email", userEmail);      // user identifier
        values.put("exercise_id", exerciseId);    // exercise identifier
        values.put("day_name", dayName);          // day label
        values.put("sets", sets);                 // count of sets
        values.put("reps", reps);                 // count of reps
        values.put("weight", weight);             // weight lifted

        // Insert the new log entry into the workout_logs table
        db.insert("workout_logs", null, values);
    }

    /**
     * Retrieves all distinct exercise categories from the exercises table.
     *
     * @return List of category names (strings)
     */
    public List<String> getAllCategories() {
        List<String> categories = new ArrayList<>();
        // Open the database for reading
        SQLiteDatabase db = this.getReadableDatabase();

        // Execute a raw SQL query to select distinct categories
        Cursor c = db.rawQuery(
                "SELECT DISTINCT " + COLUMN_EX_CATEGORY +
                        " FROM " + TABLE_EXERCISES,
                null
        );

        // Iterate through the result set and collect each category
        if (c.moveToFirst()) {
            do {
                categories.add(
                        c.getString(c.getColumnIndexOrThrow(COLUMN_EX_CATEGORY))
                );
            } while (c.moveToNext());
        }
        c.close(); // Always close the cursor
        return categories;
    }

    /**
     * Retrieves all workout log entries for a user filtered by exercise category.
     * Each entry represents a snapshot of sets, reps, weight, and timestamp.
     *
     * @param category  the exercise category to filter by
     * @param userEmail email of the user
     * @return List of WorkoutLogEntry objects matching criteria, ordered by day name
     */
    public List<WorkoutLogEntry> getLogEntriesByCategory(String category, String userEmail) {
        List<WorkoutLogEntry> out = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();

        // Compose SQL to join workout_logs with exercises on exercise ID
        String sql =
                "SELECT wl.day_name, wl.sets, wl.reps, wl.weight, wl.updated_at " +
                        "FROM workout_logs wl " +
                        "  JOIN exercises e ON wl.exercise_id = e.id " +
                        "WHERE e.category = ? AND wl.user_email = ? " +
                        "ORDER BY wl.day_name";

        // Execute the parameterized query
        Cursor c = db.rawQuery(sql, new String[]{ category, userEmail });

        // Loop through results and construct WorkoutLogEntry objects
        if (c.moveToFirst()) {
            do {
                out.add(new WorkoutLogEntry(
                        c.getString(0),   // day_name column
                        c.getInt(1),      // sets column
                        c.getInt(2),      // reps column
                        c.getFloat(3),    // weight column
                        c.getString(4)    // updated_at timestamp
                ));
            } while (c.moveToNext());
        }
        c.close();
        return out;
    }

    /**
     * Fetches all exercise IDs for a specific category.
     *
     * @param category exercise category name
     * @return List of exercise ID integers
     */
    public List<Integer> getExerciseIdsByCategory(String category) {
        List<Integer> exerciseIds = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        // Raw query to select IDs where category matches
        Cursor cursor = db.rawQuery(
                "SELECT id FROM exercises WHERE category = ?",
                new String[]{ category }
        );

        if (cursor.moveToFirst()) {
            do {
                exerciseIds.add(cursor.getInt(0)); // add the ID at column index 0
            } while (cursor.moveToNext());
        }
        cursor.close();
        return exerciseIds;
    }

    /**
     * Deletes workout log entries for a user across multiple exercises.
     *
     * @param exerciseIds list of exercise IDs to delete logs for
     * @param userEmail   email of the user
     * @return number of rows deleted
     */
    public int deleteLogEntriesByExerciseIds(List<Integer> exerciseIds, String userEmail) {
        // No-op if list is empty
        if (exerciseIds.isEmpty()) return 0;

        SQLiteDatabase db = this.getWritableDatabase();

        // Dynamically build the SQL "IN" clause placeholders
        StringBuilder inClause = new StringBuilder("(");
        for (int i = 0; i < exerciseIds.size(); i++) {
            inClause.append("?");
            if (i < exerciseIds.size() - 1) {
                inClause.append(",");
            }
        }
        inClause.append(")");

        // Prepare delete arguments: all exercise IDs followed by userEmail
        String[] args = new String[exerciseIds.size() + 1];
        for (int i = 0; i < exerciseIds.size(); i++) {
            args[i] = String.valueOf(exerciseIds.get(i));
        }
        args[exerciseIds.size()] = userEmail;

        // Execute deletion on workout_logs table
        return db.delete(
                "workout_logs",
                "exercise_id IN " + inClause.toString() + " AND user_email = ?",
                args
        );
    }

    /**
     * Inserts a nutrition log entry (meal record) for a user on a given date.
     *
     * @param userEmail email of the user
     * @param date      date of the meal (YYYY-MM-DD)
     * @param mealType  type of meal (e.g., "breakfast")
     * @param food      description of food
     * @param calories  calorie count
     * @param protein   grams of protein
     * @param carbs     grams of carbohydrates
     * @param fat       grams of fat
     * @return row ID of the inserted record, or -1 on error
     */
    public long insertNutritionLog(String userEmail, String date, String mealType,
                                   String food, int calories,
                                   float protein, float carbs, float fat) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("user_email", userEmail);
        cv.put("date", date);
        cv.put("meal_type", mealType);
        cv.put("food", food);
        cv.put("calories", calories);
        cv.put("protein", protein);
        cv.put("carbs", carbs);
        cv.put("fat", fat);

        long id = db.insert("nutrition_logs", null, cv);
        db.close();
        return id;
    }

    /**
     * Retrieves nutrition log entries for a user on a specific date.
     * Validates inputs before querying.
     *
     * @param userEmail email of the user (cannot be null/empty)
     * @param date      date string (cannot be null/empty)
     * @return List of MealLogEntry objects ordered by meal type
     * @throws IllegalArgumentException if inputs are invalid
     */
    public List<MealLogEntry> getNutritionLogsForDate(String userEmail, String date) {
        // Validate required parameters
        if (userEmail == null || userEmail.isEmpty()) {
            throw new IllegalArgumentException("userEmail must not be null or empty");
        }
        if (date == null || date.isEmpty()) {
            throw new IllegalArgumentException("date must not be null or empty");
        }

        List<MealLogEntry> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();

        // Query columns for matching user and date, sorted by meal_type
        Cursor c = db.query(
                "nutrition_logs",
                new String[]{
                        "id",         // primary key
                        "meal_type",  // e.g., breakfast
                        "food",
                        "calories",
                        "protein",
                        "carbs",
                        "fat"
                },
                "user_email = ? AND date = ?",
                new String[]{ userEmail, date },
                null,
                null,
                "meal_type ASC"
        );

        // Map each row into a MealLogEntry object
        while (c.moveToNext()) {
            list.add(new MealLogEntry(
                    c.getInt(0),    // id
                    userEmail,      // passed userEmail
                    date,           // passed date
                    c.getString(1), // mealType
                    c.getString(2), // food
                    c.getInt(3),    // calories
                    c.getFloat(4),  // protein
                    c.getFloat(5),  // carbs
                    c.getFloat(6)   // fat
            ));
        }
        c.close();
        return list;
    }

    /**
     * Deletes a single nutrition log entry by its unique ID.
     *
     * @param id primary key of the nutrition log
     * @return number of rows deleted (should be 1 if successful)
     */
    public int deleteNutritionLog(int id) {
        return getWritableDatabase()
                .delete("nutrition_logs", "id = ?", new String[]{ String.valueOf(id) });
    }

    /**
     * Updates an existing nutrition log entry with new values.
     *
     * @param id        ID of the entry to update
     * @param email     updated user_email
     * @param date      updated date
     * @param mealType  updated meal type
     * @param food      updated food description
     * @param calories  updated calories
     * @param protein   updated protein
     * @param carbs     updated carbs
     * @param fat       updated fat
     * @return number of rows affected
     */
    public int updateNutritionLog(int id,
                                  String email, String date,
                                  String mealType, String food,
                                  int calories, float protein, float carbs, float fat) {

        ContentValues cv = new ContentValues();
        cv.put("user_email", email);
        cv.put("date", date);
        cv.put("meal_type", mealType);
        cv.put("food", food);
        cv.put("calories", calories);
        cv.put("protein", protein);
        cv.put("carbs", carbs);
        cv.put("fat", fat);

        return getWritableDatabase()
                .update("nutrition_logs", cv, "id = ?", new String[]{ String.valueOf(id) });
    }

    /**
     * Retrieves a user's profile information from the users table.
     *
     * @param email user's email (non-null)
     * @return Cursor positioned before first result, containing profile columns
     */
    public Cursor getProfile(@NonNull String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(
                TABLE_USERS,
                new String[]{COLUMN_EMAIL, COLUMN_FULL_NAME, COLUMN_AGE, COLUMN_IMAGE_URI, COLUMN_HEIGHT_CM, COLUMN_WEIGHT_KG},
                COLUMN_EMAIL + " = ?",
                new String[]{email},
                null, null, null
        );
    }

    /**
     * Seeds the user_exercises table with all exercises for a new user, if not already seeded.
     * Ensures the operation runs in a single transaction for efficiency.
     *
     * @param userEmail email of the user to seed exercises for
     */
    public void seedUserExercises(String userEmail) {
        SQLiteDatabase db = getWritableDatabase();
        // Check if already seeded by querying one existing row
        Cursor c = db.query(
                "user_exercises",
                new String[]{"exercise_id"},
                "user_email=?",
                new String[]{userEmail},
                null,null,"1"
        );
        boolean seeded = c != null && c.moveToFirst();
        if (c!=null) c.close();
        if (seeded) return;  // nothing to do if already seeded

        db.beginTransaction();
        try {
            // Fetch all exercises from master table
            Cursor all = db.query(
                    "exercises",
                    new String[]{"id","name","category","tips"},
                    null,null,null,null,null
            );
            ContentValues cv = new ContentValues();
            // Insert each into user_exercises for this user
            while(all.moveToNext()) {
                cv.clear();
                cv.put("user_email",   userEmail);
                cv.put("exercise_id",  all.getInt(0));
                cv.put("name",         all.getString(1));
                cv.put("category",     all.getString(2));
                cv.put("tips",         all.getString(3));
                db.insert("user_exercises", null, cv);
            }
            all.close();
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    /**
     * Retrieves this user's personalized exercises, ordered by category then name.
     *
     * @param userEmail email of the user
     * @return Cursor over user_exercises rows with aliased columns
     */
    public Cursor getUserExercises(String userEmail) {
        return getReadableDatabase().query(
                "user_exercises",
                new String[]{"exercise_id AS id","name","category","tips"},
                "user_email = ?",
                new String[]{userEmail},
                null,null,
                "category,name"
        );
    }

    /**
     * Deletes a user and cascades deletions to all related tables via foreign key constraints.
     *
     * @param email email of the user to delete
     * @return number of rows deleted from the users table
     */
    public int deleteUser(String email) {
        SQLiteDatabase db = getWritableDatabase();
        int rows = db.delete(TABLE_USERS, COLUMN_EMAIL + " = ?", new String[]{ email });
        db.close();
        return rows;
    }
}
