package com.example.android.geotrackshare;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.geotrackshare.Data.TrackContract;
import com.example.android.geotrackshare.Utils.DistanceCalculator;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import static com.example.android.geotrackshare.Data.TrackContract.TrackingEntry.COLUMN_ALTITUDE;
import static com.example.android.geotrackshare.Data.TrackContract.TrackingEntry.COLUMN_AVR_SPEED;
import static com.example.android.geotrackshare.Data.TrackContract.TrackingEntry.COLUMN_DISTANCE;
import static com.example.android.geotrackshare.Data.TrackContract.TrackingEntry.COLUMN_LATITUDE;
import static com.example.android.geotrackshare.Data.TrackContract.TrackingEntry.COLUMN_LONGITUDE;
import static com.example.android.geotrackshare.Data.TrackContract.TrackingEntry.COLUMN_MAX_ALT;
import static com.example.android.geotrackshare.Data.TrackContract.TrackingEntry.COLUMN_MAX_SPEED;
import static com.example.android.geotrackshare.Data.TrackContract.TrackingEntry.COLUMN_MIN_ALT;
import static com.example.android.geotrackshare.Data.TrackContract.TrackingEntry.COLUMN_RUN_ID;
import static com.example.android.geotrackshare.Data.TrackContract.TrackingEntry.COLUMN_SPEED;
import static com.example.android.geotrackshare.Data.TrackContract.TrackingEntry.COLUMN_TIME;
import static com.example.android.geotrackshare.Data.TrackContract.TrackingEntry.COLUMN_TIME_COUNTER;
import static com.example.android.geotrackshare.Data.TrackContract.TrackingEntry.COLUMN_TOTAL_DISTANCE;
import static com.example.android.geotrackshare.Data.TrackContract.TrackingEntry.CONTENT_URI;
import static com.example.android.geotrackshare.Data.TrackContract.TrackingEntry._ID;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link RealTimeFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link RealTimeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class RealTimeFragment extends Fragment {
    private static final String TAG = MainActivity.class.getSimpleName();

    /**
     * Code used in requesting runtime permissions.
     */
    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 34;

    /**
     * Constant used in the location settings dialog.
     */
    private static final int REQUEST_CHECK_SETTINGS = 0x1;

    /**
     * The desired interval for location updates. Inexact. Updates may be more or less frequent.
     */
    private static final long UPDATE_INTERVAL_IN_MILLISECONDS = 5000; // 10 sec
    /**
     * The fastest rate for active location updates. Exact. Updates will never be more frequent
     * than this value.
     */
    private static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS =
            UPDATE_INTERVAL_IN_MILLISECONDS / 5;
    // Keys for storing activity state in the Bundle.
    private final static String KEY_REQUESTING_LOCATION_UPDATES = "requesting-location-updates";
    private final static String KEY_LOCATION = "location";
    private final static String KEY_ALTITUDE = "altitude";
    private final static String KEY_SPEED = "speed";
    private final static String KEY_LAST_UPDATED_TIME_STRING = "last-updated-time-string";
    private static int DISPLACEMENT = 5; // 10 meters
    long startTime;
    /**
     * Provides access to the Fused Location Provider API.
     */
    private FusedLocationProviderClient mFusedLocationClient;
    /**
     * Provides access to the Location Settings API.
     */
    private SettingsClient mSettingsClient;
    /**
     * Stores parameters for requests to the FusedLocationProviderApi.
     */
    private LocationRequest mLocationRequest;
    /**
     * Stores the types of location services the client is interested in using. Used for checking
     * settings to determine if the device has optimal location settings.
     */
    private LocationSettingsRequest mLocationSettingsRequest;
    /**
     * Callback for Location events.
     */
    private LocationCallback mLocationCallback;
    /**
     * Represents a geographical location.
     */
    private Location mCurrentLocation;
    // UI Widgets.
    private Button mStartUpdatesButton;
    private Button mStopUpdatesButton;
    private TextView mLastUpdateTimeTextView;
    private TextView mLatitudeTextView;
    private TextView mLongitudeTextView;
    private TextView mAltitudeTextView;
    private TextView mSpeedTextView;
    private TextView mMaxSpeedTextView;
    private TextView mAvgSpeedTextView;
    private TextView mMinAltitudeTextView;
    private TextView mMaxAltitudeTextView;
    private TextView mElapsedTimeTextView;
    private TextView mTotalDistanceTextView;
    private TextView mRunNumber;
    private View mLocation;
    // Labels.
    private String mLatitudeLabel;
    private String mLongitudeLabel;
    private String mLastUpdateTimeLabel;
    private String mAltitudeLabel;
    private String mSpeedLabel;
    private String mMaxSpeedLabel;
    private String mAvgSpeedLabel;
    private String mMinAltitudeLabel;
    private String mMaxAltitudeLabel;
    private String mElapsedTimeLabel;
    private String mLastRunLabel;
    private String mCurrentRunLabel;
    private String mDistanceLabel;
    private int mMaxId, mCurrentId;
    private double mCurrentLatitude, mCurrentLongitude, mCurrentAltitude, mCurrentSpeed, mMaxSpeed,
            mAverageSpeed, mMaxAltitude, mMinAltitude, mTotalTime, mDistance, mTotalDistance,
            mPreviousLatitude, mPreviousLongitude;
    private Cursor cur;
    /**
     * Tracks the status of the location updates request. Value changes when the user presses the
     * Start Updates and Stop Updates buttons.
     */
    private Boolean mRequestingLocationUpdates;

    /**
     * Time when the location was updated represented as a String.
     */
    private String mLastUpdateTime, mElapsedTime;

    public RealTimeFragment() {
        // Required empty public constructor
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_real_time, container, false);
        // Locate the UI widgets.
        mRunNumber = v.findViewById(R.id.run_number);
        mLocation = v.findViewById(R.id.locate_on_map);
        mStartUpdatesButton = v.findViewById(R.id.start_updates_button);
        mStopUpdatesButton = v.findViewById(R.id.stop_updates_button);
        mLatitudeTextView = v.findViewById(R.id.latitude_text);
        mLongitudeTextView = v.findViewById(R.id.longitude_text);
        mAltitudeTextView = v.findViewById(R.id.altitude_text);
        mSpeedTextView = v.findViewById(R.id.speed_text);
        mLastUpdateTimeTextView = v.findViewById(R.id.last_update_time_text);
        mMaxSpeedTextView = v.findViewById(R.id.max_speed);
        mAvgSpeedTextView = v.findViewById(R.id.avg_speed);
        mMinAltitudeTextView = v.findViewById(R.id.min_alt);
        mMaxAltitudeTextView = v.findViewById(R.id.max_alt);
        mElapsedTimeTextView = v.findViewById(R.id.total_time);
        mTotalDistanceTextView = v.findViewById(R.id.total_distance);



        // Set labels.
        mCurrentRunLabel = "Current Run Number";
        mLastRunLabel = "Last Run Number";
        mLatitudeLabel = getResources().getString(R.string.latitude_label);
        mLongitudeLabel = getResources().getString(R.string.longitude_label);
        mAltitudeLabel = getResources().getString(R.string.altitude_label);
        mSpeedLabel = getResources().getString(R.string.speed_label);
        mLastUpdateTimeLabel = getResources().getString(R.string.last_update_time_label);
        mMaxSpeedLabel = "Max Speed";
        mAvgSpeedLabel = "Avg Speed";
        mMinAltitudeLabel = "Minimum Altitude";
        mMaxAltitudeLabel = "Maximum Altitude";
        mElapsedTimeLabel = "Elapsed Time";
        mDistanceLabel = "Total distance";

        mRequestingLocationUpdates = false;
        mLastUpdateTime = "";
        mElapsedTime = "";


        // Update values using data stored in the Bundle.
        updateValuesFromBundle(savedInstanceState);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity());
        mSettingsClient = LocationServices.getSettingsClient(getActivity());

        // Kick off the process of building the LocationCallback, LocationRequest, and
        // LocationSettingsRequest objects.
        createLocationCallback();
        createLocationRequest();
        buildLocationSettingsRequest();

        mRunNumber.setText(String.format(Locale.ENGLISH, "%s: %s",
                mLastRunLabel, queryMaxId()));

        mLocation.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                String uri = String.format(Locale.ENGLISH, "geo:%f,%f", mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                startActivity(intent);
            }
        });

        mStartUpdatesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startUpdatesButtonHandler();
                mCurrentId = queryMaxId() + 1;
                startTime = System.currentTimeMillis();
                mRunNumber.setText(String.format(Locale.ENGLISH, "%s: %s",
                        mCurrentRunLabel, mCurrentId));
            }
        });

        mStopUpdatesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stopUpdatesButtonHandler();
                mRunNumber.setText(String.format(Locale.ENGLISH, "%s: %s",
                        mLastRunLabel, mCurrentId));

            }
        });

        return v;
    }


    /**
     * Updates fields based on data stored in the bundle.
     *
     * @param savedInstanceState The activity state saved in the Bundle.
     */
    private void updateValuesFromBundle(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            // Update the value of mRequestingLocationUpdates from the Bundle, and make sure that
            // the Start Updates and Stop Updates buttons are correctly enabled or disabled.
            if (savedInstanceState.keySet().contains(KEY_REQUESTING_LOCATION_UPDATES)) {
                mRequestingLocationUpdates = savedInstanceState.getBoolean(
                        KEY_REQUESTING_LOCATION_UPDATES);
            }

            // Update the value of mCurrentLocation from the Bundle and update the UI to show the
            // correct latitude and longitude.
            if (savedInstanceState.keySet().contains(KEY_LOCATION)) {
                // Since KEY_LOCATION was found in the Bundle, we can be sure that mCurrentLocation
                // is not null.
                mCurrentLocation = savedInstanceState.getParcelable(KEY_LOCATION);
            }
            // Update the value of mCurrentLocation from the Bundle and update the UI to show the
            // correct latitude and longitude.
            if (savedInstanceState.keySet().contains(KEY_ALTITUDE)) {
                // Since KEY_ALTITUDE was found in the Bundle, we can be sure that mCurrentLocation
                // is not null.
                mCurrentLocation = savedInstanceState.getParcelable(KEY_ALTITUDE);
            }
            if (savedInstanceState.keySet().contains(KEY_SPEED)) {
                // Since KEY_SPEED was found in the Bundle, we can be sure that mCurrentLocation
                // is not null.
                mCurrentLocation = savedInstanceState.getParcelable(KEY_SPEED);
            }

            // Update the value of mLastUpdateTime from the Bundle and update the UI.
            if (savedInstanceState.keySet().contains(KEY_LAST_UPDATED_TIME_STRING)) {
                mLastUpdateTime = savedInstanceState.getString(KEY_LAST_UPDATED_TIME_STRING);
            }
            updateUI();
        }
    }

    /**
     * Sets up the location request. Android has two location request settings:
     * {@code ACCESS_COARSE_LOCATION} and {@code ACCESS_FINE_LOCATION}. These settings control
     * the accuracy of the current location. This sample uses ACCESS_FINE_LOCATION, as defined in
     * the AndroidManifest.xml.
     * <p/>
     * When the ACCESS_FINE_LOCATION setting is specified, combined with a fast update
     * interval (5 seconds), the Fused Location Provider API returns location updates that are
     * accurate to within a few feet.
     * <p/>
     * These settings are appropriate for mapping applications that show real-time location
     * updates.
     */
    private void createLocationRequest() {
        mLocationRequest = new LocationRequest();

        // Sets the desired interval for active location updates. This interval is
        // inexact. You may not receive updates at all if no location sources are available, or
        // you may receive them slower than requested. You may also receive updates faster than
        // requested if other applications are requesting location at a faster interval.
        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);

        // Sets the fastest rate for active location updates. This interval is exact, and your
        // application will never receive updates faster than this value.
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);

        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
//        mLocationRequest.setSmallestDisplacement(DISPLACEMENT);
    }

    /**
     * Creates a callback for receiving location events.
     */
    private void createLocationCallback() {
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);

                mCurrentLocation = locationResult.getLastLocation();
                mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
                updateLocationUI();
            }
        };
    }

    @SuppressLint("DefaultLocale")
    private void getElapsedTime() {

        // Get elapsed time in milliseconds
        long elapsedTimeMillis = System.currentTimeMillis() - startTime;

        mElapsedTime = String.format("%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(elapsedTimeMillis),
                TimeUnit.MILLISECONDS.toMinutes(elapsedTimeMillis) % TimeUnit.HOURS.toMinutes(1),
                TimeUnit.MILLISECONDS.toSeconds(elapsedTimeMillis) % TimeUnit.MINUTES.toSeconds(1));

        mElapsedTimeTextView.setText(String.format(Locale.ENGLISH, "%s: %s",
                mElapsedTimeLabel, mElapsedTime));
    }



    /**
     * Uses a {@link com.google.android.gms.location.LocationSettingsRequest.Builder} to build
     * a {@link com.google.android.gms.location.LocationSettingsRequest} that is used for checking
     * if a device has the needed location settings.
     */
    private void buildLocationSettingsRequest() {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        mLocationSettingsRequest = builder.build();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            // Check for the integer request code originally supplied to startResolutionForResult().
            case REQUEST_CHECK_SETTINGS:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        Log.i(TAG, "User agreed to make required location settings changes.");
                        // Nothing to do. startLocationupdates() gets called in onResume again.
                        break;
                    case Activity.RESULT_CANCELED:
                        Log.i(TAG, "User chose not to make required location settings changes.");
                        mRequestingLocationUpdates = false;
                        updateUI();
                        break;
                }
                break;
        }
    }

    /**
     * Handles the Start Updates button and requests start of location updates. Does nothing if
     * updates have already been requested.
     */
    public void startUpdatesButtonHandler() {
        if (!mRequestingLocationUpdates) {
            mRequestingLocationUpdates = true;
            setButtonsEnabledState();
            startLocationUpdates();
        }
    }

    /**
     * Handles the Stop Updates button, and requests removal of location updates.
     */
    public void stopUpdatesButtonHandler() {
        // It is a good practice to remove location requests when the activity is in a paused or
        // stopped state. Doing so helps battery performance and is especially
        // recommended in applications that request frequent location updates.
//        stopLocationUpdates();
    }

    /**
     * Requests location updates from the FusedLocationApi. Note: we don't call this unless location
     * runtime permission has been granted.
     */
    private void startLocationUpdates() {
        // Begin by checking if the device has the necessary location settings.
        mSettingsClient.checkLocationSettings(mLocationSettingsRequest)
                .addOnSuccessListener(getActivity(), new OnSuccessListener<LocationSettingsResponse>() {
                    @SuppressLint("MissingPermission")
                    @Override
                    public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                        Log.i(TAG, "All location settings are satisfied.");


                        mFusedLocationClient.requestLocationUpdates(mLocationRequest,
                                mLocationCallback, Looper.myLooper());

                        updateUI();
                    }
                })
                .addOnFailureListener(getActivity(), new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        int statusCode = ((ApiException) e).getStatusCode();
                        switch (statusCode) {
                            case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                                Log.i(TAG, "Location settings are not satisfied. Attempting to upgrade " +
                                        "location settings ");
                                try {
                                    // Show the dialog by calling startResolutionForResult(), and check the
                                    // result in onActivityResult().
                                    ResolvableApiException rae = (ResolvableApiException) e;
                                    rae.startResolutionForResult(getActivity(), REQUEST_CHECK_SETTINGS);
                                } catch (IntentSender.SendIntentException sie) {
                                    Log.i(TAG, "PendingIntent unable to execute request.");
                                }
                                break;
                            case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                                String errorMessage = "Location settings are inadequate, and cannot be " +
                                        "fixed here. Fix in Settings.";
                                Log.e(TAG, errorMessage);
                                Toast.makeText(getActivity(), errorMessage, Toast.LENGTH_LONG).show();
                                mRequestingLocationUpdates = false;
                        }

                        updateUI();
                    }
                });
    }

    /**
     * Updates all UI fields.
     */
    private void updateUI() {
        setButtonsEnabledState();
        updateLocationUI();
    }

    /**
     * Disables both buttons when functionality is disabled due to insuffucient location settings.
     * Otherwise ensures that only one button is enabled at any time. The Start Updates button is
     * enabled if the user is not requesting location updates. The Stop Updates button is enabled
     * if the user is requesting location updates.
     */
    private void setButtonsEnabledState() {
        if (mRequestingLocationUpdates) {
            mStartUpdatesButton.setEnabled(false);
            mStopUpdatesButton.setEnabled(true);
        } else {
            mStartUpdatesButton.setEnabled(true);
            mStopUpdatesButton.setEnabled(false);
        }
    }

    /**
     * Sets the value of the UI fields for the location latitude, longitude and last update time.
     */
    private void updateLocationUI() {
        if (mCurrentLocation != null) {
            mLatitudeTextView.setText(String.format(Locale.ENGLISH, "%s: %f", mLatitudeLabel,
                    mCurrentLocation.getLatitude()));
            mLongitudeTextView.setText(String.format(Locale.ENGLISH, "%s: %f", mLongitudeLabel,
                    mCurrentLocation.getLongitude()));
            mAltitudeTextView.setText(String.format(Locale.ENGLISH, "%s: %f", mAltitudeLabel,
                    mCurrentLocation.getAltitude()));
            mSpeedTextView.setText(String.format(Locale.ENGLISH, "%s: %f", mSpeedLabel,
                    ((mCurrentLocation.getSpeed()) * 3.6)));
            mLastUpdateTimeTextView.setText(String.format(Locale.ENGLISH, "%s: %s",
                    mLastUpdateTimeLabel, mLastUpdateTime));

            mAvgSpeedTextView.setText(String.format(Locale.ENGLISH, "%s: %f",
                    mAvgSpeedLabel, calculateAverageSpeed(mCurrentId)));
            mMaxSpeedTextView.setText(String.format(Locale.ENGLISH, "%s: %f",
                    mMaxSpeedLabel, queryMaxSpeed(mCurrentId)));
            mMaxAltitudeTextView.setText(String.format(Locale.ENGLISH, "%s: %f",
                    mMaxAltitudeLabel, queryMaxAlt(mCurrentId)));
            mMinAltitudeTextView.setText(String.format(Locale.ENGLISH, "%s: %f",
                    mMinAltitudeLabel, queryMinAlt(mCurrentId)));
            mTotalDistanceTextView.setText(String.format(Locale.ENGLISH, "%s: %f",
                    mDistanceLabel, calculateTotalDistance(mCurrentId)));

            mCurrentLatitude = mCurrentLocation.getLatitude();
            mCurrentLongitude = mCurrentLocation.getLongitude();
            mCurrentAltitude = mCurrentLocation.getAltitude();
            mCurrentSpeed = ((mCurrentLocation.getSpeed()) * 3.6);
            mMaxSpeed = queryMaxSpeed(mCurrentId);
            mMaxAltitude = queryMaxAlt(mCurrentId);
            mMinAltitude = queryMinAlt(mCurrentId);
            mAverageSpeed = calculateAverageSpeed(mCurrentId);
            mDistance = calculateDistance(mCurrentId);
            mTotalDistance = calculateTotalDistance(mCurrentId);

            mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
            getElapsedTime();

            try {
                saveItem(mCurrentId, mLastUpdateTime, mCurrentLatitude, mCurrentLongitude,
                        mCurrentAltitude, mMaxAltitude, mMinAltitude, mCurrentSpeed, mMaxSpeed,
                        mAverageSpeed, mElapsedTime, mDistance, mTotalDistance);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private double calculateTotalDistance(int id) {
        int totalDistance = 0;
        String specificID = String.valueOf(id);
        String mSelectionClause = TrackContract.TrackingEntry.COLUMN_RUN_ID;
        String SELECTION = mSelectionClause + " = '" + specificID + "'";
        String[] PROJECTION = {TrackContract.TrackingEntry.COLUMN_DISTANCE};
        try {
            cur = getActivity().getContentResolver()
                    .query(TrackContract.TrackingEntry.CONTENT_URI, PROJECTION, SELECTION, null, null);

            ArrayList<Double> distanceTempList = new ArrayList<>();
            if (cur != null && cur.moveToFirst()) {
                while (cur.moveToNext()) {
                    Double i = cur.getDouble(cur.getColumnIndex(COLUMN_DISTANCE));
                    distanceTempList.add(i);
                }
            }
//            Log.i("Print list", speedTempList.toString());

            for (int i = 0; i < distanceTempList.size(); i++) {
                totalDistance += distanceTempList.get(i);
            }

            if (cur != null) {
                cur.close();
            }

        } catch (Exception e) {
            Log.e("Path Error", e.toString());
        }
        return totalDistance;
    }


    private double calculateDistance(int id) {


        mPreviousLatitude = queryPreviousLocation(id)[0];
        mPreviousLongitude = queryPreviousLocation(id)[1];
        mCurrentLatitude = mCurrentLocation.getLatitude();
        mCurrentLongitude = mCurrentLocation.getLongitude();

        if (mPreviousLatitude != 0.0 && mPreviousLongitude != 0.0) {
            mDistance = DistanceCalculator.greatCircleInKilometers(mPreviousLatitude,
                    mPreviousLongitude, mCurrentLatitude, mCurrentLongitude);
            Log.i("Print PreviousLatitude", String.valueOf(mPreviousLatitude));
            Log.i("Print PreviousLongitude", String.valueOf(mPreviousLongitude));
            Log.i("Print CurrentLatitude", String.valueOf(mCurrentLatitude));
            Log.i("Print CurrentLongitude", String.valueOf(mCurrentLongitude));
            Log.i("Print Distance", String.valueOf(mDistance));
        } else {
            mDistance = 0.0;
        }
        return mDistance;
    }


    private double[] queryPreviousLocation(int id) {
        String specificID = String.valueOf(id);
        String mSelectionClause = TrackContract.TrackingEntry.COLUMN_RUN_ID;
        String SELECTION = mSelectionClause + " = '" + specificID + "'";
        String ORDER = " " + _ID + " DESC LIMIT 1";

        try {
            cur = getActivity().getContentResolver()
                    .query(TrackContract.TrackingEntry.CONTENT_URI, null, SELECTION, null, ORDER);

            if (cur != null && cur.moveToFirst()) {
                do {
                    int latitudeColumnIndex = cur.getColumnIndex(COLUMN_LATITUDE);
                    int longitudeColumnIndex = cur.getColumnIndex(COLUMN_LONGITUDE);
                    mPreviousLatitude = cur.getDouble(latitudeColumnIndex);
                    mPreviousLongitude = cur.getDouble(longitudeColumnIndex);

                } while (cur.moveToNext());
            }
            if (cur != null) {
                cur.close();
            }

        } catch (Exception e) {
            Log.e("Path Error", e.toString());
        }

        double[] mPreviousLocation = new double[2];
        mPreviousLocation[0] = mPreviousLatitude;
        mPreviousLocation[1] = mPreviousLongitude;
        return mPreviousLocation;
    }


    private int queryMaxId() {

        String ORDER = " " + COLUMN_RUN_ID + " DESC LIMIT 1";
        try {
            cur = getActivity().getContentResolver()
                    .query(TrackContract.TrackingEntry.CONTENT_URI, null, null, null, ORDER);

            if (cur != null && cur.moveToFirst()) {
                do {
                    int idColumnIndex = cur.getColumnIndex(COLUMN_RUN_ID);
                    mMaxId = cur.getInt(idColumnIndex);

                } while (cur.moveToNext());
            }
            if (cur != null) {
                cur.close();
            }

        } catch (Exception e) {
            Log.e("Path Error", e.toString());
        }
        return mMaxId;
    }

    public double queryMaxSpeed(int id) {
        String specificID = String.valueOf(id);
        String mSelectionClause = TrackContract.TrackingEntry.COLUMN_RUN_ID;
        String SELECTION = mSelectionClause + " = '" + specificID + "'";
        String ORDER = " " + COLUMN_SPEED + " DESC LIMIT 1";

        try {
            cur = getActivity().getContentResolver()
                    .query(TrackContract.TrackingEntry.CONTENT_URI, null, SELECTION, null, ORDER);

            if (cur != null && cur.moveToFirst()) {
                do {
                    int idColumnIndex = cur.getColumnIndex(COLUMN_SPEED);
                    mMaxSpeed = cur.getInt(idColumnIndex);

                } while (cur.moveToNext());
            }
            if (cur != null) {
                cur.close();
            }

        } catch (Exception e) {
            Log.e("Path Error", e.toString());
        }

        return mMaxSpeed;
    }

    private double queryMaxAlt(int id) {
        String specificID = String.valueOf(id);
        String mSelectionClause = TrackContract.TrackingEntry.COLUMN_RUN_ID;
        String SELECTION = mSelectionClause + " = '" + specificID + "'";
        String ORDER = " " + COLUMN_ALTITUDE + " DESC LIMIT 1";

        try {
            cur = getActivity().getContentResolver()
                    .query(TrackContract.TrackingEntry.CONTENT_URI, null, SELECTION, null, ORDER);

            if (cur != null && cur.moveToFirst()) {
                do {
                    int idColumnIndex = cur.getColumnIndex(COLUMN_ALTITUDE);
                    mMaxAltitude = cur.getDouble(idColumnIndex);

                } while (cur.moveToNext());
            }
            if (cur != null) {
                cur.close();
            }

        } catch (Exception e) {
            Log.e("Path Error", e.toString());
        }
        return mMaxAltitude;
    }

    private double queryMinAlt(int id) {
        String specificID = String.valueOf(id);
        String mSelectionClause = TrackContract.TrackingEntry.COLUMN_RUN_ID;
        String SELECTION = mSelectionClause + " = '" + specificID + "'";
        String ORDER = " " + COLUMN_ALTITUDE + " ASC LIMIT 1";
//        String[] PROJECTION = {TrackContract.TrackingEntry.COLUMN_ALTITUDE};

        try {
            cur = getActivity().getContentResolver()
                    .query(TrackContract.TrackingEntry.CONTENT_URI, null, SELECTION, null, ORDER);

            if (cur != null && cur.moveToFirst()) {
                do {
                    int idColumnIndex = cur.getColumnIndex(COLUMN_ALTITUDE);
                    mMinAltitude = cur.getInt(idColumnIndex);

                } while (cur.moveToNext());
            }
            if (cur != null) {
                cur.close();
            }

        } catch (Exception e) {
            Log.e("Path Error", e.toString());
        }

        return mMinAltitude;
    }

    private double calculateAverageSpeed(int id) {
        int sum = 0;
        int averageSpeed = 0;
        int size;

        String specificID = String.valueOf(id);
        String mSelectionClause = TrackContract.TrackingEntry.COLUMN_RUN_ID;
        String SELECTION = mSelectionClause + " = '" + specificID + "'";
        String[] PROJECTION = {TrackContract.TrackingEntry.COLUMN_SPEED};
        try {
            cur = getActivity().getContentResolver()
                    .query(TrackContract.TrackingEntry.CONTENT_URI, PROJECTION, SELECTION, null, null);

            ArrayList<Double> speedTempList = new ArrayList<>();
            if (cur != null && cur.moveToFirst()) {
                while (cur.moveToNext()) {
                    Double i = cur.getDouble(cur.getColumnIndex(COLUMN_SPEED));
                    speedTempList.add(i);
                }
            }
//            Log.i("Print list", speedTempList.toString());

            for (int i = 0; i < speedTempList.size(); i++) {
                sum += speedTempList.get(i);
                size = speedTempList.size();
                averageSpeed = sum / size;
            }

            if (cur != null) {
                cur.close();
            }

        } catch (Exception e) {
            Log.e("Path Error", e.toString());
        }
        return averageSpeed;
    }


    private void saveItem(int runId, String currentTime, double currentLatitude, double currentLongitude,
                          double currentAltitude, double currentMaxAlt, double currentMinAlt,
                          double currentSpeed, double currentMaxSpeed, double currentAvrSpeed,
                          String currentElapsedTime, double currentDistance, double currentTotalDistance) throws IOException {

        ContentValues values = new ContentValues();
        values.put(COLUMN_RUN_ID, runId);
        values.put(COLUMN_TIME, currentTime);
        values.put(COLUMN_LATITUDE, currentLatitude);
        values.put(COLUMN_LONGITUDE, currentLongitude);
        values.put(COLUMN_ALTITUDE, currentAltitude);
        values.put(COLUMN_MAX_ALT, currentMaxAlt);
        values.put(COLUMN_MIN_ALT, currentMinAlt);
        values.put(COLUMN_SPEED, currentSpeed);
        values.put(COLUMN_MAX_SPEED, currentMaxSpeed);
        values.put(COLUMN_AVR_SPEED, currentAvrSpeed);
        values.put(COLUMN_TIME_COUNTER, currentElapsedTime);
        values.put(COLUMN_DISTANCE, currentDistance);
        values.put(COLUMN_TOTAL_DISTANCE, currentTotalDistance);

        // This is a NEW item, so insert a new item into the provider,
        // returning the content URI for the item item.
        getActivity().getContentResolver().insert(CONTENT_URI, values);

    }
    /**
     * Removes location updates from the FusedLocationApi.
     */
    private void stopLocationUpdates() {
        if (!mRequestingLocationUpdates) {
            Log.d(TAG, "stopLocationUpdates: updates never requested, no-op.");
            return;
        }

        // It is a good practice to remove location requests when the activity is in a paused or
        // stopped state. Doing so helps battery performance and is especially
        // recommended in applications that request frequent location updates.
        mFusedLocationClient.removeLocationUpdates(mLocationCallback)
                .addOnCompleteListener(getActivity(), new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        mRequestingLocationUpdates = false;
                        setButtonsEnabledState();
                    }
                });
    }

    @Override
    public void onResume() {
        super.onResume();
        // Within {@code onPause()}, we remove location updates. Here, we resume receiving
        // location updates if the user has requested them.
        if (mRequestingLocationUpdates && checkPermissions()) {
            startLocationUpdates();
        } else if (!checkPermissions()) {
            requestPermissions();
        }

        updateUI();
    }

    @Override
    public void onPause() {
        super.onPause();

        // Remove location updates to save battery.
        stopLocationUpdates();
    }

    /**
     * Stores activity data in the Bundle.
     */
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putBoolean(KEY_REQUESTING_LOCATION_UPDATES, mRequestingLocationUpdates);
        savedInstanceState.putParcelable(KEY_LOCATION, mCurrentLocation);
        savedInstanceState.putParcelable(KEY_ALTITUDE, mCurrentLocation);
        savedInstanceState.putParcelable(KEY_SPEED, mCurrentLocation);
        savedInstanceState.putString(KEY_LAST_UPDATED_TIME_STRING, mLastUpdateTime);
        super.onSaveInstanceState(savedInstanceState);
    }

    /**
     * Shows a {@link Snackbar}.
     *
     * @param mainTextStringId The id for the string resource for the Snackbar text.
     * @param actionStringId   The text of the action item.
     * @param listener         The listener associated with the Snackbar action.
     */
    private void showSnackbar(final int mainTextStringId, final int actionStringId,
                              View.OnClickListener listener) {
        Snackbar.make(
                getActivity().findViewById(android.R.id.content),
                getString(mainTextStringId),
                Snackbar.LENGTH_INDEFINITE)
                .setAction(getString(actionStringId), listener).show();
    }

    /**
     * Return the current state of the permissions needed.
     */
    private boolean checkPermissions() {
        int permissionState = ActivityCompat.checkSelfPermission(getActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION);
        return permissionState == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermissions() {
        boolean shouldProvideRationale =
                ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                        Manifest.permission.ACCESS_FINE_LOCATION);

        // Provide an additional rationale to the user. This would happen if the user denied the
        // request previously, but didn't check the "Don't ask again" checkbox.
        if (shouldProvideRationale) {
            Log.i(TAG, "Displaying permission rationale to provide additional context.");
            showSnackbar(R.string.permission_rationale,
                    android.R.string.ok, new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            // Request permission
                            ActivityCompat.requestPermissions(getActivity(),
                                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                    REQUEST_PERMISSIONS_REQUEST_CODE);
                        }
                    });
        } else {
            Log.i(TAG, "Requesting permission");
            // Request permission. It's possible this can be auto answered if device policy
            // sets the permission in a given state or the user denied the permission
            // previously and checked "Never ask again".
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_PERMISSIONS_REQUEST_CODE);
        }
    }

    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        Log.i(TAG, "onRequestPermissionResult");
        if (requestCode == REQUEST_PERMISSIONS_REQUEST_CODE) {
            if (grantResults.length <= 0) {
                // If user interaction was interrupted, the permission request is cancelled and you
                // receive empty arrays.
                Log.i(TAG, "User interaction was cancelled.");
            } else if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (mRequestingLocationUpdates) {
                    Log.i(TAG, "Permission granted, updates requested, starting location updates");
                    startLocationUpdates();
                }
            } else {
                // Permission denied.

                // Notify the user via a SnackBar that they have rejected a core permission for the
                // app, which makes the Activity useless. In a real app, core permissions would
                // typically be best requested during a welcome-screen flow.

                // Additionally, it is important to remember that a permission might have been
                // rejected without asking the user for permission (device policy or "Never ask
                // again" prompts). Therefore, a user interface affordance is typically implemented
                // when permissions are denied. Otherwise, your app could appear unresponsive to
                // touches or interactions which have required permissions.
                showSnackbar(R.string.permission_denied_explanation,
                        R.string.settings, new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                // Build intent that displays the App settings screen.
                                Intent intent = new Intent();
                                intent.setAction(
                                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                Uri uri = Uri.fromParts("package",
                                        BuildConfig.APPLICATION_ID, null);
                                intent.setData(uri);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                            }
                        });
            }
        }
    }
}

