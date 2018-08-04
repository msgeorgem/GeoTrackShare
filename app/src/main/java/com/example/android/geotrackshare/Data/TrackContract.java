package com.example.android.geotrackshare.Data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by Marcin on 2017-11-28.
 */

public class TrackContract {


    public static final String CONTENT_AUTHORITY = "com.example.android.geotrackshare";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    public static final String PATH_TRACK = "tracking";

    private TrackContract() {
    }

    public static final class TrackingEntry implements BaseColumns {


        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_TRACK);
        public static final String TABLE_NAME = "trackingg";
        public static final String COLUMN_RUN_ID = "run";
        public static final String COLUMN_RUNTYPE = "type";
        public static final String COLUMN_TIME = "time";
        public static final String COLUMN_LATITUDE = "latitude";
        public static final String COLUMN_LONGITUDE = "longitude";
        public static final String COLUMN_ALTITUDE = "altitude";
        public static final String COLUMN_MAX_ALT = "max_alt";
        public static final String COLUMN_MIN_ALT = "min_alt";
        public static final String COLUMN_SPEED = "speed";
        public static final String COLUMN_MAX_SPEED = "max_speed";
        public static final String COLUMN_AVR_SPEED = "avr_speed";
        public static final String COLUMN_TIME_COUNTER = "count_timer";
        public static final String COLUMN_DISTANCE = "distance";
        public static final String COLUMN_TOTAL_DISTANCE = "total_distance";
        public static final String COLUMN_MOVE_DISTANCE = "move_distance";
        public static final String COLUMN_MOVE_CLOSE = "move_close";

        /**
         * The MIME type of the {@link #CONTENT_URI} for a list of items.
         */
        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_TRACK;
        /**
         * The MIME type of the {@link #CONTENT_URI} for a single item.
         */
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_TRACK;
        public static String _ID = BaseColumns._ID;

    }
}
