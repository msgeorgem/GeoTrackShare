package com.example.android.geotrackshare.Data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by Marcin on 2017-11-28.
 */

public class TrackContract {


    public static final String CONTENT_AUTHORITY = "com.example.android.geotrackshare";
    public static final String CONTENT_AUTHORITY_POST = "com.example.android.geotrackshare.post";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    public static final Uri BASE_CONTENT_URI_POST = Uri.parse("content://" + CONTENT_AUTHORITY_POST);
    public static final String PATH_TRACK = "tracking";
    public static final String PATH_TRACK_POST = "post_tracking";

    private TrackContract() {
    }

    public static final class TrackingEntry implements BaseColumns {

        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_TRACK);
        public static final Uri CONTENT_URI_POST = Uri.withAppendedPath(BASE_CONTENT_URI_POST, PATH_TRACK_POST);
        public static final String TABLE_NAME_TRACKING = "tracking";
        public static final String TABLE_NAME_POST_TRACKING = "post_tracking";
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
        public static final String COLUMN_START_TIME = "start_time";
        public static final String COLUMN_STOP_TIME = "stop_time";

        public static final String COLUMN_RUN_IDP = "runp";
        public static final String COLUMN_START_TIMEP = "start_timep";
        public static final String COLUMN_STOP_TIMEP = "stop_timep";
        public static final String COLUMN_RUNTYPEP = "typep";
        public static final String COLUMN_TOTAL_DISTANCEP = "total_distancep";
        public static final String COLUMN_MAX_ALTP = "max_altp";
        public static final String COLUMN_MAX_SPEEDP = "max_speedp";
        public static final String COLUMN_AVR_SPEEDP = "avr_speedp";
        public static final String COLUMN_TIME_COUNTERP = "count_timerp";
        public static final String COLUMN_FAVORITEP = "is_favourite";


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

        public static final String DEFAULT_SORT = COLUMN_RUN_IDP + " DESC";

        /**
         * Read item ID item detail URI.
         */
        public static int getItemId(Uri itemUri) {
            return Integer.parseInt(itemUri.getPathSegments().get(1));
        }

        /**
         * Matches: /items/
         */
        public static Uri buildDirUri() {
            return CONTENT_URI_POST;
//            return BASE_CONTENT_URI_POST.buildUpon().appendPath(PATH_TRACK_POST).build();
        }

        /**
         * Matches: /items/[_id]/
         */
        public static Uri buildItemUri(long _id) {
            return BASE_CONTENT_URI_POST.buildUpon().appendPath(PATH_TRACK_POST).appendPath(Long.toString(_id)).build();
        }
    }
}
