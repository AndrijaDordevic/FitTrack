package com.myapp.fitnessapp.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import android.widget.TextView;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.myapp.fitnessapp.R;

public class WorkoutPlannerFragment extends Fragment {
    // Display for showing the current week or month
    private TextView tvWeekSelector;
    private ViewPager2 viewPager;
    private FragmentStateAdapter pagerAdapter;
    private TabLayout tabLayout;

    // Weekday labels for planner tabs
    private static final String[] DAYS = {
            "Monday", "Tuesday", "Wednesday",
            "Thursday", "Friday", "Saturday", "Sunday"
    };

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        // Inflate the layout containing week selector, tabs, and pager
        return inflater.inflate(
                R.layout.fragment_workout_planner,
                container,
                false
        );
    }

    // Initialize and bind ViewPager2 with tabs
    private void setupViewPager() {
        pagerAdapter = new FragmentStateAdapter(this) {
            @Override
            public int getItemCount() {
                // Number of pages equals number of days
                return DAYS.length;
            }

            @NonNull
            @Override
            public Fragment createFragment(int position) {
                // Create a DayPlannerFragment for the given day
                String dayName = DAYS[position];
                return DayPlannerFragment.newInstance(dayName);
            }
        };
        viewPager.setAdapter(pagerAdapter);

        // Link tabs to ViewPager pages and set titles
        new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) -> tab.setText(DAYS[position])
        ).attach();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Bind UI elements
        tvWeekSelector = view.findViewById(R.id.tvWeekSelector);
        tabLayout = view.findViewById(R.id.tabLayoutDays);
        viewPager = view.findViewById(R.id.viewPagerDays);

        // Configure pager and tabs
        setupViewPager();
    }
}
