package com.myapp.fitnessapp.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.preference.PreferenceManager;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.myapp.fitnessapp.R;

import java.util.Set;

public class MainActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private TextView title;
    private NavController navController;
    private String currentUserEmail;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        // Apply saved dark mode preference
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        boolean darkMode = prefs.getBoolean("pref_dark_mode", false);
        AppCompatDelegate.setDefaultNightMode(
                darkMode ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO
        );

        // Retrieve signed-in user's email
        currentUserEmail = prefs.getString("user_email", "");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Setup toolbar
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        // Toolbar title view
        title = findViewById(R.id.toolbar_title);

        // NavController
        NavHostFragment navHost = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);
        navController = navHost.getNavController();

        // Bottom navigation
        BottomNavigationView bottomNav = findViewById(R.id.bottom_nav);
        bottomNav.setOnItemSelectedListener(item -> {
            int destId = item.getItemId();
            int currentId = navController.getCurrentDestination().getId();

            if (currentId == destId) return true;

            navController.popBackStack(R.id.dashboardFragment, false);

            if (destId == R.id.dayPlannerFragment) {
                // Pass the current user's email to DayPlannerFragment
                Bundle args = new Bundle();
                args.putString("userEmail", currentUserEmail);
                navController.navigate(destId, args);
            } else {
                navController.navigate(destId);
            }
            return true;
        });

        // Show/hide UI elements on destination change
        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            boolean hide = destination.getId() == R.id.welcomeFragment
                    || destination.getId() == R.id.signUpFragment
                    || destination.getId() == R.id.loginFragment;

            findViewById(R.id.appbar).setVisibility(hide ? View.GONE : View.VISIBLE);
            bottomNav.setVisibility(hide ? View.GONE : View.VISIBLE);
            title.setVisibility(hide ? View.GONE : View.VISIBLE);

            Set<Integer> topLevel = Set.of(
                    R.id.dashboardFragment,
                    R.id.profileFragment,
                    R.id.settingsFragment
            );
            if (!hide && !topLevel.contains(destination.getId())) {
                toolbar.setNavigationIcon(R.drawable.back_arrow);
                toolbar.setNavigationOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());
            } else {
                toolbar.setNavigationIcon(null);
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        return navController.navigateUp() || super.onSupportNavigateUp();
    }
}
