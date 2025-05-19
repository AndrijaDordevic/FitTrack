package com.myapp.fitnessapp.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.myapp.fitnessapp.R;
import com.myapp.fitnessapp.utils.UserSession;

/** @noinspection ALL*/
public class WelcomeFragment extends Fragment {
    private static final int RC_GOOGLE_SIGN_IN = 9001;

    private GoogleSignInClient googleSignInClient;
    private FirebaseAuth mAuth;

    public WelcomeFragment() { /* Required empty constructor */ }

    @Override
    public void onStart() {
        super.onStart();
        // If a Firebase user is already signed in, navigate to dashboard
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null) {
            goToDashboard();
            return;
        }
        // Otherwise, check local session for stored email
        UserSession.init(requireContext());
        String email = UserSession.getEmail();
        if (email != null) {
            goToDashboard();
        }
        // Else: stay on welcome screen
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate layout
        View view = inflater.inflate(R.layout.fragment_welcome, container, false);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Configure Google Sign-In client
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(
                GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(requireContext(), gso);

        // Navigate to sign-up when sign-up button clicked
        view.findViewById(R.id.btn_signup).setOnClickListener(v ->
                NavHostFragment.findNavController(this)
                        .navigate(R.id.action_welcome_to_signup)
        );

        // Handle Google sign-in invocation
        Button googleBtn = view.findViewById(R.id.googleWelcomeButton);
        googleBtn.setOnClickListener(v -> startGoogleSignIn());

        // Make "Log in" text clickable to navigate to login screen
        TextView haveAccount = view.findViewById(R.id.tv_have_account);
        String text = "Already have an account? Log in";
        SpannableString ss = new SpannableString(text);
        ss.setSpan(new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                NavHostFragment.findNavController(WelcomeFragment.this)
                        .navigate(R.id.action_welcome_to_login);
            }
            @Override
            public void updateDrawState(@NonNull TextPaint ds) {
                super.updateDrawState(ds);
                ds.setColor(0xFF3399FF); // Link color
                ds.setUnderlineText(true);
            }
        }, 25, text.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        haveAccount.setText(ss);
        haveAccount.setMovementMethod(LinkMovementMethod.getInstance());
        haveAccount.setHighlightColor(0);

        return view;
    }

    // Launch Google Sign-In flow
    private void startGoogleSignIn() {
        startActivityForResult(
                googleSignInClient.getSignInIntent(),
                RC_GOOGLE_SIGN_IN
        );
    }

    @Override
    public void onActivityResult(int requestCode,
                                 int resultCode,
                                 @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_GOOGLE_SIGN_IN) {
            // Handle Google Sign-In result
            Task<GoogleSignInAccount> task =
                    GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                if (account != null) {
                    firebaseAuthWithGoogle(account.getIdToken());
                }
            } catch (ApiException e) {
                Toast.makeText(requireContext(),
                        "Google sign-in failed: " + e.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Authenticate with Firebase using Google credentials
    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential =
                GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(requireActivity(), task -> {
                    if (task.isSuccessful()) {
                        // Successful sign-in to navigate
                        goToDashboard();
                    } else {
                        Toast.makeText(requireContext(),
                                "Authentication failed: " +
                                        task.getException().getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // Helper to navigate to dashboard
    private void goToDashboard() {
        NavHostFragment.findNavController(this)
                .navigate(R.id.action_welcome_to_dashboard);
    }
}
