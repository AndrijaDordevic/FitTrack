package com.myapp.fitnessapp.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.myapp.fitnessapp.R;

public class WorkoutLoggingFragment extends Fragment {
    private Button btnLogFromPlan, btnLogFreestyle;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_workout_logging, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        btnLogFromPlan   = view.findViewById(R.id.btnLogFromPlan);
        btnLogFreestyle  = view.findViewById(R.id.btnLogFreestyle);

        btnLogFromPlan.setOnClickListener(v -> {
            // Open the workout plan view (tabs for each day)
            Fragment frag = new WorkoutPlanLogFragment();
            getChildFragmentManager()
                    .beginTransaction()
                    .replace(R.id.flLoggingContainer, frag)
                    .commit();
        });

        btnLogFreestyle.setOnClickListener(v -> {
            // Open free style logging
            Fragment frag = new FreestyleLoggingFragment();
            getChildFragmentManager()
                    .beginTransaction()
                    .replace(R.id.flLoggingContainer, frag)
                    .commit();
        });
    }
}
