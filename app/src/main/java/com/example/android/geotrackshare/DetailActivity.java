package com.example.android.geotrackshare;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.NavUtils;
import android.support.v4.app.ShareCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.example.android.geotrackshare.Data.TrackContract;
import com.example.android.geotrackshare.TrackList.RunListFragment;
import com.example.android.geotrackshare.databinding.ActivityDetailBinding;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;

import static com.example.android.geotrackshare.AdvancedSettingsActivity.preferenceBooleanTheme;
import static com.example.android.geotrackshare.Data.TrackContract.TrackingEntry.COLUMN_ALTITUDE;
import static com.example.android.geotrackshare.Data.TrackContract.TrackingEntry.COLUMN_LATITUDE;
import static com.example.android.geotrackshare.Data.TrackContract.TrackingEntry.COLUMN_LONGITUDE;
import static com.example.android.geotrackshare.Data.TrackContract.TrackingEntry.COLUMN_RUN_ID;
import static com.example.android.geotrackshare.Data.TrackContract.TrackingEntry.COLUMN_SPEED;
import static com.example.android.geotrackshare.Data.TrackContract.TrackingEntry.COLUMN_TIME;
import static com.example.android.geotrackshare.Data.TrackContract.TrackingEntry.COLUMN_TIME_COUNTER;
import static com.example.android.geotrackshare.Data.TrackContract.TrackingEntry.COLUMN_TOTAL_DISTANCE;
import static com.example.android.geotrackshare.Data.TrackContract.TrackingEntry.CONTENT_URI;
import static com.example.android.geotrackshare.Data.TrackContract.TrackingEntry._ID;
import static com.example.android.geotrackshare.TrackList.RunListFragment.EXTRA_TIME;

/**
 * Created by Marcin on 2017-11-29.
 */

public class DetailActivity extends AppCompatActivity implements OnMapReadyCallback {


    public static final String TEST_MDB_MOVIE_PATH = "https://api.themoviedb.org/3/movie/321612/videos?api_key=1157007d8e3f7d5e0af6d7e4165e2730";
    public static final String LOG_TAG = DetailActivity.class.getSimpleName();
    private static final String BUNDLE_RECYCLER_LAYOUT = "DetailActivity.clipsRecyclerView.activity_detail";
    //    private static final String API_key = BuildConfig.API_KEY;
    private static final String[] PROJECTION01 = {
            TrackContract.TrackingEntry._ID,
            TrackContract.TrackingEntry.COLUMN_TIME,
    };
    private static final String[] PROJECTION02 = {
            _ID,
            COLUMN_RUN_ID,
            COLUMN_LATITUDE,
            COLUMN_LONGITUDE
    };

    public static String CURRENT_RUN_ID;
    public static SharedPreferences favPrefs;
    private final String MDB_SHARE_HASHTAG = "IMDB Source";

    private String mMovieSummary;
    private Context context;
    private ToggleButton FAVtoggleButton;
    private String currentRun, currentTimeStart;
    private long currentRunId;
    private String runId;
    private int runIdInt;
    private Uri mCurrentItemUri;
    private ActivityDetailBinding mDetailBinding;
    private Cursor cur, cur0, cur1;
    private GoogleMap mMap;
    private ArrayList<LatLng> coordinatesList;
    private Location mCurrentLocation;
    private LatLng here;
    private double[] mStartLocation = new double[2];
    private double[] mStopLocation = new double[2];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        switchThemeD();
        mDetailBinding = DataBindingUtil.setContentView(this, R.layout.activity_detail);


        SupportMapFragment fm = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        fm.getMapAsync(this);


        // Find the toolbar view inside the activity layout
        Toolbar toolbar = findViewById(R.id.toolbar);
        // Sets the Toolbar to act as the ActionBar for this Activity window.
        // Make sure the toolbar exists in the activity and is not null
        //setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        // Examine the intent that was used to launch this activity,
        // in order to figure out if we're creating a new item or editing an existing one.
        Intent intent = getIntent();
        mCurrentItemUri = intent.getData();


        currentRun = intent.getStringExtra(RunListFragment.EXTRA_RUN_ID);
        currentTimeStart = intent.getStringExtra(EXTRA_TIME);

        mDetailBinding.part2.runId.setText(currentRun);
        mDetailBinding.part2.startTime.setText(currentTimeStart);

        currentRunId = Long.parseLong(currentRun);
        runIdInt = Integer.parseInt(currentRun);
        queryCoordinatesList(runIdInt);

        context = mDetailBinding.part2.favDetToggleButton.getContext();
        FAVtoggleButton = mDetailBinding.part2.favDetToggleButton;
        FAVtoggleButton.setChecked(true);

        Boolean a = checkIfInFavorites();

        if (a) {
            FAVtoggleButton.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.star_star));
            FAVtoggleButton.setChecked(true);
        } else {
            FAVtoggleButton.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.star_red));
            FAVtoggleButton.setChecked(false);
        }
        FAVtoggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {

                if (isChecked) {
//                    try {
//                        saveItem();
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }

                    FAVtoggleButton.setBackgroundDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.star_star));
                    SharedPreferences.Editor editor = favPrefs.edit();
                    editor.putBoolean("On", true);
                    editor.apply();

                } else {
                    FAVtoggleButton.setBackgroundDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.star_red));
                    SharedPreferences.Editor editor = favPrefs.edit();
                    editor.putBoolean("On", false);
                    editor.apply();
//                    delete(currentRunId);
                }
            }
        });


    }

    private void switchThemeD() {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        boolean themeBoolean = sharedPrefs.getBoolean("theme_switch", preferenceBooleanTheme);
        if (!themeBoolean) {
            setTheme(R.style.AppTheme);
        } else {
            setTheme(R.style.AppThemeDarkTheme);
        }
    }
    private boolean checkIfInFavorites() {
        cur = getContentResolver().query(CONTENT_URI, null, null, null, null);

        ArrayList<String> favsTempList = new ArrayList<>();
        boolean favourite;
        if (cur != null) {
            while (cur.moveToNext()) {
                String i = cur.getString(cur.getColumnIndex(COLUMN_RUN_ID));
                favsTempList.add(i);
            }
        }
        favourite = favsTempList.contains(runId);

        if (cur != null) {
            cur.close();
        }
        return favourite;
    }

    public void delete(long id) {
        int rowDeleted = getContentResolver().delete(CONTENT_URI, TrackContract.TrackingEntry.COLUMN_RUN_ID + "=" + id, null);
        Toast.makeText(this, rowDeleted + " " + getString(R.string.delete_one_item), Toast.LENGTH_SHORT).show();
    }

    public ArrayList queryCoordinatesList(int id) {
        String[] PROJECTION = {
                COLUMN_RUN_ID,
                COLUMN_TIME,
                COLUMN_LATITUDE,
                COLUMN_LONGITUDE,
                COLUMN_ALTITUDE,
                COLUMN_SPEED,
                COLUMN_TIME_COUNTER,
                COLUMN_TOTAL_DISTANCE
        };
        String specificID = String.valueOf(id);
        String mSelectionClause = TrackContract.TrackingEntry.COLUMN_RUN_ID;
        String SELECTION = mSelectionClause + " = '" + specificID + "'";
        String ORDER = " " + COLUMN_SPEED + " DESC LIMIT 1";

        try {
            cur = getContentResolver()
                    .query(CONTENT_URI, null, SELECTION, null, null);

            coordinatesList = new ArrayList<>();

            if (cur != null && cur.moveToFirst()) {
                while (cur.moveToNext()) {
                    double latitude = cur.getDouble(cur.getColumnIndex(COLUMN_LATITUDE));
                    double longitude = cur.getDouble(cur.getColumnIndex(COLUMN_LONGITUDE));
//                    LatLng latLng = new LatLng(latitude, longitude);
                    here = new LatLng(latitude, longitude);
                    Log.i("Print Current Location1", String.valueOf(here));
                    coordinatesList.add(here);

                }
            }
            if (cur != null) {
                cur.close();
            }

        } catch (Exception e) {
            Log.e("Path Error", e.toString());
        }

        return coordinatesList;

    }

    private double[] startLocation(int id) {

        String specificID = String.valueOf(id);
        String mSelectionClause = TrackContract.TrackingEntry.COLUMN_RUN_ID;
        String SELECTION = mSelectionClause + " = '" + specificID + "'";
        String ORDERASC = " " + _ID + " ASC LIMIT 1";

        try {
            cur = getContentResolver()
                    .query(CONTENT_URI, PROJECTION02, SELECTION, null, ORDERASC);

            if (cur != null && cur.moveToFirst()) {
                do {
                    mStartLocation[0] = cur.getDouble(cur.getColumnIndex(COLUMN_LATITUDE));
                    mStartLocation[1] = cur.getDouble(cur.getColumnIndex(COLUMN_LONGITUDE));

                    Log.i("Start Location", String.valueOf(mStartLocation[0]) +
                            " , " + String.valueOf(mStartLocation[1]));
                }
                while (cur.moveToNext());
            }

            if (cur != null) {
                cur.close();
            }

        } catch (Exception e) {
            Log.e("Path Error", e.toString());
        }
        return mStartLocation;
    }

    private double[] stopLocation(int id) {

        String specificID = String.valueOf(id);
        String mSelectionClause = TrackContract.TrackingEntry.COLUMN_RUN_ID;
        String SELECTION = mSelectionClause + " = '" + specificID + "'";
        String ORDERDESC = " " + _ID + " DESC LIMIT 1";


        try {
            cur = getContentResolver()
                    .query(CONTENT_URI, PROJECTION02, SELECTION, null, ORDERDESC);

            if (cur != null && cur.moveToFirst()) {
                do {
                    mStopLocation[0] = cur.getDouble(cur.getColumnIndex(COLUMN_LATITUDE));
                    mStopLocation[1] = cur.getDouble(cur.getColumnIndex(COLUMN_LONGITUDE));

                    Log.i("Stop Location", String.valueOf(mStopLocation[0]) +
                            " , " + String.valueOf(mStopLocation[1]));
                }
                while (cur.moveToNext());
            }

            if (cur != null) {
                cur.close();
            }

        } catch (Exception e) {
            Log.e("Path Error", e.toString());
        }
        return mStopLocation;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        double mStartLatitude = startLocation(runIdInt)[0];
        double mStartLongitude = startLocation(runIdInt)[1];
        double mStopLatitude = stopLocation(runIdInt)[0];
        double mStopLongitude = stopLocation(runIdInt)[1];

        double mSouthLatitude;
        double mWestLongitude;
        double mNorthLatitude;
        double mEastLongitude;
        if (mStartLatitude > mStopLatitude) {
            mSouthLatitude = mStopLatitude;
            mNorthLatitude = mStartLatitude;
        } else {
            mSouthLatitude = mStartLatitude;
            mNorthLatitude = mStopLatitude;
        }
        if (mStartLongitude > mStopLongitude) {
            mEastLongitude = mStartLongitude;
            mWestLongitude = mStopLongitude;
        } else {
            mEastLongitude = mStopLongitude;
            mWestLongitude = mStartLongitude;
        }

        LatLng mSouthWestPoint = new LatLng(mSouthLatitude, mWestLongitude);
        LatLng mNorthEastPoint = new LatLng(mNorthLatitude, mEastLongitude);

        LatLng mStartPoint = new LatLng(mStartLatitude, mStartLongitude);
        LatLng mStopPoint = new LatLng(mStopLatitude, mStopLongitude);
        LatLng mPoint = new LatLng(0, 0);
        LatLng poznan = new LatLng(52.406374, 16.9251681);
//        LatLngBounds(LatLng southwest, LatLng northeast)
        LatLngBounds TRIP = new LatLngBounds(mSouthWestPoint, mNorthEastPoint);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(TRIP.getCenter(), 15));
        // Add a marker in Poznan, Poland, and move the camera.
        // mMap.addMarker(new MarkerOptions().position(here).title("arker in Poznań"));
        // mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(here,15));
        // mMap.clear();  //clears all Markers and Polylines
        // Instantiates a new Polyline object and adds points to define a rectangle
        PolylineOptions options = new PolylineOptions().width(10).color(Color.BLUE).geodesic(true);

        PolylineOptions rectOptions = new PolylineOptions()
                .add(new LatLng(37.35, -122.0))
                .add(new LatLng(37.45, -122.0))  // North of the previous point, but at the same longitude
                .add(new LatLng(37.45, -122.2))  // Same latitude, and 30km to the west
                .add(new LatLng(37.35, -122.2))  // Same longitude, and 16km to the south
                .add(new LatLng(37.35, -122.0)); // Closes the polyline.

        for (int i = 0; i < coordinatesList.size(); i++) {
            mPoint = coordinatesList.get(i);
            options.add(mPoint);
        }

        mMap.addPolyline(options);

        mMap.addMarker(new MarkerOptions().position(mStopPoint).title("You are here")); //add Marker in current position
        mMap.setMinZoomPreference(1.0f);
        mMap.setMaxZoomPreference(20.0f);

        // Zoom in, animating the camera.
        mMap.animateCamera(CameraUpdateFactory.zoomIn());

        // Zoom out to zoom level 10, animating with a duration of 2 seconds.
        mMap.animateCamera(CameraUpdateFactory.zoomTo(14), 2000, null);

        // Construct a CameraPosition focusing on Mountain View and animate the camera to that position.
//        CameraPosition cameraPosition = new CameraPosition.Builder()
//                .target(mStopPoint)               // Sets the center of the map to Mountain View
//                .zoom(15)                   // Sets the zoom
//                .bearing(0)                // Sets the orientation of the camera to east
//                .tilt(30)                   // Sets the tilt of the camera to 30 degrees
//                .build();                   // Creates a CameraPosition from the builder
//        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

    }


//    private long checkIfDeleted(Long runId){
//        long tempId = 0;
//        if(!(runId == null) || !(runId == 0)){
//            Log.i(LOG_TAG, "not null or zero");
//            tempId = runId;
//        } else tempId = justDeletedMovieId;
//        return tempId;
//    };

    // Get user input from editor and save item into database.
    private void saveItem() {

        ContentValues values = new ContentValues();
        values.put(COLUMN_RUN_ID, runId);
        values.put(COLUMN_TIME, currentTimeStart);
//        values.put(COLUMN_LATITUDE, currentReleaseDate);
//        values.put(COLUMN_LONGITUDE, currentVote);
//        values.put(COLUMN_ALTITUDE, currentOverview);
//        values.put(COLUMN_SPEED currentPoster);

        // This is a NEW item, so insert a new item into the provider,
        // returning the content URI for the item item.
        Uri newUri = getContentResolver().insert(CONTENT_URI, values);

        // Show a toast message depending on whether or not the insertion was successful.
        if (newUri == null) {
            // If the new content URI is null, then there was an error with insertion.
            Toast.makeText(this, getString(R.string.editor_insert_item_failed), Toast.LENGTH_SHORT).show();
        } else {
            // Otherwise, the insertion was successful and we can display a toast.
            Toast.makeText(this, getString(R.string.editor_insert_item_success), Toast.LENGTH_SHORT).show();
        }
        currentRunId = Long.parseLong(runId);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState != null) {
            Parcelable savedRecyclerLayoutState = savedInstanceState.getParcelable(BUNDLE_RECYCLER_LAYOUT);
//            ClipsFragment.clipsRecyclerView.getLayoutManager().onRestoreInstanceState(savedRecyclerLayoutState);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
//        outState.putParcelable(BUNDLE_RECYCLER_LAYOUT, ClipsFragment.clipsRecyclerView.getLayoutManager().onSaveInstanceState());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        /* Use AppCompatActivity's method getMenuInflater to get a handle on the menu inflater */
//        MenuInflater inflater = getMenuInflater();
        /* Use the inflater's inflate method to inflate our menu layout to this menu */
//        inflater.inflate(R.menu.detail, menu);
        /* Return true so that the menu is displayed in the Toolbar */
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();


        if (id == android.R.id.home) {
            NavUtils.navigateUpFromSameTask(DetailActivity.this);
            return true;
        }
        /* Share menu item clicked */
//        if (id == R.id.action_share_d) {
//            Intent shareIntent = createShareMovieIntent();
//            startActivity(shareIntent);
//            return true;
//        }
        return super.onOptionsItemSelected(item);
    }

    private Intent createShareMovieIntent() {
        Intent shareIntent = ShareCompat.IntentBuilder.from(this)
                .setType("text/plain")
                .setText(mMovieSummary + MDB_SHARE_HASHTAG)
                .getIntent();
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
        return shareIntent;
    }


    //TODO (4) and get geolocation for first and last position,
    //TODO (5) show track on map
    //TODO (5) show speeds on graph

}