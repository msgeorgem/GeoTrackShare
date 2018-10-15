/**
 * Copyright 2017 Google Inc. All Rights Reserved.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.geotrackshare.LocationService;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.TaskStackBuilder;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.icu.util.TimeZone;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.example.android.geotrackshare.Data.TrackContract;
import com.example.android.geotrackshare.MainActivity;
import com.example.android.geotrackshare.R;
import com.example.android.geotrackshare.RealTimeFragment;
import com.example.android.geotrackshare.TrackingWidget.TrackingWidgetProvider;
import com.example.android.geotrackshare.Utils.DistanceCalculator;
import com.example.android.geotrackshare.Utils.StopWatch;
import com.example.android.geotrackshare.Utils.StopWatchHandler;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.SettingsClient;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static com.example.android.geotrackshare.Data.TrackContract.TrackingEntry.COLUMN_ALTITUDE;
import static com.example.android.geotrackshare.Data.TrackContract.TrackingEntry.COLUMN_AVR_SPEED;
import static com.example.android.geotrackshare.Data.TrackContract.TrackingEntry.COLUMN_AVR_SPEEDP;
import static com.example.android.geotrackshare.Data.TrackContract.TrackingEntry.COLUMN_DISTANCE;
import static com.example.android.geotrackshare.Data.TrackContract.TrackingEntry.COLUMN_FAVORITEP;
import static com.example.android.geotrackshare.Data.TrackContract.TrackingEntry.COLUMN_LATITUDE;
import static com.example.android.geotrackshare.Data.TrackContract.TrackingEntry.COLUMN_LONGITUDE;
import static com.example.android.geotrackshare.Data.TrackContract.TrackingEntry.COLUMN_MAX_ALT;
import static com.example.android.geotrackshare.Data.TrackContract.TrackingEntry.COLUMN_MAX_ALTP;
import static com.example.android.geotrackshare.Data.TrackContract.TrackingEntry.COLUMN_MAX_SPEED;
import static com.example.android.geotrackshare.Data.TrackContract.TrackingEntry.COLUMN_MAX_SPEEDP;
import static com.example.android.geotrackshare.Data.TrackContract.TrackingEntry.COLUMN_MIN_ALT;
import static com.example.android.geotrackshare.Data.TrackContract.TrackingEntry.COLUMN_MOVE_CLOSE;
import static com.example.android.geotrackshare.Data.TrackContract.TrackingEntry.COLUMN_MOVE_DISTANCE;
import static com.example.android.geotrackshare.Data.TrackContract.TrackingEntry.COLUMN_RUNTYPE;
import static com.example.android.geotrackshare.Data.TrackContract.TrackingEntry.COLUMN_RUNTYPEP;
import static com.example.android.geotrackshare.Data.TrackContract.TrackingEntry.COLUMN_RUN_ID;
import static com.example.android.geotrackshare.Data.TrackContract.TrackingEntry.COLUMN_RUN_IDP;
import static com.example.android.geotrackshare.Data.TrackContract.TrackingEntry.COLUMN_SPEED;
import static com.example.android.geotrackshare.Data.TrackContract.TrackingEntry.COLUMN_START_TIME;
import static com.example.android.geotrackshare.Data.TrackContract.TrackingEntry.COLUMN_START_TIMEP;
import static com.example.android.geotrackshare.Data.TrackContract.TrackingEntry.COLUMN_STOP_TIMEP;
import static com.example.android.geotrackshare.Data.TrackContract.TrackingEntry.COLUMN_TIME;
import static com.example.android.geotrackshare.Data.TrackContract.TrackingEntry.COLUMN_TIME_COUNTER;
import static com.example.android.geotrackshare.Data.TrackContract.TrackingEntry.COLUMN_TIME_COUNTERP;
import static com.example.android.geotrackshare.Data.TrackContract.TrackingEntry.COLUMN_TOTAL_DISTANCE;
import static com.example.android.geotrackshare.Data.TrackContract.TrackingEntry.COLUMN_TOTAL_DISTANCEP;
import static com.example.android.geotrackshare.Data.TrackContract.TrackingEntry.CONTENT_URI;
import static com.example.android.geotrackshare.Data.TrackContract.TrackingEntry.CONTENT_URI_POST;
import static com.example.android.geotrackshare.Data.TrackContract.TrackingEntry._ID;
import static com.example.android.geotrackshare.LocationService.LocationServiceConstants.lastTrackType;
import static com.example.android.geotrackshare.LocationService.LocationServiceConstants.requestingLocationUpdates;
import static com.example.android.geotrackshare.LocationService.LocationServiceConstants.setLastTrackID;
import static com.example.android.geotrackshare.LocationService.LocationServiceConstants.setRequestingLocationUpdates;
import static com.example.android.geotrackshare.LocationService.LocationServiceConstants.setStartTimeCurrentTrack;
import static com.example.android.geotrackshare.LocationService.LocationServiceConstants.setStopWatchRunning;
import static com.example.android.geotrackshare.MainActivity.mSharedPrefsRunType;
import static com.example.android.geotrackshare.RealTimeFragment.DELETE_LAST_ROWS;
import static com.example.android.geotrackshare.RealTimeFragment.DISABLE_AUTO_CLOSE;
import static com.example.android.geotrackshare.RealTimeFragment.RUN_TYPE_INTERVAL;
import static com.example.android.geotrackshare.RealTimeFragment.RUN_TYPE_NOISE;
import static com.example.android.geotrackshare.RealTimeFragment.RUN_TYPE_PICTURE_KEY;
import static com.example.android.geotrackshare.RealTimeFragment.RUN_TYPE_VALUE;
import static com.example.android.geotrackshare.TrackingWidget.TrackingWidgetProvider.ACTION_FROM_SERVICE;


/**
 * A bound and started service that is promoted to a foreground service when location updates have
 * been requested and all clients unbind.
 * <p>
 * For apps running in the background on "O" devices, location is computed only once every 10
 * minutes and delivered batched every 30 minutes. This restriction applies even to apps
 * targeting "N" or lower which are run on "O" devices.
 * <p>
 * This sample show how to use a long-running service for location updates. When an activity is
 * bound to this service, frequent location updates are permitted. When the activity is removed
 * from the foreground, the service promotes itself to a foreground service, and location updates
 * continue. When the activity comes back to the foreground, the foreground service stops, and the
 * notification assocaited with that service is removed.
 */
public class LocationUpdatesService extends Service implements SensorEventListener {

    /**
     * The name of the channel for notifications.
     */
    public static final String CHANNEL_ID = "channel_999999";
    public static final String EXTRA_RUN_TYPE = "EXTRA_RUN_TYPE";
    public static final String EXTRA_LATITUDE = "EXTRA LATITUDE";
    public static final String EXTRA_LONGITUDE = "EXTRA_LONGITUDE";
    public static final String EXTRA_ALTITUDE = "EXTRA_ALTITUDE";
    public static final String EXTRA_SPEED = "EXTRA_SPEED";
    public static final String EXTRA_AVG_SPEED = "EXTRA_AVG_SPEED";
    public static final String EXTRA_MAX_SPEED = "EXTRA_MAX_SPEED";
    public static final String EXTRA_MAX_ALTITUDE = "EXTRA_MAX_ALTITUDE";
    public static final String EXTRA_MIN_ALTITUDE = "EXTRA_MIN_ALTITUDE";
    public static final String EXTRA_TOTAL_DISTANCE = "EXTRA_TOTAL_DISTANCE";
    public static final String EXTRA_PREV_LATITUDE = "EXTRA_PREV_LATITUDE";
    public static final String EXTRA_PREV_LONGITUDE = "EXTRA_PREV_LONGITUDE";
    public static final String EXTRA_TOTAL_TIME = "EXTRA_TOTAL_TIME";
    public static final String EXTRA_LAST_TIME_UPDATE = "EXTRA_LAST_TIME_UPDATE";
    public static final String EXTRA_CURRENT_ID = "EXTRA_CURRENT_ID";
    public static final String EXTRA_ADDRESS = "EXTRA_ADDRESS";
    public static final String EXTRA_STOP_FROM_NOTIFICATION = "EXTRA_STOP_FROM_NOTIFICATION";
    public static final String EXTRA_REQUESTING_UDPATES = "EXTRA_REQUESTING_UDPATES";
    public static final String EXTRA_STOP_WATCH = "EXTRA_STOP_WATCH";
    /**
     * Constant used in the location settings dialog.
     */
    public static final int REQUEST_CHECK_SETTINGS = 0x1;
    private static final String PACKAGE_NAME =
            "com.google.android.gms.location.sample.locationupdatesforegroundservice";
    public static final String ACTION_BROADCAST = PACKAGE_NAME + ".broadcast";
    public static final String EXTRA_LOCATION = PACKAGE_NAME + ".location";
    private static final String TAG = LocationUpdatesService.class.getSimpleName();
    public static final String EXTRA_START_FROM_NOTIFICATION = "EXTRA_START_FROM_NOTIFICATION";
    public static final String EXTRA_START_STOPWATCH = "EXTRA_START_STOPWATCH";
    private static final int NOTIFICATION_ID = 12345678;
    public static final double NOISEc = 0.02;
    public static double NOISEd = 0.04;

    public static String UPDATE_INTERVAL_IN_MILLISECONDS_STRING;
    /**
     * The desired interval for location updates. Inexact. Updates may be more or less frequent.
     */
    private static long UPDATE_INTERVAL_IN_MILLISECONDS = 10000;
    /**
     * The fastest rate for active location updates. Updates will never be more frequent
     * than this value.
     */
    private static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS =
            UPDATE_INTERVAL_IN_MILLISECONDS / 2;
    public static long startTimeStopWatch;

    private final IBinder mBinder = new LocalBinder();
    private final int GET_GEOLOCATION_LAST_ROWS = 5;
    private String CHANNEL_NAME = "GeoTracker Channel";
    private String DESCRIPTION = "GeoTracking";
    /**
     * Used to check whether the bound activity has really gone away and not unbound as part of an
     * orientation change. We create a foreground service notification only if the former takes
     * place.
     */
    private boolean mChangingConfiguration = false;
    private NotificationManager mNotificationManager;
    private Notification notification;
    /**
     * Contains parameters used by {@link com.google.android.gms.location.FusedLocationProviderApi}.
     */
    private LocationRequest mLocationRequest;
    /**
     * Provides access to the Fused Location Provider API.
     */
    private FusedLocationProviderClient mFusedLocationClient;
    /**
     * Callback for changes in location.
     */
    private LocationCallback mLocationCallback;

    /**
     * The current location.
     */
//    private Location mLocation;
    /**
     * Provides access to the Location Settings API.
     */
    private SettingsClient mSettingsClient;
    /**
     * Represents a geographical location.
     */
    private Location mCurrentLocation;
    private Cursor cur;
    private int mMaxId, mCurrentId, mLast_ID, mRunType;
    private long mLastUpdateTimeMillis, mElapsedTimeMillis, mStartTimeinMillis, mStopTimeinMillis;
    private double mCurrentLatitude, mCurrentLongitude, mCurrentAltitude, mCurrentSpeed, mMaxSpeed,
            mAverageSpeed, mMaxAltitude, mMinAltitude, mTotalTime, mDistance, mTotalDistance,
            mPreviousLatitude, mPreviousLongitude, mRoundedDistance;
    private double checkXD, checkYD, checkZD, mMoveDistance;
    private double checkXC, checkYC, checkZC, mMoveClose;
    private SensorManager mSensorManager;
    private double ax, ay, az;   // these are the acceleration in x,y and z axis
    private double mLastX, mLastY, mLastZ;
    private double deltaXD, deltaYD, deltaZD, deltaXC, deltaYC, deltaZC;
    private boolean mNoMoveDistance = false;
    private boolean mNoMoveClose = false;
    private boolean mInitialized = false;
    private String mAddressOutput;
    private String mCurrentAddress = "";
    private String mNotificationInformation = "";
    private String mNotificationRunInformation = "";
    public Cursor mTrackingCursor;
    public Cursor mPostTrackingCursor;
    static StopWatch timer = new StopWatch();
    private static long DISPLACEMENT = 10;
    // Start and end times in milliseconds
    private static long startTime;
    private static long endTime;
    // Is the service tracking time?
    private static boolean isTimerRunning;
    private String mElapsedTime;
    private Handler mServiceHandler;
    private HandlerThread stopWatchThread;
    private final Handler mStopWatchHandler = new StopWatchHandler();
    /**
     * Stores the types of location services the client is interested in using. Used for checking
     * settings to determine if the device has optimal location settings.
     */
    private LocationSettingsRequest mLocationSettingsRequest;

    public LocationUpdatesService() {
    }

    @SuppressLint("NewApi")
    public static final String getDateFromMillis(long d) {
        android.icu.text.SimpleDateFormat df = new android.icu.text.SimpleDateFormat("HH:mm:ss");
        df.setTimeZone(TimeZone.getTimeZone("GMT"));
        return df.format(d);
    }

    /**
     * Creates a callback for receiving location events.
     */
    private void createLocationCallback() {
        mLocationCallback = new LocationCallback() {
            @TargetApi(Build.VERSION_CODES.O)
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                mCurrentLocation = locationResult.getLastLocation();
                onNewLocation(mCurrentLocation);
                updateLocationUI(mCurrentLocation);
//                mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
            }
        };
    }

    /**
     * Uses a {@link LocationSettingsRequest.Builder} to build
     * a {@link LocationSettingsRequest} that is used for checking
     * if a device has the needed location settings.
     */
    private void buildLocationSettingsRequest() {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        mLocationSettingsRequest = builder.build();
    }

    public static String getFormatedTimeInString() {
        long stopTime = System.currentTimeMillis();
        String mElapsedTime = new SimpleDateFormat("HH:mm:ss").format(new Date(stopTime));
        return mElapsedTime;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mChangingConfiguration = true;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // Called when a client (MainActivity in case of this sample) comes to the foreground
        // and binds with this service. The service should cease to be a foreground service
        // when that happens.
        Log.i(TAG, "in onBind()");
        stopForeground(true);
        mChangingConfiguration = false;
        return mBinder;
    }

    @Override
    public void onRebind(Intent intent) {
        // Called when a client (MainActivity in case of this sample) returns to the foreground
        // and binds once again with this service. The service should cease to be a foreground
        // service when that happens.
        Log.i(TAG, "in onRebind()");
        stopForeground(true);
        mChangingConfiguration = false;
        super.onRebind(intent);
    }

    /**
     * Starts the timer
     */
    public static void startStopWatch() {
        if (!isTimerRunning) {
            startTimeStopWatch = System.currentTimeMillis();
            timer.start();
            isTimerRunning = true;
        } else {
            Log.e(TAG, "startTimer request for an already running timer");
        }
    }

    /**
     * Stops the timer
     */
    public static void stopStopWatch() {
        if (isTimerRunning) {
            timer.stop();
            endTime = System.currentTimeMillis();
            isTimerRunning = false;
        } else {
            Log.e(TAG, "stopTimer request for a timer that isn't running");
        }
    }

    /**
     * Returns the  elapsed time
     *
     * @return the elapsed time in seconds
     */
    public static String elapsedTime() {
        // If the timer is running, the end time will be zero
        return timer.elapsedTimeString0();

    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.i(TAG, "Last client unbound from service");

        // Called when the last client (MainActivity in case of this sample) unbinds from this
        // service. If this method is called due to a configuration change in MainActivity, we
        // do nothing. Otherwise, we make this service a foreground service.
        if (!mChangingConfiguration && requestingLocationUpdates(this)) {
            Log.i(TAG, "Starting foreground service");

            // TODO(developer). If targeting O, use the following code.
            if (Build.VERSION.SDK_INT == Build.VERSION_CODES.O) {

                startForegroundService(new Intent(this, LocationUpdatesService.class));

            } else {
                if (requestingLocationUpdates(this)) {
                    sendNotificationAfterStart(this, mNotificationInformation, mNotificationRunInformation);
                } else {
                    sendNotificationAfterStop(this, mNotificationInformation, mNotificationRunInformation);
                }
            }
        }
        return true; // Ensures onRebind() is called when a client re-binds.
    }

    /**
     * Requests location updates from the FusedLocationApi. Note: we don't call this unless location
     * runtime permission has been granted.
     */
    private void startLocationUpdates() {


        Log.i(TAG, "Requesting location updates");
        setRequestingLocationUpdates(getApplicationContext(), true);
        startService(new Intent(getApplicationContext(), LocationUpdatesService.class));
        try {
            mFusedLocationClient.requestLocationUpdates(mLocationRequest,
                    mLocationCallback, Looper.myLooper());
        } catch (SecurityException unlikely) {
            setRequestingLocationUpdates(getApplicationContext(), false);
            Log.e(TAG, "Lost location permission. Could not request updates. " + unlikely);
        }

    }

    @SuppressLint("HandlerLeak")
    @Override
    public void onCreate() {

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mSettingsClient = LocationServices.getSettingsClient(this);
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);

        // Kick off the process of building the LocationCallback, LocationRequest, and
        // LocationSettingsRequest objects.
        createLocationCallback();
        createLocationRequest();
//        getLastLocation();
        buildLocationSettingsRequest();

        HandlerThread handlerThread = new HandlerThread(TAG);
        handlerThread.start();
        mServiceHandler = new Handler(handlerThread.getLooper());


//        stopWatchThread = new HandlerThread("StopWatch", android.os.Process.THREAD_PRIORITY_BACKGROUND);
//        stopWatchThread.start();
//        mStopWatchHandler = new StopWatchHandler(stopWatchThread.getLooper(),null);

    }

    @Override
    public void onDestroy() {
        mServiceHandler.removeCallbacksAndMessages(null);
        // Cleanup service before destruction
        // stopWatchThread.quit();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "onStartCommand");
        if (intent != null) { // May not have an Intent is the service was killed and restarted (See STICKY_SERVICE).

//            // Execute the specified code on the worker thread
//            mStopWatchHandler.post(new Runnable() {
//                @Override
//                public void run() {
//                    Log.i(TAG, "mStopWatchHandler.post(new Runnable() ");
//                    // Do something here!
//                    // Notify anyone listening for broadcasts about the new location.
//                    Intent intent = new Intent(ACTION_BROADCAST);
//                    intent.putExtra(EXTRA_STOP_WATCH, mElapsedTime);
//                    LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
//                }
//            });
        }

        if (EXTRA_START_FROM_NOTIFICATION.equals(intent.getAction())) {
            stopForeground(true);
            if (!requestingLocationUpdates(this)) {
                RealTimeFragment.mService.startUpdatesButtonHandler();
            }
        } else if (EXTRA_STOP_FROM_NOTIFICATION.equals(intent.getAction())) {
            stopForeground(true);
            if (requestingLocationUpdates(this)) {
                RealTimeFragment.mService.stopUpdatesButtonHandler();
            }
        }

        // Tells the system to not try to recreate the service after it as been killed.
        return START_NOT_STICKY;
    }

    /**
     * Removes location updates from the FusedLocationApi.
     */
    public void stopLocationUpdates() {
        mStopTimeinMillis = System.currentTimeMillis();
        if (mCurrentId != 0) {
            saveItemPost(mCurrentId, mStartTimeinMillis, mStopTimeinMillis, mRunType, mMaxAltitude, mMaxSpeed, mAverageSpeed,
                    mElapsedTimeMillis, mTotalDistance);
        } else {
            queryLastRow();
        }
        Log.i(TAG, "Removing location updates");
//        try {
        mFusedLocationClient.removeLocationUpdates(mLocationCallback);
        setRequestingLocationUpdates(this, false);
        stopSelf();
        if (!requestingLocationUpdates(this)) {
            Log.d(TAG, "stopLocationUpdates: updates never requested, no-op.");
            return;
        }

    }

    @Override
    public void onSensorChanged(final SensorEvent sensorEvent) {
        final AsyncTask<Void, Void, Void> sensor = new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... voids) {

                if (sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                    ax = sensorEvent.values[0];
                    ay = sensorEvent.values[1];
                    az = sensorEvent.values[2];

                    if (!mInitialized) {
                        mLastX = ax;
                        mLastY = ay;
                        mLastZ = az;
                        mInitialized = true;
                    } else {
                        deltaXD = Math.abs(mLastX - ax);
                        deltaYD = Math.abs(mLastY - ay);
                        deltaZD = Math.abs(mLastZ - az);
                        if (deltaXD < NOISEd) deltaXD = 0.0;
                        if (deltaYD < NOISEd) deltaYD = 0.0;
                        if (deltaZD < NOISEd) deltaZD = 0.0;
                        deltaXC = Math.abs(mLastX - ax);
                        deltaYC = Math.abs(mLastY - ay);
                        deltaZC = Math.abs(mLastZ - az);
                        if (deltaXC < NOISEc) deltaXC = 0.0;
                        if (deltaYC < NOISEc) deltaYC = 0.0;
                        if (deltaZC < NOISEc) deltaZC = 0.0;

                        mLastX = ax;
                        mLastY = ay;
                        mLastZ = az;
//                Log.i("Print deltaXD", String.valueOf(deltaXD));
//                Log.i("Print deltaYD", String.valueOf(deltaYD));
//                Log.i("Print deltaZD", String.valueOf(deltaZD))
                    }
                }
                return null;
            }
        };
        sensor.execute();

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    /**
     * Returns true if this is a foreground service.
     *
     * @param context The {@link Context}.
     */
    public boolean serviceIsRunningInForeground(Context context) {
        ActivityManager manager = (ActivityManager) context.getSystemService(
                Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(
                Integer.MAX_VALUE)) {
            if (getClass().getName().equals(service.service.getClassName())) {
                if (service.foreground) {
                    return true;
                }
            }
        }
        return false;
    }

    @SuppressLint("DefaultLocale")
    public long getElapsedTime() {

        mLastUpdateTimeMillis = System.currentTimeMillis();
        // Get elapsed time in milliseconds
        mElapsedTimeMillis = mLastUpdateTimeMillis - mStartTimeinMillis;
        if (mElapsedTimeMillis < 0) {
            mElapsedTimeMillis = 0;
        }
        return mElapsedTimeMillis;
    }

    private double calculateTotalDistance(int id) {
        double totalDistance = 0;
        String specificID = String.valueOf(id);
        String mSelectionClause = TrackContract.TrackingEntry.COLUMN_RUN_ID;
        String SELECTION = mSelectionClause + " = '" + specificID + "'";
        String[] PROJECTION = {TrackContract.TrackingEntry.COLUMN_DISTANCE};
        try {
            cur = getContentResolver()
                    .query(CONTENT_URI, PROJECTION, SELECTION, null, null);

            ArrayList<Double> distanceTempList = new ArrayList<>();
            if (cur != null && cur.moveToFirst()) {
                while (cur.moveToNext()) {
                    Double i = cur.getDouble(cur.getColumnIndex(COLUMN_DISTANCE));
                    distanceTempList.add(i);
                }
            }

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

        double mRoundedPreviousLatitude = Math.round((queryPreviousLocation(id)[0]) * 1000000) / 1000000.0d;
        double mRoundedPreviousLongitude = Math.round((queryPreviousLocation(id)[1]) * 1000000) / 1000000.0d;
//        mPreviousLatitude = 34.2000001;
//        mPreviousLongitude = -86.8000002;

        double mRoundedCurrentLatitude = Math.round((mCurrentLocation.getLatitude()) * 1000000) / 1000000.0d;
        double mRoundedCurrentLongitude = Math.round((mCurrentLocation.getLongitude()) * 1000000) / 1000000.0d;

        double mXYZDelta = (deltaXD + deltaYD + deltaZD);
        if (mPreviousLatitude != 0.0 && mPreviousLongitude != 0.0 &&
                (mXYZDelta != 0.0)) {
            mDistance = DistanceCalculator.greatCircleInKilometers(mRoundedPreviousLatitude,
                    mRoundedPreviousLongitude, mRoundedCurrentLatitude, mRoundedCurrentLongitude, 'K');
            Log.i("Print PreviousLatitude", String.valueOf(mRoundedPreviousLatitude));
            Log.i("Print PreviousLongitude", String.valueOf(mRoundedPreviousLongitude));
            Log.i("Print CurrentLatitude", String.valueOf(mRoundedCurrentLatitude));
            Log.i("Print CurrentLongitude", String.valueOf(mRoundedCurrentLongitude));
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
            cur = getContentResolver()
                    .query(CONTENT_URI, null, SELECTION, null, ORDER);

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

    public int queryMaxId() {

        String ORDER = " " + COLUMN_RUN_ID + " DESC LIMIT 1";
        try {
            cur = getContentResolver()
                    .query(CONTENT_URI, null, null, null, ORDER);

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

    private int queryLast_ID() {

        String ORDER = " " + _ID + " DESC LIMIT 1";
        try {
            cur = getContentResolver()
                    .query(CONTENT_URI, null, null, null, ORDER);

            if (cur != null && cur.moveToFirst()) {
                do {
                    int idColumnIndex = cur.getColumnIndex(_ID);
                    mLast_ID = cur.getInt(idColumnIndex);

                } while (cur.moveToNext());
            }
            if (cur != null) {
                cur.close();
            }

        } catch (Exception e) {
            Log.e("Path Error", e.toString());
        }
        return mLast_ID;
    }

    private int queryLastRow() {
        int runId = 0, runType;
        long startTimeinMillis, currentElapsedTime;
        double currentMaxAlt, currentMaxSpeed, currentAvrSpeed, currentTotalDistance;
        String ORDER = " " + _ID + " DESC LIMIT 1";
        try {
            cur = getContentResolver()
                    .query(CONTENT_URI, null, null, null, ORDER);

            if (cur != null && cur.moveToFirst()) {
                do {
                    int runIdColumnIndex = cur.getColumnIndex(COLUMN_RUN_ID);
                    int startTimeColumnIndex = cur.getColumnIndex(COLUMN_START_TIME);
                    int runTypeColumnIndex = cur.getColumnIndex(COLUMN_RUNTYPE);
                    int maxAltColumnIndex = cur.getColumnIndex(COLUMN_MAX_ALT);
                    int maxSpeedColumnIndex = cur.getColumnIndex(COLUMN_MAX_SPEED);
                    int avgSpeedColumnIndex = cur.getColumnIndex(COLUMN_AVR_SPEED);
                    int elapsedTimeColumnIndex = cur.getColumnIndex(COLUMN_TIME_COUNTER);
                    int totalColumnIndex = cur.getColumnIndex(COLUMN_TOTAL_DISTANCE);

                    runId = cur.getInt(runIdColumnIndex);

                    if (!queryLastRowPost(runId)) {
                        startTimeinMillis = cur.getInt(startTimeColumnIndex);
                        runType = cur.getInt(runTypeColumnIndex);
                        currentMaxAlt = cur.getInt(maxAltColumnIndex);
                        currentMaxSpeed = cur.getInt(maxSpeedColumnIndex);
                        currentAvrSpeed = cur.getInt(avgSpeedColumnIndex);
                        currentElapsedTime = cur.getInt(elapsedTimeColumnIndex);
                        currentTotalDistance = cur.getInt(totalColumnIndex);

                        saveItemPost(runId, startTimeinMillis, mStopTimeinMillis, runType, currentMaxAlt, currentMaxSpeed, currentAvrSpeed,
                                currentElapsedTime, currentTotalDistance);
                    }
                } while (cur.moveToNext());
            }
            if (cur != null) {
                cur.close();
            }

        } catch (Exception e) {
            Log.e("Path Error", e.toString());
        }
        return runId;
    }

    private boolean queryLastRowPost(int runId) {
        int runIdPost = 0;
        boolean duplicaded = false;

        String ORDER = " " + _ID + " DESC LIMIT 1";
        try {
            Cursor cursorP = getContentResolver()
                    .query(CONTENT_URI_POST, null, null, null, ORDER);

            if (cursorP != null && cursorP.moveToFirst()) {
                do {
                    int runIdColumnIndex = cursorP.getColumnIndex(COLUMN_RUN_IDP);
                    runIdPost = cursorP.getInt(runIdColumnIndex);

                } while (cursorP.moveToNext());
            }
            if (cursorP != null) {
                cursorP.close();
            }

        } catch (Exception e) {
            Log.e("Path Error", e.toString());
        }
        if (runId == runIdPost) {
            duplicaded = true;
        }

        return duplicaded;
    }

    public double queryMaxSpeed(int id) {
        String specificID = String.valueOf(id);
        String mSelectionClause = TrackContract.TrackingEntry.COLUMN_RUN_ID;
        String SELECTION = mSelectionClause + " = '" + specificID + "'";
        String ORDER = " " + COLUMN_SPEED + " DESC LIMIT 1";

        try {
            cur = getContentResolver()
                    .query(CONTENT_URI, null, SELECTION, null, ORDER);

            if (cur != null && cur.moveToFirst()) {
                do {
                    int idColumnIndex = cur.getColumnIndex(COLUMN_SPEED);
                    mMaxSpeed = cur.getDouble(idColumnIndex);

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
            cur = getContentResolver()
                    .query(CONTENT_URI, null, SELECTION, null, ORDER);

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
        String ORDER = " " + COLUMN_ALTITUDE + " ASC LIMIT 1 OFFSET 1";
//        String[] PROJECTION = {TrackContract.TrackingEntry.COLUMN_ALTITUDE};

        try {
            cur = getContentResolver()
                    .query(CONTENT_URI, null, SELECTION, null, ORDER);

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
        double sum = 0;
        double averageSpeed = 0;
        int size;

        String specificID = String.valueOf(id);
        String mSelectionClause = TrackContract.TrackingEntry.COLUMN_RUN_ID;
        String SELECTION = mSelectionClause + " = '" + specificID + "'";
        String[] PROJECTION = {TrackContract.TrackingEntry.COLUMN_SPEED};
        try {
            cur = getContentResolver()
                    .query(CONTENT_URI, PROJECTION, SELECTION, null, null);

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

    private boolean checkMoveDistance(int id) {
        int sum = 0;
        int size = 0;
        boolean mNoMove = false;
        String specificID = String.valueOf(id);
        String mSelectionClause = TrackContract.TrackingEntry.COLUMN_RUN_ID;
        String SELECTION = mSelectionClause + " = '" + specificID + "'";
        String[] PROJECTION = {TrackContract.TrackingEntry.COLUMN_MOVE_DISTANCE};
        String ORDER = " " + COLUMN_TIME_COUNTER + " DESC LIMIT " + GET_GEOLOCATION_LAST_ROWS;
        try {
            cur = getContentResolver()
                    .query(CONTENT_URI, PROJECTION, SELECTION, null, ORDER);

            ArrayList<Double> nomoveDistance = new ArrayList<>();
            if (cur != null && cur.moveToFirst()) {
                while (cur.moveToNext()) {
                    Double i = cur.getDouble(cur.getColumnIndex(COLUMN_MOVE_DISTANCE));
                    nomoveDistance.add(i);
                    size = nomoveDistance.size();
                    Log.i("nomoveDistance:", String.valueOf(size));
                }
            }
//            Log.i("Print list", speedTempList.toString());

            for (int i = 0; i < nomoveDistance.size(); i++) {
                sum += nomoveDistance.get(i);
            }
            mNoMove = (sum == 0.0) && (size == (GET_GEOLOCATION_LAST_ROWS - 1));

            Log.i("No Move:", String.valueOf(mNoMove));
            if (cur != null) {
                cur.close();
            }

        } catch (Exception e) {
            Log.e("Path Error", e.toString());
        }
        return mNoMove;
    }

    private boolean checkMoveClose(final int id) {
        boolean mNoMove = false;

        int sum = 0;
        int size = 0;

        String specificID = String.valueOf(id);
        String mSelectionClause = TrackContract.TrackingEntry.COLUMN_RUN_ID;
        String SELECTION = mSelectionClause + " = '" + specificID + "'";
        String[] PROJECTION = {TrackContract.TrackingEntry.COLUMN_MOVE_CLOSE};
        String ORDER = " " + COLUMN_TIME_COUNTER + " DESC LIMIT " + DELETE_LAST_ROWS;
        try {
            cur = getContentResolver()
                    .query(CONTENT_URI, PROJECTION, SELECTION, null, ORDER);

            ArrayList<Double> moveCloseList = new ArrayList<>();
            if (cur != null && cur.moveToFirst()) {
                while (cur.moveToNext()) {
                    Double i = cur.getDouble(cur.getColumnIndex(COLUMN_MOVE_CLOSE));
                    moveCloseList.add(i);
                    size = moveCloseList.size();
                    Log.i("moveCloseList:", String.valueOf(size));
                }
            }
            for (int i = 0; i < moveCloseList.size(); i++) {
                sum += moveCloseList.get(i);
            }
            mNoMove = (sum == 0.0) && (size == (DELETE_LAST_ROWS - 1));
            Log.i("No Move:", String.valueOf(mNoMove));
            if (cur != null) {
                cur.close();
            }

        } catch (Exception e) {
            Log.e("Path Error", e.toString());
        }

        return mNoMove;
    }

    private void deletelastNoMoveRows() {
        // method does not work "in background", when user gets out of an app without stopping location

        if (getApplicationContext() != null) {
            Log.i("deletelastNoMoveRows", "isAdded()&& mContext != null)");
            for (int i = 0; i < (DELETE_LAST_ROWS - GET_GEOLOCATION_LAST_ROWS - 2); i++) {
                int last_ID = queryLast_ID();
                String lastRow = TrackContract.TrackingEntry._ID + "=" + last_ID;
                getContentResolver().delete(CONTENT_URI, lastRow, null);
//                Toast.makeText(RealTimeFragment.mContext, (DELETE_LAST_ROWS - GET_GEOLOCATION_LAST_ROWS) + " " + getString(R.string.delete_one_item), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void saveItem(final int runId, final long startTimeinMillis, final int runType, final long currentTime, final double currentLatitude, final double currentLongitude,
                          final double currentAltitude, final double currentMaxAlt, final double currentMinAlt,
                          final double currentSpeed, final double currentMaxSpeed, final double currentAvrSpeed,
                          final long currentElapsedTime, final double currentDistance, final double currentTotalDistance,
                          final double currentMoveDistance, final double currentMoveClose) {

        // Database operations should not be done on the main thread
        AsyncTask<Void, Void, Void> insertItem = new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... voids) {
                ContentValues values = new ContentValues();
                values.put(COLUMN_RUN_ID, runId);
                values.put(COLUMN_START_TIME, startTimeinMillis);
                values.put(COLUMN_RUNTYPE, runType);
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
                values.put(COLUMN_MOVE_DISTANCE, currentMoveDistance);
                values.put(COLUMN_MOVE_CLOSE, currentMoveClose);
//                values.put(COLUMN_ADDRESS, currentAddress);

                // This is a NEW item, so insert a new item into the provider,
                // returning the content URI for the item item.
                getContentResolver().insert(CONTENT_URI, values);

                return null;
            }
        };
        insertItem.execute();
    }

    private void saveItemPost(final int runId, final long startTimeinMillis, final long stopTimeinMillis, final int runType, final double currentMaxAlt,
                              final double currentMaxSpeed, final double currentAvrSpeed,
                              final long currentElapsedTime, final double currentTotalDistance) {
        Log.e(TAG, "saving" + runId);
        // Database operations should not be done on the main thread
        AsyncTask<Void, Void, Void> insertItem = new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... voids) {
                ContentValues values = new ContentValues();
                values.put(COLUMN_RUN_IDP, runId);
                values.put(COLUMN_START_TIMEP, startTimeinMillis);
                values.put(COLUMN_STOP_TIMEP, stopTimeinMillis);
                values.put(COLUMN_RUNTYPEP, runType);
                values.put(COLUMN_TOTAL_DISTANCEP, currentTotalDistance);
                values.put(COLUMN_MAX_ALTP, currentMaxAlt);
                values.put(COLUMN_MAX_SPEEDP, currentMaxSpeed);
                values.put(COLUMN_AVR_SPEEDP, currentAvrSpeed);
                values.put(COLUMN_TIME_COUNTERP, currentElapsedTime);
                values.put(COLUMN_FAVORITEP, 0);

                // This is a NEW item, so insert a new item into the provider,
                // returning the content URI for the item item.
                getContentResolver().insert(CONTENT_URI_POST, values);

                return null;
            }
        };
        insertItem.execute();
    }

    private void autoStopLocationNoMovement() {
        if (DISABLE_AUTO_CLOSE) {
            Log.d(TAG, "AUTO CLOSE IS ON");
            if (mNoMoveClose) {
                stopLocationUpdates();
                deletelastNoMoveRows();
            }
        } else {
            Log.d(TAG, "AUTO CLOSE IS OFF");

        }
    }

    private String getCompleteAddressString(double LATITUDE, double LONGITUDE) {

        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(LATITUDE, LONGITUDE, 1);
            if (addresses != null) {
                Address returnedAddress = addresses.get(0);
                StringBuilder strReturnedAddress = new StringBuilder();

                for (int i = 0; i <= returnedAddress.getMaxAddressLineIndex(); i++) {
                    strReturnedAddress.append(returnedAddress.getAddressLine(i)).append("\n");
                }
                mAddressOutput = strReturnedAddress.toString();
                Log.w("My address", strReturnedAddress.toString());
            } else {
                Log.w("My address", "No Address returned!");
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.w("Myloction address", "Canont get Address!");
        }
        return mAddressOutput;
    }

    /**
     * Class used for the client Binder.  Since this service runs in the same process as its
     * clients, we don't need to deal with IPC.
     */
    public class LocalBinder extends Binder {
        public LocationUpdatesService getService() {
            return LocationUpdatesService.this;
        }
    }

    /**
     * Sets the value of the UI fields for the location latitude, longitude and last update time.
     */
    private void updateLocationUI(Location location) {

        if (location != null) {

            mCurrentLatitude = location.getLatitude();
            mCurrentLongitude = location.getLongitude();
            mCurrentAltitude = location.getAltitude();
            mCurrentSpeed = ((location.getSpeed()) * 3.6);
            mMaxSpeed = queryMaxSpeed(mCurrentId);
            mMaxAltitude = queryMaxAlt(mCurrentId);
            mMinAltitude = queryMinAlt(mCurrentId);
            mAverageSpeed = calculateAverageSpeed(mCurrentId);
            mDistance = calculateDistance(mCurrentId);
            mTotalDistance = calculateTotalDistance(mCurrentId);
            mElapsedTimeMillis = getElapsedTime();
            mPreviousLatitude = queryPreviousLocation(mCurrentId)[0];
            mPreviousLongitude = queryPreviousLocation(mCurrentId)[1];

            checkXD = deltaXD;
            checkYD = deltaYD;
            checkZD = deltaZD;
            mMoveDistance = (checkXD + checkYD + checkZD) / 3;

            checkXC = deltaXC;
            checkYC = deltaYC;
            checkZC = deltaZC;
            mMoveClose = (checkXC + checkYC + checkZC) / 3;

            mNoMoveDistance = checkMoveDistance(mCurrentId);
            mNoMoveClose = checkMoveClose(mCurrentId);
            autoStopLocationNoMovement();

            if (requestingLocationUpdates(getApplicationContext())) {

                if (mNoMoveDistance) {
                    mCurrentAddress = getCompleteAddressString(mCurrentLatitude, mCurrentLongitude);
                } else {
                    mCurrentAddress = "";
                }

                saveItem(mCurrentId, mStartTimeinMillis, mRunType, mLastUpdateTimeMillis, mCurrentLatitude, mCurrentLongitude,
                        mCurrentAltitude, mMaxAltitude, mMinAltitude, mCurrentSpeed, mMaxSpeed,
                        mAverageSpeed, mElapsedTimeMillis, mDistance, mTotalDistance, mMoveDistance,
                        mMoveClose);
            }

            int precision = 1000; //keep 3 digits
            float mTotalDistanceFloat = (float) (Math.floor(mTotalDistance * precision + .5) / precision);
            String mElapsedTime1 = getDateFromMillis(mElapsedTimeMillis);

            sendNotificationAfterStart(this, mNotificationInformation, mNotificationRunInformation);

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

        if (RUN_TYPE_VALUE == 3) {
            RealTimeFragment.sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            UPDATE_INTERVAL_IN_MILLISECONDS_STRING = RealTimeFragment.sharedPrefs.getString(
                    getString(R.string.update_interval_by_key),
                    getString(R.string.update_interval_by_default_ultimate)
            );
            UPDATE_INTERVAL_IN_MILLISECONDS = Long.parseLong(UPDATE_INTERVAL_IN_MILLISECONDS_STRING);
        } else {
            UPDATE_INTERVAL_IN_MILLISECONDS = RUN_TYPE_INTERVAL;
        }
        mLocationRequest = new LocationRequest();

        // Sets the desired interval for active location updates. This interval is
        // inexact. You may not receive updates at all if no location sources are available, or
        // you may receive them slower than requested. You may also receive updates faster than
        // requested if other applications are requesting location at a faster interval.
        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);
        Log.i("update interval", String.valueOf(UPDATE_INTERVAL_IN_MILLISECONDS));

        // Sets the fastest rate for active location updates. This interval is exact, and your
        // application will never receive updates faster than this value.
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);

        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setSmallestDisplacement(DISPLACEMENT);
    }

    private void sendNotificationAfterStart(Context context, String mNotificationInformation, String mNotificationRunInformation) {
        // Get an instance of the Notification manager
        mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        int importance = NotificationManager.IMPORTANCE_LOW;
        NotificationChannel mChannel = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            mChannel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance);
            mChannel.setDescription(DESCRIPTION);
            mChannel.enableLights(true);
            mChannel.enableVibration(true);
            mChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
            mNotificationManager.createNotificationChannel(mChannel);
        }

        // Create an explicit content Intent that starts the main Activity.
        Intent notificationIntent = new Intent(context, MainActivity.class);

        // Construct a task stack.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);

        // Add the main Activity to the task stack as the parent.
        stackBuilder.addParentStack(MainActivity.class);

        // Push the content Intent onto the stack.
        stackBuilder.addNextIntent(notificationIntent);

//         Get a PendingIntent containing the entire back stack.
        PendingIntent notificationPendingIntent =
                stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);


        Intent stopIntent = new Intent(context, LocationUpdatesService.class);
        // Extra to help us figure out if we arrived in onStartCommand via the notification or not.
        stopIntent.setAction(EXTRA_STOP_FROM_NOTIFICATION);
        PendingIntent pStopIntent = PendingIntent.getService(context, 0,
                stopIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        int RUN_TYPE_PICTURE = mSharedPrefsRunType.getInt(RUN_TYPE_PICTURE_KEY, 1);
        Bitmap icon = BitmapFactory.decodeResource(context.getResources(), RUN_TYPE_PICTURE);

        notification = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setContentTitle(mNotificationRunInformation)
//                .setTicker("Ultimate Tracker")
                .setContentText(mNotificationInformation)
                .setWhen(System.currentTimeMillis())  // the time stamp, you will probably use System.currentTimeMillis() for most scenarios
//                .setUsesChronometer(true)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setLargeIcon(
                        Bitmap.createScaledBitmap(icon, 128, 128, false))
                .setContentIntent(notificationPendingIntent)
                .setOngoing(true)
                .addAction(R.drawable.ic_stop, "Stop Tracking",
                        pStopIntent)
                .setAutoCancel(true)
                .build();

        startForeground(NOTIFICATION_ID, notification);
    }

    private void sendNotificationAfterStop(Context context, String mNotificationInformation, String mNotificationRunInformation) {
        // Get an instance of the Notification manager
        mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        int importance = NotificationManager.IMPORTANCE_LOW;
        NotificationChannel mChannel = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            mChannel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance);
            mChannel.setDescription(DESCRIPTION);
            mChannel.enableLights(true);
            mChannel.enableVibration(true);
            mChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
            mNotificationManager.createNotificationChannel(mChannel);
        }

        // Create an explicit content Intent that starts the main Activity.
        Intent notificationIntent = new Intent(context, MainActivity.class);

        // Construct a task stack.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);

        // Add the main Activity to the task stack as the parent.
        stackBuilder.addParentStack(MainActivity.class);

        // Push the content Intent onto the stack.
        stackBuilder.addNextIntent(notificationIntent);

//         Get a PendingIntent containing the entire back stack.
        PendingIntent notificationPendingIntent =
                stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent startIntent = new Intent(context, LocationUpdatesService.class);
        // Extra to help us figure out if we arrived in onStartCommand via the notification or not.
        startIntent.setAction(EXTRA_START_FROM_NOTIFICATION);
        PendingIntent pStartIntent = PendingIntent.getService(context, 0,
                startIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        int RUN_TYPE_PICTURE = mSharedPrefsRunType.getInt(RUN_TYPE_PICTURE_KEY, 1);

        Bitmap icon = BitmapFactory.decodeResource(context.getResources(), RUN_TYPE_PICTURE);

        notification = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setContentTitle(mNotificationRunInformation)
//                .setTicker("Ultimate Tracker")
                .setContentText(mNotificationInformation)
                .setWhen(System.currentTimeMillis())  // the time stamp, you will probably use System.currentTimeMillis() for most scenarios
//                .setUsesChronometer(true)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setLargeIcon(
                        Bitmap.createScaledBitmap(icon, 128, 128, false))
                .setContentIntent(notificationPendingIntent)
                .setOngoing(true)
                .addAction(R.id.icon_only, "Start Tracking",
                        pStartIntent)
                .setAutoCancel(true)
                .build();

        startForeground(NOTIFICATION_ID, notification);

    }

    /**
     * Handles the Start Updates button and requests start of location updates. Does nothing if
     * updates have already been requested.
     */
    public void startUpdatesButtonHandler() {
        if (!requestingLocationUpdates(this)) {
            mStartTimeinMillis = System.currentTimeMillis();
            setStartTimeCurrentTrack(this, mStartTimeinMillis);
            mCurrentId = queryMaxId() + 1;
            setLastTrackID(this, mCurrentId);
            mRunType = lastTrackType(this);
            NOISEd = RUN_TYPE_NOISE;
            startLocationUpdates();
            mNotificationInformation = getResources().getString(R.string.Tracking_started_at) + getFormatedTimeInString() + "      STARTED...";
            mNotificationRunInformation = getResources().getString(R.string.Current_run) + mCurrentId;
            sendNotificationAfterStart(this, mNotificationInformation, mNotificationRunInformation);

            // Notify anyone listening for broadcasts about the new location.
            Intent intent = new Intent(ACTION_BROADCAST);
            intent.putExtra(EXTRA_REQUESTING_UDPATES, requestingLocationUpdates(this));
            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
        }
    }

    /**
     * Handles the Stop Updates button, and requests removal of location updates.
     */
    public void stopUpdatesButtonHandler() {
        // It is a good practice to remove location requests when the activity is in a paused or
        // stopped state. Doing so helps battery performance and is especially
        // recommended in applications that request frequent location updates.
        stopLocationUpdates();
        setStartTimeCurrentTrack(this, 300780120);
        mNotificationInformation = getResources().getString(R.string.Tracking_stopped_at) + getFormatedTimeInString() + "     ...STOPPED";
        mNotificationRunInformation = getResources().getString(R.string.Last_run_number) + mCurrentId;
        sendNotificationAfterStop(this, mNotificationInformation, mNotificationRunInformation);

        // Notify anyone listening for broadcasts about the new location.
        Intent intent = new Intent(ACTION_BROADCAST);
        intent.putExtra(EXTRA_REQUESTING_UDPATES, requestingLocationUpdates(this));
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void onNewLocation(Location location) {
        Log.i(TAG, "New location: " + location);

        // Notify anyone listening for broadcasts about the new location.
        Intent intent = new Intent(ACTION_BROADCAST);
        intent.putExtra(EXTRA_CURRENT_ID, mCurrentId);
        intent.putExtra(EXTRA_RUN_TYPE, mRunType);
        intent.putExtra(EXTRA_LOCATION, location);
        intent.putExtra(EXTRA_LATITUDE, mCurrentLatitude);
        intent.putExtra(EXTRA_LONGITUDE, mCurrentLongitude);
        intent.putExtra(EXTRA_ALTITUDE, mCurrentAltitude);
        intent.putExtra(EXTRA_SPEED, mCurrentSpeed);
        intent.putExtra(EXTRA_MAX_SPEED, mMaxSpeed);
        intent.putExtra(EXTRA_MAX_ALTITUDE, mMaxAltitude);
        intent.putExtra(EXTRA_MIN_ALTITUDE, mMinAltitude);
        intent.putExtra(EXTRA_AVG_SPEED, mAverageSpeed);
        intent.putExtra(EXTRA_TOTAL_DISTANCE, mTotalDistance);
        intent.putExtra(EXTRA_PREV_LATITUDE, mPreviousLatitude);
        intent.putExtra(EXTRA_PREV_LONGITUDE, mPreviousLongitude);
        intent.putExtra(EXTRA_TOTAL_TIME, mElapsedTimeMillis);
        intent.putExtra(EXTRA_LAST_TIME_UPDATE, mLastUpdateTimeMillis);
        intent.putExtra(EXTRA_ADDRESS, mCurrentAddress);
        intent.putExtra(EXTRA_REQUESTING_UDPATES, requestingLocationUpdates(this));

        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);

        Intent startWidgetIntent = new Intent(this, TrackingWidgetProvider.class);
        startWidgetIntent.setAction(ACTION_FROM_SERVICE);
        startWidgetIntent.putExtra(EXTRA_TOTAL_TIME, mElapsedTimeMillis);
        startWidgetIntent.putExtra(EXTRA_CURRENT_ID, mCurrentId);
        startWidgetIntent.putExtra(EXTRA_TOTAL_DISTANCE, mTotalDistance);
        startWidgetIntent.putExtra(EXTRA_TOTAL_DISTANCE, mTotalDistance);
        startWidgetIntent.putExtra(EXTRA_SPEED, mCurrentSpeed);
        startWidgetIntent.putExtra(EXTRA_AVG_SPEED, mAverageSpeed);
        startWidgetIntent.putExtra(EXTRA_ALTITUDE, mCurrentAltitude);
        startWidgetIntent.putExtra(EXTRA_MAX_ALTITUDE, mMaxAltitude);

        sendBroadcast(startWidgetIntent);
    }

    /**
     * @return whether the timer is running
     */
    public boolean isTimerRunning() {
        setStopWatchRunning(this, isTimerRunning);
        return isTimerRunning;
    }
//    public static void elapsedTimeBroadCast(Context context) {
//        // If the timer is running, the end time will be zero
//        String elapsedTime = timer.elapsedTimeString0();
//        Intent intent = new Intent(ACTION_BROADCAST);
//        intent.putExtra(EXTRA_STOP_WATCH, elapsedTime);
//
//        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
//    }
}
