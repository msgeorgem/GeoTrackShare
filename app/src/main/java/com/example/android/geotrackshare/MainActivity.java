package com.example.android.geotrackshare;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.Toast;

import com.example.android.geotrackshare.RunTypes.RunType;
import com.example.android.geotrackshare.Sync.GeoTrackShareSyncUtils;
import com.example.android.geotrackshare.TrackList.RunListFragment;

import java.util.ArrayList;

import static com.example.android.geotrackshare.AdvancedSettingsActivity.preferenceBooleanScreenOn;
import static com.example.android.geotrackshare.AdvancedSettingsActivity.preferenceBooleanTheme;


public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    public static final String LOG_TAG = MainActivity.class.getName();
    public static ConnectivityManager cm;
    public static String BICYCLE = "BICYCLE";
    public static String WALK = "WALK";
    public static String CAR = "CAR";
    public static String EXPORTIMPORT = "EXPORTIMPORT";
    public static ArrayList<RunType> mCategories;
    public static SharedPreferences mSharedPrefsRunType;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        switchTheme();
        setContentView(R.layout.activity_main);
        switchScreenOn();
        mSharedPrefsRunType = getSharedPreferences("Run_Type", Context.MODE_PRIVATE);
        GeoTrackShareSyncUtils.initialize(this);


        // Spinner Drop down elements
        mCategories = new ArrayList<RunType>();
        mCategories.add(new RunType(R.drawable.ic_directions_walk_black_24dp,
                R.string.Run_type_walk, R.string.Run_type_walk_desc, 5000, 0.0));
        mCategories.add(new RunType(R.drawable.ic_directions_bike_black_24dp,
                R.string.Run_type_bike, R.string.Run_type_bike_desc, 6000, 0.0));
        mCategories.add(new RunType(R.drawable.ic_directions_car_black_24dp,
                R.string.Run_type_car, R.string.Run_type_car_desc, 10000, 0.0));
        mCategories.add(new RunType(R.drawable.ic_developer_board_black_48dp,
                R.string.Run_type_custom, R.string.Run_type_custom_desc, 9999, 0.0));

        BottomNavigationView bottomNavigationView = findViewById(R.id.navigation);

        bottomNavigationView.setOnNavigationItemSelectedListener
                (new BottomNavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        Fragment selectedFragment = null;
                        switch (item.getItemId()) {
                            case R.id.navigation_map:
                                selectedFragment = MapFragmentLive.newInstance();
                                break;
                            case R.id.navigation_data:
                                selectedFragment = RealTimeFragment.newInstance();
                                break;
                            case R.id.navigation_list:
                                selectedFragment = RunListFragment.newInstance();
                                break;
                            case R.id.navigation_buddies:
                                selectedFragment = RunListFragment.newInstance();
                                break;
                        }
                        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
//                        transaction.setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out, android.R.animator.fade_in, android.R.animator.fade_out);
                        transaction.replace(R.id.container, selectedFragment);
                        transaction.commit();
                        return true;
                    }
                });

        //Manually displaying the first fragment - one time only
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
//        transaction.setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out, android.R.animator.fade_in, android.R.animator.fade_out);
        transaction.replace(R.id.container, MapFragmentLive.newInstance());
        transaction.commit();

        // Find the toolbar view inside the activity layout
        Toolbar toolbar = findViewById(R.id.toolbar);
        // Sets the Toolbar to act as the ActionBar for this Activity window.
        // Make sure the toolbar exists in the activity and is not null
        setSupportActionBar(toolbar);

        cm = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);


        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

    }

    public void switchTheme() {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        boolean themeBoolean = sharedPrefs.getBoolean("theme_switch", preferenceBooleanTheme);
        if (!themeBoolean) {
            this.setTheme(R.style.AppTheme);
            Toast.makeText(this, "Light mode", Toast.LENGTH_SHORT).show();
        } else {
            this.setTheme(R.style.AppThemeDarkTheme);
            Toast.makeText(this, "Darkness mode", Toast.LENGTH_SHORT).show();
        }
    }

    public void switchScreenOn() {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        boolean screenOnBoolean = sharedPrefs.getBoolean("screen_on_switch", preferenceBooleanScreenOn);
        if (screenOnBoolean) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
    }
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.dev_mode) {
            Intent settingsIntent = new Intent(this, CustomSettingsActivity.class);
            startActivity(settingsIntent);
        } else if (id == R.id.foot_mode) {
            Intent settingsIntent = new Intent(this, ModeSettingsActivity.class);
            settingsIntent.setAction(WALK);
            startActivity(settingsIntent);
        } else if (id == R.id.bike_mode) {
            Intent settingsIntent = new Intent(this, ModeSettingsActivity.class);
            settingsIntent.setAction(BICYCLE);
            startActivity(settingsIntent);
        } else if (id == R.id.car_mode) {
            Intent settingsIntent = new Intent(this, ModeSettingsActivity.class);
            settingsIntent.setAction(CAR);
            startActivity(settingsIntent);
        } else if (id == R.id.advanced_settings) {
            Intent settingsIntent = new Intent(this, AdvancedSettingsActivity.class);
            startActivity(settingsIntent);
        } else if (id == R.id.export_import) {
            Intent settingsIntent = new Intent(this, ModeSettingsActivity.class);
            settingsIntent.setAction(EXPORTIMPORT);
            startActivity(settingsIntent);
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

}