package com.myapp.fitnessapp.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.myapp.fitnessapp.R;
import com.myapp.fitnessapp.models.MealLogEntry;

import java.util.ArrayList;
import java.util.List;

public class MealAdapter extends RecyclerView.Adapter<MealAdapter.VH> {
    public interface Listener {
        void onEdit(MealLogEntry entry);
        void onDelete(MealLogEntry entry);
    }

    private final Listener listener;
    private List<MealLogEntry> meals = new ArrayList<>();

    public MealAdapter(Listener listener) {
        this.listener = listener;
    }

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

    @Override
    public int getItemCount() {
        return meals.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvType, tvDesc, tvEdit, tvDelete;
        MealLogEntry current;

        VH(View itemView, Listener listener) {
            super(itemView);
            tvType   = itemView.findViewById(R.id.tvMealType);
            tvDesc   = itemView.findViewById(R.id.tvMealDesc);
            tvEdit   = itemView.findViewById(R.id.tvEdit);
            tvDelete = itemView.findViewById(R.id.tvDelete);

            tvEdit.setOnClickListener(v -> {
                if (current != null) listener.onEdit(current);
            });
            tvDelete.setOnClickListener(v -> {
                if (current != null) listener.onDelete(current);
            });
        }

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

