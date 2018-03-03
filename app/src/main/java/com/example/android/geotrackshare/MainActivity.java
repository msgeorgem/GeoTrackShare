package com.example.android.geotrackshare;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.Toast;

import static com.example.android.geotrackshare.AdvancedSettingsActivity.preferenceBooleanScreenOn;
import static com.example.android.geotrackshare.AdvancedSettingsActivity.preferenceBooleanTheme;


public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    public static final String LOG_TAG = MainActivity.class.getName();
    public static ConnectivityManager cm;


    SharedPreferences sharedPrefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        switchTheme();
        setContentView(R.layout.activity_main);
        switchScreenOn();

        // Find the view pager that will allow the user to swipe between fragments
        final ViewPager viewPager = findViewById(R.id.viewpager);
        //viewPager.setPageTransformer(true, new StackTransformer());
        // Create an adapter that knows which fragment should be shown on each page
        CategoryAdapter adapter = new CategoryAdapter(this, getSupportFragmentManager());

        // Set the adapter onto the view pager
        viewPager.setAdapter(adapter);

        // Find the tab layout that shows the tabs
        final TabLayout tabLayout = findViewById(R.id.tabs);

        // Connect the tab layout with the view pager. This will
        //   1. Update the tab layout when the view pager is swiped
        //   2. Update the view pager when a tab is selected
        //   3. Set the tab layout's tab names with the view pager's adapter's titles
        //      by calling onPageTitle()

        tabLayout.post(new Runnable() {
            @Override
            public void run() {
                tabLayout.setupWithViewPager(viewPager);
            }
        });

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
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Intent settingsIntent = new Intent(this, SettingsActivity.class);
            startActivity(settingsIntent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.dev_mode) {
            // Handle the camera action
        } else if (id == R.id.foot_mode) {

        } else if (id == R.id.bike_mode) {

        } else if (id == R.id.car_mode) {

        } else if (id == R.id.find_me_mode) {

        } else if (id == R.id.follow_me_mode) {

        } else if (id == R.id.advanced_settings) {
            Intent settingsIntent = new Intent(this, AdvancedSettingsActivity.class);
            startActivity(settingsIntent);
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}