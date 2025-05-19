package com.myapp.fitnessapp.fragments;

import android.database.Cursor;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import androidx.annotation.*;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.myapp.fitnessapp.R;
import com.myapp.fitnessapp.adapters.DayExerciseAdapter;
import com.myapp.fitnessapp.database.DBHelper;
import com.myapp.fitnessapp.models.ExerciseItem;
import com.myapp.fitnessapp.utils.UserSession;

import java.util.*;

public class DayPlannerFragment extends Fragment {
    private String dayName;
    private String userEmail;
    private DBHelper db;
    private List<ExerciseItem> allExercises;

    private DayExerciseAdapter fullAdapter;
    private DayExerciseAdapter summaryAdapter;
    private boolean isInEditMode;

    private TextView tvPlanName, tvInstructions, tvPromptAdd;
    private Spinner spCategory;

    private SearchView sv;
    private RecyclerView rv;
    private Button btnSave, btnClear;

    public static DayPlannerFragment newInstance(String dayName) {
        DayPlannerFragment frag = new DayPlannerFragment();
        Bundle args = new Bundle();
        args.putString("dayName", dayName);
        frag.setArguments(args);
        return frag;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize user session and database helper
        UserSession.init(requireContext());
        db = UserSession.getDbHelper();

        // Determine signed-in user; if none, redirect to welcome screen
        userEmail = UserSession.getEmail();
        if (userEmail == null) {
            NavHostFragment.findNavController(this)
                    .navigate(R.id.action_global_welcomeFragment);
            return;
        }

        // Ensure user-specific exercises exist in the database
        db.seedUserExercises(userEmail);

        // Retrieve dayName argument or default to current weekday
        if (getArguments() != null) {
            dayName = getArguments().getString("dayName");
        }
        if (TextUtils.isEmpty(dayName)) {
            String[] days = {"SUNDAY","MONDAY","TUESDAY","WEDNESDAY",
                    "THURSDAY","FRIDAY","SATURDAY"};
            int dow = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);
            dayName = days[dow - 1];
        }

        // Load all available exercises for this user
        allExercises = new ArrayList<>();
        allExercises.add(new ExerciseItem(-1, "Rest", ""));
        Cursor c = db.getUserExercises(userEmail);
        if (c != null && c.moveToFirst()) {
            int iId  = c.getColumnIndex("id");
            int iNm  = c.getColumnIndex("name");
            int iCat = c.getColumnIndex("category");
            do {
                allExercises.add(new ExerciseItem(
                        c.getInt(iId),
                        c.getString(iNm),
                        c.getString(iCat)
                ));
            } while (c.moveToNext());
            c.close();
        }
        // Sort exercises by category then name
        Collections.sort(allExercises, (a, b) -> {
            int cmp = a.getCategory().compareToIgnoreCase(b.getCategory());
            return cmp != 0
                    ? cmp
                    : a.getName().compareToIgnoreCase(b.getName());
        });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Inflate the fragment layout
        return inflater.inflate(R.layout.fragment_day_planner,
                container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Bind UI components to their views
        tvPlanName    = view.findViewById(R.id.textPlanName);
        tvInstructions= view.findViewById(R.id.tvInstructions);
        tvPromptAdd   = view.findViewById(R.id.tvPromptAdd);
        spCategory    = view.findViewById(R.id.spinnerCategoryFilter);
        sv            = view.findViewById(R.id.searchView);
        rv            = view.findViewById(R.id.recyclerExercises);
        btnSave       = view.findViewById(R.id.btnSaveDay);
        btnClear      = view.findViewById(R.id.btnClearPlanner);

        // Allow renaming plan when tapping the title (only in summary mode)
        tvPlanName.setOnClickListener(v -> {
            if (!isInEditMode) {
                List<Integer> selected = db.getDayPlan(userEmail, dayName);
                promptNameAndSwitch(selected);
            }
        });

        // Populate category filter spinner with distinct categories
        List<String> cats = new ArrayList<>();
        cats.add("All");
        Cursor cc = db.getReadableDatabase().rawQuery(
                "SELECT DISTINCT category FROM user_exercises WHERE user_email = ?",
                new String[]{ userEmail }
        );
        if (cc != null && cc.moveToFirst()) {
            int idx = cc.getColumnIndex("category");
            do {
                cats.add(cc.getString(idx));
            } while (cc.moveToNext());
            cc.close();
        }
        ArrayAdapter<String> catAd = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                cats
        );
        catAd.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );
        spCategory.setAdapter(catAd);

        // Setup RecyclerView with adapters for edit and summary modes
        fullAdapter    = new DayExerciseAdapter(allExercises);
        summaryAdapter = new DayExerciseAdapter(new ArrayList<>());
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));

        // Enter appropriate mode based on existing saved plan
        List<Integer> saved = db.getDayPlan(userEmail, dayName);
        if (saved.isEmpty()) {
            enterEditMode(saved);
        } else {
            enterSummaryMode(saved);
        }

        // Handle save/edit button clicks
        btnSave.setOnClickListener(v -> {
            if (isInEditMode) {
                List<Integer> sel = fullAdapter.getSelectedIds();

                // Persist the selected plan
                db.saveDayPlan(userEmail, dayName, sel);

                // Log detailed workout sets
                for (int exerciseId : sel) {
                    ExerciseItem exercise = findExerciseById(exerciseId);
                    if (exercise == null) continue;

                    int sets = exercise.getSets();
                    int reps = exercise.getReps();
                    float w  = exercise.getWeight();
                    if (sets <= 0 || reps <= 0 || w <= 0f) {
                        continue; // skip incomplete entries
                    }

                    for (int sn = 1; sn <= sets; sn++) {
                        if (db.hasWorkoutSet(
                                exerciseId, sn,
                                userEmail)) {
                            db.updateWorkoutSet(
                                    exerciseId, sn,
                                    reps, w,
                                    userEmail, dayName
                            );
                        } else {
                            db.insertWorkoutSet(
                                    exerciseId, sn,
                                    reps, w,
                                    userEmail, dayName
                            );
                        }
                    }
                    // Save a log entry snapshot
                    db.saveLogEntry(
                            userEmail, exerciseId,
                            dayName, sets, reps, w
                    );
                }
                // Prompt for plan name after saving
                promptNameAndSwitch(sel);

            } else {
                // Switch to edit mode from summary
                enterEditMode(db.getDayPlan(userEmail, dayName));
            }
        });

        // Clear entire planner and reset UI
        btnClear.setOnClickListener(v -> {
            db.saveDayPlan(userEmail, dayName, new ArrayList<>());
            db.savePlanName(userEmail, dayName, "");
            spCategory.setSelection(0);
            sv.setQuery("", false);
            enterEditMode(new ArrayList<>());
            Toast.makeText(
                    requireContext(),
                    dayName + " planner cleared",
                    Toast.LENGTH_SHORT
            ).show();
        });

        // Filter exercises by category in edit mode
        spCategory.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(
                            AdapterView<?> parent,
                            View view1,
                            int pos,
                            long id
                    ) {
                        if (isInEditMode) {
                            String cat = cats.get(pos);
                            fullAdapter.filterByCategory(
                                    "All".equals(cat)
                                            ? null
                                            : cat
                            );
                        }
                    }
                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {}
                });

        // Filter exercises by name in edit mode
        sv.setOnQueryTextListener(
                new SearchView.OnQueryTextListener() {
                    @Override
                    public boolean onQueryTextSubmit(String query) {
                        return false;
                    }
                    @Override
                    public boolean onQueryTextChange(String newText) {
                        if (isInEditMode) {
                            fullAdapter.filterByName(newText);
                        }
                        return true;
                    }
                });

    }

    // Switch UI to edit mode with selection enabled
    private void enterEditMode(List<Integer> pre) {
        isInEditMode = true;
        btnSave.setText("Save");
        tvPlanName.setVisibility(View.GONE);
        tvInstructions.setVisibility(View.GONE);
        tvPromptAdd.setVisibility(View.VISIBLE);
        spCategory.setVisibility(View.VISIBLE);
        sv.setVisibility(View.VISIBLE);
        fullAdapter.setSelectionEnabled(true);
        fullAdapter.setSelectedIds(pre);
        fullAdapter.setRestOnly(pre.contains(-1));
        rv.setAdapter(fullAdapter);
    }

    // Switch UI to summary mode showing saved plan
    private void enterSummaryMode(List<Integer> ids) {
        isInEditMode = false;
        btnSave.setText("Edit");
        tvPromptAdd.setVisibility(View.GONE);

        String planName = db.getPlanName(userEmail, dayName);
        if (TextUtils.isEmpty(planName)) {
            tvPlanName.setText("Add workout name");
        } else {
            tvPlanName.setText(planName);
        }
        tvPlanName.setVisibility(View.VISIBLE);
        tvInstructions.setVisibility(View.VISIBLE);
        tvPlanName.setOnClickListener(
                v -> promptNameAndSwitch(ids)
        );

        spCategory.setVisibility(View.GONE);
        sv.setVisibility(View.GONE);

        List<ExerciseItem> sel = new ArrayList<>();
        for (int id : ids) {
            for (ExerciseItem e : allExercises) {
                if (e.getId() == id) {
                    sel.add(e);
                    break;
                }
            }
        }
        summaryAdapter = new DayExerciseAdapter(sel);
        summaryAdapter.setSelectionEnabled(false);
        rv.setAdapter(summaryAdapter);
    }

    // Prompt user to name the workout plan and switch modes
    private void promptNameAndSwitch(List<Integer> selection) {
        AlertDialog.Builder builder =
                new AlertDialog.Builder(requireContext());
        builder.setTitle("Name Your Workout Plan");

        String current = db.getPlanName(userEmail, dayName);
        if (current == null) current = "";

        final EditText input = new EditText(requireContext());
        input.setHint("e.g. Push Day, Leg Blast...");
        input.setText(current);
        input.setSelection(current.length());
        builder.setView(input);

        builder.setPositiveButton(
                "Save", (dialog, which) -> {
                    String name = input.getText()
                            .toString()
                            .trim();
                    if (!TextUtils.isEmpty(name)) {
                        db.savePlanName(
                                userEmail, dayName, name
                        );
                        tvPlanName.setText(name);
                        Toast.makeText(
                                requireContext(),
                                "Plan named: " + name,
                                Toast.LENGTH_SHORT
                        ).show();
                    } else {
                        Toast.makeText(
                                requireContext(),
                                "No name entered; using default",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                    enterSummaryMode(selection);
                });

        builder.setNegativeButton(
                "Skip",
                (dialog, which) -> enterSummaryMode(selection)
        );
        builder.show();
    }

    // Lookup an ExerciseItem by its ID
    private ExerciseItem findExerciseById(int id) {
        for (ExerciseItem item : allExercises) {
            if (item.getId() == id) {
                return item;
            }
        }
        return null;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Close database when view is destroyed to free resources
        db.close();
    }
}
