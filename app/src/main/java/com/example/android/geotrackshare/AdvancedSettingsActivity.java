package com.example.android.geotrackshare;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;

import static com.example.android.geotrackshare.RealTimeFragment.UPDATE_INTERVAL_IN_MILLISECONDS_STRING;

/**
 * Created by Marcin on 2017-09-12.
 */

public class AdvancedSettingsActivity extends AppCompatActivity {



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.advanced_settings_activity);

        ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
//            actionBar.setDisplayShowHomeEnabled(true);
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
    }

}