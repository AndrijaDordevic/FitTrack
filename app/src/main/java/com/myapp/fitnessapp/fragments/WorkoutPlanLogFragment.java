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
    private static final String[] DAYS = {
            "Monday", "Tuesday", "Wednesday",
            "Thursday", "Friday", "Saturday", "Sunday"
    };

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_workout_plan_log, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        TabLayout tabLayout = view.findViewById(R.id.tabLayoutDaysLog);
        ViewPager2 viewPager = view.findViewById(R.id.viewPagerDaysLog);

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
                args.putBoolean("viewOnly", true); // Pass a flag if you want different behavior
                fragment.setArguments(args);
                return fragment;
            }
        };
        viewPager.setAdapter(pagerAdapter);

        new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) -> tab.setText(DAYS[position])
        ).attach();
    }
}
