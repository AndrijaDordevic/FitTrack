package com.myapp.fitnessapp.adapters;

import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.myapp.fitnessapp.R;
import com.myapp.fitnessapp.models.ExerciseItem;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class DayExerciseAdapter
        extends RecyclerView.Adapter<DayExerciseAdapter.ViewHolder> {

    private final List<ExerciseItem> fullList;
    private final List<ExerciseItem> filteredList;
    private final Set<Integer> selectedIds = new HashSet<>();
    private boolean restOnly = false;

    // NEW: control whether items are tappable/selectable
    private boolean selectionEnabled = true;

    public DayExerciseAdapter(@NonNull List<ExerciseItem> items) {
        this.fullList = new ArrayList<>(items);
        this.filteredList = new ArrayList<>(items);
    }

    /** Enable or disable tapping to select/deselect items */
    public void setSelectionEnabled(boolean enabled) {
        this.selectionEnabled = enabled;
        notifyDataSetChanged();
    }

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

    public void filterByCategory(String category) {
        if (restOnly) return;
        filteredList.clear();
        for (ExerciseItem e : fullList) {
            if (!e.getName().equalsIgnoreCase("Rest")) {
                if (category == null || e.getCategory().equalsIgnoreCase(category)) {
                    filteredList.add(e);
                }
            } else {
                filteredList.add(e);
            }
        }
        notifyDataSetChanged();
    }

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

    @NonNull
    public List<Integer> getSelectedIds() {
        return new ArrayList<>(selectedIds);
    }

    @Override
    public int getItemCount() {
        return filteredList.size();
    }

    @NonNull @Override
    public ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_exercise, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(
            @NonNull ViewHolder holder,
            int position
    ) {
        bindTexts(holder, position);
        bindSelection(holder, position);
    }

    @Override
    public void onBindViewHolder(
            @NonNull ViewHolder holder,
            int position,
            @NonNull List<Object> payloads
    ) {
        if (!payloads.isEmpty()) {
            bindSelection(holder, position);
        } else {
            onBindViewHolder(holder, position);
        }
    }

    private void bindTexts(@NonNull ViewHolder holder, int position) {
        ExerciseItem item = filteredList.get(position);
        holder.tvName.setText(item.getName());
        holder.tvCategory.setText(item.getCategory());
    }

    private void bindSelection(@NonNull ViewHolder holder, int position) {
        ExerciseItem item = filteredList.get(position);
        boolean isSelected = selectedIds.contains(item.getId());

        holder.itemView.setActivated(isSelected);

        if (!selectionEnabled) {
            holder.itemView.setClickable(false);
            holder.itemView.setFocusable(false);
            holder.itemView.setForeground(null); // Removes ripple on modern APIs
        } else {
            holder.itemView.setClickable(true);
            holder.itemView.setFocusable(true);

            // Restore ripple
            TypedValue outValue = new TypedValue();
            holder.itemView.getContext().getTheme()
                    .resolveAttribute(android.R.attr.selectableItemBackground, outValue, true);
            holder.itemView.setForeground(
                    ContextCompat.getDrawable(holder.itemView.getContext(), outValue.resourceId)
            );
        }
    }


    /** Replace current selection with this new list, then redraw */
    public void setSelectedIds(@NonNull List<Integer> ids) {
        selectedIds.clear();
        selectedIds.addAll(ids);
        notifyDataSetChanged();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        final TextView tvName;
        final TextView tvCategory;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName     = itemView.findViewById(R.id.textExerciseName);
            tvCategory = itemView.findViewById(R.id.textExerciseCategory);

            itemView.setOnClickListener(v -> {
                // only handle clicks when selectionEnabled
                if (!selectionEnabled) return;

                int pos = getAdapterPosition();
                if (pos == RecyclerView.NO_POSITION) return;

                ExerciseItem clicked = filteredList.get(pos);
                int id = clicked.getId();

                if (selectedIds.contains(id)) selectedIds.remove(id);
                else                           selectedIds.add(id);

                notifyItemChanged(pos, "selection");
            });
        }
    }
}
