package com.example.android.geotrackshare;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.android.geotrackshare.LocationService.LocationUpdatesService;
import com.example.android.geotrackshare.RunTypes.RunType;
import com.example.android.geotrackshare.RunTypes.RunTypesAdapter;
import com.example.android.geotrackshare.LocationService.LocationServiceConstants;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static com.example.android.geotrackshare.AdvancedSettingsActivity.preferenceBooleanDisableAutoStop;
import static com.example.android.geotrackshare.LocationService.LocationUpdatesService.REQUEST_CHECK_SETTINGS;
import static com.example.android.geotrackshare.LocationService.LocationServiceConstants.requestingLocationUpdates;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link RealTimeFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link RealTimeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class RealTimeFragment extends Fragment implements
        SharedPreferences.OnSharedPreferenceChangeListener, AdapterView.OnItemSelectedListener {


    public static final String MY_PREFERENCE_KEY = "mili";
    //    leaving these constants here in case of shared prefs need
    public static final double NOISEd = 0.05;
    public static final double NOISEc = 0.02;
    private static final String TAG = MainActivity.class.getSimpleName();
    /**
     * Code used in requesting runtime permissions.
     */
    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 34;
    /**
     * Time without move.
     */
    private static final long CHECK_NO_MOVE_TIME_IN_MILLISECONDS = 180000; // 3 min
    // Keys for storing activity state in the Bundle.
    private final static String KEY_REQUESTING_LOCATION_UPDATES = "requesting-location-updates";
    private final static String KEY_LOCATION = "location";
    private final static String KEY_ALTITUDE = "altitude";
    private final static String KEY_SPEED = "speed";
    private final static String KEY_LAST_RUN = "KEY_LAST_RUN";
    private final static String KEY_LAST_UPDATED_TIME = "last-updated-time-string";
    private final static String KEY_LAST_UPDATED_ETIME = "last-updated-elapsedtime-string";
    private final static String KEY_LAST_UPDATED_TDISTANCE = "last-updated-total-distance";
    /**
     * The desired interval for location updates. Inexact. Updates may be more or less frequent.
     */
    public static long UPDATE_INTERVAL_IN_MILLISECONDS = 13000; // 10 sec
    /**
     * The fastest rate for active location updates. Exact. Updates will never be more frequent
     * than this value.
     */

    public static String UPDATE_INTERVAL_IN_MILLISECONDS_STRING = "";
    public static String START_TIME;
    public static String DELETE_LAST_ROWS_STRING = "";
    public static int RUN_TYPE_VALUE;
    public static String RUN_TYPE_KEY = "RUN_TYPE_KEY";
    public static String RUN_TYPE_TTTLE_KEY = "RUN_TYPE_TTTLE_KEY";
    public static String RUN_TYPE_PICTURE_KEY = "RUN_TYPE_PICTURE_KEY";
    public static String RUN_TYPE_DESCRIPTION_KEY = "RUN_TYPE_DESCRIPTION_KEY";
    public static String RUN_TYPE_TITLE;
    public static String RUN_TYPE_DESCRIPTION;
    public static int RUN_TYPE_PICTURE;
    public static int DELETE_LAST_ROWS = 15;
    public static boolean DISABLE_AUTO_CLOSE;
    public static Context mContext;
    public static SharedPreferences sharedPrefs, mSharedPrefsRunType;
    public String tmDevice, tmSerial, androidId, deviceId;
    public TelephonyManager tm;
    /**
     * Time when the location was updated represented as a String.
     */
    public String mLastUpdateTime;
    public String mElapsedTime;
    long startTime = 0;
    UUID deviceUuid;
    View mView;
    // The BroadcastReceiver used to listen from broadcasts from the service.
    private MyReceiver myReceiver;
    /**
     * Provides access to the Fused Location Provider API.
     */
    private FusedLocationProviderClient mFusedLocationClient;
    /**
     * Stores parameters for requests to the FusedLocationProviderApi.
     */
    private LocationRequest mLocationRequest;
    /**
     * Callback for Location events.
     */
    private LocationCallback mLocationCallback;
    // UI Widgets.
    private Button mRequestLocationUpdatesButton;
    private Button mRemoveLocationUpdatesButton;
    private TextView mLastUpdateTimeTextView;
    private TextView mLatitudeTextView;
    private TextView mLongitudeTextView;
    private TextView mPrevLatitudeTextView;
    private TextView mPrevLongitudeTextView;
    private TextView mDevIDTextView;
    private TextView mAltitudeTextView;
    private TextView mSpeedTextView;
    private TextView mMaxSpeedTextView;
    private TextView mAvgSpeedTextView;
    private TextView mMinAltitudeTextView;
    private TextView mMaxAltitudeTextView;
    private TextView mElapsedTimeTextView;
    private TextView mTotalDistanceTextView;
    private TextView mRunNumber;
    private TextView mAddressOutputTextView;
    private TextView mIntervalTextView;
    private TextView mDeleteTextView;
    private View mLocation;
    // Labels.
    private String mLatitudeLabel;
    private String mLongitudeLabel;
    private String mPrevLatitudeLabel;
    private String mPrevLongitudeLabel;
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
    private String mAndroid_idLabel;
    private String mIntervalLabel;
    private String mDeleteLabel;
    private String mAndroid_id;
    private String mCurrentAddress = "";
    private String mStartTimeString;
    private int mMaxId, mCurrentId, mLast_ID;
    private long mLastUpdateTimeMillis, mElapsedTimeMillis;
    private double mCurrentLatitude, mCurrentLongitude, mCurrentAltitude, mCurrentSpeed, mMaxSpeed,
            mAverageSpeed, mMaxAltitude, mMinAltitude, mTotalTime, mDistance, mTotalDistance,
            mPreviousLatitude, mPreviousLongitude, mRoundedDistance;
    private Location mCurrentLocation;
    public static ArrayList<RunType> mCategories;
    private RunTypesAdapter mAdapter;
    private Spinner mSpinner;
    /**
     * Tracks the status of the location updates request. Value changes when the user presses the
     * Start Updates and Stop Updates buttons.
     */
    private Boolean mRequestingLocationUpdates;
    // A reference to the service used to get location updates.
    public static LocationUpdatesService mService = null;
    // Tracks the bound state of the service.
    private boolean mBound = false;
    // Monitors the state of the connection to the service.
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            LocationUpdatesService.LocalBinder binder = (LocationUpdatesService.LocalBinder) service;
            mService = binder.getService();
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mService = null;
            mBound = false;
        }
    };

    public RealTimeFragment() {
        // Required empty public constructor
    }

    @SuppressLint("HardwareIds")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mContext = getActivity();
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        mSharedPrefsRunType = mContext.getSharedPreferences("Run_Type", Context.MODE_PRIVATE);
        mRequestingLocationUpdates = requestingLocationUpdates(mContext);

        DISABLE_AUTO_CLOSE = switchDisableAutoStop();
        myReceiver = new MyReceiver();
        // Check that the user hasn't revoked permissions by going to Settings.
        if (mRequestingLocationUpdates) {
            if (!checkPermissions()) {
                requestPermissions();
            }
        }

        DELETE_LAST_ROWS_STRING = sharedPrefs.getString(
                getString(R.string.delete_loops_by_key),
                getString(R.string.delete_loops_by_default_ultimate)
        );
        DELETE_LAST_ROWS = Integer.parseInt((DELETE_LAST_ROWS_STRING));

        Log.i("delete rows", String.valueOf(DELETE_LAST_ROWS));

        UPDATE_INTERVAL_IN_MILLISECONDS_STRING = sharedPrefs.getString(
                getString(R.string.update_interval_by_key),
                getString(R.string.update_interval_by_default_ultimate)
        );

        UPDATE_INTERVAL_IN_MILLISECONDS = Long.parseLong(UPDATE_INTERVAL_IN_MILLISECONDS_STRING);
        Log.i("update interval", String.valueOf(UPDATE_INTERVAL_IN_MILLISECONDS));

//        Integer i = R.string.update_interval_by_default_ultimate;
//        String numberAsString = "1234";
//
//        long number = Long.parseLong(getResources().getString(R.string.update_interval_by_default_ultimate));
//
//        String key = getString(R.string.update_interval_by_key);


//        UPDATE_INTERVAL_IN_MILLISECONDS = sharedPrefs.getLong(
//                MY_PREFERENCE_KEY,
//                number
//        );
        // Inflate the layout for this fragment
        try {
            if (mView == null) {
                mView = inflater.inflate(R.layout.fragment_real_time, container, false);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        // Locate the UI widgets.
        mRunNumber = mView.findViewById(R.id.run_number);
        mLocation = mView.findViewById(R.id.locate_on_map);
        mRequestLocationUpdatesButton = mView.findViewById(R.id.start_updates_button);
        mRemoveLocationUpdatesButton = mView.findViewById(R.id.stop_updates_button);

        mAddressOutputTextView = mView.findViewById(R.id.address_text);

        mLatitudeTextView = mView.findViewById(R.id.latitude_text);
        mLongitudeTextView = mView.findViewById(R.id.longitude_text);

        mPrevLatitudeTextView = mView.findViewById(R.id.prev_latitude_text);
        mPrevLongitudeTextView = mView.findViewById(R.id.prev_longitude_text);
        mIntervalTextView = mView.findViewById(R.id.interval);
        mDeleteTextView = mView.findViewById(R.id.delete_loops);

        mAltitudeTextView = mView.findViewById(R.id.altitude_text);
        mSpeedTextView = mView.findViewById(R.id.speed_text);
        mLastUpdateTimeTextView = mView.findViewById(R.id.last_update_time_text);
        mMaxSpeedTextView = mView.findViewById(R.id.max_speed);
        mAvgSpeedTextView = mView.findViewById(R.id.avg_speed);
        mMinAltitudeTextView = mView.findViewById(R.id.min_alt);
        mMaxAltitudeTextView = mView.findViewById(R.id.max_alt);
        mElapsedTimeTextView = mView.findViewById(R.id.total_time);
        mTotalDistanceTextView = mView.findViewById(R.id.total_distance);

        // Set labels.
        mCurrentRunLabel = "Current Run Number";
        mLastRunLabel = "Last Run Number";
        mLatitudeLabel = getResources().getString(R.string.latitude_label);
        mLongitudeLabel = getResources().getString(R.string.longitude_label);
        mAltitudeLabel = getResources().getString(R.string.altitude_label);
        mPrevLatitudeLabel = "Prev Latitude";
        mPrevLongitudeLabel = "Prev Latitude";
        mSpeedLabel = getResources().getString(R.string.speed_label);
        mLastUpdateTimeLabel = getResources().getString(R.string.last_update_time_label);
        mMaxSpeedLabel = "Max Speed";
        mAvgSpeedLabel = "Avg Speed";
        mMinAltitudeLabel = "Minimum Altitude";
        mMaxAltitudeLabel = "Maximum Altitude";
        mElapsedTimeLabel = "Elapsed Time";
        mDistanceLabel = "Total distance";
        mAndroid_idLabel = "Android ID";
        mIntervalLabel = "Interval";
        mDeleteLabel = "Delete loops";
//        mAndroid_id = Secure.getString(getContext().getContentResolver(), Secure.ANDROID_ID);

        mLastUpdateTime = "";
        mElapsedTime = "";
        mStartTimeString = "";

        // Update values using data stored in the Bundle.
        updateValuesFromBundle(savedInstanceState);

        final Double mSelectedLatitude = createNewLocation()[0];
        final Double mSelectedLongitude = createNewLocation()[1];

        // Permission checked below
//        readPhoneState();

        tm = (TelephonyManager) getContext().getSystemService(Context.TELEPHONY_SERVICE);
        androidId = "" + Settings.Secure.getString(mContext.getContentResolver(), Settings.Secure.ANDROID_ID);
//        deviceUuid = new UUID(androidId.hashCode(), ((long)tmDevice.hashCode() << 32) | tmSerial.hashCode());
//        deviceId = deviceUuid.toString();

//        WifiManager wm = (WifiManager)Ctxt.getSystemService(Context.WIFI_SERVICE);
//        return wm.getConnectionInfo().getMacAddress();
//        mDevIDTextView.setText(String.format(Locale.ENGLISH, "%s: %s", mAndroid_idLabel,
//                mAndroid_id));
        mRunNumber.setText(String.format(Locale.ENGLISH, "%s: %s",
                mLastRunLabel, mLast_ID));

        long mIntervall = UPDATE_INTERVAL_IN_MILLISECONDS / 1000;
        String mIntervalll = String.valueOf(mIntervall) + " s";
        mIntervalTextView.setText(String.format(Locale.ENGLISH, "%s: %s", mIntervalLabel,
                mIntervalll));

        int mDeleteLoop = DELETE_LAST_ROWS;
        String mDeleteloopp = String.valueOf(mDeleteLoop);
        mDeleteTextView.setText(String.format(Locale.ENGLISH, "%s: %s", mDeleteLabel,
                mDeleteloopp));

        mLocation.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                String uri = String.format(Locale.ENGLISH, "geo:%f,%f", mSelectedLatitude, mSelectedLongitude);
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                startActivity(intent);
            }
        });


        // Spinner element
        mSpinner = mView.findViewById(R.id.run_type_spinner);

        // Spinner click listener
        mSpinner.setOnItemSelectedListener(this);

        // Spinner Drop down elements
        mCategories = new ArrayList<RunType>();
        mCategories.add(new RunType(R.drawable.ic_directions_walk_black_24dp, R.string.Run_type_walk, R.string.Run_type_walk_desc));
        mCategories.add(new RunType(R.drawable.ic_directions_bike_black_24dp, R.string.Run_type_bike, R.string.Run_type_bike_desc));
        mCategories.add(new RunType(R.drawable.ic_directions_car_black_24dp, R.string.Run_type_car, R.string.Run_type_car_desc));
        mCategories.add(new RunType(R.drawable.ic_developer_board_black_48dp, R.string.Run_type_custom, R.string.Run_type_custom_desc));

        // Creating adapter for spinner
        mAdapter = new RunTypesAdapter(getActivity(), R.layout.list_run_type, mCategories);

        // Setting a Custom Adapter to the Spinner
        mSpinner.setAdapter(mAdapter);

        RUN_TYPE_VALUE = mSharedPrefsRunType.getInt(RUN_TYPE_KEY, -1);
        if (RUN_TYPE_VALUE != -1) {
            // set the selected value of the spinner
            mSpinner.setSelection(RUN_TYPE_VALUE);
        }


        mRequestLocationUpdatesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!checkPermissions()) {
                    requestPermissions();
                } else {
                    mService.startUpdatesButtonHandler();
                }

                long mIntervall = UPDATE_INTERVAL_IN_MILLISECONDS / 1000;
                String mIntervalll = String.valueOf(mIntervall) + " s";
                mIntervalTextView.setText(String.format(Locale.ENGLISH, "%s: %s", mIntervalLabel,
                        mIntervalll));

                int mDeleteLoop = DELETE_LAST_ROWS;
                String mDeleteloopp = String.valueOf(mDeleteLoop);
                mDeleteTextView.setText(String.format(Locale.ENGLISH, "%s: %s", mDeleteLabel,
                        mDeleteloopp));

            }
        });

        mRemoveLocationUpdatesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                mService.stopLocationUpdates();
                mService.stopUpdatesButtonHandler();
                mRunNumber.setText(String.format(Locale.ENGLISH, "%s: %s",
                        mLastRunLabel, mCurrentId));

            }
        });

        // Restore the state of the buttons when the activity (re)launches.
        setButtonsEnabledState(mRequestingLocationUpdates);
        // Bind to the service. If the service is in foreground mode, this signals to the service
        // that since this activity is in the foreground, the service can exit foreground mode.
        mContext.getApplicationContext().bindService(new Intent(mContext, LocationUpdatesService.class), mServiceConnection,
                Context.BIND_AUTO_CREATE);
        return mView;
    }

    private double[] createNewLocation() {
        Location location = new Location("dummyprovider");
        double mLatitude = location.getLatitude();
        double mLongitude = location.getLongitude();

        double[] mLocation = new double[2];
        mLocation[0] = mLatitude;
        mLocation[1] = mLongitude;
        return mLocation;
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
//            if (savedInstanceState.keySet().contains(KEY_REQUESTING_LOCATION_UPDATES)) {
//                mRequestingLocationUpdates = savedInstanceState.getBoolean(
//                        KEY_REQUESTING_LOCATION_UPDATES);
//            }
//
//            // Update the value of mCurrentLocation from the Bundle and update the UI to show the
//            // correct latitude and longitude.
//            if (savedInstanceState.keySet().contains(KEY_LOCATION)) {
//                // Since KEY_LOCATION was found in the Bundle, we can be sure that mCurrentLocation
//                // is not null.
//                mCurrentLocation = savedInstanceState.getParcelable(KEY_LOCATION);
//            }
//            // Update the value of mCurrentLocation from the Bundle and update the UI to show the
//            // correct latitude and longitude.
//            if (savedInstanceState.keySet().contains(KEY_ALTITUDE)) {
//                // Since KEY_ALTITUDE was found in the Bundle, we can be sure that mCurrentLocation
//                // is not null.
//                mCurrentLocation = savedInstanceState.getParcelable(KEY_ALTITUDE);
//            }
//            if (savedInstanceState.keySet().contains(KEY_SPEED)) {
//                // Since KEY_SPEED was found in the Bundle, we can be sure that mCurrentLocation
//                // is not null.
//                mCurrentLocation = savedInstanceState.getParcelable(KEY_SPEED);
//            }
//
//            // Update the value of mLastUpdateTime from the Bundle and update the UI.
//            if (savedInstanceState.keySet().contains(KEY_LAST_UPDATED_TIME)) {
//                mLastUpdateTime = savedInstanceState.getString(KEY_LAST_UPDATED_TIME);
//            }
//            // Update the value of mElapsedTime from the Bundle and update the UI.
//            if (savedInstanceState.keySet().contains(KEY_LAST_UPDATED_ETIME)) {
//                mElapsedTime = savedInstanceState.getString(KEY_LAST_UPDATED_ETIME);
//            }
//
//            // Update the value of mTotalDistance from the Bundle and update the UI.
//            if (savedInstanceState.keySet().contains(KEY_LAST_UPDATED_TDISTANCE)) {
//                mTotalDistance = savedInstanceState.getDouble(KEY_LAST_UPDATED_TDISTANCE);
//            }

            // Update the value of mTotalDistance from the Bundle and update the UI.
            if (savedInstanceState.keySet().contains(KEY_LAST_RUN)) {
                mLast_ID = savedInstanceState.getInt(KEY_LAST_RUN);
            }

            mRunNumber.setText(String.format(Locale.ENGLISH, "%s: %s",
                    mCurrentRunLabel, mLast_ID));

        }
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
     * Updates all UI fields.
     */
    private void updateUI() {
        setButtonsEnabledState(mRequestingLocationUpdates);
    }

    /**
     * Disables both buttons when functionality is disabled due to insuffucient location settings.
     * Otherwise ensures that only one button is enabled at any time. The Start Updates button is
     * enabled if the user is not requesting location updates. The Stop Updates button is enabled
     * if the user is requesting location updates.
     */
    public void setButtonsEnabledState(Boolean requestingLocationUpdates) {
        if (requestingLocationUpdates) {
            mRequestLocationUpdatesButton.setEnabled(false);
            mSpinner.setEnabled(false);
            mRemoveLocationUpdatesButton.setEnabled(true);
        } else {
            mRequestLocationUpdatesButton.setEnabled(true);
            mSpinner.setEnabled(true);
            mRemoveLocationUpdatesButton.setEnabled(false);
        }
    }


    public boolean switchDisableAutoStop() {

        return sharedPrefs.getBoolean
                (getString(R.string.disable_auto_stop_switch_key), preferenceBooleanDisableAutoStop);
    }


    // This callback is called only when there is a saved instance previously saved using
// onSaveInstanceState(). We restore some state in onCreate() while we can optionally restore
// other state here, possibly usable after onStart() has completed.
// The savedInstanceState Bundle is same as the one used in onCreate().

//    @Override
//    public void onActivityCreated(Bundle savedInstanceState) {
//        super.onActivityCreated(savedInstanceState);
//
//        if (savedInstanceState != null) {
//            //probably orientation change
//            savedInstanceState.getBoolean(KEY_REQUESTING_LOCATION_UPDATES, mRequestingLocationUpdates);
//                onlyUIupdate();
//                setButtonsEnabledState();
//                Log.d(TAG, "onActivityCreated savedInstanceState != null");
//
//        } else {
//            if (mFusedLocationClient != null) {
//                Log.d(TAG, "onActivityCreated mFusedLocationClient != null");
//                mRequestingLocationUpdates = true;
////                onlyUIupdate();
//
//                //returning from backstack, data is fine, do nothing
//
//            } else {
//                //newly created, compute data
//
//            }
//        }
//    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (savedInstanceState != null) {
            if (savedInstanceState.getBoolean(KEY_REQUESTING_LOCATION_UPDATES, mRequestingLocationUpdates)) {
//                onlyUIupdate();
//                setButtonsEnabledState();
                {
                    Log.d(TAG, "mRequestingLocationUpdates is TRUE");
                }
            }
        } else {
            Log.d(TAG, "onViewStateRestored savedInstanceState = null");
        }
    }

    // invoked when the activity may be temporarily destroyed, save the instance state here
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putBoolean(KEY_REQUESTING_LOCATION_UPDATES, mRequestingLocationUpdates);
//        savedInstanceState.putParcelable(KEY_LOCATION, mCurrentLocation);
//        savedInstanceState.putParcelable(KEY_ALTITUDE, mCurrentLocation);
//        savedInstanceState.putParcelable(KEY_SPEED, mCurrentLocation);
        savedInstanceState.putInt(KEY_LAST_RUN, mLast_ID);
        // call superclass to save any view hierarchy

        Log.d(TAG, "onSaveInstanceState(Bundle savedInstanceState)");
    }


    @Override
    public void onStop() {
        super.onStop();
        Log.d(TAG, "onStop()");
        if (mBound) {
            // Unbind from the service. This signals to the service that this activity is no longer
            // in the foreground, and the service can respond by promoting itself to a foreground
            // service.
            mContext.getApplicationContext().unbindService(mServiceConnection);
            mBound = false;
        }
        PreferenceManager.getDefaultSharedPreferences(mContext)
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, "onStart()");
        PreferenceManager.getDefaultSharedPreferences(mContext)
                .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
        LocalBroadcastManager.getInstance(mContext).registerReceiver(myReceiver,
                new IntentFilter(LocationUpdatesService.ACTION_BROADCAST));
//        if (mFusedLocationClient != null){
//
//        }
//        // Within {@code onPause()}, we remove location updates. Here, we resume receiving
//        // location updates if the user has requested them.
//        Log.d(TAG, "onResume()");
//        if (ActivityCompat.checkSelfPermission
//                (mContext, Manifest.permission.ACCESS_FINE_LOCATION)
//                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission
//                (mContext, Manifest.permission.ACCESS_COARSE_LOCATION)
//                != PackageManager.PERMISSION_GRANTED) {
//            // TODO: Consider calling
//            //    ActivityCompat#requestPermissions
//            // here to request the missing permissions, and then overriding
//            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
//            //                                          int[] grantResults)
//            // to handle the case where the user grants the permission. See the documentation
//            // for ActivityCompat#requestPermissions for more details.
//            return;
//        }
//        Log.d(TAG, "onResume, getLocationAvailability()");
//
//        mFusedLocationClient.getLocationAvailability()
//                .addOnCompleteListener((Activity) mContext, new OnCompleteListener<LocationAvailability>() {
//                    @Override
//                    public void onComplete(@NonNull Task<LocationAvailability> task) {
//                        mRequestingLocationUpdates = LocationAvailability.hasLocationAvailability();
//
//                        setButtonsEnabledState();
//                    }
//                });
//    }

    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "onPause()");
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(myReceiver);
        // Remove location updates to save battery.
        //stopLocationUpdates();
//        mSensorManager.unregisterListener((SensorEventListener) mContext);
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
    private boolean checkPermissionsFineLocation() {
        int permissionState = ActivityCompat.checkSelfPermission(mContext,
                Manifest.permission.ACCESS_FINE_LOCATION);
        return permissionState == PackageManager.PERMISSION_GRANTED;
    }

    private boolean checkPermissionsReadPhoneState() {
        int permissionState = ActivityCompat.checkSelfPermission(mContext,
                Manifest.permission.READ_PHONE_STATE);
        return permissionState == PackageManager.PERMISSION_GRANTED;
    }


    private void readPhoneState() {
        if (checkPermissionsReadPhoneState()) {

//            tmDevice = "" + tm.getDeviceId();
//            tmSerial = "" + tm.getSimSerialNumber();
            mDevIDTextView.setText(String.format(Locale.ENGLISH, "%s: %s", mAndroid_idLabel,
                    tmDevice));
        } else if (!checkPermissionsReadPhoneState()) {
            requestPermissions();
        }
    }

    private void requestPermissions() {
        boolean shouldProvideRationale =
                ActivityCompat.shouldShowRequestPermissionRationale((Activity) mContext,
                        Manifest.permission.ACCESS_FINE_LOCATION);
        boolean shouldProvideRationale1 =
                ActivityCompat.shouldShowRequestPermissionRationale((Activity) mContext,
                        Manifest.permission.READ_PHONE_STATE);

        // Provide an additional rationale to the user. This would happen if the user denied the
        // request previously, but didn't check the "Don't ask again" checkbox.
        if (shouldProvideRationale || shouldProvideRationale1) {
            Log.i(TAG, "Displaying permission rationale to provide additional context.");
            showSnackbar(R.string.permission_rationale,
                    android.R.string.ok, new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            // Request permission
                            ActivityCompat.requestPermissions((Activity) mContext,
                                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                    REQUEST_PERMISSIONS_REQUEST_CODE);
                            // Request permission
                            ActivityCompat.requestPermissions((Activity) mContext,
                                    new String[]{Manifest.permission.READ_PHONE_STATE},
                                    REQUEST_PERMISSIONS_REQUEST_CODE);
                        }
                    });
        } else {
            Log.i(TAG, "Requesting permission");
            // Request permission. It's possible this can be auto answered if device policy
            // sets the permission in a given state or the user denied the permission
            // previously and checked "Never ask again".
            ActivityCompat.requestPermissions((Activity) mContext,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_PERMISSIONS_REQUEST_CODE);
            ActivityCompat.requestPermissions((Activity) mContext,
                    new String[]{Manifest.permission.READ_PHONE_STATE},
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
                    mService.startUpdatesButtonHandler();
                }
            } else {
                // Permission denied.
                setButtonsEnabledState(false);
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


    /**
     * Returns the current state of the permissions needed.
     */
    private boolean checkPermissions() {
        return PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(mContext,
                Manifest.permission.ACCESS_FINE_LOCATION);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        // Update the buttons state depending on whether location updates are being requested.
        if (s.equals(LocationServiceConstants.KEY_REQUESTING_LOCATION_UPDATES)) {
            setButtonsEnabledState(sharedPreferences.getBoolean(LocationServiceConstants.KEY_REQUESTING_LOCATION_UPDATES,
                    false));
        }
    }

    private void onlyUIupdate(Intent intent) {

        mCurrentLocation = intent.getParcelableExtra(LocationUpdatesService.EXTRA_LOCATION);
        if (mCurrentLocation != null) {

            mCurrentId = intent.getIntExtra(LocationUpdatesService.EXTRA_CURRENT_ID, 0);
            mCurrentLatitude = intent.getDoubleExtra(LocationUpdatesService.EXTRA_LATITUDE, 0);
            mCurrentLongitude = intent.getDoubleExtra(LocationUpdatesService.EXTRA_LONGITUDE, 0);
            mCurrentAltitude = intent.getDoubleExtra(LocationUpdatesService.EXTRA_ALTITUDE, 0);
            mCurrentSpeed = intent.getDoubleExtra(LocationUpdatesService.EXTRA_SPEED, 0);
            mMaxSpeed = intent.getDoubleExtra(LocationUpdatesService.EXTRA_MAX_SPEED, 0);
            mMaxAltitude = intent.getDoubleExtra(LocationUpdatesService.EXTRA_MAX_ALTITUDE, 0);
            mMinAltitude = intent.getDoubleExtra(LocationUpdatesService.EXTRA_MIN_ALTITUDE, 0);
            mAverageSpeed = intent.getDoubleExtra(LocationUpdatesService.EXTRA_AVG_SPEED, 0);
            mTotalDistance = intent.getDoubleExtra(LocationUpdatesService.EXTRA_TOTAL_DISTANCE, 0);
            mPreviousLatitude = intent.getDoubleExtra(LocationUpdatesService.EXTRA_PREV_LATITUDE, 0);
            mPreviousLongitude = intent.getDoubleExtra(LocationUpdatesService.EXTRA_PREV_LATITUDE, 0);
            mElapsedTimeMillis = intent.getLongExtra(LocationUpdatesService.EXTRA_TOTAL_TIME, 0);
            mLastUpdateTimeMillis = intent.getLongExtra(LocationUpdatesService.EXTRA_LAST_TIME_UPDATE, 0);
            mCurrentAddress = intent.getStringExtra(LocationUpdatesService.EXTRA_ADDRESS);

            mLatitudeTextView.setText(String.format(Locale.ENGLISH, "%s: %f", mLatitudeLabel,
                    mCurrentLatitude));
            mLongitudeTextView.setText(String.format(Locale.ENGLISH, "%s: %f", mLongitudeLabel,
                    mCurrentLongitude));
            mAltitudeTextView.setText(String.format(Locale.ENGLISH, "%s: %f", mAltitudeLabel,
                    mCurrentAltitude));
            mSpeedTextView.setText(String.format(Locale.ENGLISH, "%s: %.1f", mSpeedLabel,
                    mCurrentSpeed));

            mAvgSpeedTextView.setText(String.format(Locale.ENGLISH, "%s: %.1f",
                    mAvgSpeedLabel, mAverageSpeed));
            mMaxSpeedTextView.setText(String.format(Locale.ENGLISH, "%s: %.1f",
                    mMaxSpeedLabel, mMaxSpeed));
            mMaxAltitudeTextView.setText(String.format(Locale.ENGLISH, "%s: %f",
                    mMaxAltitudeLabel, mMaxAltitude));
            mMinAltitudeTextView.setText(String.format(Locale.ENGLISH, "%s: %f",
                    mMinAltitudeLabel, mMinAltitude));
            mTotalDistanceTextView.setText(String.format(Locale.ENGLISH, "%s: %.3f",
                    mDistanceLabel, mTotalDistance));

            mPrevLatitudeTextView.setText(String.format(Locale.ENGLISH, "%s: %f", mPrevLatitudeLabel,
                    mPreviousLatitude));
            mPrevLongitudeTextView.setText(String.format(Locale.ENGLISH, "%s: %f", mPrevLongitudeLabel,
                    mPreviousLongitude));

            mElapsedTime = String.format("%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(mElapsedTimeMillis),
                    TimeUnit.MILLISECONDS.toMinutes(mElapsedTimeMillis) % TimeUnit.HOURS.toMinutes(1),
                    TimeUnit.MILLISECONDS.toSeconds(mElapsedTimeMillis) % TimeUnit.MINUTES.toSeconds(1));

            mElapsedTimeTextView.setText(String.format(Locale.ENGLISH, "%s: %s",
                    mElapsedTimeLabel, mElapsedTime));

            String mHours = new SimpleDateFormat("HH:mm:ss").format(new Date(mLastUpdateTimeMillis));
            mLastUpdateTimeTextView.setText(String.format(Locale.ENGLISH, "%s: %s",
                    mLastUpdateTimeLabel, mHours));

            mRunNumber.setText(String.format(Locale.ENGLISH, "%s: %s",
                    mCurrentRunLabel, mCurrentId));
            mLast_ID = mCurrentId;

            if (mCurrentAddress != "") {
                mAddressOutputTextView.setText(mCurrentAddress);
            } else {
                mAddressOutputTextView.setText(R.string.in_motion);
            }
        }

    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
        // On selecting a spinner item

        RUN_TYPE_VALUE = mSpinner.getSelectedItemPosition();
        RUN_TYPE_TITLE = getString(mAdapter.getItem(RUN_TYPE_VALUE).getTitle());
        RUN_TYPE_PICTURE = mAdapter.getItem(RUN_TYPE_VALUE).getPicture();
        RUN_TYPE_DESCRIPTION = getString(mAdapter.getItem(RUN_TYPE_VALUE).getDescription());

        Log.e("RUN_TYPE_VALUE_onItemse", String.valueOf(RUN_TYPE_VALUE));
        SharedPreferences.Editor preferEditor = mSharedPrefsRunType.edit();
        preferEditor.putInt(RUN_TYPE_KEY, RUN_TYPE_VALUE);
        preferEditor.putString(RUN_TYPE_TTTLE_KEY, RUN_TYPE_TITLE);
        preferEditor.putInt(RUN_TYPE_PICTURE_KEY, RUN_TYPE_PICTURE);
        preferEditor.putString(RUN_TYPE_DESCRIPTION_KEY, RUN_TYPE_DESCRIPTION);
        preferEditor.apply();

    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    /**
     * Receiver for broadcasts sent by {@link LocationUpdatesService}.
     */
    private class MyReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            onlyUIupdate(intent);

        }
    }
}

