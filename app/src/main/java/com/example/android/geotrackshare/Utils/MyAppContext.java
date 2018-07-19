package com.example.android.geotrackshare.Utils;

import android.app.Application;
import android.content.Context;

public class MyAppContext extends Application {

    private static Context context;

    public static Context getAppContext() {
        return MyAppContext.context;
    }

    public void onCreate() {
        super.onCreate();
        MyAppContext.context = getApplicationContext();
    }
}
