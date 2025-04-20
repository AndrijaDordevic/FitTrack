package com.myapp.fitnessapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;
import com.myapp.fitnessapp.R;
import com.myapp.fitnessapp.fragments.LoginFragment;
import com.myapp.fitnessapp.fragments.SignUpFragment;

public class WelcomeActivity extends AppCompatActivity {
    private Button loginButton, signupButton;
    private ImageView backgroundImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        // Bind views
        backgroundImageView = findViewById(R.id.backgroundImageView);
        loginButton = findViewById(R.id.btn_login);
        signupButton = findViewById(R.id.btn_signup);

        // Optional: change background dynamically
        // backgroundImageView.setImageResource(R.drawable.another_image);
        // backgroundImageView.setAlpha(0.8f);

        loginButton.setOnClickListener(v -> {
            startActivity(new Intent(WelcomeActivity.this, LoginFragment.class));
        });

        signupButton.setOnClickListener(v -> {
            startActivity(new Intent(WelcomeActivity.this, SignUpFragment.class));
        });
    }
}