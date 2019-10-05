package com.example.android.geotrackshare;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.example.android.geotrackshare.Data.TrackContract;
import com.example.android.geotrackshare.RunTypes.RunTypesAdapterNoUI;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import static com.example.android.geotrackshare.Data.TrackContract.TrackingEntry.COLUMN_AVR_SPEEDP;
import static com.example.android.geotrackshare.Data.TrackContract.TrackingEntry.COLUMN_LATITUDE;
import static com.example.android.geotrackshare.Data.TrackContract.TrackingEntry.COLUMN_LONGITUDE;
import static com.example.android.geotrackshare.Data.TrackContract.TrackingEntry.COLUMN_MAX_ALTP;
import static com.example.android.geotrackshare.Data.TrackContract.TrackingEntry.COLUMN_MAX_SPEEDP;
import static com.example.android.geotrackshare.Data.TrackContract.TrackingEntry.COLUMN_RUNTYPEP;
import static com.example.android.geotrackshare.Data.TrackContract.TrackingEntry.COLUMN_RUN_ID;
import static com.example.android.geotrackshare.Data.TrackContract.TrackingEntry.COLUMN_RUN_IDP;
import static com.example.android.geotrackshare.Data.TrackContract.TrackingEntry.COLUMN_START_TIMEP;
import static com.example.android.geotrackshare.Data.TrackContract.TrackingEntry.COLUMN_STOP_TIMEP;
import static com.example.android.geotrackshare.Data.TrackContract.TrackingEntry.COLUMN_TIME_COUNTERP;
import static com.example.android.geotrackshare.Data.TrackContract.TrackingEntry.COLUMN_TOTAL_DISTANCEP;
import static com.example.android.geotrackshare.Data.TrackContract.TrackingEntry.CONTENT_URI;
import static com.example.android.geotrackshare.Data.TrackContract.TrackingEntry._ID;
import static com.example.android.geotrackshare.MainActivity.mCategories;
import static com.example.android.geotrackshare.RealTimeFragment.RUN_TYPE_PICTURE;
import static com.example.android.geotrackshare.TrackList.RunListFragment.EXTRA_AVG_SPEED;
import static com.example.android.geotrackshare.TrackList.RunListFragment.EXTRA_RUNTYPE;
import static com.example.android.geotrackshare.TrackList.RunListFragment.EXTRA_RUN_ID;
import static com.example.android.geotrackshare.TrackList.RunListFragment.EXTRA_TOTAL_DISTANCE;
import static com.example.android.geotrackshare.TrackList.RunListFragment.EXTRA_TOTAL_TIME;
import static com.example.android.geotrackshare.TrackList.RunListFragment.PROJECTION_POST;

/**
 * A fragment representing a single detail screen.
 */
public class DetailFragment extends Fragment implements OnMapReadyCallback {
    public static final String ARG_ITEM_ID = "item_id";
    public static final String ACTION_FROM_DETAILFRAGMENT = "ACTION_FROM_DETAILFRAGMENT";
    private static final String TAG = "DetailFragment";

    private static final String[] PROJECTION02 = {
            _ID,
            COLUMN_RUN_ID,
            COLUMN_LATITUDE,
            COLUMN_LONGITUDE
    };
    //    public static FragmentDetailBinding mDetailBinding;
    View view;
    private String MAPSTATE = "MAPSTATE";
    public FloatingActionButton fabShare;
    private Cursor cur;
    public static FloatingActionButton fab;
    private ArrayList<LatLng> coordinatesList;
    private LatLng here;
    private double[] mStartLocation = new double[2];
    private double[] mStopLocation = new double[2];
    private int mRunType;
    private int runIdInt;
    private ImageView mIconView;
    private TextView maxAltTextView;
    private TextView maxSpeedTextView;
    private TextView startTimeTextView;
    private TextView dateTextView;
    private TextView distanceTextView;
    private TextView durationTextView;
    private TextView stopTimeTextView;
    private TextView runIdTextView;
    private TextView avgSpeedTextView;
    private MapFragment mapFragment;
    private static GoogleMap mMap;
    private static Bitmap mMapBitmap;
    private Bitmap mSharedImageBitmap;
    private ScrollView mScreenShotted;
    private double mTotalDistance, mAvgSpeed;
    private FrameLayout mMapFrame;
    private ImageView mapScreenShottedTemp;
    private FrameLayout mUpButtonContainer;
    private String currentRunText, mTotalTime;
    private Animation animationApear, animationDisapear;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public DetailFragment() {
    }

    public static DetailFragment newInstance(int itemId) {
        Bundle arguments = new Bundle();
        arguments.putInt(ARG_ITEM_ID, itemId);
        DetailFragment fragment = new DetailFragment();
        fragment.setArguments(arguments);
        return fragment;
    }


    static String formatDate(long dateInMillis) {
        Date date = new Date(dateInMillis);
        return DateFormat.getDateInstance(DateFormat.MEDIUM).format(date);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments().containsKey(ARG_ITEM_ID)) {
            runIdInt = getArguments().getInt(ARG_ITEM_ID);
            Log.e(TAG, String.valueOf(runIdInt));
        }
    }

    public DetailActivity getActivityCast() {
        return (DetailActivity) getActivity();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_detail, container, false);
        mIconView = view.findViewById(R.id.run_type);
        maxAltTextView = view.findViewById(R.id.max_alt_value);
        maxSpeedTextView = view.findViewById(R.id.max_speed_value);
        startTimeTextView = view.findViewById(R.id.start_time_value);
        dateTextView = view.findViewById(R.id.date_value);
        distanceTextView = view.findViewById(R.id.distance_value);
        durationTextView = view.findViewById(R.id.duration_value);
        stopTimeTextView = view.findViewById(R.id.stop_time_value);
        runIdTextView = view.findViewById(R.id.run_id);
        avgSpeedTextView = view.findViewById(R.id.avg_speed_value);
        mScreenShotted = view.findViewById(R.id.scrollScreenShotted);
        mMapFrame = view.findViewById(R.id.mapmap);
        mapScreenShottedTemp = view.findViewById(R.id.mapImageView);
        mapScreenShottedTemp.setVisibility(View.INVISIBLE);
        mapFragment = MapFragment.newInstance();
        mUpButtonContainer = view.findViewById(R.id.up_container);

        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
        transaction.add(R.id.mapmap, mapFragment, "FRAGMENT_TAG").commit();
        getChildFragmentManager().executePendingTransactions();

        mapFragment.getMapAsync(this);

        onGetDataFromDataBaseAndDisplay(runIdInt);
        queryCoordinatesList(runIdInt);

        animationApear = new AlphaAnimation(0.0f, 1.0f);
        animationApear.setDuration(1500);
        animationApear.setStartOffset(500);

        animationDisapear = new AlphaAnimation(1.0f, 0.0f);
        animationDisapear.setDuration(1000);
        animationDisapear.setStartOffset(0);

        fabShare = view.findViewById(R.id.fab2);
        fabShare.startAnimation(animationApear);
        fabShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onShareClick(runIdInt, mTotalTime, mTotalDistance, mAvgSpeed, mRunType);

            }
        });

        return view;
    }

    public ArrayList queryCoordinatesList(int id) {

        String specificID = String.valueOf(id);
        String mSelectionClause = TrackContract.TrackingEntry.COLUMN_RUN_ID;
        String SELECTION = mSelectionClause + " = '" + specificID + "'";

        try {
            cur = getActivity().getContentResolver()
                    .query(CONTENT_URI, null, SELECTION, null, null);

            coordinatesList = new ArrayList<>();

            if (cur != null && cur.moveToFirst()) {
                while (cur.moveToNext()) {
                    double latitude = cur.getDouble(cur.getColumnIndex(COLUMN_LATITUDE));
                    double longitude = cur.getDouble(cur.getColumnIndex(COLUMN_LONGITUDE));
                    here = new LatLng(latitude, longitude);
//                    Log.i("Print Current Location1", String.valueOf(here));
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

    public void onGetDataFromDataBaseAndDisplay(int id) {

        Log.e("onGetDataFromDataB..", String.valueOf(id));
        String specificID = String.valueOf(id);
        String mSelectionClause = TrackContract.TrackingEntry.COLUMN_RUN_IDP;
        String mSelection = mSelectionClause + " = '" + specificID + "'";
        try {
            Cursor cursor = getActivity().getContentResolver().query(TrackContract.TrackingEntry.CONTENT_URI_POST, PROJECTION_POST, mSelection, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    int runColumnIndex = cursor.getColumnIndex(COLUMN_RUN_IDP);
                    int startTimeColumnIndex = cursor.getColumnIndex(COLUMN_START_TIMEP);
                    int stopTimeColumnIndex = cursor.getColumnIndex(COLUMN_STOP_TIMEP);
                    int runTypeColumnIndex = cursor.getColumnIndex(COLUMN_RUNTYPEP);
                    int totalDistanceColumnIndex = cursor.getColumnIndex(COLUMN_TOTAL_DISTANCEP);
                    int maxAltitudeColumnIndex = cursor.getColumnIndex(COLUMN_MAX_ALTP);
                    int maxSpeedColumnIndex = cursor.getColumnIndex(COLUMN_MAX_SPEEDP);
                    int avrSpeedColumnIndex = cursor.getColumnIndex(COLUMN_AVR_SPEEDP);
                    int totalTimeColumnIndex = cursor.getColumnIndex(COLUMN_TIME_COUNTERP);

                    int runID = cursor.getInt(runColumnIndex);
//                    Log.e("RUN LIsT FRAGMENT", String.valueOf(runID));
                    Long startTime = cursor.getLong(startTimeColumnIndex);
                    String mHoursStart = new SimpleDateFormat("HH:mm:ss").format(new Date(startTime));
                    Long stopTime = cursor.getLong(stopTimeColumnIndex);
                    String mHoursStop = new SimpleDateFormat("HH:mm:ss").format(new Date(stopTime));

                    mRunType = cursor.getInt(runTypeColumnIndex);
                    mTotalDistance = cursor.getDouble(totalDistanceColumnIndex);
                    Double maxAltitude = cursor.getDouble(maxAltitudeColumnIndex);
                    Double maxSpeed = cursor.getDouble(maxSpeedColumnIndex);
                    mAvgSpeed = cursor.getDouble(avrSpeedColumnIndex);
                    Long totalTime = cursor.getLong(totalTimeColumnIndex);

                    mTotalTime = String.format("%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(totalTime),
                            TimeUnit.MILLISECONDS.toMinutes(totalTime) % TimeUnit.HOURS.toMinutes(1),
                            TimeUnit.MILLISECONDS.toSeconds(totalTime) % TimeUnit.MINUTES.toSeconds(1));

                    String currentRun = String.valueOf(id);
                    currentRunText = getResources().getString(R.string.Run_no) + currentRun;

                    String totlaDistance3Dec = String.format("%.3f", mTotalDistance);
                    String totalDistanceString = String.valueOf(totlaDistance3Dec + " km");

                    String maxAltitudeNoDecimal = String.format("%.0f", maxAltitude);
                    String maxAltitudeString = String.valueOf(maxAltitudeNoDecimal + " m");

                    String maxSpeed1Decimal = String.format("%.1f", maxSpeed);
                    String maxSpeedString = String.valueOf(maxSpeed1Decimal + " km/h");

                    String avrSpeed1Decimal = String.format("%.1f", mAvgSpeed);
                    String avrSpeedString = String.valueOf(avrSpeed1Decimal + " km/h");

                    String mDate = formatDate(stopTime);
                    dateTextView.setText(mDate);
                    startTimeTextView.setText(mHoursStart);
                    distanceTextView.setText(totalDistanceString);
                    durationTextView.setText(mTotalTime);
                    stopTimeTextView.setText(mHoursStop);

                    runIdTextView.setText(currentRunText);
                    avgSpeedTextView.setText(avrSpeedString);
                    maxSpeedTextView.setText(maxSpeedString);
                    maxAltTextView.setText(maxAltitudeString);

                    RunTypesAdapterNoUI mAdapter = new RunTypesAdapterNoUI(getActivityCast(), mCategories);
                    RUN_TYPE_PICTURE = mAdapter.getItem(mRunType).getPicture();
                    Log.e("RUN RUN_TYPE_PICTURE", String.valueOf(RUN_TYPE_PICTURE));
                    Bitmap icon = BitmapFactory.decodeResource(getResources(), RUN_TYPE_PICTURE);
                    mIconView.setImageBitmap(icon);

                } while (cursor.moveToNext());
            }
            if (cursor != null) {
                cursor.close();
            }

        } catch (Exception e) {
            Log.e("Path Error123", e.toString());
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        double mStartLatitude = startLocation(runIdInt)[0];
        double mStartLongitude = startLocation(runIdInt)[1];
        double mStopLatitude = stopLocation(runIdInt)[0];
        double mStopLongitude = stopLocation(runIdInt)[1];

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

        mMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
            @Override
            public void onMapLoaded() {
                mMap.snapshot(new GoogleMap.SnapshotReadyCallback() {
                    @Override
                    public void onSnapshotReady(Bitmap bitmap) {
//                        Toast.makeText(getActivityCast(), getResources().getString(R.string.map_is_loaded),
//                                Toast.LENGTH_SHORT).show();
                        mMapBitmap = bitmap;

                    }
                });
            }
        });
    }

    public void onShareClick(int id, String time, Double distance, Double speed, int runType) {
        Intent intent = new Intent(getActivity(), ScreenShotActivity.class);
        intent.setAction(ACTION_FROM_DETAILFRAGMENT);
        intent.putExtra(EXTRA_RUN_ID, id);
        intent.putExtra(EXTRA_TOTAL_TIME, time);
        intent.putExtra(EXTRA_TOTAL_DISTANCE, distance);
        intent.putExtra(EXTRA_AVG_SPEED, speed);
        intent.putExtra(EXTRA_RUNTYPE, runType);
        startActivity(intent);
    }

}
