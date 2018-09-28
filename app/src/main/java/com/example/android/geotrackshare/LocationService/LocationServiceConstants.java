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


import android.content.Context;
import android.location.Location;
import android.preference.PreferenceManager;

import com.example.android.geotrackshare.R;

import java.text.DateFormat;
import java.util.Date;

public class LocationServiceConstants {

    public static final String KEY_REQUESTING_LOCATION_UPDATES = "requesting_locaction_updates";
    private static final String KEY_IS_TRACK_PAUSED = "KEY_IS_TRACK_PAUSED";
    private static final String KEY_IS_STOPWATCH_RUNNING = "KEY_IS_STOPWATCH_RUNNING";
    private static final String KEY_IS_SERVICE_BOUND = "KEY_IS_SERVICE_BOUND";
    private static final String KEY_LAST_TRACK_TYPE = "KEY_LAST_TRACK_TYPE";
    private static final String KEY_LAST_TRACK_ID = "KEY_LAST_TRACK_ID";
    private static final String KEY_START_TIME_TRACK = "KEY_START_TIME_TRACK";
    private static final String KEY_PAUSE_TIME_TRACK = "KEY_PAUSE_TIME_TRACK";
    private static final String KEY_LAST_DB_EXPORT = "KEY_LAST_DB_EXPORT";
    private static final String KEY_LAST_DB_AUTO_EXPORT = "KEY_LAST_DB_AUTO_EXPORT";
    private static final String KEY_IS_DB_EXPORT_DONE = "KEY_IS_DB_EXPORT_DONE";
    private static final String KEY_IS_DB_AUTO_EXPORT_DONE = "KEY_IS_DB_AUTO_EXPORT_DONE";



    /**
     * Returns true if requesting location updates, otherwise returns false.
     *
     * @param context The {@link Context}.
     */
    public static boolean requestingLocationUpdates(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(KEY_REQUESTING_LOCATION_UPDATES, false);
    }

    /**
     * Stores the location updates state in SharedPreferences.
     *
     * @param requestingLocationUpdates The location updates state.
     */
    public static void setRequestingLocationUpdates(Context context, boolean requestingLocationUpdates) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putBoolean(KEY_REQUESTING_LOCATION_UPDATES, requestingLocationUpdates)
                .apply();
    }

    /**
     * Returns true if requesting paused track state, otherwise returns false.
     *
     * @param context The {@link Context}.
     */
    public static boolean requestingIfTrackIsPaused(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(KEY_IS_TRACK_PAUSED, false);
    }

    /**
     * Stores the paused track state in SharedPreferences.
     *
     * @param trackIsPaused The location updates state.
     */
    public static void setIfTrackIsPaused(Context context, boolean trackIsPaused) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putBoolean(KEY_IS_TRACK_PAUSED, trackIsPaused)
                .apply();
    }


    /**
     * Returns true if requesting last track type, otherwise returns -1.
     *
     * @param context The {@link Context}.
     */
    public static int lastTrackType(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getInt(KEY_LAST_TRACK_TYPE, 0);
    }

    /**
     * Stores the last track type state in SharedPreferences.
     *
     * @param trackType The location updates state.
     */
    public static void setLastTrackType(Context context, int trackType) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putInt(KEY_LAST_TRACK_TYPE, trackType)
                .apply();
    }

    /**
     * Returns true if requesting last track type, otherwise returns -1.
     *
     * @param context The {@link Context}.
     */
    public static int lastTrackID(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getInt(KEY_LAST_TRACK_ID, -1);
    }

    /**
     * Stores the last track ID  in SharedPreferences.
     *
     * @param trackID The location updates state.
     */
    public static void setLastTrackID(Context context, int trackID) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putInt(KEY_LAST_TRACK_ID, trackID)
                .apply();
    }

    /**
     * Returns true if requesting track start time, otherwise returns 0.
     *
     * @param context The {@link Context}.
     */
    public static long startTimeCurrentTrack(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getLong(KEY_START_TIME_TRACK, 300780120);
    }

    /**
     * Stores the last track start time  in SharedPreferences.
     *
     * @param startTimeTrack updates start time.
     */
    public static void setStartTimeCurrentTrack(Context context, long startTimeTrack) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putLong(KEY_START_TIME_TRACK, startTimeTrack)
                .apply();
    }

    /**
     * Returns true if requesting track pause time, otherwise returns 0.
     *
     * @param context The {@link Context}.
     */
    public static long pauseTimeCurrentTrack(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getLong(KEY_PAUSE_TIME_TRACK, 0);
    }

    /**
     * Stores the current track pause time in SharedPreferences.
     *
     * @param pauseTimeTrack updates start time.
     */
    public static void setPauseTimeCurrentTrack(Context context, long pauseTimeTrack) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putLong(KEY_PAUSE_TIME_TRACK, pauseTimeTrack)
                .apply();
    }

    /**
     * Returns true if stopWatchRunning, otherwise returns false.
     *
     * @param context The {@link Context}.
     */
    public static Boolean stopWatchRunning(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(KEY_IS_STOPWATCH_RUNNING, false);
    }

    /**
     * Stores the boolean isRunning in SharedPreferences.
     *
     * @param isRunning updates start time.
     */
    public static void setStopWatchRunning(Context context, boolean isRunning) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putBoolean(KEY_IS_STOPWATCH_RUNNING, isRunning)
                .apply();
    }


    /**
     * Returns the {@code location} object as a human readable string.
     *
     * @param location The {@link Location}.
     */
    public static String getLocationText(Location location) {
        return location == null ? "Unknown location" :
                "(" + location.getLatitude() + ", " + location.getLongitude() + ")";
    }

    public static String getLocationTitle(Context context) {
        return context.getString(R.string.location_updated,
                DateFormat.getDateTimeInstance().format(new Date()));
    }

    /**
     * Returns true if service in Bound, otherwise returns false.
     *
     * @param context The {@link Context}.
     */
    public static Boolean serviceBound(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(KEY_IS_SERVICE_BOUND, false);
    }

    /**
     * Stores the boolean isBound in SharedPreferences.
     *
     * @param isBound updates start time.
     */
    public static void setServiceBound(Context context, boolean isBound) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putBoolean(KEY_IS_SERVICE_BOUND, isBound)
                .apply();
    }

    /**
     * Returns last backup date and time in string, otherwise .
     *
     * @param context The {@link Context}.
     */
    public static String lastDBExportTime(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getString(KEY_LAST_DB_EXPORT, String.valueOf(R.string.no_backup));
    }

    /**
     * Stores the backup date and time  in SharedPreferences.
     *
     * @param dateTimeOfExport Last backup date and time.
     */
    public static void setLastExportTime(Context context, String dateTimeOfExport) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putString(KEY_LAST_DB_EXPORT, dateTimeOfExport)
                .apply();
    }


    /**
     * Returns true if db export done, otherwise returns false.
     *
     * @param context The {@link Context}.
     */
    public static boolean isExportDone(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(KEY_IS_DB_EXPORT_DONE, false);
    }

    /**
     * Stores flag if export was done state in SharedPreferences.
     *
     * @param exportDone The export updates state.
     */
    public static void setExportDone(Context context, boolean exportDone) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putBoolean(KEY_IS_DB_EXPORT_DONE, exportDone)
                .apply();
    }

    /**
     * Returns last backup date and time in string, otherwise .
     *
     * @param context The {@link Context}.
     */
    public static String lastDBAutoExportTime(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getString(KEY_LAST_DB_AUTO_EXPORT, String.valueOf(R.string.no_backup));
    }

    /**
     * Stores the backup date and time  in SharedPreferences.
     *
     * @param dateTimeOfExport Last backup date and time.
     */
    public static void setLastAutoExportTime(Context context, String dateTimeOfExport) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putString(KEY_LAST_DB_AUTO_EXPORT, dateTimeOfExport)
                .apply();
    }


    /**
     * Returns true if db export done, otherwise returns false.
     *
     * @param context The {@link Context}.
     */
    public static boolean isAutoExportDone(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(KEY_IS_DB_AUTO_EXPORT_DONE, false);
    }

    /**
     * Stores flag if export was done state in SharedPreferences.
     *
     * @param exportDone The export updates state.
     */
    public static void setAutoExportDone(Context context, boolean exportDone) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putBoolean(KEY_IS_DB_AUTO_EXPORT_DONE, exportDone)
                .apply();
    }

}
