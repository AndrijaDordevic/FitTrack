package com.myapp.fitnessapp.fragments;

import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.myapp.fitnessapp.R;
import com.myapp.fitnessapp.database.DBHelper;
import com.myapp.fitnessapp.models.WorkoutLogEntry;
import com.myapp.fitnessapp.utils.UserSession;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class ProgressFragment extends Fragment {
    private DBHelper     db;
    private LinearLayout categoryContainer;
    private String       userEmail;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        View view = inflater.inflate(R.layout.fragment_progress, container, false);

        // 1) Initialize shared DB helper
        UserSession.init(requireContext());
        db = UserSession.getDbHelper();

        // 2) Get current Firebase user
        FirebaseUser fb = FirebaseAuth.getInstance().getCurrentUser();
        if (fb == null || fb.getEmail() == null) {
            Toast.makeText(requireContext(),
                            "Please log in first", Toast.LENGTH_SHORT)
                    .show();
            return view;
        }
        userEmail = fb.getEmail();

        // 3) Wire up container and render
        categoryContainer = view.findViewById(R.id.categoryContainer);
        populateStats();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        populateStats();
    }

    private void populateStats() {
        // 1) Read weight unit preference from default prefs
        boolean useKg = PreferenceManager
                .getDefaultSharedPreferences(requireContext())
                .getBoolean("use_kg", false);

        // 2) Clear out old
        categoryContainer.removeAllViews();

        // 3) Loop categories
        List<String> categories = db.getAllCategories();
        for (String category : categories) {
            // — Header
            TextView header = new TextView(requireContext());
            header.setText(category);
            header.setTextSize(20f);
            header.setPadding(0, 16, 0, 8);
            TypedValue tv = new TypedValue();
            requireContext().getTheme()
                    .resolveAttribute(
                            com.google.android.material.R.attr.colorOnBackground,
                            tv, true
                    );
            header.setTextColor(tv.data);
            categoryContainer.addView(header);

            // — Fetch logs
            List<WorkoutLogEntry> entries =
                    db.getLogEntriesByCategory(category, userEmail);

            if (entries.isEmpty()) {
                TextView none = new TextView(requireContext());
                none.setText("No history for " + category);
                none.setTextSize(17f);
                none.setPadding(0, 0, 0, 16);
                none.setTextColor(tv.data);
                categoryContainer.addView(none);
                continue;
            }

            // — Aggregate
            Set<String> sessions = new HashSet<>();
            int sumReps = 0;
            float maxLbs = 0f;
            for (WorkoutLogEntry e : entries) {
                sumReps += e.getReps();
                sessions.add(e.getDayName());
                if (e.getWeight() > maxLbs) {
                    maxLbs = e.getWeight();
                }
            }
            int totalSessions = sessions.size();
            double avgReps = sumReps / (double) entries.size();

            // — Compute weight display
            float displayW = useKg
                    ? (maxLbs / 2.20462f)
                    : maxLbs;
            float dispRounded = Math.round(displayW * 10f) / 10f;
            String unit = useKg ? "kg" : "lbs";

            float estVolume = (float) avgReps * dispRounded;

            // — Build card
            CardView card = new CardView(requireContext());
            card.setRadius(8f);
            card.setCardElevation(4f);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            lp.setMargins(0, 0, 0, 16);
            card.setLayoutParams(lp);

            LinearLayout inner = new LinearLayout(requireContext());
            inner.setOrientation(LinearLayout.VERTICAL);
            inner.setPadding(12, 12, 12, 12);

            TextView tv1 = new TextView(requireContext());
            tv1.setText("Sessions: " + totalSessions);
            tv1.setTextColor(tv.data);
            inner.addView(tv1);

            TextView tv2 = new TextView(requireContext());
            tv2.setText(String.format(
                    Locale.getDefault(),
                    "Volume: %.1f %s", estVolume, unit
            ));
            tv2.setTextColor(tv.data);
            tv2.setPadding(0, 4, 0, 0);
            inner.addView(tv2);

            TextView tv3 = new TextView(requireContext());
            tv3.setText(String.format(
                    Locale.getDefault(),
                    "Avg Reps: %.1f", avgReps
            ));
            tv3.setTextColor(tv.data);
            tv3.setPadding(0, 4, 0, 0);
            inner.addView(tv3);

            TextView tv4 = new TextView(requireContext());
            tv4.setText(String.format(
                    Locale.getDefault(),
                    "Max: %.1f %s", dispRounded, unit
            ));
            tv4.setTextColor(tv.data);
            tv4.setPadding(0, 4, 0, 0);
            inner.addView(tv4);

            Button clear = new Button(requireContext());
            clear.setText("Clear History");
            clear.setPadding(0, 12, 0, 0);
            clear.setOnClickListener(v -> {
                List<Integer> ids = db.getExerciseIdsByCategory(category);
                int deleted = db.deleteLogEntriesByExerciseIds(ids, userEmail);
                Toast.makeText(requireContext(),
                        "Deleted " + deleted + " entries",
                        Toast.LENGTH_SHORT
                ).show();
                populateStats();
            });
            inner.addView(clear);

            card.addView(inner);
            categoryContainer.addView(card);
        }
    }
}
