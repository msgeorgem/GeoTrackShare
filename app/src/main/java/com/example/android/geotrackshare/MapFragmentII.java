package com.example.android.geotrackshare;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.android.geotrackshare.Data.TrackContract;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;

import static com.example.android.geotrackshare.Data.TrackContract.TrackingEntry.COLUMN_ALTITUDE;
import static com.example.android.geotrackshare.Data.TrackContract.TrackingEntry.COLUMN_LATITUDE;
import static com.example.android.geotrackshare.Data.TrackContract.TrackingEntry.COLUMN_LONGITUDE;
import static com.example.android.geotrackshare.Data.TrackContract.TrackingEntry.COLUMN_RUN_ID;
import static com.example.android.geotrackshare.Data.TrackContract.TrackingEntry.COLUMN_SPEED;
import static com.example.android.geotrackshare.Data.TrackContract.TrackingEntry.COLUMN_TIME;
import static com.example.android.geotrackshare.Data.TrackContract.TrackingEntry.COLUMN_TIME_COUNTER;
import static com.example.android.geotrackshare.Data.TrackContract.TrackingEntry.COLUMN_TOTAL_DISTANCE;
import static com.example.android.geotrackshare.Data.TrackContract.TrackingEntry.CONTENT_URI;
import static com.example.android.geotrackshare.Data.TrackContract.TrackingEntry._ID;
import static com.example.android.geotrackshare.DetailFragment.ARG_ITEM_ID;

public class MapFragmentII extends Fragment implements OnMapReadyCallback {
    public static final String ARG_BITMAP = "ARG_BITMAP";
    private static final String TAG = "MapFragmentII";
    private static final String[] PROJECTION02 = {
            _ID,
            COLUMN_RUN_ID,
            COLUMN_LATITUDE,
            COLUMN_LONGITUDE
    };
    private static Bitmap mMapBitmap;
    private static GoogleMap mMap;
    private View view;
    private Cursor cur;
    private int runIdInt;
    private ArrayList<LatLng> coordinatesList;
    private ImageView mapScreenShottedTemp;
    private FrameLayout mMapFrame;
    private MapFragment mapFragmentInside;
    private double[] mStartLocation = new double[2];
    private double[] mStopLocation = new double[2];
    private Bitmap mPassedBitmap;

    public MapFragmentII() {
        // Required empty public constructor
    }

    public static void screenshotMap() {
//        mMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
//            @Override
//            public void onMapLoaded() {
//                mMap.snapshot(new GoogleMap.SnapshotReadyCallback() {
//                    @Override
//                    public void onSnapshotReady(Bitmap bitmap) {
//                        Toast.makeText(getActivityCast(),"screenshotMap.setOnMapLoadedCallback,onMapLoaded,onSnapshotReady",
//                                Toast.LENGTH_SHORT).show();
//                        mMapBitmap = bitmap;
//                        mapScreenShottedTemp.setImageBitmap(mMapBitmap);
//                    }
//                });
//
//            }
//        });
        final GoogleMap.SnapshotReadyCallback callback = new GoogleMap.SnapshotReadyCallback() {

            @Override
            public void onSnapshotReady(Bitmap snapshot) {
                mMapBitmap = snapshot;
                DetailFragment.newInstance1(snapshot);

            }
        };
        mMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
            @Override
            public void onMapLoaded() {
                mMap.snapshot(callback);

            }
        });

//        mapScreenShottedTemp.setImageBitmap(mMapBitmap);
//        mapScreenShottedTemp.setVisibility(View.VISIBLE);
//        mMapFrame.setVisibility(View.INVISIBLE);
//        mMap.snapshot(callback);

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments().containsKey(ARG_ITEM_ID)) {
            runIdInt = getArguments().getInt(ARG_ITEM_ID);
            Log.e(TAG, String.valueOf(runIdInt));
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragmentii_map, container, false);


        mMapFrame = view.findViewById(R.id.mapmap);
        mapScreenShottedTemp = view.findViewById(R.id.mapImageView);
        mapScreenShottedTemp.setVisibility(View.INVISIBLE);
        mapFragmentInside = MapFragment.newInstance();
        queryCoordinatesList(runIdInt);

        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
        transaction.add(R.id.mapmap, mapFragmentInside, "FRAGMENT_TAG")
                .addToBackStack("FRAGMENT_TAG").commit();
        getChildFragmentManager().executePendingTransactions();
        mapFragmentInside.getMapAsync(this);

        return view;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.e(TAG, String.valueOf("onMapReady"));
        Log.e(TAG, String.valueOf("onMapReady" + runIdInt));
        mMap = googleMap;
        double mStartLatitude = startLocation(runIdInt)[0];
        double mStartLongitude = startLocation(runIdInt)[1];
        double mStopLatitude = stopLocation(runIdInt)[0];
        double mStopLongitude = stopLocation(runIdInt)[1];
        Log.e("onMapReady..", String.valueOf(mStartLatitude));
        Log.e("onMapReady..", String.valueOf(mStartLongitude));
        Log.e("onMapReady..", String.valueOf(mStopLatitude));
        Log.e("onMapReady..", String.valueOf(mStopLongitude));

        double mSouthLatitude;
        double mWestLongitude;
        double mNorthLatitude;
        double mEastLongitude;
        if (mStartLatitude > mStopLatitude) {
            mSouthLatitude = mStopLatitude;
            mNorthLatitude = mStartLatitude;
        } else {
            mSouthLatitude = mStartLatitude;
            mNorthLatitude = mStopLatitude;
        }
        if (mStartLongitude > mStopLongitude) {
            mEastLongitude = mStartLongitude;
            mWestLongitude = mStopLongitude;
        } else {
            mEastLongitude = mStopLongitude;
            mWestLongitude = mStartLongitude;
        }

        LatLng mSouthWestPoint = new LatLng(mSouthLatitude, mWestLongitude);
        LatLng mNorthEastPoint = new LatLng(mNorthLatitude, mEastLongitude);

        LatLng mStartPoint = new LatLng(mStartLatitude, mStartLongitude);
        LatLng mStopPoint = new LatLng(mStopLatitude, mStopLongitude);
        LatLng mPoint = new LatLng(0, 0);
        LatLng poznan = new LatLng(52.406374, 16.9251681);
//        LatLngBounds(LatLng southwest, LatLng northeast)
        LatLngBounds TRIP = new LatLngBounds(mSouthWestPoint, mNorthEastPoint);
        LatLngBounds POZNAN = new LatLngBounds(poznan, poznan);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(TRIP.getCenter(), 15));

        // Add a marker in Poznan, Poland, and move the camera.
        // mMap.addMarker(new MarkerOptions().position(here).title("arker in Poznań"));
        // mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(here,15));
        // mMap.clear();  //clears all Markers and Polylines
        // Instantiates a new Polyline object and adds points to define a rectangle
        PolylineOptions options = new PolylineOptions().width(10).color(Color.BLUE).geodesic(true);

        PolylineOptions rectOptions = new PolylineOptions()
                .add(new LatLng(37.35, -122.0))
                .add(new LatLng(37.45, -122.0))  // North of the previous point, but at the same longitude
                .add(new LatLng(37.45, -122.2))  // Same latitude, and 30km to the west
                .add(new LatLng(37.35, -122.2))  // Same longitude, and 16km to the south
                .add(new LatLng(37.35, -122.0)); // Closes the polyline.

        for (int i = 0; i < coordinatesList.size(); i++) {
            mPoint = coordinatesList.get(i);
            options.add(mPoint);
        }

        mMap.addPolyline(options);

        mMap.addMarker(new MarkerOptions().position(mStopPoint).title("You are here")); //add Marker in current position
        mMap.setMinZoomPreference(1.0f);
        mMap.setMaxZoomPreference(20.0f);

        // Zoom in, animating the camera.
        mMap.animateCamera(CameraUpdateFactory.zoomIn());

        // Zoom out to zoom level 10, animating with a duration of 2 seconds.
        mMap.animateCamera(CameraUpdateFactory.zoomTo(14), 2000, null);

        // Construct a CameraPosition focusing on Mountain View and animate the camera to that position.
//        CameraPosition cameraPosition = new CameraPosition.Builder()
//                .target(mStopPoint)               // Sets the center of the map to Mountain View
//                .zoom(15)                   // Sets the zoom
//                .bearing(0)                // Sets the orientation of the camera to east
//                .tilt(30)                   // Sets the tilt of the camera to 30 degrees
//                .build();                   // Creates a CameraPosition from the builder
//        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));


        mMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
            @Override
            public void onMapLoaded() {
                mMap.snapshot(new GoogleMap.SnapshotReadyCallback() {
                    @Override
                    public void onSnapshotReady(Bitmap bitmap) {
                        Toast.makeText(getActivity(), getResources().getString(R.string.map_is_loaded),
                                Toast.LENGTH_SHORT).show();
                        mMapBitmap = bitmap;
                        DetailFragment.newInstance1(bitmap);
//                        mapScreenShottedTemp.setImageBitmap(mMapBitmap);
                    }
                });

            }
        });

    }

    private double[] startLocation(int id) {

        String specificID = String.valueOf(id);
        String mSelectionClause = TrackContract.TrackingEntry.COLUMN_RUN_ID;
        String SELECTION = mSelectionClause + " = '" + specificID + "'";
        String ORDERASC = " " + _ID + " ASC LIMIT 1";

        try {
            cur = getActivity().getContentResolver()
                    .query(CONTENT_URI, PROJECTION02, SELECTION, null, ORDERASC);

            if (cur != null && cur.moveToFirst()) {
                do {
                    mStartLocation[0] = cur.getDouble(cur.getColumnIndex(COLUMN_LATITUDE));
                    mStartLocation[1] = cur.getDouble(cur.getColumnIndex(COLUMN_LONGITUDE));

                    Log.i("Start Location", String.valueOf(mStartLocation[0]) +
                            " , " + String.valueOf(mStartLocation[1]));
                }
                while (cur.moveToNext());
            }

            if (cur != null) {
                cur.close();
            }

        } catch (Exception e) {
            Log.e("Path Error", e.toString());
        }
        return mStartLocation;
    }

    private double[] stopLocation(int id) {

        String specificID = String.valueOf(id);
        String mSelectionClause = TrackContract.TrackingEntry.COLUMN_RUN_ID;
        String SELECTION = mSelectionClause + " = '" + specificID + "'";
        String ORDERDESC = " " + _ID + " DESC LIMIT 1";


        try {
            cur = getActivity().getContentResolver()
                    .query(CONTENT_URI, PROJECTION02, SELECTION, null, ORDERDESC);

            if (cur != null && cur.moveToFirst()) {
                do {
                    mStopLocation[0] = cur.getDouble(cur.getColumnIndex(COLUMN_LATITUDE));
                    mStopLocation[1] = cur.getDouble(cur.getColumnIndex(COLUMN_LONGITUDE));

                    Log.i("Stop Location", String.valueOf(mStopLocation[0]) +
                            " , " + String.valueOf(mStopLocation[1]));
                }
                while (cur.moveToNext());
            }

            if (cur != null) {
                cur.close();
            }

        } catch (Exception e) {
            Log.e("Path Error", e.toString());
        }
        return mStopLocation;
    }

    public ArrayList queryCoordinatesList(int id) {
        String[] PROJECTION = {
                COLUMN_RUN_ID,
                COLUMN_TIME,
                COLUMN_LATITUDE,
                COLUMN_LONGITUDE,
                COLUMN_ALTITUDE,
                COLUMN_SPEED,
                COLUMN_TIME_COUNTER,
                COLUMN_TOTAL_DISTANCE
        };
        String specificID = String.valueOf(id);
        String mSelectionClause = TrackContract.TrackingEntry.COLUMN_RUN_ID;
        String SELECTION = mSelectionClause + " = '" + specificID + "'";
        String ORDER = " " + COLUMN_SPEED + " DESC LIMIT 1";

        try {
            cur = getActivity().getContentResolver()
                    .query(CONTENT_URI, null, SELECTION, null, null);

            coordinatesList = new ArrayList<>();

            if (cur != null && cur.moveToFirst()) {
                while (cur.moveToNext()) {
                    double latitude = cur.getDouble(cur.getColumnIndex(COLUMN_LATITUDE));
                    double longitude = cur.getDouble(cur.getColumnIndex(COLUMN_LONGITUDE));
//                    LatLng latLng = new LatLng(latitude, longitude);
                    LatLng here = new LatLng(latitude, longitude);
                    Log.i("Print Current Location1", String.valueOf(here));
                    coordinatesList.add(here);

                }
            }
            if (cur != null) {
                cur.close();
            }

        } catch (Exception e) {
            Log.e("Path Error", e.toString());
        }
        return coordinatesList;
    }
}
