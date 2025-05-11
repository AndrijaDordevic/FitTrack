package com.myapp.fitnessapp.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.myapp.fitnessapp.R;
import com.myapp.fitnessapp.database.DBHelper;
import com.myapp.fitnessapp.utils.UserSession;

public class LoginFragment extends Fragment {

    private static final int RC_GOOGLE_SIGN_IN = 9001;

    private FirebaseAuth mAuth;
    private GoogleSignInClient googleSignInClient;
    private DBHelper dbHelper;

    private SignInButton googleSignInButton;
    private ProgressBar progressBar;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        View view = inflater.inflate(R.layout.fragment_login, container, false);

        // 1) Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // 2) Initialize our shared DB helper
        UserSession.init(requireContext());
        dbHelper = UserSession.getDbHelper();

        // 3) Configure Google Sign-In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(
                GoogleSignInOptions.DEFAULT_SIGN_IN
        )
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(requireContext(), gso);

        // 4) View bindings
        googleSignInButton = view.findViewById(R.id.googleSignInButton);
        progressBar       = view.findViewById(R.id.progressBar);

        googleSignInButton.setOnClickListener(v -> startGoogleSignIn());
        view.findViewById(R.id.loginButton)
                .setOnClickListener(v -> loginWithEmail(view));
        view.findViewById(R.id.signUpButton)
                .setOnClickListener(v ->
                        NavHostFragment.findNavController(this)
                                .navigate(R.id.action_login_to_signUp)
                );

        return view;
    }

    private void loginWithEmail(View view) {
        String email = ((android.widget.EditText) view.findViewById(R.id.emailEditText))
                .getText().toString().trim();
        String password = ((android.widget.EditText) view.findViewById(R.id.passwordEditText))
                .getText().toString().trim();

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(requireContext(),
                    "Please enter email and password",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(requireActivity(), task -> {
                    progressBar.setVisibility(View.GONE);
                    if (task.isSuccessful()) {
                        saveUserLocallyAndNavigate();
                    } else {
                        Toast.makeText(requireContext(),
                                "Authentication failed: " + task.getException().getMessage(),
                                Toast.LENGTH_LONG
                        ).show();
                    }
                });
    }

    private void startGoogleSignIn() {
        Intent signInIntent = googleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_GOOGLE_SIGN_IN);
    }

    @Override
    public void onActivityResult(
            int requestCode, int resultCode, @Nullable Intent data
    ) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_GOOGLE_SIGN_IN) {
            Task<GoogleSignInAccount> task =
                    GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                Toast.makeText(requireContext(),
                        "Google sign-in failed: " + e.getStatusCode(),
                        Toast.LENGTH_LONG
                ).show();
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        progressBar.setVisibility(View.VISIBLE);
        AuthCredential credential =
                GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(requireActivity(), task -> {
                    progressBar.setVisibility(View.GONE);
                    if (task.isSuccessful()) {
                        saveUserLocallyAndNavigate();
                    } else {
                        Toast.makeText(requireContext(),
                                "Firebase auth failed: " + task.getException().getMessage(),
                                Toast.LENGTH_LONG
                        ).show();
                    }
                });
    }

    private void saveUserLocallyAndNavigate() {
        // 1) Grab email and name from FirebaseAuth
        String email = mAuth.getCurrentUser().getEmail();
        String name  = mAuth.getCurrentUser().getDisplayName();

        // 2) Ensure user exists in local DB
        if (!dbHelper.checkUser(email, "")) {
            dbHelper.addUser(email, name, "");
        }

        dbHelper.seedUserExercises(email);

        // 3) Navigate to dashboard
        Toast.makeText(requireContext(),
                "Login successful!", Toast.LENGTH_SHORT
        ).show();
        NavHostFragment.findNavController(this)
                .navigate(R.id.action_login_to_dashboard);
    }
}
