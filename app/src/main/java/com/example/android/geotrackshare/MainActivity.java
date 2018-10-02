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
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.Toast;

import com.example.android.geotrackshare.RunTypes.RunType;
import com.example.android.geotrackshare.Sync.GeoTrackShareSyncUtils;
import com.example.android.geotrackshare.TrackList.RunListFragment;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import java.util.ArrayList;

import static com.example.android.geotrackshare.AdvancedSettingsActivity.preferenceBooleanScreenOn;
import static com.example.android.geotrackshare.AdvancedSettingsActivity.preferenceBooleanTheme;
import static com.example.android.geotrackshare.LocationService.LocationServiceConstants.setAuthToFirebase;
import static com.example.android.geotrackshare.LocationService.LocationServiceConstants.setUserID;


public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    public static final String TAG = MainActivity.class.getName();
    public static ConnectivityManager cm;
    public static String BICYCLE = "BICYCLE";
    public static String WALK = "WALK";
    public static String CAR = "CAR";
    public static String EXPORTIMPORT = "EXPORTIMPORT";
    public static ArrayList<RunType> mCategories;
    public static SharedPreferences mSharedPrefsRunType;
    private static final int RC_SIGN_IN = 9001;
    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        switchTheme();
        setContentView(R.layout.activity_main);
        switchScreenOn();
        mSharedPrefsRunType = getSharedPreferences("Run_Type", Context.MODE_PRIVATE);
        GeoTrackShareSyncUtils.initialize(this);

        // [START config_signin]
        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        // [END config_signin]

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        // [START initialize_auth]
        mAuth = FirebaseAuth.getInstance();
        // [END initialize_auth]


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
        signIn();
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

    // [START on_start_check_user]
    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
//        Update UI when necessary
//        updateUI(currentUser);
    }

    // [START onactivityresult]
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
                setAuthToFirebase(getApplicationContext(), true);
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Log.w(TAG, "Google sign in failed", e);
                // [START_EXCLUDE]
                updateUI(null);
                setAuthToFirebase(getApplicationContext(), false);
                // [END_EXCLUDE]
            }
        }
    }

    // [END onactivityresult]
// [START auth_with_google]
    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());
        // [START_EXCLUDE silent]
//        showProgressDialog();
        // [END_EXCLUDE]

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            setUserID(getApplicationContext(), user.getUid());
                            updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            Snackbar.make(findViewById(R.id.main_layout), "Authentication Failed.", Snackbar.LENGTH_SHORT).show();
                            updateUI(null);
                        }

                        // [START_EXCLUDE]
//                        hideProgressDialog();
                        // [END_EXCLUDE]
                    }
                });
    }

    // [END auth_with_google]
    // [START signin]
    public void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }
    // [END signin]

    public void signOut() {
        // Firebase sign out
        mAuth.signOut();

        // Google sign out
        mGoogleSignInClient.signOut().addOnCompleteListener(this,
                new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        updateUI(null);
                        setAuthToFirebase(getApplicationContext(), false);
                    }
                });
    }

    private void revokeAccess() {
        // Firebase sign out
        mAuth.signOut();

        // Google revoke access
        mGoogleSignInClient.revokeAccess().addOnCompleteListener(this,
                new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        updateUI(null);
                        setAuthToFirebase(getApplicationContext(), false);
                    }
                });
    }

    private void updateUI(FirebaseUser user) {
//        hideProgressDialog();
        if (user != null) {
//            mStatusTextView.setText(getString(R.string.google_status_fmt, user.getEmail()));
//            mDetailTextView.setText(getString(R.string.firebase_status_fmt, user.getUid()));

//            findViewById(R.id.sign_in_button).setVisibility(View.GONE);
//            findViewById(R.id.sign_out_and_disconnect).setVisibility(View.VISIBLE);
        } else {
//            mStatusTextView.setText(R.string.signed_out);
//            mDetailTextView.setText(null);

//            findViewById(R.id.sign_in_button).setVisibility(View.VISIBLE);
//            findViewById(R.id.sign_out_and_disconnect).setVisibility(View.GONE);
        }
    }
}