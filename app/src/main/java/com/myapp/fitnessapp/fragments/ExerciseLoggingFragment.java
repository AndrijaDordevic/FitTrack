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
import android.widget.ToggleButton;
import android.widget.CompoundButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.preference.PreferenceManager;

import com.myapp.fitnessapp.R;
import com.myapp.fitnessapp.database.DBHelper;
import com.myapp.fitnessapp.models.WorkoutSet;
import com.myapp.fitnessapp.utils.UserSession;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ExerciseLoggingFragment extends Fragment {
    private int    exerciseId;
    private String exerciseName;
    private String userEmail;
    private String dayName;

    private LinearLayout layoutSets;
    private Button       btnAddSet, btnRemoveSet, btnSaveWorkout;
    private DBHelper     dbHelper;

    private ToggleButton toggleUnit;
    private boolean      useKg;

    public static ExerciseLoggingFragment newInstance(
            int    exerciseId,
            String exerciseName,
            String userEmail,
            String dayName
    ) {
        Bundle args = new Bundle();
        args.putInt("exerciseId", exerciseId);
        args.putString("exerciseName", exerciseName);
        args.putString("userEmail", userEmail);
        args.putString("dayName", dayName);

        ExerciseLoggingFragment frag = new ExerciseLoggingFragment();
        frag.setArguments(args);
        return frag;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 1) Initialize our session helper so we get a shared DBHelper and current user
        UserSession.init(requireContext());

        // 2) Ask Firebase who’s signed in right now
        userEmail = UserSession.getEmail();
        if (userEmail == null) {
            // nobody logged in → go back to Welcome
            NavHostFragment.findNavController(this)
                    .navigate(R.id.action_global_welcomeFragment);
            return;
        }

        // 3) Load your “kg” toggle from prefs
        useKg = PreferenceManager
                .getDefaultSharedPreferences(requireContext())
                .getBoolean("use_kg", false);

        // 4) Pull arguments for which exercise/day we’re logging
        Bundle args = getArguments();
        if (args != null) {
            exerciseId   = args.getInt("exerciseId", -1);
            exerciseName = args.getString("exerciseName", "Exercise");
            dayName      = args.getString("dayName", "");
        }
    }

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        View view = inflater.inflate(
                R.layout.fragment_exercise_logging, container, false
        );

        // title + unit toggle
        ((TextView)view.findViewById(R.id.textExerciseName))
                .setText(exerciseName);

        toggleUnit = view.findViewById(R.id.toggleUnit);
        toggleUnit.setChecked(useKg);
        toggleUnit.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                useKg = isChecked;
                // persist new unit
                PreferenceManager.getDefaultSharedPreferences(requireContext())
                        .edit().putBoolean("use_kg", useKg).apply();
                // rebuild every row, converting the weight values
                rebuildSetRows();
            }
        });

        // views + DB
        layoutSets     = view.findViewById(R.id.layoutSets);
        btnAddSet      = view.findViewById(R.id.btnAddSet);
        btnRemoveSet   = view.findViewById(R.id.btnRemoveSet);
        btnSaveWorkout = view.findViewById(R.id.btnSaveWorkout);
        dbHelper       = UserSession.getDbHelper();

        if (exerciseId == -1) {
            btnAddSet.setEnabled(false);
            btnRemoveSet.setEnabled(false);
            btnSaveWorkout.setEnabled(false);
        }

        // load existing from DB and convert display weight once
        List<WorkoutSet> saved =
                dbHelper.getWorkoutSetsForExercise(exerciseId, userEmail);
        layoutSets.removeAllViews();
        if (saved.isEmpty()) {
            addSetFields(1, "", "");
        } else {
            for (WorkoutSet s : saved) {
                float displayW = useKg
                        ? (s.getWeight() / 2.20462f)
                        : s.getWeight();
                String displayWStr = String.format(Locale.getDefault(), "%.1f", displayW);
                addSetFields(
                        s.getSetNumber(),
                        String.valueOf(s.getReps()),
                        displayWStr
                );
            }
        }

        // add/remove handlers
        btnAddSet.setOnClickListener(v ->
                addSetFields(layoutSets.getChildCount() + 1, "", "")
        );
        btnRemoveSet.setOnClickListener(v -> {
            int count = layoutSets.getChildCount();
            if (count > 1) {
                View last = layoutSets.getChildAt(count - 1);
                int setNum = Integer.parseInt(
                        ((TextView)last.findViewById(R.id.tvSetNumber))
                                .getText().toString().split(" ")[1]
                );
                layoutSets.removeViewAt(count - 1);
                dbHelper.deleteWorkoutSet(exerciseId, setNum, userEmail);
                // renumber remaining
                for (int i = 0; i < layoutSets.getChildCount(); i++) {
                    ((TextView)layoutSets.getChildAt(i)
                            .findViewById(R.id.tvSetNumber))
                            .setText("Set " + (i + 1));
                }
            } else {
                Toast.makeText(requireContext(),
                        "At least one set required",
                        Toast.LENGTH_SHORT
                ).show();
            }
        });

        btnSaveWorkout.setOnClickListener(v -> saveWorkout());
        return view;
    }

    /**
     * Rebuild all the existing set-rows, converting the weight text
     * from the *old* unit into the *new* unit.
     */
    private void rebuildSetRows() {
        List<String[]> data = new ArrayList<>();
        int n = layoutSets.getChildCount();
        for (int i = 0; i < n; i++) {
            View row = layoutSets.getChildAt(i);
            String repsText   = ((EditText)row.findViewById(R.id.inputReps))
                    .getText().toString();
            String weightText = ((EditText)row.findViewById(R.id.inputWeight))
                    .getText().toString();

            // convert the weightText into the newly selected unit
            String converted = "";
            if (!weightText.isEmpty()) {
                try {
                    float val = Float.parseFloat(weightText);
                    float newVal;
                    if (useKg) {
                        // was lbs, now kg
                        newVal = val / 2.20462f;
                    } else {
                        // was kg, now lbs
                        newVal = val * 2.20462f;
                    }
                    converted = String.format(Locale.getDefault(), "%.1f", newVal);
                } catch (NumberFormatException ex) {
                    // if parse fails, just keep the raw text
                    converted = weightText;
                }
            }
            data.add(new String[]{ repsText, converted });
        }

        // clear and re-inflate with converted values
        layoutSets.removeAllViews();
        if (data.isEmpty()) {
            addSetFields(1, "", "");
        } else {
            for (int i = 0; i < data.size(); i++) {
                addSetFields(i + 1,
                        data.get(i)[0],
                        data.get(i)[1]);
            }
        }
    }

    private void addSetFields(int setNumber,
                              String preReps,
                              String preWeight) {
        View row = LayoutInflater.from(getContext())
                .inflate(R.layout.item_set_entry, layoutSets, false);

        ((TextView)row.findViewById(R.id.tvSetNumber))
                .setText("Set " + setNumber);

        EditText etReps   = row.findViewById(R.id.inputReps);
        EditText etWeight = row.findViewById(R.id.inputWeight);
        etReps.setInputType(InputType.TYPE_CLASS_NUMBER);
        etWeight.setInputType(
                InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL
        );

        // update hint to match the selected unit
        etWeight.setHint(useKg ? "Weight (kg)" : "Weight (lb)");

        // pre-fill if we were passed values
        if (!preWeight.isEmpty()) {
            etWeight.setText(preWeight);
        }
        etReps.setText(preReps);
        layoutSets.addView(row);
    }

    private void saveWorkout() {
        for (int i = 0; i < layoutSets.getChildCount(); i++) {
            View row = layoutSets.getChildAt(i);
            String repsStr   = ((EditText)row.findViewById(R.id.inputReps))
                    .getText().toString().trim();
            String weightStr = ((EditText)row.findViewById(R.id.inputWeight))
                    .getText().toString().trim();
            if (repsStr.isEmpty() || weightStr.isEmpty()) continue;

            int reps = Integer.parseInt(repsStr);
            float w  = Float.parseFloat(weightStr);
            // convert back to pounds for storage if needed
            if (useKg) w *= 2.20462f;

            int setNumber = i + 1;
            if (dbHelper.hasWorkoutSet(exerciseId, setNumber, userEmail)) {
                dbHelper.updateWorkoutSet(
                        exerciseId, setNumber,
                        reps, w,
                        userEmail, dayName
                );
            } else {
                dbHelper.insertWorkoutSet(
                        exerciseId, setNumber,
                        reps, w,
                        userEmail, dayName
                );
            }
        }

        Toast.makeText(requireContext(),
                "Workout Saved!",
                Toast.LENGTH_SHORT
        ).show();
        Navigation.findNavController(requireView()).popBackStack();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        dbHelper.close();
    }
}
