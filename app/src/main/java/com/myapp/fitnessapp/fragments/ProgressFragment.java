package com.myapp.fitnessapp.fragments;

import android.content.SharedPreferences;
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

import com.myapp.fitnessapp.R;
import com.myapp.fitnessapp.database.DBHelper;
import com.myapp.fitnessapp.models.WorkoutLogEntry;

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
        db = new DBHelper(requireContext());

        SharedPreferences prefs =
                PreferenceManager.getDefaultSharedPreferences(requireContext());
        userEmail = prefs.getString("user_email", "");

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
        // 1) Read the user's unit preference
        boolean useKg = PreferenceManager
                .getDefaultSharedPreferences(requireContext())
                .getBoolean("use_kg", false);

        // 2) Clear out any old views
        categoryContainer.removeAllViews();

        // 3) Loop each exercise-category
        List<String> categories = db.getAllCategories();
        for (String category : categories) {
            // --- HEADER ---
            TextView header = new TextView(requireContext());
            header.setText(category);
            header.setTextSize(20f);
            header.setPadding(0, 16, 0, 8);
            // resolve colorOnBackground
            TypedValue tv = new TypedValue();
            requireContext().getTheme()
                    .resolveAttribute(com.google.android.material.R.attr.colorOnBackground, tv, true);
            int textColor = tv.data;
            header.setTextColor(textColor);
            categoryContainer.addView(header);

            // --- FETCH LOG ENTRIES FOR THIS CATEGORY ---
            List<WorkoutLogEntry> entries =
                    db.getLogEntriesByCategory(category, userEmail);

            if (entries.isEmpty()) {
                // no history case
                TextView empty = new TextView(requireContext());
                empty.setText("No recorded history for " + category);
                empty.setTextSize(17f);
                empty.setPadding(0, 0, 0, 16);
                empty.setTextColor(textColor);
                categoryContainer.addView(empty);
                continue;
            }

            // --- AGGREGATE YOUR STATS ---
            Set<String> sessions     = new HashSet<>();
            int         sumReps      = 0;
            float       maxWeightLbs = 0f;

            for (WorkoutLogEntry e : entries) {
                sumReps      += e.getReps();
                if (e.getWeight() > maxWeightLbs) {
                    maxWeightLbs = e.getWeight();
                }
            }

            int   totalSessions = sessions.size();
            double avgReps      = sumReps / (double) entries.size();

            // --- ROUND THE DISPLAYED MAX-WEIGHT to 1 decimal ---
            float rawDisplayW = useKg
                    ? (maxWeightLbs / 2.20462f)
                    : maxWeightLbs;
            float displayMaxW = Math.round(rawDisplayW * 10f) / 10f;
            String unit       = useKg ? "kg" : "lbs";

            // --- COMPUTE estVolume = avgReps × roundedDisplayMaxW ---
            float estVolume = (float) avgReps * displayMaxW;

            // --- BUILD A CARDVIEW FOR THIS CATEGORY ---
            CardView card = new CardView(requireContext());
            card.setRadius(8f);
            card.setCardElevation(4f);
            LinearLayout.LayoutParams cardLp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            cardLp.setMargins(0, 0, 0, 16);
            card.setLayoutParams(cardLp);

            LinearLayout inner = new LinearLayout(requireContext());
            inner.setOrientation(LinearLayout.VERTICAL);
            inner.setPadding(12, 12, 12, 12);

            // total sessions
            TextView tvTotal = new TextView(requireContext());
            tvTotal.setText("Total Sessions: " + totalSessions);
            tvTotal.setTextColor(textColor);
            inner.addView(tvTotal);

            // estVolume (avg × max)
            TextView tvVol = new TextView(requireContext());
            tvVol.setText(String.format(
                    Locale.getDefault(),
                    "Volume: %.1f %s",
                    estVolume, unit));
            tvVol.setPadding(0, 4, 0, 0);
            tvVol.setTextColor(textColor);
            inner.addView(tvVol);

            // avg reps
            TextView tvAvg = new TextView(requireContext());
            tvAvg.setText(String.format(
                    Locale.getDefault(),
                    "Avg Reps: %.1f",
                    avgReps));
            tvAvg.setPadding(0, 4, 0, 0);
            tvAvg.setTextColor(textColor);
            inner.addView(tvAvg);

            // max weight (rounded)
            TextView tvMax = new TextView(requireContext());
            tvMax.setText(String.format(
                    Locale.getDefault(),
                    "Max Weight: %.1f %s",
                    displayMaxW, unit));
            tvMax.setPadding(0, 4, 0, 0);
            tvMax.setTextColor(textColor);
            inner.addView(tvMax);

            // clear history button
            Button clearBtn = new Button(requireContext());
            clearBtn.setText("Clear History");
            clearBtn.setPadding(0, 12, 0, 0);
            clearBtn.setOnClickListener(v -> {
                List<Integer> exerciseIds =
                        db.getExerciseIdsByCategory(category);
                int deleted = db.deleteLogEntriesByExerciseIds(
                        exerciseIds, userEmail);
                Toast.makeText(requireContext(),
                        "Cleared " + deleted + " entries for " + category,
                        Toast.LENGTH_SHORT).show();
                populateStats();
            });
            inner.addView(clearBtn);

            card.addView(inner);
            categoryContainer.addView(card);
        }
    }
}
