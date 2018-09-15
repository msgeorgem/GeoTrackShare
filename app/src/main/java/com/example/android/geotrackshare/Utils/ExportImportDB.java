package com.example.android.geotrackshare.Utils;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;

import static com.example.android.geotrackshare.Data.TrackContract.CONTENT_AUTHORITY;
import static com.example.android.geotrackshare.Data.TrackDbHelper.DATABASE_NAME;

public class ExportImportDB extends Activity {
    public static File appDir = null;

    //importing database
    public static void importDB() {

        FileChannel src = null;
        FileChannel dst = null;

        try {
            File sd = Environment.getExternalStorageDirectory();
            File data = Environment.getDataDirectory();

            if (sd.canWrite()) {
                String currentDBPath = "//data//" + CONTENT_AUTHORITY
                        + "//databases//" + DATABASE_NAME;
                String backupDBPath = "/BackupFolder/GeoTrackShare/geotrackshare.db";
                File appDB = new File(data, currentDBPath);
                File backedupDB = new File(sd, backupDBPath);


                try {
                    src = new FileInputStream(backedupDB).getChannel();
                    dst = new FileOutputStream(appDB).getChannel();
                    src.transferTo(0, src.size(), dst);
                    Log.i("ImportDB", backedupDB.toString());
                    Log.i("ImportToAppDB", appDB.toString());

//                    Log.i("ImportDB", backedupDB.toString());
                } finally {
                    try {
                        if (src != null) {
                            src.close();
                        }
                    } finally {
                        if (dst != null) {
                            dst.close();
                        }
                    }
                }
            }
        } catch (Exception e) {

            Log.e("ImportDB", e.toString());

        }
    }

    //exporting database
    public static void exportDB() {

        appDir = new File(Environment.getExternalStorageDirectory() + "/BackupFolder/GeoTrackShare");

        if (!appDir.exists() && !appDir.isDirectory()) {
            // create empty directory
            if (appDir.mkdirs()) {
                Log.i("CreateDir", "App dir created");
            } else {
                Log.w("CreateDir", "Unable to create app dir!");
            }
        } else {
            Log.i("CreateDir", "App dir already exists");
        }


        FileChannel src = null;
        FileChannel dst = null;

        try {
            File sd = Environment.getExternalStorageDirectory();
            File data = Environment.getDataDirectory();


            if (sd.canWrite()) {

                String currentDBPath = "//data//" + CONTENT_AUTHORITY
                        + "//databases//" + DATABASE_NAME;
                Log.i("currentDBPath", data.toString());

                String backupDBPath = "/BackupFolder/Geotrackshare/geotrackshare.db";
                File currentDB = new File(data, currentDBPath);
                File backupDB = new File(sd, backupDBPath);

                try {
                    src = new FileInputStream(currentDB).getChannel();
                    dst = new FileOutputStream(backupDB).getChannel();
                    src.transferTo(0, src.size(), dst);
                    Log.i("ExportToDB", backupDB.toString());
                    Log.i("ExportDB", currentDB.toString());
                } finally {
                    try {
                        if (src != null) {
                            src.close();
                        }
                    } finally {
                        if (dst != null) {
                            dst.close();
                        }
                    }
                }
            }

        } catch (Exception e) {
            Log.e("ExportDB", e.toString());

        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        //creating a new folder for the database to be backuped to
        File direct = new File(Environment.getExternalStorageDirectory() + "/GeoTrackShare");

        if (!direct.exists()) {
            if (direct.mkdir()) {
                //directory is created;
            }

        }
//        exportDB();
//        importDB();

    }

}
