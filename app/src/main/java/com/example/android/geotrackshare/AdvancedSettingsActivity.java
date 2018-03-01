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

import static com.example.android.geotrackshare.RealTimeFragment.DELETE_LAST_ROWS_STRING;
import static com.example.android.geotrackshare.RealTimeFragment.UPDATE_INTERVAL_IN_MILLISECONDS_STRING;

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
        setContentView(R.layout.advanced_settings_activity);

        ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
            actionBar.setDisplayShowTitleEnabled(true);
        }

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        UPDATE_INTERVAL_IN_MILLISECONDS_STRING = RealTimeFragment.sharedPrefs.getString(
                getString(R.string.update_interval_by_key),
                getString(R.string.update_interval_by_default_ultimate)
        );
        RealTimeFragment.UPDATE_INTERVAL_IN_MILLISECONDS = Long.parseLong(UPDATE_INTERVAL_IN_MILLISECONDS_STRING);

        DELETE_LAST_ROWS_STRING = RealTimeFragment.sharedPrefs.getString(
                getString(R.string.delete_loops_by_key),
                getString(R.string.delete_loops_by_default_ultimate)
        );
        RealTimeFragment.DELETE_LAST_ROWS = Integer.parseInt((DELETE_LAST_ROWS_STRING));

//        MainActivity.THEME_BOOLEAN = RealTimeFragment.sharedPrefs.getBoolean("theme",TEMP_BOOLEAN);
//        RealTimeFragment.THEME = Integer.parseInt((tempString));
    }

    public static class TracksPreferenceFragment extends PreferenceFragment implements Preference.OnPreferenceChangeListener {


        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
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
                if (preferenceBooleanTheme) {
                    MainActivity.THEME_BOOLEAN = false;
                    Toast.makeText(getActivity(), "Restart app to apply changes", Toast.LENGTH_SHORT).show();
                } else {
                    MainActivity.THEME_BOOLEAN = true;
                    Toast.makeText(getActivity(), "Restart app to apply changes", Toast.LENGTH_SHORT).show();
                }
            }
            return true;
        }

        private void bindPreferenceSummaryToValue1(Preference preference) {
            preference.setOnPreferenceChangeListener(this);
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(preference.getContext());
            String preferenceString = preferences.getString(preference.getKey(), "");
            onPreferenceChange(preference, preferenceString);
        }

        private void bindPreferenceSummaryToValue2(Preference preference) {
            preference.setOnPreferenceChangeListener(this);
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(preference.getContext());
            String preferenceString = preferences.getString(preference.getKey(), "");
            onPreferenceChange(preference, preferenceString);
        }

        private void bindPreferenceSummaryToValue3(Preference preference) {
            preference.setOnPreferenceChangeListener(this);
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(preference.getContext());
            preferenceBooleanTheme = preferences.getBoolean(preference.getKey(), false);
            onPreferenceChange(preference, preferenceBooleanTheme);
        }

    }

}