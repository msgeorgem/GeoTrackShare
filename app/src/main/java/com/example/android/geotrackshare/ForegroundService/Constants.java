package com.example.android.geotrackshare.ForegroundService;

/**
 * Created by Marcin on 2018-02-04.
 */

public class Constants {


    public interface ACTION {
        String MAIN_ACTION = "com.example.android.geotrackshare.action.main";
        String PLAY_ACTION = "com.example.android.geotrackshare.action.start";
        String PAUSE_ACTION = "com.example.android.geotrackshare.action.pause";
        String STOP_ACTION = "com.example.android.geotrackshare.action.stop";
        String STARTFOREGROUND_ACTION = "com.example.android.geotrackshare.action.startforeground";
        String STOPFOREGROUND_ACTION = "com.example.android.geotrackshare.action.stopforeground";
    }

    public interface NOTIFICATION_ID {
        int FOREGROUND_SERVICE = 101;
    }
}
