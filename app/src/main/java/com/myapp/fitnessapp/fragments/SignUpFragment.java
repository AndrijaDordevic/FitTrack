package com.myapp.fitnessapp.fragments;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.myapp.fitnessapp.R;
import com.myapp.fitnessapp.database.DBHelper;

public class SignUpFragment extends AppCompatActivity {

    private EditText emailEditText, passwordEditText;
    private Button signUpButton;
    private DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_sign_up);

        dbHelper = new DBHelper(this);
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        signUpButton = findViewById(R.id.signUpButton);

        signUpButton.setOnClickListener(v -> registerUser());
    }

    private void registerUser() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean inserted = dbHelper.addUser(email, password);
        if (inserted) {
            Toast.makeText(SignUpFragment.this, "User registered!", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(SignUpFragment.this, "Registration failed. Email may already exist.", Toast.LENGTH_SHORT).show();
        }
    }
}
