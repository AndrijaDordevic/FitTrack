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
    private TextView tvWeekSelector;
    private ViewPager2 viewPager;
    private FragmentStateAdapter pagerAdapter;
    private TabLayout tabLayout;
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
        // Inflate the layout for this fragment
        return inflater.inflate(
                R.layout.fragment_workout_planner,
                container,
                false
        );
    }

    private void setupViewPager() {
        pagerAdapter = new FragmentStateAdapter(this) {
            @Override
            public int getItemCount() {
                return DAYS.length;
            }

            @NonNull
            @Override
            public Fragment createFragment(int position) {
                String dayName = DAYS[position];
                return DayPlannerFragment.newInstance(dayName, "user@example.com");
            }
        };
        viewPager.setAdapter(pagerAdapter);

        new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) -> tab.setText(DAYS[position])
        ).attach();
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Find views
        tvWeekSelector = view.findViewById(R.id.tvWeekSelector);
        tabLayout = view.findViewById(R.id.tabLayoutDays);
        viewPager = view.findViewById(R.id.viewPagerDays);


        // Set up ViewPager
        setupViewPager();
    }
}
