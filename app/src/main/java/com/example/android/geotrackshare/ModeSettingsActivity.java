package com.example.android.geotrackshare;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.example.android.geotrackshare.RunTypes.RunTypesAdapterNoUI;

import static com.example.android.geotrackshare.MainActivity.BICYCLE;
import static com.example.android.geotrackshare.MainActivity.CAR;
import static com.example.android.geotrackshare.MainActivity.WALK;
import static com.example.android.geotrackshare.MainActivity.mCategories;

/**
 * Created by Marcin on 2017-09-12.
 */

public class ModeSettingsActivity extends AppCompatActivity {

    public static boolean preferenceBooleanTheme;
    RunTypesAdapterNoUI mAdapter;
    int description;
    int interval;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        switchThemeS();
        setContentView(R.layout.mode_settings);

        ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
            actionBar.setDisplayShowTitleEnabled(true);
        }
        mAdapter = new RunTypesAdapterNoUI(this, mCategories);
        TextView modeDesctription = findViewById(R.id.mode_description);
        TextView modeInterval = findViewById(R.id.interval_value);

        Intent intent = getIntent();
        if ((WALK).equals(intent.getAction())) {
            description = mAdapter.getItem(0).getDescription();
            interval = (int) mAdapter.getItem(0).getIntervalPreset();
        } else if ((BICYCLE).equals(intent.getAction())) {
            description = mAdapter.getItem(1).getDescription();
            interval = (int) mAdapter.getItem(1).getIntervalPreset();
        } else if ((CAR).equals(intent.getAction())) {
            description = mAdapter.getItem(2).getDescription();
            interval = (int) mAdapter.getItem(2).getIntervalPreset();
        }
        modeDesctription.setText(description);

        String intervalValue = String.valueOf(interval / 1000);
        modeInterval.setText(intervalValue + " s");
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


}