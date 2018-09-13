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
    public static final int MSG_STOP_TIMER = 1;
    public static final int MSG_UPDATE_TIMER = 2;
    private static final String TAG = StopWatchHandler.class.getSimpleName();
    public static long mStartTime;
    final int REFRESH_RATE = 100;
    private WeakReference<RealTimeFragment> realTimeFragment;
    private WeakReference<MapFragmentLive> mapFragmentLive;
    StopWatch timer = new StopWatch();
    Handler mStopWatchHandler;
    private volatile HandlerThread stopWatchThread;

//    public StopWatchHandler(Looper looper) {
//    }

    //    public StopWatchHandler(RealTimeFragment fragment) {
//        this.fragment = new WeakReference<>(fragment);
//    }
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

        switch (msg.what) {
            case MSG_START_TIMER:

                LocationUpdatesService.startStopWatch();
                sendEmptyMessage(MSG_UPDATE_TIMER);

                break;
            case MSG_UPDATE_TIMER:
//                Log.i(TAG,"mStopWatchHandler");

//                String elapsedTime = timer.elapsedTimeString0();
                String elapsedTime = LocationUpdatesService.elapsedTime();
//                Log.i(TAG,elapsedTime);
//                mElapsedTime = timer.elapsedTimeString0();

                try {
                    realTimeFragment.get().updateStopWatch(elapsedTime);
                    mapFragmentLive.get().updateStopWatch(elapsedTime);
                } catch (NullPointerException e) {
                    System.out.print("Caught the NullPointerException");
                }

//                mStopWatchHandler.sendEmptyMessageDelayed(MSG_UPDATE_TIMER,REFRESH_RATE); //text view is updated every second,
                sendEmptyMessageDelayed(MSG_UPDATE_TIMER, REFRESH_RATE); //text view is updated every second,
                break;                                  //though the timer is still running
            case MSG_STOP_TIMER:
//                mStopWatchHandler.removeMessages(MSG_UPDATE_TIMER); // no more updates.
                removeMessages(MSG_UPDATE_TIMER); // no more updates.
                realTimeFragment.get().updateStopWatchStop();
//                mapFragmentLive.get().updateStopWatchStop();
//                timer.stop();//stop timer
                LocationUpdatesService.stopStopWatch();
//                        RealTimeFragment.mElapsedTimeTextView.setText(""+ timer.toString1());
                break;

            default:
                break;
        }
    }
}
