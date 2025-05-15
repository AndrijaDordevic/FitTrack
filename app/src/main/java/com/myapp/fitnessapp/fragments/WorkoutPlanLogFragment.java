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

public class WorkoutPlanLogFragment extends Fragment {
    // Weekday labels for the tabs
    private static final String[] DAYS = {
            "Monday", "Tuesday", "Wednesday",
            "Thursday", "Friday", "Saturday", "Sunday"
    };

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Inflate the layout containing TabLayout and ViewPager2
        return inflater.inflate(R.layout.fragment_workout_plan_log, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        // Find the TabLayout and ViewPager2 in the inflated view
        TabLayout    tabLayout = view.findViewById(R.id.tabLayoutDaysLog);
        ViewPager2   viewPager = view.findViewById(R.id.viewPagerDaysLog);

        // Adapter that creates a DayPlannerFragment for each day
        FragmentStateAdapter pagerAdapter = new FragmentStateAdapter(this) {
            @Override
            public int getItemCount() {
                // Number of tabs equals number of days
                return DAYS.length;
            }

            @NonNull
            @Override
            public Fragment createFragment(int position) {
                // Create fragment instance, set viewOnly flag for log display
                DayPlannerFragment fragment = DayPlannerFragment.newInstance(DAYS[position]);
                Bundle args = new Bundle();
                args.putBoolean("viewOnly", true);
                fragment.setArguments(args);
                return fragment;
            }
        };
        viewPager.setAdapter(pagerAdapter);

        // Link the TabLayout and ViewPager2 so tabs display day names
        new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) -> tab.setText(DAYS[position])
        ).attach();
    }
}
