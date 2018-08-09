package com.example.android.geotrackshare;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
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
import android.widget.Toast;

import com.example.android.geotrackshare.Data.TrackContract;
import com.example.android.geotrackshare.RunTypes.RunTypesAdapterNoUI;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.io.File;
import java.io.FileOutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.concurrent.TimeUnit;

import butterknife.Unbinder;

import static com.example.android.geotrackshare.Data.TrackContract.TrackingEntry.COLUMN_ALTITUDE;
import static com.example.android.geotrackshare.Data.TrackContract.TrackingEntry.COLUMN_AVR_SPEEDP;
import static com.example.android.geotrackshare.Data.TrackContract.TrackingEntry.COLUMN_LATITUDE;
import static com.example.android.geotrackshare.Data.TrackContract.TrackingEntry.COLUMN_LONGITUDE;
import static com.example.android.geotrackshare.Data.TrackContract.TrackingEntry.COLUMN_MAX_ALTP;
import static com.example.android.geotrackshare.Data.TrackContract.TrackingEntry.COLUMN_MAX_SPEEDP;
import static com.example.android.geotrackshare.Data.TrackContract.TrackingEntry.COLUMN_RUNTYPEP;
import static com.example.android.geotrackshare.Data.TrackContract.TrackingEntry.COLUMN_RUN_ID;
import static com.example.android.geotrackshare.Data.TrackContract.TrackingEntry.COLUMN_RUN_IDP;
import static com.example.android.geotrackshare.Data.TrackContract.TrackingEntry.COLUMN_SPEED;
import static com.example.android.geotrackshare.Data.TrackContract.TrackingEntry.COLUMN_START_TIMEP;
import static com.example.android.geotrackshare.Data.TrackContract.TrackingEntry.COLUMN_STOP_TIMEP;
import static com.example.android.geotrackshare.Data.TrackContract.TrackingEntry.COLUMN_TIME;
import static com.example.android.geotrackshare.Data.TrackContract.TrackingEntry.COLUMN_TIME_COUNTER;
import static com.example.android.geotrackshare.Data.TrackContract.TrackingEntry.COLUMN_TIME_COUNTERP;
import static com.example.android.geotrackshare.Data.TrackContract.TrackingEntry.COLUMN_TOTAL_DISTANCE;
import static com.example.android.geotrackshare.Data.TrackContract.TrackingEntry.COLUMN_TOTAL_DISTANCEP;
import static com.example.android.geotrackshare.Data.TrackContract.TrackingEntry.CONTENT_URI;
import static com.example.android.geotrackshare.Data.TrackContract.TrackingEntry._ID;
import static com.example.android.geotrackshare.RealTimeFragment.RUN_TYPE_PICTURE;
import static com.example.android.geotrackshare.RealTimeFragment.mCategories;
import static com.example.android.geotrackshare.TrackList.RunListFragment.PROJECTION_POST;

/**
 * A fragment representing a single detail screen.
 */
public class DetailFragment extends Fragment implements OnMapReadyCallback {
    public static final String ARG_ITEM_ID = "item_id";
    private static final String TAG = "DetailFragment";


    private static final String[] PROJECTION02 = {
            _ID,
            COLUMN_RUN_ID,
            COLUMN_LATITUDE,
            COLUMN_LONGITUDE
    };
    private static final float PARALLAX_FACTOR = 1.25f;
    //    public static FragmentDetailBinding mDetailBinding;
    View view;
    private String MAPSTATE = "MAPSTATE";
    private Unbinder mUnbinder;
    private Cursor mCursor;
    private int mItemId;
    private View mRootView;
    private int mMutedColor = 0xFF333333;
    private Cursor cur;
    public static FloatingActionButton fab;
    private ArrayList<LatLng> coordinatesList;
    private LatLng here;
    private double[] mStartLocation = new double[2];
    private double[] mStopLocation = new double[2];
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
    private MapView mMapView;
    private MapFragment mapFragment;
    private Boolean mRecuringBoolean;
    private static GoogleMap mMap;
    private Bitmap mMapBitmap;
    private Bitmap mSharedImageBitmap;
    private ScrollView mScreenShotted;
    private String currentRunText;
    private FrameLayout mMapFrame;
    private ImageView mapScreenShottedTemp;
    private int mTopInset;
    private FrameLayout mUpButtonContainer;
    private int mSelectedItemUpButtonFloor = Integer.MAX_VALUE;
    private ImageView mScreenShotPreview;


    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.sss");
    // Use default locale format
    private SimpleDateFormat outputFormat = new SimpleDateFormat();
    // Most time functions can only handle 1902 - 2037
    private GregorianCalendar START_OF_EPOCH = new GregorianCalendar(2, 1, 1);

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
        mRecuringBoolean = false;
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
        mScreenShotPreview = view.findViewById(R.id.screenshotpreview);
        mScreenShotPreview.setVisibility(View.INVISIBLE);

        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
        transaction.add(R.id.mapmap, mapFragment, "FRAGMENT_TAG")
                .addToBackStack("FRAGMENT_TAG").commit();
        getChildFragmentManager().executePendingTransactions();

        mapFragment.getMapAsync(this);

        onGetDataFromDataBaseAndDisplay(runIdInt);
        queryCoordinatesList(runIdInt);

        fab = view.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//                createShareTrackIntent();

                takeScreenshot();
            }
        });

        Animation animation = new AlphaAnimation(0.0f, 1.0f);
        animation.setDuration(3000);
        animation.setStartOffset(5000);
        fab.startAnimation(animation);

        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        Log.e("onSaveInstanceState..", String.valueOf("maps"));
        outState.putBoolean(MAPSTATE, true);

        super.onSaveInstanceState(outState);
//        mapFragment.onSaveInstanceState(outState);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.e("onActivityCreated..", String.valueOf("maps"));
//        mapFragment.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {
            mRecuringBoolean = savedInstanceState.getBoolean(MAPSTATE);
        }


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
                    here = new LatLng(latitude, longitude);
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

            String[] columnnames = cursor.getColumnNames();
//            Log.e("columnnames..", Arrays.toString(columnnames));

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
                    Log.e("RUN LIsT FRAGMENT", String.valueOf(runID));
                    Long startTime = cursor.getLong(startTimeColumnIndex);
                    String mHoursStart = new SimpleDateFormat("HH:mm:ss").format(new Date(startTime));
                    Long stopTime = cursor.getLong(stopTimeColumnIndex);
                    String mHoursStop = new SimpleDateFormat("HH:mm:ss").format(new Date(stopTime));

                    int runType = cursor.getInt(runTypeColumnIndex);
                    Double totalDistance = cursor.getDouble(totalDistanceColumnIndex);
                    Double maxAltitude = cursor.getDouble(maxAltitudeColumnIndex);
                    Double maxSpeed = cursor.getDouble(maxSpeedColumnIndex);
                    Double avrSpeed = cursor.getDouble(avrSpeedColumnIndex);
                    Long totalTime = cursor.getLong(totalTimeColumnIndex);

                    String mTotalTime = String.format("%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(totalTime),
                            TimeUnit.MILLISECONDS.toMinutes(totalTime) % TimeUnit.HOURS.toMinutes(1),
                            TimeUnit.MILLISECONDS.toSeconds(totalTime) % TimeUnit.MINUTES.toSeconds(1));

                    String currentRun = String.valueOf(id);
                    currentRunText = getResources().getString(R.string.Run_no) + currentRun;

                    String totlaDistance3Dec = String.format("%.3f", totalDistance);
                    String totalDistanceString = String.valueOf(totlaDistance3Dec + " km");

                    String maxAltitudeNoDecimal = String.format("%.0f", maxAltitude);
                    String maxAltitudeString = String.valueOf(maxAltitudeNoDecimal + " m");

                    String maxSpeed1Decimal = String.format("%.1f", maxSpeed);
                    String maxSpeedString = String.valueOf(maxSpeed1Decimal + " km/h");

                    String avrSpeed1Decimal = String.format("%.1f", avrSpeed);
                    String avrSpeedString = String.valueOf(avrSpeed1Decimal + " km/h");

                    Log.e("RUN LIsT FRAGMENrunType", String.valueOf(runType));
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
                    RUN_TYPE_PICTURE = mAdapter.getItem(runType).getPicture();
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

//        try {
//            Thread.sleep(2500);
//        } catch (InterruptedException ex) {
//            Thread.currentThread().interrupt();

//        }
        mMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
            @Override
            public void onMapLoaded() {
                mMap.snapshot(new GoogleMap.SnapshotReadyCallback() {
                    @Override
                    public void onSnapshotReady(Bitmap bitmap) {
                        Toast.makeText(getActivityCast(), "onMapReady.setOnMapLoadedCallback,onMapLoaded,onSnapshotReady",
                                Toast.LENGTH_SHORT).show();
                        mMapBitmap = bitmap;
//                        mapScreenShottedTemp.setImageBitmap(mMapBitmap);
                    }
                });

            }
        });

    }


    public void screenshotMap() {
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


            }
        };
        mMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
            @Override
            public void onMapLoaded() {
                mMap.snapshot(callback);
//                mapScreenShottedTemp.setImageBitmap(mMapBitmap);
            }
        });

//        mMap.snapshot(callback);

    }
    /*  Method which will take screenshot on Basis of Screenshot Type ENUM  */
    private void takeScreenshot() {
        Bitmap bitmap = null;
        screenshotMap();

        mapScreenShottedTemp.setImageBitmap(mMapBitmap);
        mMapFrame.setVisibility(View.INVISIBLE);
        mapScreenShottedTemp.setVisibility(View.VISIBLE);
        runIdTextView.setText("  G-Track");
        dateTextView.setText("ScreenShot");

//                Screenshot taken after map view replaced by bitmap
        mSharedImageBitmap = getScreenShot(mScreenShotted);

//                After screenshot taken bitmapg oes invisible
        mapScreenShottedTemp.setVisibility(View.INVISIBLE);
        mMapFrame.setVisibility(View.VISIBLE);


        //If bitmap is not null
        if (mSharedImageBitmap != null) {
            showScreenShotImage(mSharedImageBitmap);//show bitmap over imageview

            File saveFile = getMainDirectoryName(getActivityCast());//get the path to save screenshot

            File sharedFile = store(mSharedImageBitmap, "screenshot" + ".jpg", saveFile);//save the screenshot to selected path
            Uri uri = FileProvider.getUriForFile(getActivityCast(), BuildConfig.APPLICATION_ID + ".provider", sharedFile);
            Log.e("shareScreenshot", String.valueOf(uri));
//            shareScreenshot(file);//finally share screenshot
            showScreenShotToAccept(sharedFile);

        } else
            //If bitmap is null show toast message
            Toast.makeText(getActivityCast(), R.string.screenshot_take_failed, Toast.LENGTH_SHORT).show();

    }

    /*  Show screenshot Bitmap */
    private void showScreenShotImage(Bitmap bitmap) {
//        mScreenShotPreview.setImageBitmap(bitmap);

    }

    /*  Share Screenshot  */
    private void shareScreenshot(File fileImagePath) {
//        Uri uri = Uri.fromFile(file);//Convert file path into Uri for sharing
        Uri uri = FileProvider.getUriForFile(getActivityCast(), BuildConfig.APPLICATION_ID + ".provider", fileImagePath);
        Log.e("shareScreenshot", String.valueOf(uri));
        Intent intent = new Intent();
//        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.setAction(Intent.ACTION_SEND);
        intent.setType("image/*");
        intent.putExtra(android.content.Intent.EXTRA_SUBJECT, R.string.sharing_title);
        intent.putExtra(android.content.Intent.EXTRA_TEXT, getString(R.string.sharing_text));
        intent.putExtra(Intent.EXTRA_STREAM, uri);//pass uri here
        startActivity(Intent.createChooser(intent, getString(R.string.sharing_title)));
    }


    public void showScreenShotToAccept(final File sharedFile) {

        ImageView image = null;
        Uri uri = FileProvider.getUriForFile(getActivityCast(), BuildConfig.APPLICATION_ID + ".provider", sharedFile);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            View dialogView = getLayoutInflater().inflate(R.layout.dialog_box, null);
//            View dialogView = getLayoutInflater().inflate(R.layout.dialog_box, null, false);
            image = dialogView.findViewById(R.id.image1);
            Bitmap tempSmallerBitmap = getResizedBitmap(mSharedImageBitmap, 400, 800);

            image.setImageBitmap(tempSmallerBitmap);
//            Picasso.with(getActivityCast()).load(uri).resize(600, 1200).into(image);
        }
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        if (image.getParent() != null)
            ((ViewGroup) image.getParent()).removeView(image);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(R.string.question_to_accept);
        builder.setView(image);
        builder.setPositiveButton(R.string.accept, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the item.
                //remove from DB
                shareScreenshot(sharedFile);
            }
        });
        builder.setNegativeButton(R.string.dismiss, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                if (dialog != null) {
                    dialog.dismiss();
                }

            }
        });
        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
        alertDialog.setCanceledOnTouchOutside(false);

    }

    public Bitmap getResizedBitmap(Bitmap bm, int newWidth, int newHeight) {
        int width = bm.getWidth();
        int height = bm.getHeight();
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        // CREATE A MATRIX FOR THE MANIPULATION
        Matrix matrix = new Matrix();
        // RESIZE THE BIT MAP
        matrix.postScale(scaleWidth, scaleHeight);

        // "RECREATE" THE NEW BITMAP
        Bitmap resizedBitmap = Bitmap.createBitmap(
                bm, 0, 0, width, height, matrix, false);
        bm.recycle();
        return resizedBitmap;
    }

    /*  Method which will return Bitmap after taking screenshot. We have to pass the view which we want to take screenshot.  */
    public Bitmap getScreenShot(View view) {
        View screenView = view.getRootView();
        screenView.setDrawingCacheEnabled(true);
        Bitmap bitmap = Bitmap.createBitmap(screenView.getDrawingCache());
        screenView.setDrawingCacheEnabled(false);
        return bitmap;
    }


    /*  Create Directory where screenshot will save for sharing screenshot  */
    public File getMainDirectoryName(Context context) {
        //Here we will use getExternalFilesDir and inside that we will make our Demo folder
        //benefit of getExternalFilesDir is that whenever the app uninstalls the images will get deleted automatically.
        File mainDir = new File(
                context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), "Demo");

        //If File is not present create directory
        if (!mainDir.exists()) {
            if (mainDir.mkdir())
                Log.e("Create Directory", "Main Directory Created : " + mainDir);
        }
        return mainDir;
    }

    /*  Store taken screenshot into above created path  */
    public File store(Bitmap bm, String fileName, File saveFilePath) {
        File dir = new File(saveFilePath.getAbsolutePath());
        if (!dir.exists())
            dir.mkdirs();
        File file = new File(saveFilePath.getAbsolutePath(), fileName);
        try {
            FileOutputStream fOut = new FileOutputStream(file);
            bm.compress(Bitmap.CompressFormat.JPEG, 100, fOut);
            fOut.flush();
            fOut.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return file;
    }
}
