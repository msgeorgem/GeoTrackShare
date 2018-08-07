package com.example.android.geotrackshare.Data;

import android.content.Context;
import android.content.CursorLoader;
import android.net.Uri;

/**
 * Helper for loading a list of articles or a single article.
 */
public class TrackLoader extends CursorLoader {
    private TrackLoader(Context context, Uri uri) {
        super(context, uri, Query.PROJECTION, null, null, TrackContract.TrackingEntry.DEFAULT_SORT);
    }

    public static TrackLoader newAllArticlesInstance(Context context) {
        return new TrackLoader(context, TrackContract.TrackingEntry.buildDirUri());
    }

    public static TrackLoader newInstanceForItemId(Context context, long itemId) {
        return new TrackLoader(context, TrackContract.TrackingEntry.buildItemUri(itemId));
    }

    public interface Query {
        String[] PROJECTION = {
                TrackContract.TrackingEntry._ID,
                TrackContract.TrackingEntry.COLUMN_RUN_IDP,
                TrackContract.TrackingEntry.COLUMN_START_TIMEP,
                TrackContract.TrackingEntry.COLUMN_STOP_TIMEP,
                TrackContract.TrackingEntry.COLUMN_RUNTYPEP,
                TrackContract.TrackingEntry.COLUMN_TOTAL_DISTANCEP,
                TrackContract.TrackingEntry.COLUMN_MAX_ALTP,
                TrackContract.TrackingEntry.COLUMN_MAX_SPEEDP,
                TrackContract.TrackingEntry.COLUMN_AVR_SPEEDP,
                TrackContract.TrackingEntry.COLUMN_TIME_COUNTERP,
        };

        int _ID = 0;
        int COLUMN_RUN_IDP = 1;
        int COLUMN_START_TIMEP = 2;
        int COLUMN_STOP_TIMEP = 3;
        int COLUMN_RUNTYPEP = 4;
        int COLUMN_TOTAL_DISTANCEP = 5;
        int COLUMN_MAX_ALTP = 6;
        int COLUMN_MAX_SPEEDP = 7;
        int COLUMN_AVR_SPEEDP = 8;
        int COLUMN_TIME_COUNTERP = 9;
    }
}
