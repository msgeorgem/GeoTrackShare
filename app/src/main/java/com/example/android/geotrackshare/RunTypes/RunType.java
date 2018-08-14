package com.example.android.geotrackshare.RunTypes;

/**
 * Created by Marcin on 2017-05-05.
 */

public class RunType {

    private static final int NO_IMAGE_PROVIDED = -1;
    private int mPicture = NO_IMAGE_PROVIDED;
    private int mTitle;
    private int mDescription;
    private long mInterval;
    private double mNoise;


    public RunType(int picture, int title, int description, long interval, double noise) {
        mPicture = picture;
        mTitle = title;
        mDescription = description;
        mInterval = interval;
        mNoise = noise;
    }


    public int getPicture() {
        return mPicture;
    }

    public int getTitle() {
        return mTitle;
    }

    public int getDescription() {
        return mDescription;
    }

    public long getIntervalPreset() {
        return mInterval;
    }

    public double getNoisePreset() {
        return mNoise;
    }


    /**
     * Returns whether or not there is an image for this item.
     */
    public boolean hasImage() {
        return mPicture != NO_IMAGE_PROVIDED;
    }

    @Override
    public String toString() {
        return "Item{" +
                "mPicture='" + mPicture + '\'' +
                ", mTitle='" + mTitle + '\'' +
                ", mDescription=" + mDescription +
                '}';
    }
}
