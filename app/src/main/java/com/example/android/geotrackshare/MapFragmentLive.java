package com.example.android.geotrackshare;


import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.geotrackshare.Data.TrackContract;
import com.example.android.geotrackshare.LocationService.LocationUpdatesService;
import com.example.android.geotrackshare.RunTypes.RunTypesAdapterNoUI;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

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
import static com.example.android.geotrackshare.LocationService.LocationServiceConstants.lastTrackType;
import static com.example.android.geotrackshare.LocationService.LocationServiceConstants.requestingLocationUpdates;
import static com.example.android.geotrackshare.MainActivity.mCategories;
import static com.example.android.geotrackshare.RealTimeFragment.RUN_TYPE_PICTURE;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link MapFragmentLive#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MapFragmentLive extends Fragment implements OnMapReadyCallback {

    private static final String[] PROJECTION02 = {
            _ID,
            COLUMN_RUN_ID,
            COLUMN_LATITUDE,
            COLUMN_LONGITUDE
    };
    private static final String TAG = MapFragmentLive.class.getSimpleName();
    public static Boolean mRequestingLocationUpdates = false;
    public static String ARG_PARAM1 = "ARG_PARAM1";
    public static String ARG_PARAM2 = "ARG_PARAM2";
    public FloatingActionButton fabRecord, fabPause, fabStop;
    View mView;
    private double[] mStartLocation = new double[2];
    private double[] mStopLocation = new double[2];
    private Cursor cur;
    private int runIdInt, mRunType;
    private ArrayList<LatLng> coordinatesList;
    private TextView mTimeTextView, mDistanceTextView;
    private FrameLayout mMapFrame;
    private MapView mMapView;
    private GoogleMap mMap;
    private MapFragment mapFragment;
    private SupportMapFragment mSupportMapFragment;
    private ImageView mapScreenShottedTemp, mRunTypeIcon;
    // The BroadcastReceiver used to listen from broadcasts from the service.
    private MyReceiver myReceiver;
    private Context mContext;
    private int mCurrentType, mCurrentId;
    private LatLng mCurrentLocation;
    private RunTypesAdapterNoUI mAdapter;


    public MapFragmentLive() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment MapFragmentLive.
     */
    // TODO: Rename and change types and number of parameters
    public static MapFragmentLive newInstance() {
        MapFragmentLive fragment = new MapFragmentLive();
//        Bundle args = new Bundle();
//        args.putBoolean(ARG_PARAM1, param1);
//        args.putString(ARG_PARAM2, param2);
//        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mRequestingLocationUpdates = getArguments().getBoolean(ARG_PARAM1);
//            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mView = inflater.inflate(R.layout.fragment_map_live, container, false);
        mContext = getActivity();
        Log.d(TAG, "onCreateView");
        mTimeTextView = mView.findViewById(R.id.total_time);
        mDistanceTextView = mView.findViewById(R.id.total_distance);
        mRunTypeIcon = mView.findViewById(R.id.run_type_icon);
        fabPause = mView.findViewById(R.id.fab_pause);
        fabStop = mView.findViewById(R.id.fab_stop);
        fabRecord = mView.findViewById(R.id.fab_record);
        mCurrentType = lastTrackType(mContext);

        myReceiver = new MyReceiver();
        currentLocation();
        queryCoordinatesList(mCurrentId);

        mRequestingLocationUpdates = requestingLocationUpdates(mContext);
        if (!mRequestingLocationUpdates) {
            fabPause.setVisibility(View.INVISIBLE);
            fabStop.setVisibility(View.INVISIBLE);
            fabRecord.setVisibility(View.VISIBLE);
        } else {
            fabPause.setVisibility(View.VISIBLE);
            fabStop.setVisibility(View.VISIBLE);
            fabRecord.setVisibility(View.INVISIBLE);
        }

        mAdapter = new RunTypesAdapterNoUI(getActivity(), mCategories);
        RUN_TYPE_PICTURE = mAdapter.getItem(mCurrentType).getPicture();
        Bitmap icon = BitmapFactory.decodeResource(getResources(), RUN_TYPE_PICTURE);
        mRunTypeIcon.setImageBitmap(icon);

//        mapFragment = MapFragment.newInstance();
//        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
//        transaction.add(R.id.mapmap, mapFragment, "MAP_TAG").commit();
//        getChildFragmentManager().executePendingTransactions();
//        mapFragment.getMapAsync(this);

        mSupportMapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.mapmap);

        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        mSupportMapFragment = SupportMapFragment.newInstance();
        fragmentTransaction.replace(R.id.mapmap, mSupportMapFragment).commit();

        mSupportMapFragment.getMapAsync(this);

//        mMapView = (MapView) mView.findViewById(R.id.mapmap);
//        mMapView.onCreate(savedInstanceState);
//        mMapView.onResume();// needed to get the map to display immediately
//        try {
//            MapsInitializer.initialize(getActivity().getApplicationContext());
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        mMapView.getMapAsync(this);


        return mView;
    }

    private void currentLocation() {

        LocationManager lm = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

        } else {
            Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            try {
                double mLatitude = location.getLatitude();
                double mLongitude = location.getLongitude();
                mCurrentLocation = new LatLng(mLatitude, mLongitude);
            } catch (NullPointerException e) {
                System.out.print("Caught the NullPointerException");
                Toast.makeText(getActivity(), "No location", Toast.LENGTH_SHORT).show();
            }

        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.e(TAG, String.valueOf("onMapReady"));
        Log.e(TAG, String.valueOf("onMapReady" + mCurrentId));
        mMap = googleMap;

        double mStartLatitude = startLocation(mCurrentId)[0];
        double mStartLongitude = startLocation(mCurrentId)[1];
        double mStopLatitude = stopLocation(mCurrentId)[0];
        double mStopLongitude = stopLocation(mCurrentId)[1];

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

        if (mSouthLatitude == 0 && mWestLongitude == 0) {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mCurrentLocation, 13));
        } else {

            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(TRIP.getCenter(), 13));
        }

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
    }


    /**
     * Disables both buttons when functionality is disabled due to insuffucient location settings.
     * Otherwise ensures that only one button is enabled at any time. The Start Updates button is
     * enabled if the user is not requesting location updates. The Stop Updates button is enabled
     * if the user is requesting location updates.
     */
    public void setButtonsEnabledState(Intent intent) {

        mRequestingLocationUpdates = intent.getBooleanExtra(LocationUpdatesService.EXTRA_REQUESTING_UDPATES, mRequestingLocationUpdates);

        if (!mRequestingLocationUpdates) {
            fabPause.setVisibility(View.INVISIBLE);
            fabStop.setVisibility(View.INVISIBLE);
            fabRecord.setVisibility(View.VISIBLE);
        } else {
            fabPause.setVisibility(View.VISIBLE);
            fabStop.setVisibility(View.VISIBLE);
            fabRecord.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onResume() {

        Log.d(TAG, "onResume");
        LocalBroadcastManager.getInstance(mContext).registerReceiver(myReceiver,
                new IntentFilter(LocationUpdatesService.ACTION_BROADCAST));
//        mMapView.onResume();
        super.onResume();
    }

    @Override
    public void onPause() {

        Log.d(TAG, "onPause()");
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(myReceiver);
//        mMapView.onPause();
        super.onPause();

    }

    @Override
    public void onDestroy() {
//        mMapView.onDestroy();
        super.onDestroy();
    }

    @Override
    public void onLowMemory() {
//        mMapView.onLowMemory();
        super.onLowMemory();
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

    /**
     * Receiver for broadcasts sent by {@link LocationUpdatesService}.
     */
    private class MyReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            setButtonsEnabledState(intent);
            mCurrentId = intent.getIntExtra(LocationUpdatesService.EXTRA_CURRENT_ID, 0);
            mCurrentType = intent.getIntExtra(LocationUpdatesService.EXTRA_RUN_TYPE, 0);


            long elapsedTimeMillis = intent.getLongExtra(LocationUpdatesService.EXTRA_TOTAL_TIME, 0);
            String elapsedTimeString = String.format("%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(elapsedTimeMillis),
                    TimeUnit.MILLISECONDS.toMinutes(elapsedTimeMillis) % TimeUnit.HOURS.toMinutes(1),
                    TimeUnit.MILLISECONDS.toSeconds(elapsedTimeMillis) % TimeUnit.MINUTES.toSeconds(1));
            mTimeTextView.setText(elapsedTimeString);
            double totalDistance = intent.getDoubleExtra(LocationUpdatesService.EXTRA_TOTAL_DISTANCE, 0);
            String totlaDistance3Dec = String.format("%.3f", totalDistance);
            String totalDistanceString = String.valueOf(totlaDistance3Dec + " km");
            mDistanceTextView.setText(totalDistanceString);
        }
    }
}

