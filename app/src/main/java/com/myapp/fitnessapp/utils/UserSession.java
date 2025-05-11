package com.myapp.fitnessapp.utils;

import android.content.Context;

import androidx.annotation.Nullable;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.myapp.fitnessapp.database.DBHelper;

public class UserSession {
    private static final FirebaseAuth auth = FirebaseAuth.getInstance();
    private static DBHelper db;

    /** Call once, e.g. in MainActivity.onCreate() */
    public static void init(Context ctx) {
        if (db == null) {
            db = new DBHelper(ctx.getApplicationContext());
        }
    }

    /** Returns the currently signed-in user’s email, or null if none. */
    @Nullable
    public static String getEmail() {
        FirebaseUser u = auth.getCurrentUser();
        return (u != null ? u.getEmail() : null);
    }

    /** Shortcut to the shared DB helper. */
    public static DBHelper getDbHelper() {
        return db;
    }
}
