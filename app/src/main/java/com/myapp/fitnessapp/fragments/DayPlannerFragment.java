package com.myapp.fitnessapp.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
// Use AppCompat SearchView for compatibility
import androidx.appcompat.widget.SearchView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.myapp.fitnessapp.R;
import com.myapp.fitnessapp.adapters.DayExerciseAdapter;
import com.myapp.fitnessapp.database.DBHelper;
import com.myapp.fitnessapp.models.ExerciseItem;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

public class DayPlannerFragment extends Fragment {
    private static final String PREFS_NAME = "app_prefs";
    private static final String KEY_USER_EMAIL = "user_email";

    private String dayName;
    private String userEmail;
    private DBHelper db;
    private List<ExerciseItem> allExercises;

    private DayExerciseAdapter fullAdapter;
    private DayExerciseAdapter summaryAdapter;
    private boolean isInEditMode;

    private TextView tvPlanName;
    private TextView tvPromptAdd;
    private Spinner spCategory;
    private CheckBox cbRest;
    private SearchView sv;
    private RecyclerView rv;
    private Button btnSave, btnClear;

    public static DayPlannerFragment newInstance(String dayName, String userEmail) {
        DayPlannerFragment frag = new DayPlannerFragment();
        Bundle args = new Bundle();
        args.putString("dayName", dayName);
        args.putString("userEmail", userEmail);
        frag.setArguments(args);
        return frag;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Read arguments
        Bundle args = getArguments();
        if (args != null) {
            dayName   = args.getString("dayName");
            userEmail = args.getString("userEmail");
        }
        // Fallback: read logged-in user from SharedPreferences
        SharedPreferences prefs = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        if (TextUtils.isEmpty(userEmail)) {
            userEmail = prefs.getString(KEY_USER_EMAIL, "");
        }
        // Fallback: default dayName to current day-of-week
        if (TextUtils.isEmpty(dayName)) {
            String[] days = {"SUNDAY","MONDAY","TUESDAY","WEDNESDAY","THURSDAY","FRIDAY","SATURDAY"};
            int dow = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);
            dayName = days[dow - 1];
        }
        db = new DBHelper(requireContext());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_day_planner, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Bind UI
        tvPlanName  = view.findViewById(R.id.textPlanName);
        tvPromptAdd = view.findViewById(R.id.tvPromptAdd);
        spCategory  = view.findViewById(R.id.spinnerCategoryFilter);
        cbRest      = view.findViewById(R.id.checkRest);
        sv          = view.findViewById(R.id.searchView);
        rv          = view.findViewById(R.id.recyclerExercises);
        btnSave     = view.findViewById(R.id.btnSaveDay);
        btnClear    = view.findViewById(R.id.btnClearPlanner);

        // Rename on plan title click
        tvPlanName.setOnClickListener(v -> {
            if (!isInEditMode) {
                List<Integer> selected = db.getDayPlan(userEmail, dayName);
                promptNameAndSwitch(selected);
            }
        });

        // Load exercises
        allExercises = new ArrayList<>();
        allExercises.add(new ExerciseItem(-1, "Rest", ""));

        Cursor c = db.getAllExercises();
        if (c != null && c.moveToFirst()) {
            int iId = c.getColumnIndex("id");
            int iNm = c.getColumnIndex("name");
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
        Collections.sort(allExercises, (a, b) -> {
            int cmp = a.getCategory().compareToIgnoreCase(b.getCategory());
            return cmp != 0 ? cmp : a.getName().compareToIgnoreCase(b.getName());
        });

        // Setup category spinner
        List<String> cats = new ArrayList<>();
        cats.add("All");
        Cursor cc = db.getReadableDatabase().rawQuery(
                "SELECT DISTINCT category FROM exercises", null
        );
        if (cc != null && cc.moveToFirst()) {
            int idx = cc.getColumnIndex("category");
            do {
                cats.add(cc.getString(idx));
            } while (cc.moveToNext());
            cc.close();
        }
        ArrayAdapter<String> catAd = new ArrayAdapter<>(
                requireContext(), android.R.layout.simple_spinner_item, cats
        );
        catAd.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spCategory.setAdapter(catAd);

        // RecyclerView & Adapters
        fullAdapter = new DayExerciseAdapter(allExercises);
        summaryAdapter = new DayExerciseAdapter(new ArrayList<>());
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));

        // Initial mode
        List<Integer> saved = db.getDayPlan(userEmail, dayName);
        if (saved.isEmpty()) {
            enterEditMode(saved);
        } else {
            enterSummaryMode(saved);
        }

        // Save/Edit button
        btnSave.setOnClickListener(v -> {
            if (isInEditMode) {
                List<Integer> sel = fullAdapter.getSelectedIds();
                if (cbRest.isChecked()) sel = Collections.singletonList(-1);
                db.saveDayPlan(userEmail, dayName, sel);
                promptNameAndSwitch(sel);
            } else {
                enterEditMode(db.getDayPlan(userEmail, dayName));
            }
        });

        // Clear button
        btnClear.setOnClickListener(v -> {
            db.saveDayPlan(userEmail, dayName, new ArrayList<>());
            db.savePlanName(userEmail, dayName, "");
            cbRest.setChecked(false);
            spCategory.setSelection(0);
            sv.setQuery("", false);
            enterEditMode(new ArrayList<>());
            Toast.makeText(requireContext(), dayName + " planner cleared", Toast.LENGTH_SHORT).show();
        });

        // Filters listeners
        spCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view1, int pos, long id) {
                if (isInEditMode) {
                    String cat = cats.get(pos);
                    fullAdapter.filterByCategory("All".equals(cat) ? null : cat);
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        sv.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) { return false; }
            @Override
            public boolean onQueryTextChange(String newText) {
                if (isInEditMode) fullAdapter.filterByName(newText);
                return true;
            }
        });

        cbRest.setOnCheckedChangeListener((buttonView, checked) -> {
            if (isInEditMode) fullAdapter.setRestOnly(checked);
        });
    }

    private void enterEditMode(List<Integer> pre) {
        isInEditMode = true;
        btnSave.setText("Save");
        tvPlanName.setVisibility(View.GONE);
        tvPromptAdd.setVisibility(View.VISIBLE);
        spCategory.setVisibility(View.VISIBLE);
        sv.setVisibility(View.VISIBLE);
        cbRest.setVisibility(View.VISIBLE);
        fullAdapter.setSelectionEnabled(true);
        fullAdapter.setSelectedIds(pre);
        fullAdapter.setRestOnly(pre.contains(-1));
        rv.setAdapter(fullAdapter);
    }

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
        tvPlanName.setOnClickListener(v -> promptNameAndSwitch(ids));

        spCategory.setVisibility(View.GONE);
        sv.setVisibility(View.GONE);
        cbRest.setVisibility(View.GONE);

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

    private void promptNameAndSwitch(List<Integer> selection) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Name Your Workout Plan");

        String current = db.getPlanName(userEmail, dayName);
        if (current == null) current = "";

        final EditText input = new EditText(requireContext());
        input.setHint("e.g. Push Day, Leg Blast...");
        input.setText(current);
        input.setSelection(current.length());
        builder.setView(input);

        builder.setPositiveButton("Save", (dialog, which) -> {
            String name = input.getText().toString().trim();
            if (!TextUtils.isEmpty(name)) {
                db.savePlanName(userEmail, dayName, name);
                tvPlanName.setText(name);
                Toast.makeText(requireContext(), "Plan named: " + name, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(requireContext(), "No name entered; using default", Toast.LENGTH_SHORT).show();
            }
            enterSummaryMode(selection);
        });

        builder.setNegativeButton("Skip", (dialog, which) -> enterSummaryMode(selection));
        builder.show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        db.close();
    }
}
