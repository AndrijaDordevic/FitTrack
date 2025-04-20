package com.myapp.fitnessapp.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.myapp.fitnessapp.R;
import com.myapp.fitnessapp.activities.WelcomeActivity;
import com.myapp.fitnessapp.database.DBHelper;

public class LoginFragment extends AppCompatActivity {

    private EditText emailEditText, passwordEditText;
    private Button loginButton, signUpButton;
    private DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_login);

        dbHelper = new DBHelper(this);
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        loginButton = findViewById(R.id.loginButton);
        signUpButton = findViewById(R.id.signUpButton);

        loginButton.setOnClickListener(v -> loginUser());
        signUpButton.setOnClickListener(v -> navigateToSignUp());
    }

    private void loginUser() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean exists = dbHelper.checkUser(email, password);
        if (exists) {
            startActivity(new Intent(LoginFragment.this, WelcomeActivity.class));
            finish();
        } else {
            Toast.makeText(LoginFragment.this, "Authentication failed. Invalid credentials.", Toast.LENGTH_SHORT).show();
        }
    }

    private void navigateToSignUp() {
        startActivity(new Intent(LoginFragment.this, SignUpFragment.class));
    }
}
