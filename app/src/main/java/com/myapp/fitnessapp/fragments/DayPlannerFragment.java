package com.myapp.fitnessapp.fragments;

import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.myapp.fitnessapp.R;
import com.myapp.fitnessapp.adapters.DayExerciseAdapter;
import com.myapp.fitnessapp.database.DBHelper;
import com.myapp.fitnessapp.models.ExerciseItem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DayPlannerFragment extends Fragment {
    private String dayName;
    private DBHelper db;
    private List<ExerciseItem> allExercises;

    private DayExerciseAdapter fullAdapter;
    private DayExerciseAdapter summaryAdapter;
    private boolean isInEditMode;

    private Spinner spCategory;
    private CheckBox cbRest;
    private SearchView sv;
    private RecyclerView rv;
    private Button btnSave;

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
        if (getArguments() != null) {
            dayName = getArguments().getString("dayName");
        }
        db = new DBHelper(requireContext());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_day_planner, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        spCategory = view.findViewById(R.id.spinnerCategoryFilter);
        cbRest     = view.findViewById(R.id.checkRest);
        sv         = view.findViewById(R.id.searchView);
        rv         = view.findViewById(R.id.recyclerExercises);
        btnSave    = view.findViewById(R.id.btnSaveDay);

        // Load exercises + "Rest"
        allExercises = new ArrayList<>();
        allExercises.add(new ExerciseItem(-1, "Rest", ""));
        Cursor c = db.getAllExercises();
        if (c != null && c.moveToFirst()) {
            int idxId = c.getColumnIndex("id");
            int idxName = c.getColumnIndex("name");
            int idxCat = c.getColumnIndex("category");
            do {
                allExercises.add(new ExerciseItem(
                        c.getInt(idxId),
                        c.getString(idxName),
                        c.getString(idxCat)
                ));
            } while (c.moveToNext());
            c.close();
        }
        Collections.sort(allExercises, (a, b) -> {
            int cmp = a.getCategory().compareToIgnoreCase(b.getCategory());
            return cmp != 0 ? cmp : a.getName().compareToIgnoreCase(b.getName());
        });

        // Category spinner
        List<String> categories = new ArrayList<>();
        categories.add("All");
        Cursor catCur = db.getReadableDatabase()
                .rawQuery("SELECT DISTINCT category FROM exercises", null);
        if (catCur != null && catCur.moveToFirst()) {
            int idx = catCur.getColumnIndex("category");
            do { categories.add(catCur.getString(idx)); }
            while (catCur.moveToNext());
            catCur.close();
        }
        ArrayAdapter<String> catAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                categories
        );
        catAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spCategory.setAdapter(catAdapter);

        // RecyclerView + Adapters
        fullAdapter = new DayExerciseAdapter(allExercises);
        summaryAdapter = new DayExerciseAdapter(new ArrayList<>());
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));

        // Initial mode
        List<Integer> saved = db.getDayPlan(dayName);
        if (saved.isEmpty()) enterEditMode(saved);
        else                enterSummaryMode(saved);

        btnSave.setOnClickListener(v -> {
            if (isInEditMode) {
                List<Integer> sel = fullAdapter.getSelectedIds();
                if (cbRest.isChecked()) sel = Collections.singletonList(-1);
                db.saveDayPlan(dayName, sel);
                Toast.makeText(requireContext(), dayName + " saved", Toast.LENGTH_SHORT).show();
                enterSummaryMode(sel);
            } else {
                enterEditMode(db.getDayPlan(dayName));
            }
        });

        Button btnClear = view.findViewById(R.id.btnClearPlanner);
        btnClear.setOnClickListener(v -> {
            // 1) Wipe the DB
            List<Integer> empty = new ArrayList<>();
            db.saveDayPlan(dayName, empty);

            // 2) Reset the UI
            cbRest.setChecked(false);           // uncheck Rest Day
            spCategory.setSelection(0);         // go back to "All"
            sv.setQuery("", false);             // clear the SearchView
            fullAdapter.setRestOnly(false);     // show all exercises again
            fullAdapter.setSelectedIds(empty);  // no selections
            enterEditMode(empty);               // switch into edit mode on an empty plan

            Toast.makeText(requireContext(),
                    dayName + " planner cleared", Toast.LENGTH_SHORT).show();
        });

        spCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> p, View vw, int pos, long id) {
                if (isInEditMode) {
                    String cat = categories.get(pos);
                    fullAdapter.filterByCategory("All".equals(cat) ? null : cat);
                }
            }
            @Override public void onNothingSelected(AdapterView<?> p) {}
        });

        sv.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override public boolean onQueryTextSubmit(String q) { return false; }
            @Override public boolean onQueryTextChange(String txt) {
                if (isInEditMode) fullAdapter.filterByName(txt);
                return true;
            }
        });

        cbRest.setOnCheckedChangeListener((btn, chk) -> {
            if (isInEditMode) fullAdapter.setRestOnly(chk);
        });
    }

    private void enterEditMode(List<Integer> pre) {
        isInEditMode = true;
        btnSave.setText("Save");
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
        spCategory.setVisibility(View.GONE);
        sv.setVisibility(View.GONE);
        cbRest.setVisibility(View.GONE);
        List<ExerciseItem> sel = new ArrayList<>();
        for (int id : ids) {
            for (ExerciseItem e : allExercises) {
                if (e.getId() == id) { sel.add(e); break; }
            }
        }
        summaryAdapter = new DayExerciseAdapter(sel);
        summaryAdapter.setSelectionEnabled(false);
        rv.setAdapter(summaryAdapter);
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        db.close();
    }
}
