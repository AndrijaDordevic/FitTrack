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

public class DBHelper extends SQLiteOpenHelper {
    private static final String TAG = "DBHelper";
    private static final String DATABASE_NAME = "FitnessApp.db";
    private static final int DATABASE_VERSION = 26;

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

    private static final String COLUMN_HEIGHT_CM  = "height_cm";
    private static final String COLUMN_WEIGHT_KG  = "weight_kg";
    private static final String COLUMN_LOG_ID = "log_id";
    private static final String COLUMN_USER_EMAIL = "user_email";
    private static final String COLUMN_EXERCISE_ID = "exercise_id";
    private static final String COLUMN_SETS = "sets";
    private static final String COLUMN_REPS = "reps";
    private static final String COLUMN_WEIGHT = "weight";

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }
    @Override
    public void onCreate(SQLiteDatabase db) {
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

        // 2) Create the AFTER INSERT trigger:
        db.execSQL(
                "CREATE TRIGGER IF NOT EXISTS trg_workout_sets_after_insert " +
                        "AFTER INSERT ON workout_sets " +
                        "BEGIN " +
                        "  INSERT INTO workout_logs(" +
                        "    user_email,exercise_id,day_name,sets,reps,weight,updated_at" +
                        "  ) VALUES (" +
                        "    NEW.user_email,NEW.exercise_id,NEW.day_name,1,NEW.reps,NEW.weight,date('now')" +
                        "  ); " +
                        "END;"
        );

        // 3) Create the AFTER UPDATE trigger:
        db.execSQL(
                "CREATE TRIGGER IF NOT EXISTS trg_workout_sets_after_update " +
                        "AFTER UPDATE ON workout_sets " +
                        "BEGIN " +
                        "  INSERT INTO workout_logs(" +
                        "    user_email,exercise_id,day_name,sets,reps,weight,updated_at" +
                        "  ) VALUES (" +
                        "    NEW.user_email,NEW.exercise_id,NEW.day_name,1,NEW.reps,NEW.weight,date('now')" +
                        "  ); " +
                        "END;"
        );

        db.execSQL("CREATE TABLE IF NOT EXISTS nutrition_logs (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "user_email TEXT NOT NULL, " +
                "date TEXT NOT NULL, " +            // yyyy-MM-dd
                "meal_type TEXT NOT NULL, " +      // Breakfast, Lunch, etc.
                "food TEXT NOT NULL, " +
                "calories INTEGER, " +
                "protein REAL, " +
                "carbs REAL, " +
                "fat REAL, " +
                "FOREIGN KEY(user_email) REFERENCES users(email) ON DELETE CASCADE" +
                ")");


    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // temporarily turn off FKs to allow dropping in any order
        db.execSQL("PRAGMA foreign_keys=OFF");

        // drop child tables first
        db.execSQL("DROP TABLE IF EXISTS day_plans");
        db.execSQL("DROP TABLE IF EXISTS workout_sets");
        db.execSQL("DROP TABLE IF EXISTS workout_logs");
        db.execSQL("DROP TABLE IF EXISTS nutrition_logs");
        db.execSQL("DROP TABLE IF EXISTS user_exercises");

        // then parent tables
        db.execSQL("DROP TABLE IF EXISTS exercises");
        db.execSQL("DROP TABLE IF EXISTS users");

        // recreate fresh schema
        onCreate(db);

        // re-enable FKs
        db.execSQL("PRAGMA foreign_keys=ON");
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


    public boolean addExercise(String name, String category) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_EX_NAME, name);
        values.put(COLUMN_EX_CATEGORY, category);
        long result = db.insert(TABLE_EXERCISES, null, values);
        db.close();
        return result != -1;
    }

    public Cursor getAllExercises() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(
                TABLE_EXERCISES,
                new String[]{ COLUMN_EX_ID, COLUMN_EX_NAME, COLUMN_EX_CATEGORY },
                null, null, null, null, null
        );
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


    public long insertWorkoutSet(int exerciseId, int setNumber, int reps, float weight, String userEmail, String dayName) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("exercise_id", exerciseId);
        cv.put("user_email",   userEmail);
        cv.put("set_number",  setNumber);
        cv.put("reps",        reps);
        cv.put("weight",      weight);
        cv.put("day_name",   dayName);
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
    public int updateWorkoutSet(int exerciseId, int setNumber, int reps, float weight, String userEmail, String dayName) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("reps",   reps);
        cv.put("weight", weight);
        cv.put("day_name",   dayName);
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

    public void saveLogEntry(String userEmail, int exerciseId, String dayName, int sets, int reps, float weight) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("user_email", userEmail);
        values.put("exercise_id", exerciseId);
        values.put("day_name", dayName);
        values.put("sets", sets);
        values.put("reps", reps);
        values.put("weight", weight);


        db.insert("workout_logs", null, values);
    }

    public List<WorkoutSet> getAllWorkoutSets(String userEmail) {
        List<WorkoutSet> sets = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(
                "workout_sets",    // table name
                null,            // all columns
                "user_email = ?", // WHERE clause
                new String[]{userEmail},
                null, null, "exercise_id, set_number"
        );

        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
                int exerciseId = cursor.getInt(cursor.getColumnIndexOrThrow("exercise_id"));
                int setNumber = cursor.getInt(cursor.getColumnIndexOrThrow("set_number"));
                int reps = cursor.getInt(cursor.getColumnIndexOrThrow("reps"));
                float weight = cursor.getFloat(cursor.getColumnIndexOrThrow("weight"));
                String email = cursor.getString(cursor.getColumnIndexOrThrow("user_email"));

                sets.add(new WorkoutSet(id, exerciseId, email, setNumber, reps, weight));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return sets;
    }

    public List<WorkoutSet> getWorkoutSetsByCategory(String category, String userEmail) {
        List<WorkoutSet> out = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        // join workout_sets to exercises on exercise_id, filter by category+user
        String sql =
                "SELECT ws.id, ws.exercise_id, ws.user_email, ws.set_number, ws.reps, ws.weight " +
                        "FROM workout_sets ws " +
                        "  JOIN exercises e ON ws.exercise_id = e.id " +
                        "WHERE e.category = ? AND ws.user_email = ? " +
                        "ORDER BY ws.exercise_id, ws.set_number";
        Cursor c = db.rawQuery(sql, new String[]{ category, userEmail });

        if (c.moveToFirst()) {
            do {
                out.add(new WorkoutSet(
                        c.getInt(0),
                        c.getInt(1),
                        c.getString(2),
                        c.getInt(3),
                        c.getInt(4),
                        c.getFloat(5)
                ));
            } while (c.moveToNext());
        }
        c.close();
        return out;
    }
    public List<String> getAllCategories() {
        List<String> categories = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(
                "SELECT DISTINCT " + COLUMN_EX_CATEGORY +
                        " FROM " + TABLE_EXERCISES,
                null
        );
        if (c.moveToFirst()) {
            do {
                categories.add(
                        c.getString(c.getColumnIndexOrThrow(COLUMN_EX_CATEGORY))
                );
            } while (c.moveToNext());
        }
        c.close();
        return categories;
    }

    /**
     * Returns all workout_log entries for a given category & user.
     * Each row is one archived snapshot of sets/reps/weight.
     */
    public List<WorkoutLogEntry> getLogEntriesByCategory(String category, String userEmail) {
        List<WorkoutLogEntry> out = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();

        // 1) Select the five columns, including updated_at
        String sql =
                "SELECT wl.day_name, wl.sets, wl.reps, wl.weight, wl.updated_at " +
                        "FROM workout_logs wl " +
                        "  JOIN exercises e ON wl.exercise_id = e.id " +
                        "WHERE e.category = ? AND wl.user_email = ? " +
                        "ORDER BY wl.day_name";

        Cursor c = db.rawQuery(sql, new String[]{ category, userEmail });
        if (c.moveToFirst()) {
            do {
                out.add(new WorkoutLogEntry(
                        c.getString(0),   // day_name
                        c.getInt(1),      // sets
                        c.getInt(2),      // reps
                        c.getFloat(3),    // weight
                        c.getString(4)    // updated_at
                ));
            } while (c.moveToNext());
        }
        c.close();
        return out;
    }

    public List<Integer> getExerciseIdsByCategory(String category) {
        List<Integer> exerciseIds = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT id FROM exercises WHERE category = ?",
                new String[]{ category }
        );
        if (cursor.moveToFirst()) {
            do {
                exerciseIds.add(cursor.getInt(0));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return exerciseIds;
    }

    public int deleteLogEntriesByExerciseIds(List<Integer> exerciseIds, String userEmail) {
        if (exerciseIds.isEmpty()) return 0;

        SQLiteDatabase db = this.getWritableDatabase();

        // Build "IN (?, ?, ?, ?)" part dynamically
        StringBuilder inClause = new StringBuilder("(");
        for (int i = 0; i < exerciseIds.size(); i++) {
            inClause.append("?");
            if (i < exerciseIds.size() - 1) {
                inClause.append(",");
            }
        }
        inClause.append(")");

        // Prepare the arguments
        String[] args = new String[exerciseIds.size() + 1];
        for (int i = 0; i < exerciseIds.size(); i++) {
            args[i] = String.valueOf(exerciseIds.get(i));
        }
        args[exerciseIds.size()] = userEmail;

        return db.delete(
                "workout_logs",
                "exercise_id IN " + inClause.toString() + " AND user_email = ?",
                args
        );
    }
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

    public List<MealLogEntry> getNutritionLogsForDate(String userEmail, String date) {
        // 1) Validate inputs up‐front
        if (userEmail == null || userEmail.isEmpty()) {
            throw new IllegalArgumentException("userEmail must not be null or empty");
        }
        if (date == null || date.isEmpty()) {
            throw new IllegalArgumentException("date must not be null or empty");
        }

        List<MealLogEntry> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();

        // 2) Now we know neither bind value is null
        Cursor c = db.query(
                "nutrition_logs",
                new String[]{
                        "id",         // 0
                        "meal_type",  // 1
                        "food",       // 2
                        "calories",   // 3
                        "protein",    // 4
                        "carbs",      // 5
                        "fat"         // 6
                },
                "user_email = ? AND date = ?",
                new String[]{ userEmail, date },
                null,
                null,
                "meal_type ASC"
        );

        // 3) Read out the results safely
        while (c.moveToNext()) {
            list.add(new MealLogEntry(
                    c.getInt(0),    // id
                    userEmail,      // userEmail (from method arg)
                    date,           // date      (from method arg)
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

    public int deleteNutritionLog(int id) {
        return getWritableDatabase()
                .delete("nutrition_logs", "id = ?", new String[]{ String.valueOf(id) });
    }

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

    public void seedUserExercises(String userEmail) {
        SQLiteDatabase db = getWritableDatabase();
        // already seeded?
        Cursor c = db.query(
                "user_exercises",
                new String[]{"exercise_id"},
                "user_email=?",
                new String[]{userEmail},
                null,null,"1"
        );
        boolean seeded = c != null && c.moveToFirst();
        if (c!=null) c.close();
        if (seeded) return;

        db.beginTransaction();
        try {
            Cursor all = db.query(
                    "exercises",
                    new String[]{"id","name","category","tips"},
                    null,null,null,null,null
            );
            ContentValues cv = new ContentValues();
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

    /** List only this user’s exercises, in category/name order */
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

    public int deleteUser(String email) {
        SQLiteDatabase db = getWritableDatabase();
        // this will cascade-delete all day_plans, workout_sets, workout_logs, nutrition_logs, user_exercises, etc.
        int rows = db.delete(TABLE_USERS, COLUMN_EMAIL + " = ?", new String[]{ email });
        db.close();
        return rows;
    }

    public boolean updateEmail(String oldEmail, String newEmail) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();
        try {
            // 1) Update the users table
            ContentValues cv = new ContentValues();
            cv.put(COLUMN_EMAIL, newEmail);
            int rows = db.update(
                    TABLE_USERS,
                    cv,
                    COLUMN_EMAIL + " = ?",
                    new String[]{ oldEmail }
            );

            // 2) Propagate to all tables that reference user_email
            String[] tables = {
                    "user_exercises",
                    "day_plans",
                    "workout_sets",
                    "workout_logs",
                    "nutrition_logs"
            };
            ContentValues cv2 = new ContentValues();
            cv2.put("user_email", newEmail);
            for (String tbl : tables) {
                db.update(
                        tbl,
                        cv2,
                        "user_email = ?",
                        new String[]{ oldEmail }
                );
            }

            db.setTransactionSuccessful();
            return rows > 0;
        } finally {
            db.endTransaction();
            db.close();
        }
    }

}

