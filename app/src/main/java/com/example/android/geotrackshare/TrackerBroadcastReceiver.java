package com.example.android.geotrackshare;

/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.icu.util.TimeZone;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.widget.Toast;

import com.example.android.geotrackshare.LocationService.LocationUpdatesService;
import com.example.android.geotrackshare.Utils.Constants;

import java.util.concurrent.TimeUnit;

import static com.example.android.geotrackshare.LocationService.LocationUpdatesService.EXTRA_CURRENT_ID;


public class TrackerBroadcastReceiver extends BroadcastReceiver {

    public static final String TAG = TrackerBroadcastReceiver.class.getSimpleName();
    private String CHANNEL_ID = "02";
    private String CHANNEL_NAME = "GeoTracker Channel";
    private String DESCRIPTION = "GeoTracking";
    private long startTime;
    private int currentRun;
    private Notification notification;
    private NotificationManager mNotificationManager;

    @SuppressLint("NewApi")
    public static final String getDateFromMillis(long d) {
        android.icu.text.SimpleDateFormat df = new android.icu.text.SimpleDateFormat("HH:mm:ss");
        df.setTimeZone(TimeZone.getTimeZone("GMT"));
        return df.format(d);
    }

    /***
     * Handles the Broadcast message sent when the Tracker is triggered
     * Careful here though, this is running on the main thread so make sure you start an AsyncTask for
     * anything that takes longer than say 10 second to run
     *
     * @param context
     * @param intent
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onReceive(Context context, Intent intent) {
        // Get the Geofence Event from the Intent sent through
        Toast.makeText(context, "GeoTracker is running.",
                Toast.LENGTH_SHORT).show();
//        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
//        if (geofencingEvent.hasError()) {
//            Log.e(TAG, String.format("Error code : %d", geofencingEvent.getErrorCode()));
//            return;
//        }
//
//        // Get the transition type.
//        int geofenceTransition = geofencingEvent.getGeofenceTransition();
//        // Check which transition type has triggered this event
//        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER) {
//            setRingerMode(context, AudioManager.RINGER_MODE_SILENT);
//        } else if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {
//            setRingerMode(context, AudioManager.RINGER_MODE_NORMAL);
//        } else {
//            // Log the error.
//            Log.e(TAG, String.format("Unknown transition : %d", geofenceTransition));
//            // No need to do anything else
//            return;
//        }
        String mCurrentAddress = intent.getStringExtra(LocationUpdatesService.EXTRA_ADDRESS);
        String startTimeString = intent.getStringExtra(RealTimeFragment.START_TIME);
        Double mTotalDistance = intent.getDoubleExtra(LocationUpdatesService.EXTRA_TOTAL_DISTANCE, 0);

        String action = intent.getAction();
        currentRun = intent.getIntExtra(EXTRA_CURRENT_ID, 0);
        Long mElapsedTimeMillis = intent.getLongExtra(LocationUpdatesService.EXTRA_TOTAL_TIME, 0);
        String mElapsedTime = String.format("%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(mElapsedTimeMillis),
                TimeUnit.MILLISECONDS.toMinutes(mElapsedTimeMillis) % TimeUnit.HOURS.toMinutes(1),
                TimeUnit.MILLISECONDS.toSeconds(mElapsedTimeMillis) % TimeUnit.MINUTES.toSeconds(1));
        String mElapsedTime1 = getDateFromMillis(mElapsedTimeMillis);

        currentRun = intent.getIntExtra(RealTimeFragment.CURRENT_RUN,0);
        if (action.equals(Constants.ACTION.STARTFOREGROUND_ACTION)) {
            // Send the notification
            sendNotification(context, mElapsedTime1, currentRun);
        }
    }

    /**
     * Posts a notification in the notification bar when a transition is detected
     * Uses different icon drawables for different transition types
     * If the user clicks the notification, control goes to the MainActivity
     *
     * @param context The calling context for building a task stack
     */


    private void sendNotification(Context context, String elapsedTime, int currentRun) {
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

        // Get a PendingIntent containing the entire back stack.
        PendingIntent notificationPendingIntent =
                stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
//        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
//                notificationIntent, 0);

        Intent previousIntent = new Intent(context,RealTimeFragment.class);
        previousIntent.setAction(Constants.ACTION.MAIN_ACTION);
        PendingIntent ppreviousIntent = PendingIntent.getService(context, 0,
                previousIntent, 0);

        Intent pauseIntent = new Intent(context,RealTimeFragment.class);
        pauseIntent.setAction(Constants.ACTION.PAUSE_ACTION);
        PendingIntent pPauseIntent = PendingIntent.getService(context, 0,
                pauseIntent, 0);

        Intent stopIntent = new Intent(context,RealTimeFragment.class);
        stopIntent.setAction(Constants.ACTION.STOP_ACTION);
        PendingIntent pStopIntent = PendingIntent.getService(context, 0,
                stopIntent, 0);

        Bitmap icon = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_directions_walk_black_24dp);

        notification = new NotificationCompat.Builder(context,CHANNEL_ID)
                .setContentTitle("Ultimate Tracker")
                .setTicker("Ultimate Tracker")
                .setContentText("Current Run: " + currentRun + "lasting: " + elapsedTime)
//                .setWhen(System.currentTimeMillis())  // the time stamp, you will probably use System.currentTimeMillis() for most scenarios
//                .setUsesChronometer(true)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setLargeIcon(
                        Bitmap.createScaledBitmap(icon, 128, 128, false))
                .setContentIntent(notificationPendingIntent)
                .setOngoing(true)
//                .addAction(android.R.drawable.ic_input_add,
//                        "Change Mode", ppreviousIntent)
//                .addAction(android.R.drawable.ic_media_pause, "Pause",
//                        pPauseIntent)
                .addAction(R.drawable.ic_stop, "Stop Tracing Your Activity",
                        pStopIntent)
                .setAutoCancel(false)
                .build();
//        } else if (intent.getAction().equals(Constants.ACTION.MAIN_ACTION)) {
//            Log.i(TAG, "Clicked Change Mode");
//        } else if (intent.getAction().equals(Constants.ACTION.PLAY_ACTION)) {
//            Log.i(TAG, "Clicked Start");
//        } else if (intent.getAction().equals(Constants.ACTION.STOP_ACTION)) {
//            Log.i(TAG, "Clicked Stop");

//            Intent stopIntent = new Intent();
////            stopIntent.setAction(Constants.ACTION.STOPFOREGROUND_ACTION);
////            this.startService(stopIntent);
////
////
////        } else if (intent.getAction().equals(
////                Constants.ACTION.STOPFOREGROUND_ACTION)) {
////            Log.i(TAG, "Received Stop Foreground Intent");
////            stopForeground(true);
////            stopSelf();
////        }
//        // Check the transition type to display the relevant icon image
//        if (transitionType == Geofence.GEOFENCE_TRANSITION_ENTER) {
//            builder.setSmallIcon(R.drawable.ic_volume_off_white_24dp)
//                    .setLargeIcon(BitmapFactory.decodeResource(context.getResources(),
//                            R.drawable.ic_volume_off_white_24dp))
//                    .setContentTitle(context.getString(R.string.silent_mode_activated));
//        } else if (transitionType == Geofence.GEOFENCE_TRANSITION_EXIT) {
//            builder.setSmallIcon(R.drawable.ic_volume_up_white_24dp)
//                    .setLargeIcon(BitmapFactory.decodeResource(context.getResources(),
//                            R.drawable.ic_volume_up_white_24dp))
//                    .setContentTitle(context.getString(R.string.back_to_normal));
//        }
//
//        // Continue building the notification
//        builder.setContentText(context.getString(R.string.touch_to_relaunch));
//        builder.setContentIntent(notificationPendingIntent);
//
//        // Dismiss notification once the user touches it.
//        builder.setAutoCancel(true);


        // Issue the notification
        assert mNotificationManager != null;
        mNotificationManager.notify(0, notification);
    }

    /**
     * Changes the ringer mode on the device to either silent or back to normal
     *
     * @param context The context to access AUDIO_SERVICE
     * @param mode    The desired mode to switch device to, can be AudioManager.RINGER_MODE_SILENT or
     *                AudioManager.RINGER_MODE_NORMAL
     */
//    private void setRingerMode(Context context, int mode) {
//        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
//        // Check for DND permissions for API 24+
//        if (android.os.Build.VERSION.SDK_INT < 24 ||
//                (android.os.Build.VERSION.SDK_INT >= 24 && !nm.isNotificationPolicyAccessGranted())) {
//            AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
//            audioManager.setRingerMode(mode);
//        }
//    }

}
