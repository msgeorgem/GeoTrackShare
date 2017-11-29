package com.example.android.geotrackshare.Data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Marcin on 2017-11-28.
 */

public class TrackDbHelper extends SQLiteOpenHelper {


    public static final String DATABASE_NAME = "geotrackshare.db";

    private static final int DATABASE_VERSION = 1;

    public TrackDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        final String SQL_CREATE_TRACKING_TABLE =

                "CREATE TABLE " + TrackContract.TrackingEntry.TABLE_NAME + " (" +
                        TrackContract.TrackingEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        TrackContract.TrackingEntry.COLUMN_RUN_ID + " INTEGER NOT NULL, " +
                        TrackContract.TrackingEntry.COLUMN_TIME + " TEXT NOT NULL, " +
                        TrackContract.TrackingEntry.COLUMN_LATITUDE + " REAL NOT NULL, " +
                        TrackContract.TrackingEntry.COLUMN_LONGITUDE + " REAL NOT NULL, " +
                        TrackContract.TrackingEntry.COLUMN_ALTITUDE + " REAL NOT NULL, " +
                        TrackContract.TrackingEntry.COLUMN_SPEED + " REAL NOT NULL);";

        db.execSQL(SQL_CREATE_TRACKING_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TrackContract.TrackingEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }
}