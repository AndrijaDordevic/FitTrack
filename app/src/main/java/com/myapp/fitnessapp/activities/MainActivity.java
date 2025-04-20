package com.myapp.fitnessapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import com.myapp.fitnessapp.R;
import com.myapp.fitnessapp.fragments.LoginFragment;
import com.myapp.fitnessapp.fragments.SignUpFragment;

public class MainActivity extends AppCompatActivity {

    private Button welcomeButton, loginButton, signupButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        welcomeButton = findViewById(R.id.btn_welcome);
        loginButton   = findViewById(R.id.btn_login);
        signupButton  = findViewById(R.id.btn_signup);

        welcomeButton.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, WelcomeActivity.class))
        );

        loginButton.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, LoginFragment.class))
        );

        signupButton.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, SignUpFragment.class))
        );
    }
}
