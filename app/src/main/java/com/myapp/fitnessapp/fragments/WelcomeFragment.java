package com.myapp.fitnessapp.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import com.myapp.fitnessapp.R;

public class WelcomeFragment extends Fragment {

    public WelcomeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_welcome, container, false);

        view.findViewById(R.id.btn_login).setOnClickListener(v ->
                NavHostFragment.findNavController(this).navigate(R.id.action_welcome_to_login)
        );

        view.findViewById(R.id.btn_signup).setOnClickListener(v ->
                NavHostFragment.findNavController(this).navigate(R.id.action_welcome_to_signup)
        );

        return view;
    }
}
