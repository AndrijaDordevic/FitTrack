package com.myapp.fitnessapp.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.myapp.fitnessapp.R;
import com.myapp.fitnessapp.models.MealLogEntry;

import java.util.ArrayList;
import java.util.List;

public class MealAdapter extends RecyclerView.Adapter<MealAdapter.VH> {
    // Listener interface to handle edit and delete actions
    public interface Listener {
        void onEdit(MealLogEntry entry);
        void onDelete(MealLogEntry entry);
    }

    private final Listener listener;
    // Dataset of meal entries
    private List<MealLogEntry> meals = new ArrayList<>();

    // Constructor takes a listener for callbacks
    public MealAdapter(Listener listener) {
        this.listener = listener;
    }

    /**
     * Update adapter data and refresh the RecyclerView
     */
    public void setMeals(List<MealLogEntry> list) {
        meals = list;
        notifyDataSetChanged();
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate layout for each meal log item
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_meal_log, parent, false);
        return new VH(v, listener);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        // Bind data for the given position
        holder.bind(meals.get(position));
    }

    @Override
    public int getItemCount() {
        return meals.size();
    }

    /**
     * ViewHolder class to represent and bind views for each meal entry
     */
    static class VH extends RecyclerView.ViewHolder {
        TextView tvType, tvDesc, tvEdit, tvDelete;
        MealLogEntry current;

        VH(View itemView, Listener listener) {
            super(itemView);
            // Initialize views
            tvType   = itemView.findViewById(R.id.tvMealType);
            tvDesc   = itemView.findViewById(R.id.tvMealDesc);
            tvEdit   = itemView.findViewById(R.id.tvEdit);
            tvDelete = itemView.findViewById(R.id.tvDelete);

            // Edit button listener invokes callback
            tvEdit.setOnClickListener(v -> {
                if (current != null) listener.onEdit(current);
            });
            // Delete button listener invokes callback
            tvDelete.setOnClickListener(v -> {
                if (current != null) listener.onDelete(current);
            });
        }

        /**
         * Bind a MealLogEntry object to this ViewHolder's views
         */
        void bind(MealLogEntry m) {
            current = m;
            tvType.setText(m.getMealType());
            // Format and display nutrition details
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
