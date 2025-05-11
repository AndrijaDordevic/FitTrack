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

public class WelcomeFragment extends Fragment {
    private static final int RC_GOOGLE_SIGN_IN = 9001;

    private GoogleSignInClient googleSignInClient;
    private FirebaseAuth mAuth;

    public WelcomeFragment() { /* Required empty constructor */ }

    @Override
    public void onStart() {
        super.onStart();
        // 1) If Firebase has a live user, go straight in
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null) {
            goToDashboard();
            return;
        }
        // 2) Otherwise, check our UserSession
        UserSession.init(requireContext());
        String email = UserSession.getEmail();
        if (email != null) {
            goToDashboard();
        }
        // else: stay here
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_welcome, container, false);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Configure Google Sign-In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(
                GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(requireContext(), gso);

        // “Sign up” nav
        view.findViewById(R.id.btn_signup).setOnClickListener(v ->
                NavHostFragment.findNavController(this)
                        .navigate(R.id.action_welcome_to_signup)
        );

        // Google sign-in
        Button googleBtn = view.findViewById(R.id.googleWelcomeButton);
        googleBtn.setOnClickListener(v -> startGoogleSignIn());

        // “Already have an account? Log in”
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
                ds.setColor(0xFF3399FF);
                ds.setUnderlineText(true);
            }
        }, 25, text.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        haveAccount.setText(ss);
        haveAccount.setMovementMethod(LinkMovementMethod.getInstance());
        haveAccount.setHighlightColor(0);

        return view;
    }

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

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential =
                GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(requireActivity(), task -> {
                    if (task.isSuccessful()) {
                        // No need to write prefs—UserSession.getEmail() will now return a non-null value
                        goToDashboard();
                    } else {
                        Toast.makeText(requireContext(),
                                "Authentication failed: " +
                                        task.getException().getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void goToDashboard() {
        NavHostFragment.findNavController(this)
                .navigate(R.id.action_welcome_to_dashboard);
    }
}
