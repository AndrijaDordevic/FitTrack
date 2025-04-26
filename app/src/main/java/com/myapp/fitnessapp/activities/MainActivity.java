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

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        // 🌙 Apply saved dark mode preference before anything else
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        boolean darkMode = prefs.getBoolean("pref_dark_mode", false);
        AppCompatDelegate.setDefaultNightMode(
                darkMode ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO
        );

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Setup toolbar
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        // Toolbar title
        title = findViewById(R.id.toolbar_title);

        // NavController
        NavHostFragment navHost = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);
        navController = navHost.getNavController();

        // Bottom navigation
        BottomNavigationView bottomNav = findViewById(R.id.bottom_nav);

        // Intercept tab selections
        bottomNav.setOnItemSelectedListener(item -> {
            int destId = item.getItemId();
            int currentId = navController.getCurrentDestination().getId();

            if (currentId == destId) return true;

            navController.popBackStack(R.id.dashboardFragment, false);
            navController.navigate(destId);
            return true;
        });

        // Destination change listener for custom UI control
        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            boolean hide = destination.getId() == R.id.welcomeFragment
                    || destination.getId() == R.id.signUpFragment
                    || destination.getId() == R.id.loginFragment;

            // Hide or show appbar and bottom nav
            findViewById(R.id.appbar).setVisibility(hide ? View.GONE : View.VISIBLE);
            bottomNav.setVisibility(hide ? View.GONE : View.VISIBLE);
            title.setVisibility(hide ? View.GONE : View.VISIBLE);

            // Show/hide back button based on top-level destinations
            Set<Integer> topLevelDestinations = Set.of(
                    R.id.dashboardFragment,
                    R.id.profileFragment,
                    R.id.settingsFragment
            );

            if (!hide && !topLevelDestinations.contains(destination.getId())) {
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
