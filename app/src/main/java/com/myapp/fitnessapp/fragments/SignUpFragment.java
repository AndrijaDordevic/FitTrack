package com.myapp.fitnessapp.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.myapp.fitnessapp.R;
import com.myapp.fitnessapp.database.DBHelper;
import android.content.Intent;

public class SignUpFragment extends Fragment {
    private EditText emailEt, userEt, passEt;
    private Button signUpBtn;
    private DBHelper db;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_sign_up, container, false);
        emailEt   = v.findViewById(R.id.emailEditText);
        userEt    = v.findViewById(R.id.usernameEditText);
        passEt    = v.findViewById(R.id.passwordEditText);
        signUpBtn = v.findViewById(R.id.signUpButton);
        db = new DBHelper(requireContext());

        signUpBtn.setOnClickListener(view -> registerUser());
        return v;
    }

    private void registerUser() {
        String email = emailEt.getText().toString().trim();
        String user  = userEt.getText().toString().trim();
        String pass  = passEt.getText().toString().trim();
        if (email.isEmpty() || user.isEmpty() || pass.isEmpty()) {
            Toast.makeText(getContext(), "Fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (db.addUser(email, user, pass)) {
            Toast.makeText(getContext(), "Registered!", Toast.LENGTH_SHORT).show();

            // pass your data on
            Bundle args = new Bundle();
            args.putString("email", email);
            args.putString("username", user);

            // navigate
            NavHostFragment.findNavController(this)
                    .navigate(R.id.action_signup_to_profile, args);
        } else {
            Toast.makeText(getContext(),
                    "Registration failed: email may exist",
                    Toast.LENGTH_SHORT).show();
        }
    }
}

