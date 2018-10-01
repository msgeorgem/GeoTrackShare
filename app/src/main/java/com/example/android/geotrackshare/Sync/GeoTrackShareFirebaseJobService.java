/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.android.geotrackshare.Sync;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.example.android.geotrackshare.Utils.ExportImportDB;
import com.firebase.jobdispatcher.Job;
import com.firebase.jobdispatcher.JobParameters;
import com.firebase.jobdispatcher.JobService;
import com.firebase.jobdispatcher.RetryStrategy;

import static com.example.android.geotrackshare.LocationService.LocationServiceConstants.setAutoExportDone;
import static com.example.android.geotrackshare.LocationService.LocationServiceConstants.setLastAutoExportTime;
import static com.example.android.geotrackshare.Utils.StopWatch.formatDate;


public class GeoTrackShareFirebaseJobService extends JobService {

    public static final String EXTRA_TIME_DATE = "EXTRA_TIME_DATE";
    public static final String ACTION_BROADCAST_TIME = "ACTION_BROADCAST_TIME";
    private static AsyncTask<Void, Void, Void> mBackupTask;
    //    private GoogleApiClient mGoogleApiClient;
    public static final String TAG = GeoTrackShareFirebaseJobService.class.getName();
    private ExportImportDB exportImportDB;

    /**
     * The entry point to your Job. Implementations should offload work to another thread of
     * execution as soon as possible.
     * <p>
     * This is called by the Job Dispatcher to tell us we should start our job. Keep in mind this
     * method is run on the application's main thread, so we need to offload work to a background
     * thread.
     *
     * @return whether there is more work remaining.
     */
    @Override
    public boolean onStartJob(final JobParameters jobParameters) {

        mBackupTask = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {




                String currentDateTimeString = formatDate();
                // Notify anyone listening for broadcasts about the new location.
                Intent intent = new Intent(ACTION_BROADCAST_TIME);
                intent.putExtra(EXTRA_TIME_DATE, currentDateTimeString);
                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);

                setLastAutoExportTime(getApplicationContext(), currentDateTimeString);
                setAutoExportDone(getApplicationContext(), true);

                ExportImportDB.autoExportDB();
                ExportImportDB.uploadToFirebaseStorage();
                Log.i("OnStartJob", "doInBackground");
                jobFinished(jobParameters, false);
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                jobFinished(jobParameters, false);
            }
        };

        mBackupTask.execute();

//        ExportImportDB.autoExportDB();
        Log.i("OnStartJob", "doInBackground");
        jobFinished(jobParameters, false);

        return true;
    }

    /**
     * Called when the scheduling engine has decided to interrupt the execution of a running job,
     * most likely because the runtime constraints associated with the job are no longer satisfied.
     *
     * @return whether the job should be retried
     * @see Job.Builder#setRetryStrategy(RetryStrategy)
     * @see RetryStrategy
     */
    @Override
    public boolean onStopJob(JobParameters jobParameters) {
        if (mBackupTask != null) {
            mBackupTask.cancel(true);
        }
        return true;
    }

}