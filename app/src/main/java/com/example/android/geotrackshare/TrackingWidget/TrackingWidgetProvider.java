package com.example.android.geotrackshare.TrackingWidget;

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

import android.annotation.TargetApi;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.widget.RemoteViews;

import com.example.android.geotrackshare.LocationService.LocationUpdatesService;
import com.example.android.geotrackshare.MainActivity;
import com.example.android.geotrackshare.R;
import com.example.android.geotrackshare.Utils.MyAppContext;
import com.example.android.geotrackshare.Utils.StopWatchHandler;

import static com.example.android.geotrackshare.LocationService.LocationServiceConstants.requestingLocationUpdates;
import static com.example.android.geotrackshare.LocationService.LocationServiceConstants.serviceBound;
import static com.example.android.geotrackshare.LocationService.LocationUpdatesService.EXTRA_CURRENT_ID;
import static com.example.android.geotrackshare.MainActivity.mSharedPrefsRunType;
import static com.example.android.geotrackshare.RealTimeFragment.RUN_TYPE_PICTURE_KEY;
import static com.example.android.geotrackshare.Utils.StopWatchHandler.MSG_UPDATE_TIMER_WIDGET;


public class TrackingWidgetProvider extends AppWidgetProvider {

    private static final String TAG = TrackingWidgetProvider.class.getSimpleName();
    // Handler to update the UI every second when the timer is running
    private final Handler mStopWatchHandler = new StopWatchHandler(this);

    public static String ACTION_FROM_SERVICE = "ACTION_FROM_SERVICE";
    String mElapsedTime;

    private RemoteViews mRemoteViews;
    private int mRunId;

    @Override
    public void onReceive(Context context, Intent intent) {

        mRemoteViews = new RemoteViews(context.getPackageName(), R.layout.tracking_widget);
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        ComponentName thisWidget = new ComponentName(context, TrackingWidgetProvider.class);

        if (ACTION_FROM_SERVICE.equals(intent.getAction())) {

            int RUN_TYPE_PICTURE = mSharedPrefsRunType.getInt(RUN_TYPE_PICTURE_KEY, -1);
            Bitmap icon = BitmapFactory.decodeResource(context.getResources(), RUN_TYPE_PICTURE);
            mRemoteViews.setImageViewBitmap(R.id.run_type, icon);

            Double mTotalDistance = intent.getDoubleExtra(LocationUpdatesService.EXTRA_TOTAL_DISTANCE, 0);
            int precision = 1000; //keep 3 digits
            String mTotalDistanceString = String.valueOf((float) (Math.floor(mTotalDistance * precision + .5) / precision));
            mRemoteViews.setTextViewText(R.id.tracking_distance_dynamic, mTotalDistanceString);

            String runNumber = String.valueOf(intent.getIntExtra(EXTRA_CURRENT_ID, 0));
            mRunId = Integer.parseInt(runNumber);
            mRemoteViews.setTextViewText(R.id.run_number, runNumber);

            Long mElapsedTimeMillis = intent.getLongExtra(LocationUpdatesService.EXTRA_TOTAL_TIME, 0);
//            String mElapsedTime = LocationUpdatesService.getDateFromMillis(mElapsedTimeMillis);

//            mRemoteViews.setTextViewText(R.id.tracking_time_dynamic, mElapsedTime);

            Double mMaxAltitudeDouble = intent.getDoubleExtra(LocationUpdatesService.EXTRA_MAX_ALTITUDE, 0);
            String maxAltDecimal = String.format("%.1f", mMaxAltitudeDouble);
            mRemoteViews.setTextViewText(R.id.max_altitude_dynamic, maxAltDecimal);

            Double mAverageSpeedDouble = intent.getDoubleExtra(LocationUpdatesService.EXTRA_AVG_SPEED, 0);
            String avrSpeed1Decimal = String.format("%.1f", mAverageSpeedDouble);
            mRemoteViews.setTextViewText(R.id.avg_speed_dynamic, avrSpeed1Decimal);

            Double mCurrentAltitudeDouble = intent.getDoubleExtra(LocationUpdatesService.EXTRA_ALTITUDE, 0);
            String currentAltDecimal = String.format("%.1f", mCurrentAltitudeDouble);
            mRemoteViews.setTextViewText(R.id.current_altitude_dynamic, currentAltDecimal);

            Double mCurrentSpeedDouble = intent.getDoubleExtra(LocationUpdatesService.EXTRA_SPEED, 0);
            String currentSpeedDecimal = String.format("%.1f", mCurrentSpeedDouble);
            mRemoteViews.setTextViewText(R.id.current_speed_dynamic, currentSpeedDecimal);

        } else {

            String runNumber = context.getString(R.string.no_tracking);
            mRemoteViews.setTextViewText(R.id.run_number, runNumber);

            super.onReceive(context, intent);
        }
        appWidgetManager.updateAppWidget(thisWidget, mRemoteViews);
    }


    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        //Start the intent service update widget action, the service takes care of updating the widgets UI

        if (!requestingLocationUpdates(context)) {
            String runNumber = context.getString(R.string.no_tracking);
            mRemoteViews.setTextViewText(R.id.run_number, runNumber);
            mRemoteViews.setTextViewText(R.id.tracking_time_dynamic, mElapsedTime);
        }

        for (int widgetId : appWidgetIds) {
            mRemoteViews = new RemoteViews(context.getPackageName(), R.layout.tracking_widget);
            Intent intentActivity = new Intent(context, MainActivity.class);

            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intentActivity, 0);
            mRemoteViews.setOnClickPendingIntent(R.id.tracking_widget, pendingIntent);

            appWidgetManager.updateAppWidget(widgetId, mRemoteViews);
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager,
                                          int appWidgetId, Bundle newOptions) {
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions);
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        // Perform any action when one or more AppWidget instances have been deleted
    }

    @Override
    public void onEnabled(Context context) {
        // Perform any action when an AppWidget for this provider is instantiated
    }

    @Override
    public void onDisabled(Context context) {
        // Perform any action when the last AppWidget instance for this provider is deleted
    }

    public void updateStopWatchStop() {
        mStopWatchHandler.removeMessages(MSG_UPDATE_TIMER_WIDGET);
        mRemoteViews.setTextViewText(R.id.tracking_time_dynamic, "Stopped");

    }

    /**
     * Updates the StopWatch readout in the UI; the service must be bound
     */
    public String updateStopWatch(String elapsedTime) {

        if (serviceBound(MyAppContext.getAppContext())) {
            mElapsedTime = elapsedTime;
        }
        return mElapsedTime;
    }

}
