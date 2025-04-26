package com.myapp.fitnessapp.fragments;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreferenceCompat;
import androidx.preference.PreferenceManager;

import com.myapp.fitnessapp.R;

public class SettingsFragment extends PreferenceFragmentCompat {
    private static final String KEY_DARK_MODE = "pref_dark_mode";

    @Override
    public void onCreatePreferences(@Nullable Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.fragment_settings, rootKey);

        Preference togglePref = findPreference("pref_dark_mode_toggle");
        if (togglePref != null) {
            togglePref.setOnPreferenceClickListener(preference -> {
                SharedPreferences prefs = PreferenceManager
                        .getDefaultSharedPreferences(requireContext());
                boolean isDarkMode = prefs.getBoolean(KEY_DARK_MODE, false);
                boolean newMode = !isDarkMode;

                prefs.edit()
                        .putBoolean(KEY_DARK_MODE, newMode)
                        .commit();

                AppCompatDelegate.setDefaultNightMode(
                        newMode ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO
                );

                requireActivity().recreate();

                return true;
            });
        }
    }

}
