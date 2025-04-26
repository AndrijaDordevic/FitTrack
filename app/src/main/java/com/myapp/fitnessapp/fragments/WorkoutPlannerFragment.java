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

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.myapp.fitnessapp.R;

public class WorkoutPlannerFragment extends Fragment {
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

    @Override
    public void onViewCreated(
            @NonNull View view,
            @Nullable Bundle savedInstanceState
    ) {
        super.onViewCreated(view, savedInstanceState);

        // 1. Find the TabLayout and ViewPager2
        TabLayout tabLayout = view.findViewById(R.id.tabLayoutDays);
        ViewPager2 viewPager = view.findViewById(R.id.viewPagerDays);

        // 2. Create a FragmentStateAdapter that supplies one DayPlannerFragment per day
        FragmentStateAdapter pagerAdapter = new FragmentStateAdapter(this) {
            @Override
            public int getItemCount() {
                return DAYS.length;
            }

            @NonNull
            @Override
            public Fragment createFragment(int position) {
                DayPlannerFragment fragment = new DayPlannerFragment();
                Bundle args = new Bundle();
                args.putString("dayName", DAYS[position]);
                fragment.setArguments(args);
                return fragment;
            }
        };
        viewPager.setAdapter(pagerAdapter);

        // 3. Link the TabLayout and ViewPager2 so tabs show the day names
        new TabLayoutMediator(
                tabLayout,
                viewPager,
                (tab, position) -> tab.setText(DAYS[position])
        ).attach();
    }
}
