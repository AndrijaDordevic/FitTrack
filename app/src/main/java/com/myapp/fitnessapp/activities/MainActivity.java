package com.myapp.fitnessapp.activities;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
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
    private ImageView backButton;
    private TextView title;
    private NavController navController;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 1) Draw behind both bars, then make the nav bar transparent
        View decor = getWindow().getDecorView();
        decor.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
        );
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        // Remove any translucent flags so we can set true transparency
        getWindow().clearFlags(
                WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS |
                        WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION
        );
        // Status bar: transparent (you still see notifications)
        getWindow().setStatusBarColor(Color.TRANSPARENT);
        // Navigation bar: transparent background, only buttons remain
        getWindow().setNavigationBarColor(Color.TRANSPARENT);

        // 2) Init your session helper
        UserSession.init(this);

        // 3) Check Firebase + local session
        FirebaseUser fbUser  = FirebaseAuth.getInstance().getCurrentUser();
        String    localEmail = UserSession.getEmail();  // "" if none saved

        // 4) Inflate your main layout
        setContentView(R.layout.activity_main);

        // 5) NavController setup & dynamic start destination
        NavHostFragment navHost = (NavHostFragment)
                getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        navController = navHost.getNavController();

        if (savedInstanceState == null) {
            NavGraph graph = navController.getNavInflater().inflate(R.navigation.nav_graph);
            boolean signedIn = fbUser != null && localEmail != null && !localEmail.isEmpty();
            graph.setStartDestination(
                    signedIn ? R.id.dashboardFragment : R.id.welcomeFragment
            );
            navController.setGraph(graph);
        }

        // 6) Show/hide appbar & bottom-nav based on signed-in state
        View appbar = findViewById(R.id.appbar);
        BottomNavigationView bottomNav = findViewById(R.id.bottom_nav);
        boolean showAppChrome = fbUser != null && localEmail != null && !localEmail.isEmpty();
        appbar.setVisibility(showAppChrome ? View.VISIBLE : View.GONE);
        bottomNav.setVisibility(showAppChrome ? View.VISIBLE : View.GONE);

        // 7) Apply saved dark-mode
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        boolean darkMode = prefs.getBoolean("pref_dark_mode", false);
        AppCompatDelegate.setDefaultNightMode(
                darkMode
                        ? AppCompatDelegate.MODE_NIGHT_YES
                        : AppCompatDelegate.MODE_NIGHT_NO
        );

        // 8) Seed exercises if signed in
        if (showAppChrome) {
            UserSession.getDbHelper().seedUserExercises(localEmail);
        }

        // 9) Toolbar + custom back-button wiring
        toolbar    = findViewById(R.id.toolbar);
        backButton = findViewById(R.id.toolbar_back);
        title      = findViewById(R.id.toolbar_title);

        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        // 10) Bottom-nav item selection
        bottomNav.setOnItemSelectedListener(item -> {
            int destId    = item.getItemId();
            int currentId = navController.getCurrentDestination().getId();
            if (currentId == destId) return true;
            navController.popBackStack(R.id.dashboardFragment, false);
            navController.navigate(destId);
            return true;
        });

        // 11) Destination change listener for appbar & back-button
        Set<Integer> topLevel = Set.of(
                R.id.dashboardFragment,
                R.id.profileFragment,
                R.id.settingsFragment
        );
        navController.addOnDestinationChangedListener((nc, dest, args) -> {
            // hide app chrome on auth flows
            boolean hideAppbar = dest.getId() == R.id.welcomeFragment
                    || dest.getId() == R.id.loginFragment
                    || dest.getId() == R.id.signUpFragment
                    || dest.getId() == R.id.profileSetupFragment;

            appbar.setVisibility(hideAppbar ? View.GONE : View.VISIBLE);
            bottomNav.setVisibility(hideAppbar ? View.GONE : View.VISIBLE);
            title.setVisibility(hideAppbar ? View.GONE : View.VISIBLE);

            // show custom back-button when not top-level
            backButton.setVisibility(
                    !hideAppbar && !topLevel.contains(dest.getId())
                            ? View.VISIBLE
                            : View.GONE
            );
        });

        // 12) Back-button click
        backButton.setOnClickListener(v -> onSupportNavigateUp());
    }

    @Override
    public boolean onSupportNavigateUp() {
        return navController.navigateUp() || super.onSupportNavigateUp();
    }
}
