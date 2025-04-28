package com.myapp.fitnessapp.fragments;

import android.content.Intent;
import android.content.SharedPreferences;
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
import androidx.preference.PreferenceManager;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;

import com.myapp.fitnessapp.R;
import com.myapp.fitnessapp.database.DBHelper;

public class SignUpFragment extends Fragment {
    private static final int RC_SIGN_IN = 9001;

    private EditText emailEt, userEt, passEt;
    private Button signUpBtn;
    private SignInButton googleSignUpBtn;
    private ProgressBar progressBar;

    private FirebaseAuth mAuth;
    private DBHelper db;
    private GoogleSignInClient mGoogleSignInClient;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_sign_up, container, false);

        // --- Initialize Firebase Auth & DB ---
        mAuth = FirebaseAuth.getInstance();
        db = new DBHelper(requireContext());

        // --- Configure Google Sign-In ---
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(requireContext(), gso);

        // --- Find views ---
        emailEt         = v.findViewById(R.id.emailEditText);
        userEt          = v.findViewById(R.id.usernameEditText);
        passEt          = v.findViewById(R.id.passwordEditText);
        signUpBtn       = v.findViewById(R.id.signUpButton);
        googleSignUpBtn = v.findViewById(R.id.googleSignUpButton);
        progressBar     = v.findViewById(R.id.progressBar);

        // --- Wire up listeners ---
        signUpBtn.setOnClickListener(view -> registerUser());
        googleSignUpBtn.setSize(SignInButton.SIZE_WIDE);
        googleSignUpBtn.setColorScheme(SignInButton.COLOR_DARK);
        googleSignUpBtn.setOnClickListener(view -> signInWithGoogle());

        return v;
    }

    private void registerUser() {
        String email    = emailEt.getText().toString().trim();
        String username = userEt.getText().toString().trim();
        String pass     = passEt.getText().toString().trim();

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(username) || TextUtils.isEmpty(pass)) {
            Toast.makeText(getContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        mAuth.createUserWithEmailAndPassword(email, pass)
                .addOnCompleteListener(requireActivity(), task -> {
                    progressBar.setVisibility(View.GONE);
                    if (task.isSuccessful()) {
                        // Save to local DB
                        db.addUser(email, username, "");
                        // Save to prefs
                        SharedPreferences prefs = PreferenceManager
                                .getDefaultSharedPreferences(requireContext());
                        prefs.edit().putString("user_email", email).apply();

                        Toast.makeText(getContext(),
                                "Registration successful!", Toast.LENGTH_SHORT).show();

                        // Navigate with args
                        Bundle args = new Bundle();
                        args.putString("email", email);
                        args.putString("username", username);
                        NavHostFragment.findNavController(this)
                                .navigate(R.id.action_signup_to_profile, args);
                    } else {
                        Toast.makeText(getContext(),
                                "Registration failed: " + task.getException().getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void signInWithGoogle() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task =
                    GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                if (account != null) {
                    firebaseAuthWithGoogle(account.getIdToken());
                }
            } catch (ApiException e) {
                Toast.makeText(getContext(),
                        "Google sign-in failed: " + e.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        progressBar.setVisibility(View.VISIBLE);
        mAuth.signInWithCredential(
                GoogleAuthProvider.getCredential(idToken, null)
        ).addOnCompleteListener(requireActivity(), task -> {
            progressBar.setVisibility(View.GONE);
            if (task.isSuccessful()) {
                // Grab account details
                GoogleSignInAccount acct =
                        GoogleSignIn.getLastSignedInAccount(requireContext());
                if (acct != null) {
                    String email    = acct.getEmail();
                    String username = acct.getDisplayName();

                    // Save locally
                    db.addUser(email, username, "");
                    SharedPreferences prefs = PreferenceManager
                            .getDefaultSharedPreferences(requireContext());
                    prefs.edit().putString("user_email", email).apply();

                    // Navigate
                    Bundle args = new Bundle();
                    args.putString("email", email);
                    args.putString("username", username);
                    NavHostFragment.findNavController(this)
                            .navigate(R.id.action_signup_to_profile, args);

                    Toast.makeText(getContext(),
                            "Google sign-in successful!", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(getContext(),
                        "Authentication Failed: " + task.getException().getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }
}
