package com.myapp.fitnessapp.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.myapp.fitnessapp.R;
import com.myapp.fitnessapp.database.DBHelper;
import com.myapp.fitnessapp.utils.UserSession;

public class SignUpFragment extends Fragment {
    private static final int RC_SIGN_IN = 9001;

    private EditText emailEt, userEt, passEt, confirmPassEt;
    private Button signUpBtn;
    private Button googleSignUpBtn;
    private ProgressBar progressBar;

    private FirebaseAuth mAuth;
    private DBHelper db;
    private GoogleSignInClient mGoogleSignInClient;

    @Nullable @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_sign_up, container, false);

        // Initialize Firebase Auth and local DB helper
        mAuth = FirebaseAuth.getInstance();
        UserSession.init(requireContext());
        db    = UserSession.getDbHelper();

        // Configure Google Sign-In to request ID token and email
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(
                GoogleSignInOptions.DEFAULT_SIGN_IN
        )
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(requireContext(), gso);

        // Bind UI elements
        emailEt         = v.findViewById(R.id.emailEditText);
        passEt          = v.findViewById(R.id.passwordEditText);
        confirmPassEt   = v.findViewById(R.id.confirmPasswordEditText);
        signUpBtn       = v.findViewById(R.id.signUpButton);
        googleSignUpBtn = v.findViewById(R.id.googleSignInButton);
        progressBar     = v.findViewById(R.id.progressBar);

        // Set click listeners for signup actions
        signUpBtn.setOnClickListener(view -> registerUser());
        googleSignUpBtn.setOnClickListener(vv -> signInWithGoogle());

        // Handle back navigation
        v.findViewById(R.id.btnBack)
                .setOnClickListener(back ->
                        NavHostFragment.findNavController(SignUpFragment.this)
                                .popBackStack()
                );

        return v;
    }

    // Register new user with email/password and navigate to profile setup
    private void registerUser() {
        String email         = emailEt.getText().toString().trim();
        String pass          = passEt.getText().toString().trim();
        String confirmPass   = confirmPassEt.getText().toString().trim(); // <-- New line

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(pass) || TextUtils.isEmpty(confirmPass)) {
            Toast.makeText(getContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!pass.equals(confirmPass)) {
            Toast.makeText(getContext(), "Passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }

        // Show progress and create Firebase account
        progressBar.setVisibility(View.VISIBLE);
        mAuth.createUserWithEmailAndPassword(email, pass)
                .addOnCompleteListener(requireActivity(), task -> {
                    progressBar.setVisibility(View.GONE);
                    if (task.isSuccessful()) {
                        // Mirror new user locally
                        db.addUser(email, "");
                        Toast.makeText(getContext(), "Registration successful!", Toast.LENGTH_SHORT).show();

                        Bundle args = new Bundle();
                        args.putString("email",    email);
                        NavHostFragment.findNavController(this)
                                .navigate(R.id.action_signup_to_profile, args);
                    } else {
                        // Show error message
                        Toast.makeText(getContext(),
                                "Registration failed: " + task.getException().getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    // Start Google Sign-In intent
    private void signInWithGoogle() {
        startActivityForResult(mGoogleSignInClient.getSignInIntent(), RC_SIGN_IN);
    }

    @Override public void onActivityResult(int req, int res, @Nullable Intent data) {
        super.onActivityResult(req, res, data);
        if (req == RC_SIGN_IN) {
            // Handle Google Sign-In result
            Task<GoogleSignInAccount> task = GoogleSignIn
                    .getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount acct = task.getResult(ApiException.class);
                if (acct != null) firebaseAuthWithGoogle(acct.getIdToken());
            } catch (ApiException e) {
                Toast.makeText(requireContext(),
                        "Google sign-in failed: " + e.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Authenticate with Firebase using Google credentials
    private void firebaseAuthWithGoogle(String idToken) {
        progressBar.setVisibility(View.VISIBLE);
        mAuth.signInWithCredential(GoogleAuthProvider
                        .getCredential(idToken, null))
                .addOnCompleteListener(requireActivity(), t -> {
                    progressBar.setVisibility(View.GONE);
                    if (t.isSuccessful()) {
                        String email    = mAuth.getCurrentUser().getEmail();

                        // Ensure user exists locally
                        if (!db.checkUser(email, "")) {
                            db.addUser(email, "");
                        }
                        // Preload exercises then proceed
                        db.seedUserExercises(email);

                        Bundle args = new Bundle();
                        args.putString("email",    email);
                        NavHostFragment.findNavController(this)
                                .navigate(R.id.action_signup_to_profile, args);

                        Toast.makeText(getContext(),
                                "Google sign-in successful!", Toast.LENGTH_SHORT).show();
                    } else {
                        // Authentication error
                        Toast.makeText(getContext(),
                                "Authentication Failed: " + t.getException().getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
