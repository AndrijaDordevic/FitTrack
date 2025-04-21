package com.myapp.fitnessapp.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.myapp.fitnessapp.R;
import com.myapp.fitnessapp.database.DBHelper;

public class LoginFragment extends Fragment {

    private EditText emailEditText, passwordEditText;
    private Button loginButton, signUpButton;
    private DBHelper dbHelper;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login, container, false);

        dbHelper = new DBHelper(requireContext());
        emailEditText = view.findViewById(R.id.emailEditText);
        passwordEditText = view.findViewById(R.id.passwordEditText);
        loginButton = view.findViewById(R.id.loginButton);
        signUpButton = view.findViewById(R.id.signUpButton);

        loginButton.setOnClickListener(v -> loginUser());
        signUpButton.setOnClickListener(v ->
                NavHostFragment.findNavController(this)
                        .navigate(R.id.action_login_to_dashboard)
        );

        return view;
    }

    private void loginUser() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(requireContext(), "Please enter email and password", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean exists = dbHelper.checkUser(email, password);
        if (exists) {
            Toast.makeText(requireContext(), "Login successful!", Toast.LENGTH_SHORT).show();
            NavHostFragment.findNavController(this)
                    .navigate(R.id.action_login_to_dashboard);
        } else {
            Toast.makeText(requireContext(), "Authentication failed. Invalid credentials.", Toast.LENGTH_SHORT).show();
        }
    }
}
