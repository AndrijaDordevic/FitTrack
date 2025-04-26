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

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Inflate the layout (just the container)
        View view = inflater.inflate(R.layout.fragment_workout_logging, container, false);

        getChildFragmentManager()
                .beginTransaction()
                .replace(R.id.flLoggingContainer, new WorkoutPlanLogFragment())
                .commit();

        return view;
    }

}