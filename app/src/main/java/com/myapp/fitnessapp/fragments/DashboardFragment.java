package com.myapp.fitnessapp.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.myapp.fitnessapp.R;
import com.myapp.fitnessapp.data.QuoteRepository;
import com.myapp.fitnessapp.data.QuoteService;

public class DashboardFragment extends Fragment {

    private CardView cardExerciseLibrary,
            cardWorkoutPlanner,
            cardProgress,
            cardNutritionTracker,
            cardQuoteOfDay;
    private View btnTimer;
    private NavController navController;

    // Holds today's quote once loaded
    private String dailyQuoteContent;
    private String dailyQuoteAuthor;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_dashboard, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 1) Navigation controller
        navController = NavHostFragment.findNavController(this);

        // 2) Find cards & timer
        cardExerciseLibrary  = view.findViewById(R.id.cardExerciseLibrary);
        cardWorkoutPlanner   = view.findViewById(R.id.cardWorkoutPlanner);
        cardProgress         = view.findViewById(R.id.cardProgress);
        cardNutritionTracker = view.findViewById(R.id.cardNutritionTracker);
        cardQuoteOfDay       = view.findViewById(R.id.cardQuoteOfDay);
        btnTimer             = view.findViewById(R.id.btnTimer);

        // 3) Wire up nav on the main cards
        cardExerciseLibrary.setOnClickListener(v ->
                navController.navigate(R.id.action_dashboard_to_exerciseLibrary));
        cardWorkoutPlanner.setOnClickListener(v ->
                navController.navigate(R.id.action_dashboard_to_workoutPlanner));
        cardProgress.setOnClickListener(v ->
                navController.navigate(R.id.action_dashboard_to_progress));
        cardNutritionTracker.setOnClickListener(v ->
                navController.navigate(R.id.action_dashboard_to_nutritionTracker));
        btnTimer.setOnClickListener(v ->
                navController.navigate(R.id.action_dashboard_to_timer));

        // 4) Fetch & cache one quote per day
        QuoteRepository repo = new QuoteRepository(requireContext());
        repo.getDailyQuote(new QuoteService.QuoteCallback() {
            @Override
            public void onSuccess(String content, String author) {
                dailyQuoteContent = content;
                dailyQuoteAuthor  = author;
            }
            @Override
            public void onError(Exception e) {
                // leave content null to indicate failure
                dailyQuoteContent = null;
            }
        });

        // 5) Show pop-up when Quote card is tapped
        cardQuoteOfDay.setOnClickListener(v -> {
            if (dailyQuoteContent != null) {
                new AlertDialog.Builder(requireContext())
                        .setTitle("Quote of the day")
                        .setMessage("“" + dailyQuoteContent + "”\n\n— " + dailyQuoteAuthor)
                        .setPositiveButton(android.R.string.ok, null)
                        .show();
            } else {
                Toast.makeText(requireContext(),
                        "Quote is loading…",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }
}
