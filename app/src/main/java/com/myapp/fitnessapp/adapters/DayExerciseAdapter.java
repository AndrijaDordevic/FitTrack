package com.myapp.fitnessapp.adapters;

import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.myapp.fitnessapp.R;
import com.myapp.fitnessapp.models.ExerciseItem;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class DayExerciseAdapter extends RecyclerView.Adapter<DayExerciseAdapter.ViewHolder> {

    // Full dataset of exercises and current filtered subset
    private final List<ExerciseItem> fullList;
    private final List<ExerciseItem> filteredList;
    // Track selected exercise IDs for edit mode
    private final Set<Integer> selectedIds = new HashSet<>();
    private boolean restOnly = false;

    // Control whether tapping selects items or navigates
    private boolean selectionEnabled = true;

    // Constructor: initialize lists
    public DayExerciseAdapter(@NonNull List<ExerciseItem> items) {
        this.fullList = new ArrayList<>(items);
        this.filteredList = new ArrayList<>(items);
    }

    /**
     * Enable/disable selection taps. In navigate mode, taps go to logging.
     */
    public void setSelectionEnabled(boolean enabled) {
        this.selectionEnabled = enabled;
        notifyDataSetChanged();
    }

    /**
     * If restOnly, only include the "Rest" item in filtered list.
     */
    public void setRestOnly(boolean restOnly) {
        this.restOnly = restOnly;
        filteredList.clear();
        if (restOnly) {
            for (ExerciseItem e : fullList) {
                if (e.getId() == -1) {
                    filteredList.add(e);
                    break;
                }
            }
        } else {
            filteredList.addAll(fullList);
        }
        notifyDataSetChanged();
    }

    // Filter exercises by category, preserving "Rest"
    public void filterByCategory(String category) {
        if (restOnly) return;
        filteredList.clear();
        for (ExerciseItem e : fullList) {
            if (!e.getName().equalsIgnoreCase("Rest") &&
                    (category == null || e.getCategory().equalsIgnoreCase(category))) {
                filteredList.add(e);
            } else if (e.getName().equalsIgnoreCase("Rest")) {
                filteredList.add(e);
            }
        }
        notifyDataSetChanged();
    }

    // Filter exercises by name substring, preserving "Rest"
    public void filterByName(String query) {
        if (restOnly) return;
        String q = (query == null ? "" : query.trim().toLowerCase(Locale.ROOT));
        filteredList.clear();
        for (ExerciseItem e : fullList) {
            if (e.getName().toLowerCase(Locale.ROOT).contains(q) || e.getId() == -1) {
                filteredList.add(e);
            }
        }
        notifyDataSetChanged();
    }

    // Return a copy of selected IDs
    @NonNull
    public List<Integer> getSelectedIds() {
        return new ArrayList<>(selectedIds);
    }

    @Override
    public int getItemCount() {
        return filteredList.size();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate item layout
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_exercise, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // Bind texts and selection visuals
        ExerciseItem item = filteredList.get(position);
        holder.tvName.setText(item.getName());
        holder.tvCategory.setText(item.getCategory());
        boolean isSelected = selectedIds.contains(item.getId());
        holder.itemView.setActivated(isSelected);

        // Always make clickable for ripple feedback
        holder.itemView.setClickable(true);
        holder.itemView.setFocusable(true);
        TypedValue outValue = new TypedValue();
        holder.itemView.getContext().getTheme()
                .resolveAttribute(android.R.attr.selectableItemBackground, outValue, true);
        holder.itemView.setForeground(
                ContextCompat.getDrawable(holder.itemView.getContext(), outValue.resourceId));
    }

    /**
     * Replace selection set and refresh visuals
     */
    public void setSelectedIds(@NonNull List<Integer> ids) {
        selectedIds.clear();
        selectedIds.addAll(ids);
        notifyDataSetChanged();
    }

    // ViewHolder sets up click behavior
    class ViewHolder extends RecyclerView.ViewHolder {
        final TextView tvName, tvCategory;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName     = itemView.findViewById(R.id.textExerciseName);
            tvCategory = itemView.findViewById(R.id.textExerciseCategory);

            // Handle taps for selection or navigation
            itemView.setOnClickListener(v -> {
                int pos = getAdapterPosition();
                if (pos == RecyclerView.NO_POSITION) return;
                ExerciseItem clicked = filteredList.get(pos);
                if (!selectionEnabled) {
                    // Navigate to logging fragment
                    Bundle bundle = new Bundle();
                    bundle.putInt("exerciseId", clicked.getId());
                    Navigation.findNavController(v)
                            .navigate(R.id.exerciseLoggingFragment, bundle);
                } else {
                    // Toggle selection
                    int id = clicked.getId();
                    if (selectedIds.contains(id)) {
                        selectedIds.remove(id);
                    } else {
                        selectedIds.add(id);
                    }
                    // Refresh only this item
                    notifyItemChanged(pos);
                }
            });
        }
    }
}
