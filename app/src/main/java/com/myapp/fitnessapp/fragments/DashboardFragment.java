package com.myapp.fitnessapp.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.myapp.fitnessapp.R;

public class DashboardFragment extends Fragment {

    private CardView cardNutritionTracker, cardExerciseLibrary, cardWorkoutPlanner, cardProgress;
    private View btnTimer;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the fragment's layout
        return inflater.inflate(R.layout.fragment_dashboard, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        NavController navController = NavHostFragment.findNavController(this);


        cardExerciseLibrary  = view.findViewById(R.id.cardExerciseLibrary);
        cardWorkoutPlanner   = view.findViewById(R.id.cardWorkoutPlanner);
        cardProgress         = view.findViewById(R.id.cardProgress);
        cardNutritionTracker = view.findViewById(R.id.cardNutritionTracker);
        btnTimer             = view.findViewById(R.id.btnTimer);

        cardExerciseLibrary.setOnClickListener(v ->
                navController.navigate(R.id.action_dashboard_to_exerciseLibrary)
        );
        cardWorkoutPlanner.setOnClickListener(v ->
                navController.navigate(R.id.action_dashboard_to_workoutPlanner)
        );
        cardProgress.setOnClickListener(v ->
                navController.navigate(R.id.action_dashboard_to_progress)
        );
        cardNutritionTracker.setOnClickListener(v ->
                navController.navigate(R.id.action_dashboard_to_nutritionTracker)
        );
        btnTimer.setOnClickListener(v ->
                navController.navigate(R.id.action_dashboard_to_timer)
        );

    }
}