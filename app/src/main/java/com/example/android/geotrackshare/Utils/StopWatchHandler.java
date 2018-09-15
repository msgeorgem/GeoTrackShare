package com.example.android.geotrackshare.Utils;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;

import com.example.android.geotrackshare.LocationService.LocationUpdatesService;
import com.example.android.geotrackshare.MapFragmentLive;
import com.example.android.geotrackshare.RealTimeFragment;

import java.lang.ref.WeakReference;

public class StopWatchHandler extends Handler {
    public static final int MSG_START_TIMER = 0;
    public static final int MSG_STOP_TIMER_REAL_TIME = 1;
    public static final int MSG_STOP_TIMER_MAP_LIVE = 11;
    public static final int MSG_UPDATE_TIMER_REAL_TIME = 2;
    public static final int MSG_UPDATE_TIMER_MAP_LIVE = 21;
    private static final String TAG = StopWatchHandler.class.getSimpleName();
    public static long mStartTime;
    final int REFRESH_RATE = 100;
    private WeakReference<RealTimeFragment> realTimeFragment;
    private WeakReference<MapFragmentLive> mapFragmentLive;
    StopWatch timer = new StopWatch();
    Handler mStopWatchHandler;
    private volatile HandlerThread stopWatchThread;


    public StopWatchHandler(RealTimeFragment fragment) {
        this.realTimeFragment = new WeakReference<>(fragment);

    }
    public StopWatchHandler(MapFragmentLive fragment) {
        this.mapFragmentLive = new WeakReference<>(fragment);

    }

    // Define how to handle any incoming messages here
    @Override
    public void handleMessage(Message msg) {
        super.handleMessage(msg);
        String elapsedTime = LocationUpdatesService.elapsedTime();
        switch (msg.what) {
            case MSG_START_TIMER:

                LocationUpdatesService.startStopWatch();
                sendEmptyMessage(MSG_UPDATE_TIMER_REAL_TIME);
                sendEmptyMessage(MSG_UPDATE_TIMER_MAP_LIVE);

                break;
            case MSG_UPDATE_TIMER_REAL_TIME:

                try {
                    realTimeFragment.get().updateStopWatch(elapsedTime);
                } catch (NullPointerException e) {
                    System.out.print("Caught the NullPointerException");
                }
                sendEmptyMessageDelayed(MSG_UPDATE_TIMER_REAL_TIME, REFRESH_RATE); //text view is updated every second,
                break;                                  //though the timer is still running
            case MSG_UPDATE_TIMER_MAP_LIVE:

                try {
                    mapFragmentLive.get().updateStopWatch(elapsedTime);
                } catch (NullPointerException e) {
                    System.out.print("Caught the NullPointerException");
                }
                sendEmptyMessageDelayed(MSG_UPDATE_TIMER_MAP_LIVE, REFRESH_RATE); //text view is updated every second,
                break;

            case MSG_STOP_TIMER_REAL_TIME:

                removeMessages(MSG_UPDATE_TIMER_REAL_TIME); // no more updates.
                realTimeFragment.get().updateStopWatchStop();
                LocationUpdatesService.stopStopWatch();
                break;

            case MSG_STOP_TIMER_MAP_LIVE:

                removeMessages(MSG_UPDATE_TIMER_MAP_LIVE); // no more updates.
                mapFragmentLive.get().updateStopWatchStop();
                LocationUpdatesService.stopStopWatch();
                break;

            default:
                break;
        }
    }
}
