package com.myapp.fitnessapp.database;

import android.content.Context;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase;
import android.content.ContentValues;
import android.database.Cursor;

public class DBHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "FitnessApp.db";
    private static final int DATABASE_VERSION = 1;
    private static final String TABLE_USERS = "users";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_EMAIL = "email";
    private static final String COLUMN_USERNAME = "username";
    private static final String COLUMN_PASSWORD = "password";
    private static final String COLUMN_FULL_NAME   = "full_name";
    private static final String COLUMN_AGE         = "age";
    private static final String COLUMN_IMAGE_URI   = "image_uri";
    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_USERS_TABLE = "CREATE TABLE " + TABLE_USERS + " ("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_USERNAME + " TEXT UNIQUE,"
                + COLUMN_EMAIL + " TEXT UNIQUE,"
                + COLUMN_PASSWORD + " TEXT,"
                + COLUMN_FULL_NAME + " TEXT,"
                + COLUMN_AGE + " INTEGER,"
                + COLUMN_IMAGE_URI + " TEXT"
                + ")";
        db.execSQL(CREATE_USERS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        onCreate(db);
    }

    /**
     * Insert a new user into the users table.
     * @return true if insertion was successful, false otherwise.
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
     * @return true if at least one row was updated.
     */
    public boolean updateProfile(String email, String fullName, int age, String imageUri) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_FULL_NAME, fullName);
        values.put(COLUMN_AGE,         age);
        values.put(COLUMN_IMAGE_URI,   imageUri);
        // update the row where email matches
        int rows = db.update(
                TABLE_USERS,
                values,
                COLUMN_EMAIL + " = ?",
                new String[]{ email }
        );
        db.close();
        return rows > 0;
    }

    public Cursor getUserProfile(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(
                TABLE_USERS,
                new String[]{COLUMN_FULL_NAME, COLUMN_AGE, COLUMN_IMAGE_URI},
                COLUMN_EMAIL + " = ?",
                new String[]{ email },
                null, null, null
        );
    }
}