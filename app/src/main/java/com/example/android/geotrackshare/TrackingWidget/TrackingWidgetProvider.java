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
import android.util.Log;
import android.widget.RemoteViews;

import com.example.android.geotrackshare.LocationService.LocationUpdatesService;
import com.example.android.geotrackshare.MainActivity;
import com.example.android.geotrackshare.R;

import static com.example.android.geotrackshare.LocationService.LocationUpdatesService.EXTRA_CURRENT_ID;
import static com.example.android.geotrackshare.RealTimeFragment.RUN_TYPE_PICTURE;
import static com.example.android.geotrackshare.RealTimeFragment.RUN_TYPE_PICTURE_KEY;
import static com.example.android.geotrackshare.RealTimeFragment.mSharedPrefsRunType;


public class TrackingWidgetProvider extends AppWidgetProvider {

    private static final String TAG = TrackingWidgetProvider.class.getSimpleName();
    public static String WIDGET_BUTTON_ENTER_ACTIVITY = "com.example.android.geotrackshare.WIDGET_BUTTON_ENTER_ACTIVITY";
    public static String WIDGET_BUTTON_START_SERVICE = "com.example.android.geotrackshare.WIDGET_BUTTON_START_SERVICE";
    public static String WIDGET_BUTTON_STOP_SERVICE = "com.example.android.geotrackshare.WIDGET_BUTTON_STOP_SERVICE";
    public static String WIDGET_ELAPSED_TIME_TOTAL_DISTANCE = "com.example.android.geotrackshare.WIDGET_ELAPSED_TIME_TOTAL_DISTANCE";
    static Intent appIntent;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (WIDGET_ELAPSED_TIME_TOTAL_DISTANCE.equals(intent.getAction())) {

            RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.tracking_widget);
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            ComponentName thisWidget = new ComponentName(context, TrackingWidgetProvider.class);

            int RUN_TYPE_PICTURE = mSharedPrefsRunType.getInt(RUN_TYPE_PICTURE_KEY, -1);
            Bitmap icon = BitmapFactory.decodeResource(context.getResources(), RUN_TYPE_PICTURE);
            remoteViews.setImageViewBitmap(R.id.run_type, icon);

            Double mTotalDistance = intent.getDoubleExtra(LocationUpdatesService.EXTRA_TOTAL_DISTANCE, 0);
            int precision = 1000; //keep 3 digits
            String mTotalDistanceString = String.valueOf((float) (Math.floor(mTotalDistance * precision + .5) / precision));
            remoteViews.setTextViewText(R.id.tracking_distance_dynamic, mTotalDistanceString);

            String runNumber = String.valueOf(intent.getIntExtra(EXTRA_CURRENT_ID, 0));
            remoteViews.setTextViewText(R.id.run_number, runNumber);

            Long mElapsedTimeMillis = intent.getLongExtra(LocationUpdatesService.EXTRA_TOTAL_TIME, 0);
            String mElapsedTime = LocationUpdatesService.getDateFromMillis(mElapsedTimeMillis);
            remoteViews.setTextViewText(R.id.tracking_time_dynamic, mElapsedTime);

            String mMaxAltitude = String.valueOf(intent.getDoubleExtra(LocationUpdatesService.EXTRA_MAX_ALTITUDE, 0));
            remoteViews.setTextViewText(R.id.max_altitude_dynamic, mMaxAltitude);

            String mAverageSpeed = String.valueOf(intent.getDoubleExtra(LocationUpdatesService.EXTRA_AVG_SPEED, 0));
            remoteViews.setTextViewText(R.id.avg_speed_dynamic, mAverageSpeed);

            String mCurrentAltitude = String.valueOf(intent.getDoubleExtra(LocationUpdatesService.EXTRA_ALTITUDE, 0));
            remoteViews.setTextViewText(R.id.current_altitude_dynamic, mCurrentAltitude);

            String mCurrentSpeed = String.valueOf(intent.getDoubleExtra(LocationUpdatesService.EXTRA_SPEED, 0));
            remoteViews.setTextViewText(R.id.current_speed_dynamic, mCurrentSpeed);

            Log.e(TAG, runNumber + " " + mElapsedTime + " " + mTotalDistanceString);

            appWidgetManager.updateAppWidget(thisWidget, remoteViews);

        } else {
            super.onReceive(context, intent);
        }

    }


    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        //Start the intent service update widget action, the service takes care of updating the widgets UI

        final int count = appWidgetIds.length;

        for (int i = 0; i < count; i++) {
            int widgetId = appWidgetIds[i];

            RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.tracking_widget);
            Intent intentActivity = new Intent(context, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intentActivity, 0);
            remoteViews.setOnClickPendingIntent(R.id.tracking_widget, pendingIntent);

            appWidgetManager.updateAppWidget(widgetId, remoteViews);

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

}
