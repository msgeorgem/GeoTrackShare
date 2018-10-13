package com.example.android.geotrackshare.Utils;

/**
 * Created by Marcin on 2017-12-30.
 */

public class DistanceCalculator {

    /**
     * Use Great Circle distance formula to calculate distance between 2 coordinates in kilometers.
     * https://software.intel.com/en-us/blogs/2012/11/25/calculating-geographic-distances-in-location-aware-apps
     */
    public static double greatCircleInKilometers(double lat1, double long1, double lat2, double long2, char unit) {
        double theta = long1 - long2;

        double dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2)) + Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) * Math.cos(deg2rad(theta));

        dist = Math.acos(dist);

        dist = rad2deg(dist);

        dist = dist * 60 * 1.1515;

        if (unit == 'K') {

            dist = dist * 1.609344;

        } else if (unit == 'N') {

            dist = dist * 0.8684;

        }

        return (dist);
    }


    /*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/

    /*::  This function converts decimal degrees to radians             :*/

    /*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/

    private static double deg2rad(double deg) {

        return (deg * Math.PI / 180.0);

    }

    /*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/

    /*::  This function converts radians to decimal degrees             :*/

    /*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/

    private static double rad2deg(double rad) {

        return (rad * 180.0 / Math.PI);

    }


}
