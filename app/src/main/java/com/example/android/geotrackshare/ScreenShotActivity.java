package com.example.android.geotrackshare;

import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

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
import static com.example.android.geotrackshare.DetailActivity.ACTION_FROM_RUNLISTFRAGMENT;
import static com.example.android.geotrackshare.DetailFragment.ACTION_FROM_DETAILFRAGMENT;
import static com.example.android.geotrackshare.RealTimeFragment.RUN_TYPE_PICTURE;
import static com.example.android.geotrackshare.RealTimeFragment.mCategories;
import static com.example.android.geotrackshare.TrackList.RunListFragment.EXTRA_AVG_SPEED;
import static com.example.android.geotrackshare.TrackList.RunListFragment.EXTRA_RUNTYPE;
import static com.example.android.geotrackshare.TrackList.RunListFragment.EXTRA_RUN_ID;
import static com.example.android.geotrackshare.TrackList.RunListFragment.EXTRA_TOTAL_DISTANCE;
import static com.example.android.geotrackshare.TrackList.RunListFragment.EXTRA_TOTAL_TIME;
import static com.example.android.geotrackshare.TrackList.RunListFragment.PROJECTION_POST;

public class ScreenShotActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final String TAG = "MapFragmentII";
    private static final String[] PROJECTION02 = {
            _ID,
            COLUMN_RUN_ID,
            COLUMN_LATITUDE,
            COLUMN_LONGITUDE
    };
    private static GoogleMap mMap;
    public FloatingActionButton fabShare, fabScreenShot;
    private Bitmap mMapBitmap, mSharredBitmap;
    private Cursor cur;
    private int runIdInt, mRunType;
    private ArrayList<LatLng> coordinatesList;
    private ImageView mapScreenShottedTemp, mRunTypeIcon;
    private TextView mTimeTextView, mDistanceTextView, mAvgSpeedTextView;
    private FrameLayout mMapFrame;
    private MapFragment mapFragmentInside;
    private double[] mStartLocation = new double[2];
    private double[] mStopLocation = new double[2];
    private FrameLayout mScreenShotted;
    private Intent intent;
    private Animation animationApear, animationDisapear;
    private String mTime;
    private Double mDistance, mAvgSpeed;

    private long mStartId;
    private boolean mComesFromDetailFragment = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Remove title bar
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);

        //Remove notification bar
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_screen_shot);

        mMapFrame = findViewById(R.id.mapmap);
        mapScreenShottedTemp = findViewById(R.id.mapImageView);
        mapScreenShottedTemp.setVisibility(View.INVISIBLE);
        mapFragmentInside = MapFragment.newInstance();
        mScreenShotted = findViewById(R.id.screenShotted);
        mTimeTextView = findViewById(R.id.total_time);
        mDistanceTextView = findViewById(R.id.total_distance);
        mAvgSpeedTextView = findViewById(R.id.speed);
        mRunTypeIcon = findViewById(R.id.run_type_icon);

        intent = getIntent();
        if (ACTION_FROM_RUNLISTFRAGMENT.equals(intent.getAction())) {
            mComesFromDetailFragment = false;
            runIdInt = intent.getIntExtra(EXTRA_RUN_ID, 0);
            onGetDataFromDataBaseAndDisplay(runIdInt);

        } else if (ACTION_FROM_DETAILFRAGMENT.equals(intent.getAction())) {
            mComesFromDetailFragment = true;
            runIdInt = intent.getIntExtra(EXTRA_RUN_ID, 0);
            mTime = intent.getStringExtra(EXTRA_TOTAL_TIME);
            mDistance = intent.getDoubleExtra(EXTRA_TOTAL_DISTANCE, 0);
            mAvgSpeed = intent.getDoubleExtra(EXTRA_AVG_SPEED, 0);
            mRunType = intent.getIntExtra(EXTRA_RUNTYPE, 0);
        } else

        mStartId = runIdInt;


        String totlaDistance3Dec = String.format("%.3f", mDistance);
        String totalDistanceString = String.valueOf(totlaDistance3Dec + " km");

        String avrSpeed1Decimal = String.format("%.1f", mAvgSpeed);
        String avrSpeedString = String.valueOf(avrSpeed1Decimal + " km/h");

        RunTypesAdapterNoUI mAdapter = new RunTypesAdapterNoUI(this, mCategories);
        RUN_TYPE_PICTURE = mAdapter.getItem(mRunType).getPicture();
        Bitmap icon = BitmapFactory.decodeResource(getResources(), RUN_TYPE_PICTURE);

        mTimeTextView.setText(mTime);
        mDistanceTextView.setText(totalDistanceString);
        mAvgSpeedTextView.setText(avrSpeedString);
        mRunTypeIcon.setImageBitmap(icon);

        animationApear = new AlphaAnimation(0.0f, 1.0f);
        animationApear.setDuration(1000);
        animationApear.setStartOffset(2000);

        animationDisapear = new AlphaAnimation(1.0f, 0.0f);
        animationDisapear.setDuration(1000);
        animationDisapear.setStartOffset(0);

        queryCoordinatesList(runIdInt);

        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.add(R.id.mapmap, mapFragmentInside, "FRAGMENT_TAG").commit();
        getFragmentManager().executePendingTransactions();
        mapFragmentInside.getMapAsync(this);

        fabShare = findViewById(R.id.fab);
        fabShare.setVisibility(View.INVISIBLE);
        fabShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                takeScreenshot();
            }
        });

        fabScreenShot = findViewById(R.id.fab2);
        fabScreenShot.setAnimation(animationApear);
        fabScreenShot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                mMapBitmap = screenshotMap();
                fabShare.setVisibility(View.VISIBLE);
                fabShare.startAnimation(animationApear);
                fabScreenShot.startAnimation(animationDisapear);
                fabScreenShot.setVisibility(View.INVISIBLE);
            }
        });

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

    }


    private double[] startLocation(int id) {

        String specificID = String.valueOf(id);
        String mSelectionClause = TrackContract.TrackingEntry.COLUMN_RUN_ID;
        String SELECTION = mSelectionClause + " = '" + specificID + "'";
        String ORDERASC = " " + _ID + " ASC LIMIT 1";

        try {
            cur = getContentResolver()
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
            cur = getContentResolver()
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
            cur = getContentResolver()
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

    public Bitmap screenshotMap() {

        mMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
            @Override
            public void onMapLoaded() {
                mMap.snapshot(new GoogleMap.SnapshotReadyCallback() {
                    @Override
                    public void onSnapshotReady(Bitmap bitmap) {
                        Toast.makeText(getApplication(), "snap taken",
                                Toast.LENGTH_SHORT).show();
                        mMapBitmap = bitmap;
//                        mapScreenShottedTemp.setImageBitmap(mMapBitmap);
                    }
                });
            }
        });
        return mMapBitmap;
    }

    /*  Method which will take screenshot on Basis of Screenshot Type ENUM  */
    private void takeScreenshot() {
        mMapBitmap = screenshotMap();

        mapScreenShottedTemp.setImageBitmap(mMapBitmap);
        fabShare.setVisibility(View.INVISIBLE);
        mMapFrame.setVisibility(View.INVISIBLE);
        mapScreenShottedTemp.setVisibility(View.VISIBLE);

//                Screenshot taken after map view replaced by bitmap
        mSharredBitmap = getScreenShot(mScreenShotted);

//                After screenshot taken bitmapg oes invisible
        mapScreenShottedTemp.setVisibility(View.INVISIBLE);
        mMapFrame.setVisibility(View.VISIBLE);
        fabShare.setVisibility(View.VISIBLE);


        //If bitmap is not null
        if (mSharredBitmap != null) {
            showScreenShotImage(mSharredBitmap);//show bitmap over imageview

            File saveFile = getMainDirectoryName(getApplication());//get the path to save screenshot

            File sharedFile = store(mSharredBitmap, "screenshot" + ".jpg", saveFile);//save the screenshot to selected path
            Uri uri = FileProvider.getUriForFile(getApplication(), BuildConfig.APPLICATION_ID + ".provider", sharedFile);
            Log.e("shareScreenshot", String.valueOf(uri));

            shareScreenshot(sharedFile);

        } else
            //If bitmap is null show toast message
            Toast.makeText(getApplication(), R.string.screenshot_take_failed, Toast.LENGTH_SHORT).show();
    }

    /*  Show screenshot Bitmap */
    private void showScreenShotImage(Bitmap bitmap) {
//        mScreenShotPreview.setImageBitmap(bitmap);

    }
    /*  Share Screenshot  */
    private void shareScreenshot(File fileImagePath) {
        Uri uri = FileProvider.getUriForFile(getApplication(), BuildConfig.APPLICATION_ID + ".provider", fileImagePath);
        Log.e("shareScreenshot", String.valueOf(uri));
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND);
        intent.setType("image/*");
        intent.putExtra(android.content.Intent.EXTRA_SUBJECT, R.string.sharing_title);
        intent.putExtra(android.content.Intent.EXTRA_TEXT, getString(R.string.sharing_text));
        intent.putExtra(Intent.EXTRA_STREAM, uri);//pass uri here
        startActivity(Intent.createChooser(intent, getString(R.string.sharing_title)));
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


    public void onGetDataFromDataBaseAndDisplay(int id) {

        Log.e("onGetDataFromDataB..", String.valueOf(id));
        String specificID = String.valueOf(id);
        String mSelectionClause = TrackContract.TrackingEntry.COLUMN_RUN_IDP;
        String mSelection = mSelectionClause + " = '" + specificID + "'";
        try {
            Cursor cursor = getContentResolver().query(TrackContract.TrackingEntry.CONTENT_URI_POST, PROJECTION_POST, mSelection, null, null);

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

                    runIdInt = cursor.getInt(runColumnIndex);

                    mRunType = cursor.getInt(runTypeColumnIndex);
                    mDistance = cursor.getDouble(totalDistanceColumnIndex);
                    Double maxAltitude = cursor.getDouble(maxAltitudeColumnIndex);
                    Double maxSpeed = cursor.getDouble(maxSpeedColumnIndex);
                    mAvgSpeed = cursor.getDouble(avrSpeedColumnIndex);
                    Long totalTime = cursor.getLong(totalTimeColumnIndex);

                    mTime = String.format("%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(totalTime),
                            TimeUnit.MILLISECONDS.toMinutes(totalTime) % TimeUnit.HOURS.toMinutes(1),
                            TimeUnit.MILLISECONDS.toSeconds(totalTime) % TimeUnit.MINUTES.toSeconds(1));


                } while (cursor.moveToNext());
            }
            if (cursor != null) {
                cursor.close();
            }

        } catch (Exception e) {
            Log.e("Path Error123", e.toString());
        }

    }

}
