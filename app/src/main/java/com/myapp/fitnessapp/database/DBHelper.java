package com.myapp.fitnessapp.database;

import android.content.Context;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase;
import android.content.ContentValues;
import android.database.Cursor;

import com.myapp.fitnessapp.models.ExerciseItem;
import com.myapp.fitnessapp.models.WorkoutSet;
import androidx.annotation.NonNull;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class DBHelper extends SQLiteOpenHelper {
    private static final String TAG = "DBHelper";
    private static final String DATABASE_NAME = "FitnessApp.db";
    private static final int DATABASE_VERSION = 14;

    // Users table
    private static final String TABLE_USERS       = "users";
    private static final String COLUMN_ID         = "id";
    private static final String COLUMN_EMAIL      = "email";
    private static final String COLUMN_USERNAME   = "username";
    private static final String COLUMN_PASSWORD   = "password";
    private static final String COLUMN_FULL_NAME  = "full_name";
    private static final String COLUMN_AGE        = "age";
    private static final String COLUMN_IMAGE_URI  = "image_uri";

    // Exercises table
    private static final String TABLE_EXERCISES       = "exercises";
    private static final String COLUMN_EX_ID          = "id";
    private static final String COLUMN_EX_NAME        = "name";
    private static final String COLUMN_EX_CATEGORY    = "category";
    private static final String COLUMN_EX_TIPS        = "tips";

    private static final String TABLE_DAY_PLAN   = "day_plans";
    private static final String COL_DAY_NAME     = "day_name";
    private static final String COL_EX_ID        = "exercise_id";
    private static final String COL_USER_EMAIL  = "user_email";

    private static final String TABLE_WORKOUT_LOGS = "workout_logs";
    private static final String COLUMN_LOG_ID = "log_id";
    private static final String COLUMN_USER_EMAIL = "user_email";
    private static final String COLUMN_EXERCISE_ID = "exercise_id";
    private static final String COLUMN_SETS = "sets";
    private static final String COLUMN_REPS = "reps";
    private static final String COLUMN_WEIGHT = "weight";
    private static final String COLUMN_DATE = "date";
    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS users (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "username TEXT," +
                "email TEXT," +
                "password TEXT," +
                "full_name TEXT," +
                "age INTEGER," +
                "image_uri TEXT)");

        db.execSQL(
                "CREATE TABLE IF NOT EXISTS " + TABLE_EXERCISES + " (" +
                        COLUMN_EX_ID       + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        COLUMN_EX_NAME     + " TEXT NOT NULL, " +
                        COLUMN_EX_CATEGORY + " TEXT NOT NULL, " +
                        COLUMN_EX_TIPS     + " TEXT" +
                        ")"
        );

        // Optional: insert starter data
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

        db.execSQL(
                "CREATE TABLE IF NOT EXISTS " + TABLE_DAY_PLAN + " (" +
                        COL_USER_EMAIL + " TEXT    NOT NULL, " +
                        COL_DAY_NAME   + " TEXT    NOT NULL, " +
                        COL_EX_ID      + " INTEGER NOT NULL, " +
                        "name           TEXT, " +             // ← new
                        "PRIMARY KEY(" + COL_USER_EMAIL + ","+ COL_DAY_NAME + ","+ COL_EX_ID + "), " +
                        "FOREIGN KEY(" + COL_EX_ID + ")     REFERENCES exercises(id)    ON DELETE CASCADE, " +
                        "FOREIGN KEY(" + COL_USER_EMAIL + ") REFERENCES users(email) ON DELETE CASCADE" +
                        ")"
        );

        db.execSQL(
                "CREATE TABLE IF NOT EXISTS " + TABLE_WORKOUT_LOGS + " (" +
                        COLUMN_LOG_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        COLUMN_USER_EMAIL + " TEXT NOT NULL, " +
                        COLUMN_EXERCISE_ID + " INTEGER NOT NULL, " +
                        COLUMN_SETS + " INTEGER, " +
                        COLUMN_REPS + " INTEGER, " +
                        COLUMN_WEIGHT + " REAL, " +
                        COLUMN_DATE + " TEXT, " + // Example: "2025-04-26"
                        "FOREIGN KEY(" + COLUMN_EXERCISE_ID + ") REFERENCES " + TABLE_EXERCISES + "(" + COLUMN_EX_ID + ")" +
                        ")"
        );

        db.execSQL(
                "CREATE TABLE IF NOT EXISTS workout_sets (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "exercise_id INTEGER NOT NULL, " +
                        "user_email TEXT    NOT NULL, " +
                        "set_number INTEGER NOT NULL, " +
                        "reps INTEGER, " +
                        "weight REAL, " +
                        "FOREIGN KEY(exercise_id) REFERENCES exercises(id) ON DELETE CASCADE, " +
                        "FOREIGN KEY(user_email)    REFERENCES users(email) ON DELETE CASCADE" +
                        ")"
        );
    }



    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop tables whose schema changed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_DAY_PLAN);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_EXERCISES);
        db.execSQL("DROP TABLE IF EXISTS workout_sets");
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_WORKOUT_LOGS);

        // Recreate from scratch
        onCreate(db);
    }

    /**
     * Helper to insert an exercise during database creation
     */
    private void insertExercise(SQLiteDatabase db, String name, String category) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_EX_NAME, name);
        values.put(COLUMN_EX_CATEGORY, category);
        db.insert(TABLE_EXERCISES, null, values);
    }

    /**
     * Insert a new user into the users table.
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
     * Check if a user exists with the given email and password.
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
     * Update the profile fields for a user identified by email.
     */
    public boolean updateProfile(String email, String fullName, int age, String imageUri) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_FULL_NAME, fullName);
        values.put(COLUMN_AGE, age);
        values.put(COLUMN_IMAGE_URI, imageUri);
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
     * Retrieve a user’s profile by email.
     */
    public Cursor getUserProfile(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(
                TABLE_USERS,
                new String[]{ COLUMN_FULL_NAME, COLUMN_AGE, COLUMN_IMAGE_URI },
                COLUMN_EMAIL + " = ?",
                new String[]{ email },
                null, null, null
        );
    }

    /**
     * Add a new exercise to the exercises table.
     */
    public boolean addExercise(String name, String category) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_EX_NAME, name);
        values.put(COLUMN_EX_CATEGORY, category);
        long result = db.insert(TABLE_EXERCISES, null, values);
        db.close();
        return result != -1;
    }

    /**
     * Get all exercises.
     */
    public Cursor getAllExercises() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(
                TABLE_EXERCISES,
                new String[]{ COLUMN_EX_ID, COLUMN_EX_NAME, COLUMN_EX_CATEGORY },
                null, null, null, null, null
        );
    }

    /**
     * Get exercises filtered by category.
     */
    public Cursor getExercisesByCategory(String category) {
        SQLiteDatabase db = this.getReadableDatabase();
        String selection = COLUMN_EX_CATEGORY + " = ?";
        String[] selectionArgs = { category };
        return db.query(
                TABLE_EXERCISES,
                new String[]{ COLUMN_EX_ID, COLUMN_EX_NAME, COLUMN_EX_CATEGORY },
                selection, selectionArgs, null, null, null
        );
    }

    public String getTipsForExercise(int exId) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query(
                TABLE_EXERCISES,
                new String[]{ COLUMN_EX_TIPS },
                COLUMN_EX_ID + "=?",
                new String[]{ String.valueOf(exId) },
                null,null,null
        );
        if (c != null && c.moveToFirst()) {
            String tips = c.getString(c.getColumnIndexOrThrow(COLUMN_EX_TIPS));
            c.close();
            return tips;
        }
        return null;
    }

    public Cursor getAllExercisesWithTips() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM exercises", null);
    }

    public Cursor getExercisesByCategoryWithTips(String category) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM exercises WHERE category = ?", new String[]{category});
    }

    public void saveDayPlan(String userEmail, String dayName, List<Integer> exerciseIds) {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            // delete existing for this user+day
            db.delete(TABLE_DAY_PLAN,
                    COL_USER_EMAIL + " = ? AND " + COL_DAY_NAME + " = ?",
                    new String[]{ userEmail, dayName });

            // insert new entries
            ContentValues cv = new ContentValues();
            for (Integer exId : exerciseIds) {
                cv.clear();
                cv.put(COL_USER_EMAIL, userEmail);
                cv.put(COL_DAY_NAME,   dayName);
                cv.put(COL_EX_ID,      exId);
                db.insert(TABLE_DAY_PLAN, null, cv);
            }

            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    /** Load the saved plan (list of exercise IDs) for a given dayName */
    @NonNull
    public List<Integer> getDayPlan(String userEmail, String dayName) {
        List<Integer> result = new ArrayList<>();
        // 1) refuse to bind null into the SQL
        if (userEmail == null || dayName == null) {
            Log.w(TAG, "getDayPlan called with null: userEmail="
                    + userEmail + " dayName=" + dayName);
            return result;  // empty plan rather than crash
        }

        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query(
                TABLE_DAY_PLAN,
                new String[]{ COL_EX_ID },
                COL_USER_EMAIL + " = ? AND " + COL_DAY_NAME + " = ?",
                new String[]{ userEmail, dayName },
                null, null, null
        );
        while (c != null && c.moveToNext()) {
            result.add(c.getInt(c.getColumnIndexOrThrow(COL_EX_ID)));
        }
        if (c != null) c.close();
        return result;
    }

    public boolean hasAnyPlan() {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery("SELECT 1 FROM day_plans LIMIT 1", null);
        boolean has = (c != null && c.moveToFirst());
        if (c != null) c.close();
        return has;
    }

    public void savePlanName(String userEmail, String dayName, String planName) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("name", planName);
        db.update(TABLE_DAY_PLAN,
                cv,
                COL_USER_EMAIL + " = ? AND " + COL_DAY_NAME + " = ?",
                new String[]{ userEmail, dayName });
    }

    public String getPlanName(String userEmail, String dayName) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query(
                TABLE_DAY_PLAN,
                new String[]{ "name" },
                COL_USER_EMAIL + " = ? AND " + COL_DAY_NAME + " = ?",
                new String[]{ userEmail, dayName },
                null, null,
                "1"
        );
        String name = "";
        if (c != null && c.moveToFirst()) {
            name = c.getString(c.getColumnIndexOrThrow("name"));
            c.close();
        }
        return name;
    }

    public boolean addWorkoutLog(String userEmail, int exerciseId, int sets, int reps, float weight, String date) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_USER_EMAIL, userEmail);
        values.put(COLUMN_EXERCISE_ID, exerciseId);
        values.put(COLUMN_SETS, sets);
        values.put(COLUMN_REPS, reps);
        values.put(COLUMN_WEIGHT, weight);
        values.put(COLUMN_DATE, date);
        long result = db.insert(TABLE_WORKOUT_LOGS, null, values);
        db.close();
        return result != -1;
    }

    public long insertWorkoutSet(int exerciseId, int setNumber, int reps, float weight, String userEmail) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("exercise_id", exerciseId);
        cv.put("user_email",   userEmail);
        cv.put("set_number",  setNumber);
        cv.put("reps",        reps);
        cv.put("weight",      weight);
        long result = db.insertWithOnConflict(
                "workout_sets",
                null,
                cv,
                SQLiteDatabase.CONFLICT_REPLACE
        );
        db.close();
        return result;
    }

    public List<WorkoutSet> getWorkoutSetsForExercise(int exerciseId, String userEmail) {
        List<WorkoutSet> out = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query(
                "workout_sets",
                new String[]{"id","exercise_id","user_email","set_number","reps","weight"},
                "exercise_id = ? AND user_email = ?",
                new String[]{String.valueOf(exerciseId), userEmail},
                null, null,
                "set_number ASC"
        );
        if (c != null) {
            while (c.moveToNext()) {
                out.add(new WorkoutSet(
                        c.getInt(c.getColumnIndexOrThrow("id")),
                        c.getInt(c.getColumnIndexOrThrow("exercise_id")),
                        c.getString(c.getColumnIndexOrThrow("user_email")),
                        c.getInt(c.getColumnIndexOrThrow("set_number")),
                        c.getInt(c.getColumnIndexOrThrow("reps")),
                        c.getFloat(c.getColumnIndexOrThrow("weight"))
                ));
            }
            c.close();
        }
        return out;
    }

    /** Checks if a specific set exists */
    public boolean hasWorkoutSet(int exerciseId, int setNumber, String userEmail) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query(
                "workout_sets",
                new String[]{"id"},
                "exercise_id = ? AND set_number = ? AND user_email = ?",
                new String[]{String.valueOf(exerciseId), String.valueOf(setNumber), userEmail},
                null, null, null
        );
        boolean exists = (c != null && c.moveToFirst());
        if (c != null) c.close();
        return exists;
    }

    /** Updates an existing set */
    public int updateWorkoutSet(int exerciseId, int setNumber, int reps, float weight, String userEmail) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("reps",   reps);
        cv.put("weight", weight);
        int rows = db.update(
                "workout_sets",
                cv,
                "exercise_id = ? AND set_number = ? AND user_email = ?",
                new String[]{String.valueOf(exerciseId), String.valueOf(setNumber), userEmail}
        );
        db.close();
        return rows;
    }

    public int deleteWorkoutSet(int exerciseId, int setNumber, String userEmail) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rows = db.delete(
                "workout_sets",
                "exercise_id = ? AND set_number = ? AND user_email = ?",
                new String[]{
                        String.valueOf(exerciseId),
                        String.valueOf(setNumber),
                        userEmail
                }
        );
        db.close();
        return rows;
    }

    public List<ExerciseItem> getAllExerciseItems() {
        List<ExerciseItem> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query(
                TABLE_EXERCISES,
                new String[]{ COLUMN_EX_ID, COLUMN_EX_NAME, COLUMN_EX_CATEGORY },
                null, null, null, null,
                COLUMN_EX_NAME + " ASC"
        );
        while (c.moveToNext()) {
            list.add(new ExerciseItem(
                    c.getInt(c.getColumnIndexOrThrow(COLUMN_EX_ID)),
                    c.getString(c.getColumnIndexOrThrow(COLUMN_EX_NAME)),
                    c.getString(c.getColumnIndexOrThrow(COLUMN_EX_CATEGORY))
            ));
        }
        c.close();
        return list;
    }

}

