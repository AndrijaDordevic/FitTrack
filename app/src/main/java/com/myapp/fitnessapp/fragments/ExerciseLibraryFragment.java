package com.myapp.fitnessapp.fragments;

import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.myapp.fitnessapp.R;
import com.myapp.fitnessapp.database.DBHelper;
import com.myapp.fitnessapp.utils.UserSession;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ExerciseLibraryFragment extends Fragment {

    private Spinner filterSpinner;
    private RecyclerView recyclerView;
    private ExerciseAdapter adapter;
    private DBHelper dbHelper;
    private TextView bonusTipMessage;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_exercise_library, container, false);

        // Initialize filter UI and recycler view
        filterSpinner = view.findViewById(R.id.spinnerFilter);
        recyclerView = view.findViewById(R.id.recyclerViewExercises);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new ExerciseAdapter();
        recyclerView.setAdapter(adapter);

        // Bonus tip message shown after an exercise is clicked
        bonusTipMessage = view.findViewById(R.id.textBonusTipMessage);

        // Setup category spinner with predefined categories
        List<String> cats = Arrays.asList(
                "All", "Bicep", "Tricep", "Legs",
                "Shoulders", "Back", "Chest", "Abs"
        );
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                cats
        );
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        filterSpinner.setAdapter(spinnerAdapter);

        // Load exercises when a category is selected
        filterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                loadExercises(cats.get(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // No action needed
            }
        });

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize user session and database helper
        UserSession.init(requireContext());
        dbHelper = UserSession.getDbHelper();
    }

    // Fetch exercises from DB, filtering by category and include tips
    private void loadExercises(String category) {
        Cursor c = "All".equals(category)
                ? dbHelper.getAllExercisesWithTips()
                : dbHelper.getExercisesByCategoryWithTips(category);

        List<Exercise> list = new ArrayList<>();
        while (c.moveToNext()) {
            list.add(new Exercise(
                    c.getInt(c.getColumnIndexOrThrow("id")),
                    c.getString(c.getColumnIndexOrThrow("name")),
                    c.getString(c.getColumnIndexOrThrow("category")),
                    c.getString(c.getColumnIndexOrThrow("tips"))
            ));
        }
        c.close();  // Always close cursor
        adapter.setExercises(list);
    }

    // Simple POJO to hold exercise data
    private static class Exercise {
        int id;
        String name, category, tips;
        Exercise(int id, String n, String c, String t) {
            this.id = id;
            this.name = n;
            this.category = c;
            this.tips = t;
        }
    }

    // RecyclerView adapter to display exercises and show tips dialog
    private class ExerciseAdapter extends RecyclerView.Adapter<ExerciseAdapter.VH> {
        private final List<Exercise> list = new ArrayList<>();

        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_exercise, parent, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(@NonNull VH holder, int pos) {
            Exercise ex = list.get(pos);
            holder.name.setText(ex.name);
            holder.category.setText(ex.category);

            holder.itemView.setOnClickListener(v -> {
                // Show an alert dialog with exercise tips
                new AlertDialog.Builder(requireContext())
                        .setTitle(ex.name + " Tips")
                        .setMessage(ex.tips)
                        .setPositiveButton("OK", null)
                        .show();

                // Reveal bonus tip message if hidden
                if (bonusTipMessage.getVisibility() != View.VISIBLE) {
                    bonusTipMessage.setVisibility(View.VISIBLE);
                }
            });
        }

        @Override
        public int getItemCount() {
            return list.size();
        }

        // Update adapter data and refresh list
        void setExercises(List<Exercise> data) {
            list.clear();
            list.addAll(data);
            notifyDataSetChanged();
        }

        // ViewHolder for exercise item
        class VH extends RecyclerView.ViewHolder {
            TextView name, category;
            VH(View itemView) {
                super(itemView);
                name = itemView.findViewById(R.id.textExerciseName);
                category = itemView.findViewById(R.id.textExerciseCategory);
            }
        }
    }
}
