package com.myapp.fitnessapp.fragments;

import android.app.DatePickerDialog;
import android.content.ContentValues;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.myapp.fitnessapp.R;
import com.myapp.fitnessapp.database.DBHelper;
import com.myapp.fitnessapp.models.MealLogEntry;
import com.myapp.fitnessapp.utils.UserSession;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class NutritionTrackerFragment extends Fragment {
    private String userEmail;
    private String selectedDate;
    private TextView tvDate;
    private Button btnPickDate, btnAddMeal;
    private RecyclerView rvMeals;
    private MealAdapter adapter;
    private DBHelper db;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_nutrition_tracker, container, false);

        // Bind UI elements
        tvDate      = v.findViewById(R.id.tvDate);
        btnPickDate = v.findViewById(R.id.btnPickDate);
        btnAddMeal  = v.findViewById(R.id.btnAddMeal);
        rvMeals     = v.findViewById(R.id.rvMeals);

        // Initialize session and database helper
        UserSession.init(requireContext());
        db = UserSession.getDbHelper();

        // Retrieve current user or redirect if not logged in
        userEmail = UserSession.getEmail();
        if (userEmail == null) {
            Toast.makeText(requireContext(),
                    "User not logged in", Toast.LENGTH_LONG).show();
            NavHostFragment.findNavController(this)
                    .navigate(R.id.action_global_welcomeFragment);
            return v;
        }

        // Set default date to today
        Calendar cal = Calendar.getInstance();
        selectedDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                .format(cal.getTime());

        // Configure RecyclerView for meal logs
        rvMeals.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new MealAdapter(new MealAdapter.Listener() {
            @Override
            public void onEdit(MealLogEntry entry) {
                showEditMealDialog(entry);
            }
            @Override
            public void onDelete(MealLogEntry entry) {
                db.deleteNutritionLog(entry.getId());
                loadMealsForDate();
            }
        });
        rvMeals.setAdapter(adapter);

        // Display date and attach button handlers
        updateDateDisplay();
        btnPickDate.setOnClickListener(view -> showDatePicker());
        btnAddMeal.setOnClickListener(view -> showAddMealDialog());
        loadMealsForDate();

        return v;
    }

    // Update the date TextView with the selectedDate
    private void updateDateDisplay() {
        tvDate.setText(selectedDate);
    }

    // Show a DatePickerDialog to select a date
    private void showDatePicker() {
        Calendar cal = Calendar.getInstance();
        DatePickerDialog dpd = new DatePickerDialog(requireContext(),
                (DatePicker view, int y, int m, int d) -> {
                    cal.set(y, m, d);
                    selectedDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                            .format(cal.getTime());
                    updateDateDisplay();
                    loadMealsForDate();
                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
        );
        dpd.show();
    }

    // Load and display meal logs for the selectedDate
    private void loadMealsForDate() {
        List<MealLogEntry> meals = db.getNutritionLogsForDate(userEmail, selectedDate);
        adapter.setMeals(meals);
    }

    // Launch dialog to add a new meal entry
    private void showAddMealDialog() {
        showMealDialog("Add Meal", null);
    }

    // Launch dialog to edit an existing meal entry
    private void showEditMealDialog(MealLogEntry entry) {
        showMealDialog("Edit Meal", entry);
    }

    // Generic meal dialog for add/edit with validation and persistence
    private void showMealDialog(String title, @Nullable MealLogEntry entry) {
        View dialog = LayoutInflater.from(getContext())
                .inflate(R.layout.dialog_add_meal, null);
        EditText etMealType = dialog.findViewById(R.id.etMealType);
        EditText etFood     = dialog.findViewById(R.id.etFood);
        EditText etCal      = dialog.findViewById(R.id.etCalories);
        EditText etP        = dialog.findViewById(R.id.etProtein);
        EditText etC        = dialog.findViewById(R.id.etCarbs);
        EditText etF        = dialog.findViewById(R.id.etFat);

        // Pre-fill fields when editing an entry
        if (entry != null) {
            etMealType.setText(entry.getMealType());
            etFood    .setText(entry.getFood());
            etCal     .setText(String.valueOf(entry.getCalories()));
            etP       .setText(String.valueOf(entry.getProtein()));
            etC       .setText(String.valueOf(entry.getCarbs()));
            etF       .setText(String.valueOf(entry.getFat()));
        }

        // Set numeric input types for nutritional values
        etCal.setInputType(InputType.TYPE_CLASS_NUMBER);
        etP  .setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        etC  .setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        etF  .setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);

        AlertDialog dlg = new AlertDialog.Builder(requireContext())
                .setTitle(title)
                .setView(dialog)
                .setPositiveButton("Save", null)
                .setNegativeButton("Cancel", (d, which) -> d.cancel())
                .create();

        // Override positive button to validate input before dismissing
        dlg.setOnShowListener(d -> {
            Button btnSave = dlg.getButton(AlertDialog.BUTTON_POSITIVE);
            btnSave.setOnClickListener(v -> {
                String mt   = etMealType.getText().toString().trim();
                String food = etFood    .getText().toString().trim();
                if (mt.isEmpty() || food.isEmpty()) {
                    Toast.makeText(requireContext(),
                            "Please enter meal type and food", Toast.LENGTH_SHORT).show();
                    return;
                }
                int   cal = parseOrZero(etCal.getText().toString().trim());
                float p   = parseFloatOrZero(etP.getText().toString().trim());
                float c   = parseFloatOrZero(etC.getText().toString().trim());
                float f   = parseFloatOrZero(etF.getText().toString().trim());

                if (entry == null) {
                    db.insertNutritionLog(userEmail, selectedDate,
                            mt, food, cal, p, c, f);
                } else {
                    db.updateNutritionLog(entry.getId(), userEmail, selectedDate,
                            mt, food, cal, p, c, f);
                }
                loadMealsForDate();
                dlg.dismiss();
            });
        });

        dlg.show();
    }

    // Safely parse integer or return zero
    private int parseOrZero(String s) {
        if (s.isEmpty()) return 0;
        try { return Integer.parseInt(s); }
        catch (NumberFormatException e) { return 0; }
    }

    // Safely parse float or return zero
    private float parseFloatOrZero(String s) {
        if (s.isEmpty()) return 0f;
        try { return Float.parseFloat(s); }
        catch (NumberFormatException e) { return 0f; }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Close database to release resources
        db.close();
    }

    // RecyclerView Adapter for meal entries with edit/delete callbacks
    private static class MealAdapter extends RecyclerView.Adapter<MealAdapter.VH> {
        interface Listener {
            void onEdit(MealLogEntry entry);
            void onDelete(MealLogEntry entry);
        }

        private final Listener listener;
        private List<MealLogEntry> meals = new ArrayList<>();

        MealAdapter(Listener listener) {
            this.listener = listener;
        }

        // Update adapter data and refresh UI
        public void setMeals(List<MealLogEntry> list) {
            meals = list;
            notifyDataSetChanged();
        }

        @NonNull @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_meal_log, parent, false);
            return new VH(v, listener);
        }

        @Override
        public void onBindViewHolder(@NonNull VH holder, int position) {
            holder.bind(meals.get(position));
        }

        @Override public int getItemCount() { return meals.size(); }

        static class VH extends RecyclerView.ViewHolder {
            TextView tvType, tvDesc, tvEdit, tvDelete;
            MealLogEntry current;

            VH(View itemView, Listener listener) {
                super(itemView);
                tvType   = itemView.findViewById(R.id.tvMealType);
                tvDesc   = itemView.findViewById(R.id.tvMealDesc);
                tvEdit   = itemView.findViewById(R.id.tvEdit);
                tvDelete = itemView.findViewById(R.id.tvDelete);

                // Set edit and delete click callbacks
                tvEdit.setOnClickListener(v -> {
                    if (current != null) listener.onEdit(current);
                });
                tvDelete.setOnClickListener(v -> {
                    if (current != null) listener.onDelete(current);
                });
            }

            // Bind MealLogEntry data to UI
            void bind(MealLogEntry m) {
                current = m;
                tvType.setText(m.getMealType());
                tvDesc.setText(String.format(
                        "%s: %dkcal, Protein %.1fg, Carbs %.1fg, Fat %.1fg",
                        m.getFood(),
                        m.getCalories(),
                        m.getProtein(),
                        m.getCarbs(),
                        m.getFat()
                ));
            }
        }
    }
}
