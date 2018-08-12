package com.example.android.geotrackshare;

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
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
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
import static com.example.android.geotrackshare.DetailActivity.ACTION_FROM_RUNLISTFRAGMENT;
import static com.example.android.geotrackshare.DetailFragment.ACTION_FROM_DETAILFRAGMENT;
import static com.example.android.geotrackshare.RealTimeFragment.RUN_TYPE_PICTURE;
import static com.example.android.geotrackshare.RealTimeFragment.mCategories;
import static com.example.android.geotrackshare.TrackList.RunListFragment.EXTRA_AVG_SPEED;
import static com.example.android.geotrackshare.TrackList.RunListFragment.EXTRA_RUNTYPE;
import static com.example.android.geotrackshare.TrackList.RunListFragment.EXTRA_RUN_ID;
import static com.example.android.geotrackshare.TrackList.RunListFragment.EXTRA_TOTAL_DISTANCE;
import static com.example.android.geotrackshare.TrackList.RunListFragment.EXTRA_TOTAL_TIME;

public class ScreenShotActivity extends AppCompatActivity implements OnMapReadyCallback {
    public static final String ARG_BITMAP = "ARG_BITMAP";
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



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
            runIdInt = intent.getIntExtra(EXTRA_RUN_ID, 0);

        } else if (ACTION_FROM_DETAILFRAGMENT.equals(intent.getAction())) {

        }

        runIdInt = intent.getIntExtra(EXTRA_RUN_ID, 0);
        mTime = intent.getStringExtra(EXTRA_TOTAL_TIME);
        mDistance = intent.getDoubleExtra(EXTRA_TOTAL_DISTANCE, 0);
        mAvgSpeed = intent.getDoubleExtra(EXTRA_AVG_SPEED, 0);
        mRunType = intent.getIntExtra(EXTRA_RUNTYPE, 0);

        String totlaDistance3Dec = String.format("%.3f", mDistance);
        String totalDistanceString = String.valueOf(totlaDistance3Dec + " km");

//        String maxAltitudeNoDecimal = String.format("%.0f", maxAltitude);
//        String maxAltitudeString = String.valueOf(maxAltitudeNoDecimal + " m");

//        String maxSpeed1Decimal = String.format("%.1f", maxSpeed);
//        String maxSpeedString = String.valueOf(maxSpeed1Decimal + " km/h");

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
        transaction.add(R.id.mapmap, mapFragmentInside, "FRAGMENT_TAG")
                .addToBackStack("FRAGMENT_TAG").commit();
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
                        Toast.makeText(getApplication(), getResources().getString(R.string.map_is_loaded),
                                Toast.LENGTH_SHORT).show();
//                        mMapBitmap = bitmap;

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
//        try {
//            Thread.sleep(1000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
        mapScreenShottedTemp.setImageBitmap(mMapBitmap);
        fabShare.setVisibility(View.INVISIBLE);
        mMapFrame.setVisibility(View.INVISIBLE);
        mapScreenShottedTemp.setVisibility(View.VISIBLE);
//        runIdTextView.setText("  G-Track");
//        dateTextView.setText("ScreenShot");

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
//            shareScreenshot(file);//finally share screenshot
//            showScreenShotToAccept(sharedFile);
            shareScreenshot(sharedFile);

        } else
            //If bitmap is null show toast message
            Toast.makeText(getApplication(), R.string.screenshot_take_failed, Toast.LENGTH_SHORT).show();

    }

    /*  Show screenshot Bitmap */
    private void showScreenShotImage(Bitmap bitmap) {
//        mScreenShotPreview.setImageBitmap(bitmap);

    }

    public void showScreenShotToAccept(final File sharedFile) {

        ImageView image = null;
        Uri uri = FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID + ".provider", sharedFile);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            View dialogView = getLayoutInflater().inflate(R.layout.dialog_box, null);
//            View dialogView = getLayoutInflater().inflate(R.layout.dialog_box, null, false);
            image = dialogView.findViewById(R.id.image1);
            Bitmap tempSmallerBitmap = getResizedBitmap(mSharredBitmap, 400, 800);

            image.setImageBitmap(tempSmallerBitmap);
//            Picasso.with(getActivityCast()).load(uri).resize(600, 1200).into(image);
        }
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        if (image.getParent() != null)
            ((ViewGroup) image.getParent()).removeView(image);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.question_to_accept);
        builder.setView(image);
        builder.setPositiveButton(R.string.accept, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the item.
                //remove from DB
                shareScreenshot(sharedFile);
                fabShare.startAnimation(animationDisapear);
                fabShare.setVisibility(View.INVISIBLE);
                fabScreenShot.startAnimation(animationApear);
                fabScreenShot.setVisibility(View.VISIBLE);

            }
        });
        builder.setNegativeButton(R.string.dismiss, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                if (dialog != null) {
                    dialog.dismiss();
                }
                fabShare.startAnimation(animationDisapear);
                fabShare.setVisibility(View.INVISIBLE);
                fabScreenShot.startAnimation(animationApear);
                fabScreenShot.setVisibility(View.VISIBLE);
            }
        });
        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
        alertDialog.setCanceledOnTouchOutside(false);

    }

    /*  Share Screenshot  */
    private void shareScreenshot(File fileImagePath) {
//        Uri uri = Uri.fromFile(file);//Convert file path into Uri for sharing
        Uri uri = FileProvider.getUriForFile(getApplication(), BuildConfig.APPLICATION_ID + ".provider", fileImagePath);
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

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(getApplication(), MainActivity.class);
        startActivity(intent);
    }
}
