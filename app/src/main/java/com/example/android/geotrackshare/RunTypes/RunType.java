package com.example.android.geotrackshare.RunTypes;

/**
 * Created by Marcin on 2017-05-05.
 */

public class RunType {

    private static final int NO_IMAGE_PROVIDED = -1;
    private int mPicture = NO_IMAGE_PROVIDED;
    private int mTitle;
    private int mDescription;


    public RunType(int picture, int title, int description) {
        mPicture = picture;
        mTitle = title;
        mDescription = description;
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
