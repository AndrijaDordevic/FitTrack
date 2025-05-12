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
import androidx.navigation.NavGraph;
import androidx.navigation.fragment.NavHostFragment;
import androidx.preference.PreferenceManager;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.myapp.fitnessapp.R;
import com.myapp.fitnessapp.utils.UserSession;

import java.util.Set;

public class MainActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private TextView title;
    private NavController navController;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 0) Init session helper
        UserSession.init(this);

        // 0a) Check Firebase + local session
        FirebaseUser fbUser    = FirebaseAuth.getInstance().getCurrentUser();
        String       localEmail = UserSession.getEmail();  // "" if none saved

        // 1) Inflate layout
        setContentView(R.layout.activity_main);

        // 2) Grab NavController
        NavHostFragment navHost = (NavHostFragment)
                getSupportFragmentManager()
                        .findFragmentById(R.id.nav_host_fragment);
        navController = navHost.getNavController();

        // ==== ONLY on a fresh launch do we override the startDestination ====
        if (savedInstanceState == null) {
            // 3) Inflate the one XML nav-graph
            NavGraph graph = navController.getNavInflater()
                    .inflate(R.navigation.nav_graph);

            // 4) Decide start destination at runtime
            boolean signedIn = (fbUser != null && localEmail != null && !localEmail.isEmpty());
            int startDest = signedIn
                    ? R.id.dashboardFragment
                    : R.id.welcomeFragment;

            // 5) Override and set the graph
            graph.setStartDestination(startDest);
            navController.setGraph(graph);
        }
        // =======================================================================

        // 7) Show/hide appbar & bottom-nav based on signed-in state
        boolean showAppChrome = (fbUser != null && localEmail != null && !localEmail.isEmpty());
        findViewById(R.id.appbar)
                .setVisibility(showAppChrome ? View.VISIBLE : View.GONE);
        BottomNavigationView bottomNav = findViewById(R.id.bottom_nav);

        bottomNav.setVisibility(showAppChrome ? View.VISIBLE : View.GONE);

        // 5) Apply saved dark-mode
        SharedPreferences prefs =
                PreferenceManager.getDefaultSharedPreferences(this);
        boolean darkMode = prefs.getBoolean("pref_dark_mode", false);
        AppCompatDelegate.setDefaultNightMode(
                darkMode
                        ? AppCompatDelegate.MODE_NIGHT_YES
                        : AppCompatDelegate.MODE_NIGHT_NO
        );

        // 6) Seed exercises only if signed in
        if (showAppChrome) {
            UserSession.getDbHelper().seedUserExercises(localEmail);
        }

        // 7) Toolbar setup
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        title = findViewById(R.id.toolbar_title);

        // 8) Bottom-nav wiring
        bottomNav.setOnItemSelectedListener(item -> {
            int destId    = item.getItemId();
            int currentId = navController.getCurrentDestination().getId();
            if (currentId == destId) return true;
            navController.popBackStack(R.id.dashboardFragment, false);
            navController.navigate(destId);
            return true;
        });

        // 9) Show/hide back-arrow and appbar based on destination
        navController.addOnDestinationChangedListener((c, dest, args) -> {
            boolean hide = dest.getId() == R.id.welcomeFragment
                    || dest.getId() == R.id.loginFragment
                    || dest.getId() == R.id.signUpFragment
                    || dest.getId() == R.id.profileSetupFragment;

            findViewById(R.id.appbar)
                    .setVisibility(hide ? View.GONE : View.VISIBLE);
            bottomNav.setVisibility(hide ? View.GONE : View.VISIBLE);
            title.setVisibility(hide ? View.GONE : View.VISIBLE);

            Set<Integer> topLevel = Set.of(
                    R.id.dashboardFragment,
                    R.id.profileFragment,
                    R.id.settingsFragment
            );
            if (!hide && !topLevel.contains(dest.getId())) {
                toolbar.setNavigationIcon(R.drawable.back_arrow);
                toolbar.setNavigationOnClickListener(
                        v -> getOnBackPressedDispatcher().onBackPressed()
                );
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
