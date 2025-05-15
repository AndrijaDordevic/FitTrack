package com.myapp.fitnessapp.fragments;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.InputType;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.UserInfo;
import com.myapp.fitnessapp.R;
import com.myapp.fitnessapp.activities.MainActivity;
import com.myapp.fitnessapp.database.DBHelper;


public class SettingsFragment extends PreferenceFragmentCompat {
    private static final int RC_REAUTH = 9002;
    private FirebaseAuth mAuth;
    private GoogleSignInClient googleSignInClient;
    private Runnable reauthSuccessAction;

    @Override
    public void onCreatePreferences(@Nullable Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.fragment_settings, rootKey);
        // Initialize Firebase auth and Google client for re-auth scenarios
        mAuth = FirebaseAuth.getInstance();
        googleSignInClient = GoogleSignIn.getClient(
                requireContext(),
                new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestIdToken(getString(R.string.default_web_client_id))
                        .requestEmail()
                        .build()
        );

        // Dark mode toggle: persist preference and recreate activity
        findPreference("pref_dark_mode_toggle").setOnPreferenceClickListener(pref -> {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(requireContext());
            boolean dark = prefs.getBoolean("pref_dark_mode", false);
            prefs.edit().putBoolean("pref_dark_mode", !dark).apply();
            AppCompatDelegate.setDefaultNightMode(
                    dark ? AppCompatDelegate.MODE_NIGHT_NO : AppCompatDelegate.MODE_NIGHT_YES
            );
            requireActivity().recreate();
            return true;
        });

        // Logout: sign out and clear back stack
        findPreference("pref_logout").setOnPreferenceClickListener(pref -> {
            mAuth.signOut();
            googleSignInClient.signOut();
            clearSavedEmail();
            Intent intent = new Intent(requireContext(), MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                    Intent.FLAG_ACTIVITY_NEW_TASK |
                    Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            requireActivity().finish();
            return true;
        });

        // Delete account: confirm and handle re-auth if needed
        findPreference("pref_delete_account").setOnPreferenceClickListener(pref -> {
            FirebaseUser user = mAuth.getCurrentUser();
            if (user == null || user.getEmail() == null) {
                Toast.makeText(getContext(), "No user signed in", Toast.LENGTH_SHORT).show();
            } else {
                showDeleteDialog(user);
            }
            return true;
        });

        // Password reset: send email
        findPreference("pref_change_password").setOnPreferenceClickListener(pref -> {
            FirebaseUser user = mAuth.getCurrentUser();
            if (user != null && user.getEmail() != null) {
                mAuth.sendPasswordResetEmail(user.getEmail())
                        .addOnCompleteListener(t -> Toast.makeText(
                                getContext(),
                                t.isSuccessful() ? "Reset link sent" : "Failed: " + t.getException().getMessage(),
                                Toast.LENGTH_SHORT
                        ).show());
            }
            return true;
        });
    }

    // Show dialog with countdown before allowing delete
    private void showDeleteDialog(FirebaseUser user) {
        AlertDialog dlg = new AlertDialog.Builder(requireContext())
                .setTitle("Delete account?")
                .setMessage("This is irreversible.\nPlease wait 5 seconds.")
                .setPositiveButton("Delete", null)
                .setNegativeButton("Cancel", (d, w) -> d.dismiss())
                .create();
        dlg.setOnShowListener(dialog -> {
            Button btn = dlg.getButton(AlertDialog.BUTTON_POSITIVE);
            btn.setEnabled(false);
            // Countdown to prevent accidental clicks
            new CountDownTimer(5000, 1000) {
                int sec = 5;
                @Override public void onTick(long m) { btn.setText("Delete (" + sec-- + ")"); }
                @Override public void onFinish() {
                    btn.setText("Delete");
                    btn.setEnabled(true);
                    btn.setOnClickListener(v -> attemptDelete(user, dlg));
                }
            }.start();
        });
        dlg.show();
    }

    // Attempt deletion; handle re-auth if required
    private void attemptDelete(FirebaseUser user, AlertDialog dlg) {
        user.delete().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                finalizeDeletion(user.getEmail());
                dlg.dismiss();
            } else {
                Exception e = task.getException();
                if (e instanceof FirebaseAuthRecentLoginRequiredException) {
                    // Need to re-authenticate
                    reauthSuccessAction = () -> showDeleteDialog(mAuth.getCurrentUser());
                    promptReauth();
                } else {
                    Toast.makeText(getContext(), "Delete failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    // Prompt user for credentials or Google to re-authenticate
    private void promptReauth() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) return;
        boolean hasPassword = false;
        for (UserInfo info : user.getProviderData()) {
            if (EmailAuthProvider.PROVIDER_ID.equals(info.getProviderId())) {
                hasPassword = true;
                break;
            }
        }
        if (hasPassword) {
            // Ask for password
            EditText pwd = new EditText(requireContext());
            pwd.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            pwd.setHint("Password");
            new AlertDialog.Builder(requireContext())
                    .setTitle("Re-enter Password")
                    .setView(pwd)
                    .setPositiveButton("OK", (d, w) -> {
                        AuthCredential cred = EmailAuthProvider.getCredential(user.getEmail(), pwd.getText().toString());
                        user.reauthenticate(cred).addOnCompleteListener(r -> {
                            if (r.isSuccessful() && reauthSuccessAction != null) reauthSuccessAction.run();
                            else Toast.makeText(getContext(), "Re-auth failed: " + r.getException().getMessage(), Toast.LENGTH_LONG).show();
                        });
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        } else {
            // Use Google sign-in
            startActivityForResult(googleSignInClient.getSignInIntent(), RC_REAUTH);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_REAUTH) {
            try {
                GoogleSignInAccount acct = GoogleSignIn.getSignedInAccountFromIntent(data).getResult(ApiException.class);
                AuthCredential cred = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
                FirebaseUser user = mAuth.getCurrentUser();
                if (user != null && reauthSuccessAction != null) {
                    user.reauthenticate(cred).addOnCompleteListener(r -> {
                        if (r.isSuccessful()) reauthSuccessAction.run();
                        else Toast.makeText(getContext(), "Re-auth failed", Toast.LENGTH_SHORT).show();
                    });
                }
            } catch (ApiException ex) {
                Toast.makeText(getContext(), "Google sign-in error", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Cleanup local and remote data, then navigate
    private void finalizeDeletion(String email) {
        mAuth.signOut();
        googleSignInClient.signOut();
        googleSignInClient.revokeAccess();
        new DBHelper(requireContext()).deleteUser(email);
        clearSavedEmail();
        Toast.makeText(getContext(), "Account fully deleted", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(requireContext(), MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        requireActivity().finish();
    }

    // Remove stored email from preferences
    private void clearSavedEmail() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(requireContext());
        prefs.edit().remove("user_email").apply();
    }
}
