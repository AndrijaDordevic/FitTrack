package com.myapp.fitnessapp.fragments;

import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.myapp.fitnessapp.R;
import com.myapp.fitnessapp.database.DBHelper;
import com.myapp.fitnessapp.models.WorkoutSet;

import java.util.List;

public class ExerciseLoggingFragment extends Fragment {
    private int exerciseId;
    private String exerciseName;
    private String userEmail;  // current user's email

    private LinearLayout layoutSets;
    private Button btnAddSet, btnRemoveSet, btnSaveWorkout;
    private DBHelper dbHelper;

    public interface OnWorkoutSaveListener {
        void onWorkoutSaved(int exerciseId);
    }

    private OnWorkoutSaveListener saveListener;
    public static ExerciseLoggingFragment newInstance(
            int exerciseId,
            String exerciseName,
            String userEmail
    ) {
        Bundle args = new Bundle();
        args.putInt("exerciseId", exerciseId);
        args.putString("exerciseName", exerciseName);
        args.putString("userEmail", userEmail);
        ExerciseLoggingFragment frag = new ExerciseLoggingFragment();
        frag.setArguments(args);
        return frag;
    }

    @Nullable @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        View view = inflater.inflate(R.layout.fragment_exercise_logging, container, false);

        // 1) Read arguments
        Bundle args = getArguments();
        if (args != null) {
            exerciseId   = args.getInt("exerciseId", -1);
            exerciseName = args.getString("exerciseName", "Exercise");
            userEmail    = args.getString("userEmail", "");
        }

        // 2) Show exercise name
        TextView tvName = view.findViewById(R.id.textExerciseName);
        tvName.setText(exerciseName);

        // 3) Find views and init DB
        layoutSets     = view.findViewById(R.id.layoutSets);
        btnAddSet      = view.findViewById(R.id.btnAddSet);
        btnRemoveSet   = view.findViewById(R.id.btnRemoveSet);
        btnSaveWorkout = view.findViewById(R.id.btnSaveWorkout);
        dbHelper       = new DBHelper(requireContext());

        // Disable add/remove for "Rest" exercise (id == -1)
        if (exerciseId == -1) {
            btnAddSet.setEnabled(false);
            btnRemoveSet.setEnabled(false);
            btnSaveWorkout.setEnabled(false);
        }

        // 4) Load existing sets; if none, show one blank row
        List<WorkoutSet> saved = dbHelper.getWorkoutSetsForExercise(exerciseId, userEmail);
        if (saved.isEmpty()) {
            addSetFields(1, "", "");
        } else {
            for (WorkoutSet s : saved) {
                addSetFields(
                        s.getSetNumber(),
                        String.valueOf(s.getReps()),
                        String.valueOf(s.getWeight())
                );
            }
        }

        // 5) Add set button listener
        btnAddSet.setOnClickListener(v -> {
            int nextSet = layoutSets.getChildCount() + 1;
            addSetFields(nextSet, "", "");
        });

        // 6) Remove set button listener
        btnRemoveSet.setOnClickListener(v -> {
            int count = layoutSets.getChildCount();
            if (count > 1) {
                // 1) Figure out which set number we’re deleting
                View lastRow = layoutSets.getChildAt(count - 1);
                TextView tv       = lastRow.findViewById(R.id.tvSetNumber);
                int setNumber;
                try {
                    setNumber = Integer.parseInt(tv.getText().toString().split(" ")[1]);
                } catch (Exception ex) {
                    setNumber = count;  // fallback
                }

                // 2) Remove from UI
                layoutSets.removeViewAt(count - 1);

                // 3) Remove from DB
                dbHelper.deleteWorkoutSet(exerciseId, setNumber, userEmail);

            } else {
                Toast.makeText(requireContext(), "At least one set required", Toast.LENGTH_SHORT).show();
            }
        });


        // 7) Save button listener
        btnSaveWorkout.setOnClickListener(v -> saveWorkout());

        return view;
    }

    /**
     * Inflates one row of inputs: Set #, Reps, Weight.
     */
    private void addSetFields(int setNumber, String preReps, String preWeight) {
        View row = LayoutInflater.from(getContext())
                .inflate(R.layout.item_set_entry, layoutSets, false);

        TextView tvSetNumber = row.findViewById(R.id.tvSetNumber);
        tvSetNumber.setText("Set " + setNumber);

        EditText etReps   = row.findViewById(R.id.inputReps);
        EditText etWeight = row.findViewById(R.id.inputWeight);

        etReps.setInputType(InputType.TYPE_CLASS_NUMBER);
        etWeight.setInputType(
                InputType.TYPE_CLASS_NUMBER |
                        InputType.TYPE_NUMBER_FLAG_DECIMAL
        );

        etReps.setText(preReps);
        etWeight.setText(preWeight);

        layoutSets.addView(row);
    }

    /** Reads every row and upserts into workout_sets table */
    private void saveWorkout() {
        int rowCount = layoutSets.getChildCount();
        for (int i = 0; i < rowCount; i++) {
            View row    = layoutSets.getChildAt(i);
            EditText er = row.findViewById(R.id.inputReps);
            EditText ew = row.findViewById(R.id.inputWeight);
            TextView tv = row.findViewById(R.id.tvSetNumber);

            String repsStr   = er.getText().toString().trim();
            String weightStr = ew.getText().toString().trim();
            if (repsStr.isEmpty() || weightStr.isEmpty()) {
                continue;
            }

            int reps     = Integer.parseInt(repsStr);
            float weight = Float.parseFloat(weightStr);
            int setNumber;
            try {
                String[] parts = tv.getText().toString().split(" ");
                setNumber = Integer.parseInt(parts[1]);
            } catch (Exception ex) {
                setNumber = i + 1;
            }

            if (dbHelper.hasWorkoutSet(exerciseId, setNumber, userEmail)) {
                dbHelper.updateWorkoutSet(exerciseId, setNumber, reps, weight, userEmail);
            } else {
                dbHelper.insertWorkoutSet(exerciseId, setNumber, reps, weight, userEmail);
            }
        }

        Toast.makeText(requireContext(), "Workout Saved!", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        dbHelper.close();
    }
}
