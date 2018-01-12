package com.example.android.geotrackshare.Data;

import android.app.Application;
import android.content.Context;
import android.os.Environment;

import com.example.android.geotrackshare.R;

import java.io.File;

/**
 * Created by Marcin on 2018-01-12.
 */

public class FileUtils extends Application {

    private static Context context;


    public static String getAppDir() {

        return context.getExternalFilesDir(null) + "/" + context.getString(R.string.app_name);
    }

    public static File createDirIfNotExist(String path) {
        File dir = new File(path);
        if (!dir.exists()) {
            dir.mkdir();
        }
        return dir;
    }

    /* Checks if external storage is available for read and write */
    public static boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }

    /* Checks if external storage is available to at least read */
    public static boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state);
    }
}