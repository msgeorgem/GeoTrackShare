package com.example.android.geotrackshare.Utils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

public class StopWatch {
    private long startTime = 0;
    private boolean running = false;
    private long currentTime = 0;

    public long start() {
        this.startTime = System.currentTimeMillis();
        this.running = true;
        return startTime;
    }

    public void stop() {
        this.running = false;
    }

    public void pause() {
        this.running = false;
        currentTime = System.currentTimeMillis() - startTime;
    }

    public void resume() {
        this.running = true;
        this.startTime = System.currentTimeMillis() - currentTime;
    }

    //elaspsed time in milliseconds
    public long getElapsedTimeMili() {
        long elapsed = 0;
        if (running) {
            elapsed = ((System.currentTimeMillis() - startTime) / 100) % 1000;
        }
        return elapsed;
    }

    //elaspsed time in milliseconds
    public long getElapsedTimeMili0() {
        long elapsed = 0;
        if (running) {
            elapsed = (System.currentTimeMillis() - startTime);
        }
        return elapsed;
    }

    //elaspsed time in seconds
    public long getElapsedTimeSecs() {
        long elapsed = 0;
        if (running) {
            elapsed = ((System.currentTimeMillis() - startTime) / 1000) % 60;
        }
        return elapsed;
    }

    //elaspsed time in minutes
    public long getElapsedTimeMin() {
        long elapsed = 0;
        if (running) {
            elapsed = (((System.currentTimeMillis() - startTime) / 1000) / 60) % 60;
        }
        return elapsed;
    }

    //elaspsed time in hours
    public long getElapsedTimeHour() {
        long elapsed = 0;
        if (running) {
            elapsed = ((((System.currentTimeMillis() - startTime) / 1000) / 60) / 60);
        }
        return elapsed;
    }

    public String toString() {
        return getElapsedTimeHour() + ":" + getElapsedTimeMin() + ":"
                + getElapsedTimeSecs() + "." + getElapsedTimeMili();
    }

    public String toString1() {
        return getElapsedTimeHour() + ":" + getElapsedTimeMin() + ":"
                + getElapsedTimeSecs();
    }

    public String elapsedTimeString0() {
        long totalTime = getElapsedTimeMili0();
        return String.format("%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(totalTime),
                TimeUnit.MILLISECONDS.toMinutes(totalTime) % TimeUnit.HOURS.toMinutes(1),
                TimeUnit.MILLISECONDS.toSeconds(totalTime) % TimeUnit.MINUTES.toSeconds(1));

    }

    public static String formatDate() {
        Date currentTime = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("EEE, MMM d, yyyy hh:mm:ss a z");
        SimpleDateFormat sdf1 = new SimpleDateFormat("hh:mm:ss");
        String tz = String.valueOf(TimeZone.getDefault());
        sdf1.setTimeZone(TimeZone.getTimeZone(tz));

        long milis = System.currentTimeMillis();
        String date = DateFormat.getDateInstance(DateFormat.FULL).format(milis);
        String time = DateFormat.getTimeInstance().format(milis);
        return time + " " + date;
//        return sdf1.format(currentTime);
    }
}