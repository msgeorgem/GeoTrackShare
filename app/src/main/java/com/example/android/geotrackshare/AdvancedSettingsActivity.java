package com.example.android.geotrackshare;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;


/**
 * Created by Marcin on 2017-09-12.
 */

public class AdvancedSettingsActivity extends AppCompatActivity {

    public static boolean preferenceBooleanDisableAutoStop;
    public static boolean preferenceBooleanTheme;
    public static boolean preferenceBooleanScreenOn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        switchThemeS();
        setContentView(R.layout.advanced_settings_activity);

        ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
            actionBar.setDisplayShowTitleEnabled(true);
        }

    }

    private void switchThemeS() {

        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        boolean themeBoolean = sharedPrefs.getBoolean("theme_switch", preferenceBooleanTheme);
        if (!themeBoolean) {
            this.setTheme(R.style.AppThemeSettings);
        } else {
            this.setTheme(R.style.AppThemeSettingsDarkTheme);
    }
    }

    public static class TracksPreferenceFragment extends PreferenceFragmentCompat implements Preference.OnPreferenceChangeListener {

//        @Override
//        public void onCreate(Bundle savedInstanceState) {
//            super.onCreate(savedInstanceState);
//            setPreferenceScreen(null);
//            addPreferencesFromResource(R.xml.advanced_settings);
//
//            Preference disableAutoStopPreference = findPreference(getString(R.string.disable_auto_stop_switch_key));
//            bindPreferenceSummaryToValue2(disableAutoStopPreference);
//
//            Preference deleteloopBy = findPreference(getString(R.string.delete_loops_by_key));
//            bindPreferenceSummaryToValue3(deleteloopBy);
//
//            Preference themePreference = findPreference(getString(R.string.theme_switch_key));
//            bindPreferenceSummaryToValue4(themePreference);
//
//            Preference screenOnPreference = findPreference(getString(R.string.screen_on_switch_key));
//            bindPreferenceSummaryToValue5(screenOnPreference);
//
//
//        }

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferenceScreen(null);
            addPreferencesFromResource(R.xml.advanced_settings);

            Preference disableAutoStopPreference = findPreference(getString(R.string.disable_auto_stop_switch_key));
            bindPreferenceSummaryToValue2(disableAutoStopPreference);

            Preference deleteloopBy = findPreference(getString(R.string.delete_loops_by_key));
            bindPreferenceSummaryToValue3(deleteloopBy);

            Preference themePreference = findPreference(getString(R.string.theme_switch_key));
            bindPreferenceSummaryToValue4(themePreference);

            Preference screenOnPreference = findPreference(getString(R.string.screen_on_switch_key));
            bindPreferenceSummaryToValue5(screenOnPreference);
        }

        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {

            String stringValue = value.toString();
            if (preference instanceof ListPreference) {
                ListPreference listPreference = (ListPreference) preference;
                int prefIndex = listPreference.findIndexOfValue(stringValue);
                if (prefIndex >= 0) {
                    CharSequence[] labels = listPreference.getEntries();
                    preference.setSummary(labels[prefIndex]);
                }
            } else {
                preference.setSummary(stringValue);
            }

            return true;
        }

        private void bindPreferenceSummaryToValue2(Preference preference) {
            preference.setOnPreferenceChangeListener(this);
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
            preferenceBooleanDisableAutoStop = preferences.getBoolean(preference.getKey(), false);
            onPreferenceChange(preference, preferenceBooleanDisableAutoStop);
        }

        private void bindPreferenceSummaryToValue3(Preference preference) {
            preference.setOnPreferenceChangeListener(this);
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
            String preferenceString = preferences.getString(preference.getKey(), "");
            onPreferenceChange(preference, preferenceString);
        }

        private void bindPreferenceSummaryToValue4(Preference preference) {
            preference.setOnPreferenceChangeListener(this);
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
            preferenceBooleanTheme = preferences.getBoolean(preference.getKey(), false);
            onPreferenceChange(preference, preferenceBooleanTheme);
        }

        private void bindPreferenceSummaryToValue5(Preference preference) {
            preference.setOnPreferenceChangeListener(this);
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
            preferenceBooleanScreenOn = preferences.getBoolean(preference.getKey(), true);
            onPreferenceChange(preference, preferenceBooleanScreenOn);
        }
    }
}