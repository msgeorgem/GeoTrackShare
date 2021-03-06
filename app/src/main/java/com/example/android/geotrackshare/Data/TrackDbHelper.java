package com.example.android.geotrackshare.Data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Marcin on 2017-11-28.
 */

public class TrackDbHelper extends SQLiteOpenHelper {


    public static final String DATABASE_NAME = "geotrackshare.db";

    private static final int DATABASE_VERSION = 12;

    public TrackDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        final String SQL_CREATE_TRACKING_TABLE =

                "CREATE TABLE " + TrackContract.TrackingEntry.TABLE_NAME_TRACKING + " (" +
                        TrackContract.TrackingEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        TrackContract.TrackingEntry.COLUMN_RUN_ID + " INTEGER NOT NULL, " +
                        TrackContract.TrackingEntry.COLUMN_RUNTYPE + " INTEGER NOT NULL, " +
                        TrackContract.TrackingEntry.COLUMN_TIME + " TEXT NOT NULL, " +
                        TrackContract.TrackingEntry.COLUMN_DISTANCE + " REAL NOT NULL, " +
                        TrackContract.TrackingEntry.COLUMN_TOTAL_DISTANCE + " REAL NOT NULL, " +
                        TrackContract.TrackingEntry.COLUMN_LATITUDE + " REAL NOT NULL, " +
                        TrackContract.TrackingEntry.COLUMN_LONGITUDE + " REAL NOT NULL, " +
                        TrackContract.TrackingEntry.COLUMN_ALTITUDE + " REAL NOT NULL, " +
                        TrackContract.TrackingEntry.COLUMN_MAX_ALT + " REAL NOT NULL, " +
                        TrackContract.TrackingEntry.COLUMN_MIN_ALT + " REAL NOT NULL, " +
                        TrackContract.TrackingEntry.COLUMN_SPEED + " REAL NOT NULL, " +
                        TrackContract.TrackingEntry.COLUMN_MAX_SPEED + " REAL NOT NULL, " +
                        TrackContract.TrackingEntry.COLUMN_AVR_SPEED + " REAL NOT NULL, " +
                        TrackContract.TrackingEntry.COLUMN_START_TIME + " REAL NOT NULL, " +
                        TrackContract.TrackingEntry.COLUMN_TIME_COUNTER + " REAL NOT NULL, " +
                        TrackContract.TrackingEntry.COLUMN_MOVE_DISTANCE + " REAL NOT NULL, " +
                        TrackContract.TrackingEntry.COLUMN_MOVE_CLOSE + " REAL NOT NULL);";

        final String SQL_CREATE_POST_TRACKING_TABLE =

                "CREATE TABLE " + TrackContract.TrackingEntry.TABLE_NAME_POST_TRACKING + " (" +
                        TrackContract.TrackingEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        TrackContract.TrackingEntry.COLUMN_RUN_IDP + " INTEGER NOT NULL, " +
                        TrackContract.TrackingEntry.COLUMN_START_TIMEP + " REAL NOT NULL, " +
                        TrackContract.TrackingEntry.COLUMN_STOP_TIMEP + " REAL NOT NULL, " +
                        TrackContract.TrackingEntry.COLUMN_RUNTYPEP + " INTEGER NOT NULL, " +
                        TrackContract.TrackingEntry.COLUMN_TOTAL_DISTANCEP + " REAL NOT NULL, " +
                        TrackContract.TrackingEntry.COLUMN_MAX_ALTP + " REAL NOT NULL, " +
                        TrackContract.TrackingEntry.COLUMN_MAX_SPEEDP + " REAL NOT NULL, " +
                        TrackContract.TrackingEntry.COLUMN_AVR_SPEEDP + " REAL NOT NULL, " +
                        TrackContract.TrackingEntry.COLUMN_TIME_COUNTERP + " REAL NOT NULL, " +
                        TrackContract.TrackingEntry.COLUMN_FAVORITEP + " REAL NOT NULL);";


        db.execSQL(SQL_CREATE_TRACKING_TABLE);
        db.execSQL(SQL_CREATE_POST_TRACKING_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TrackContract.TrackingEntry.TABLE_NAME_TRACKING);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TrackContract.TrackingEntry.TABLE_NAME_POST_TRACKING);
        onCreate(sqLiteDatabase);
    }
}