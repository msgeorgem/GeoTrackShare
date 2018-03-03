package com.example.android.geotrackshare;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

/**
 * Created by Marcin on 2017-09-12.
 */

public class AdvancedSettingsActivity extends AppCompatActivity {

    static String TEMP_THEME_STRING;
    static boolean preferenceBooleanTheme;
    static boolean TEMP_BOOLEAN;

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
        boolean themeBoolean = sharedPrefs.getBoolean("theme_switch", TEMP_BOOLEAN);
        if (!themeBoolean) {
            this.setTheme(R.style.AppThemeSettings);
            Toast.makeText(this, "Light mode", Toast.LENGTH_SHORT).show();
        } else {
            this.setTheme(R.style.AppThemeSettingsDarkTheme);
            Toast.makeText(this, "Darkness mode", Toast.LENGTH_SHORT).show();
    }
    }
    public static class TracksPreferenceFragment extends PreferenceFragment implements Preference.OnPreferenceChangeListener {


        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setPreferenceScreen(null);
            addPreferencesFromResource(R.xml.advanced_settings);

            Preference intervalBy = findPreference(getString(R.string.update_interval_by_key));
            bindPreferenceSummaryToValue1(intervalBy);

            Preference deleteloopBy = findPreference(getString(R.string.delete_loops_by_key));
            bindPreferenceSummaryToValue2(deleteloopBy);

            Preference themePreference = findPreference("theme_switch");
            bindPreferenceSummaryToValue3(themePreference);

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

            if (value instanceof Boolean) {
                TEMP_BOOLEAN = preferenceBooleanTheme;
            }
            return true;

        }

        private void bindPreferenceSummaryToValue1(Preference preference) {
            preference.setOnPreferenceChangeListener(this);
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
            String preferenceString = preferences.getString(preference.getKey(), "");
            onPreferenceChange(preference, preferenceString);
        }

        private void bindPreferenceSummaryToValue2(Preference preference) {
            preference.setOnPreferenceChangeListener(this);
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
            String preferenceString = preferences.getString(preference.getKey(), "");
            onPreferenceChange(preference, preferenceString);
        }

        private void bindPreferenceSummaryToValue3(Preference preference) {
            preference.setOnPreferenceChangeListener(this);
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
            preferenceBooleanTheme = preferences.getBoolean(preference.getKey(), true);
            onPreferenceChange(preference, preferenceBooleanTheme);
        }

    }

}