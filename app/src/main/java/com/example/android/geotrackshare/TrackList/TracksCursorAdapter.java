package com.example.android.geotrackshare.TrackList;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.example.android.geotrackshare.DetailActivity;
import com.example.android.geotrackshare.R;
import com.example.android.geotrackshare.RunTypes.RunTypesAdapterNoUI;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import static com.example.android.geotrackshare.Data.TrackContract.TrackingEntry.COLUMN_AVR_SPEEDP;
import static com.example.android.geotrackshare.Data.TrackContract.TrackingEntry.COLUMN_FAVORITEP;
import static com.example.android.geotrackshare.Data.TrackContract.TrackingEntry.COLUMN_MAX_ALTP;
import static com.example.android.geotrackshare.Data.TrackContract.TrackingEntry.COLUMN_MAX_SPEEDP;
import static com.example.android.geotrackshare.Data.TrackContract.TrackingEntry.COLUMN_RUNTYPEP;
import static com.example.android.geotrackshare.Data.TrackContract.TrackingEntry.COLUMN_RUN_IDP;
import static com.example.android.geotrackshare.Data.TrackContract.TrackingEntry.COLUMN_STOP_TIMEP;
import static com.example.android.geotrackshare.Data.TrackContract.TrackingEntry.COLUMN_TIME_COUNTERP;
import static com.example.android.geotrackshare.Data.TrackContract.TrackingEntry.COLUMN_TOTAL_DISTANCEP;
import static com.example.android.geotrackshare.Data.TrackContract.TrackingEntry._ID;
import static com.example.android.geotrackshare.MainActivity.mCategories;
import static com.example.android.geotrackshare.TrackList.RunListFragment.updateFavouritePost;
import static com.example.android.geotrackshare.Utils.SqliteExporter.createBackupFileName;


/**
 * Created by Marcin on 2017-10-28.
 */

public class TracksCursorAdapter extends CursorRecyclerAdapter<TracksCursorAdapter.ViewHolder> {

    public static String fileName;
    private RunListFragment fragment = new RunListFragment();
    private long id, id2long;
    private String mmElapsedTime, mDate, mHours, mRunTypeString;
    private Double totalDistance, avrSpeed;

    private int mRunType;
    private String ORDER = " DESC LIMIT 1";


    public TracksCursorAdapter(RunListFragment context, Cursor cursor) {
        super(context, cursor);
        this.fragment = context;

    }

    public TracksCursorAdapter(Cursor cursor) {
        super(cursor);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_tracks, parent, false);
        ViewHolder vh = new ViewHolder(itemView);
        return vh;

    }


    @Override
    public long getItemId(int position) {
        return position;
    }
//
//    @Override
//    public int getItemViewType(int position) {
//        return position;
//    }

    @Override
    public void onBindViewHolder(final ViewHolder viewHolder, Cursor cursor) {

        final Context context = viewHolder.itemView.getContext();
        final int mFavourite;
        final int mRunId;

//        Log.e("numberofcolumns", Arrays.toString(cursor.getColumnNames()));

        // Find the columns of item attributes that we're interested in
        id = cursor.getLong(cursor.getColumnIndex(_ID));
        mRunId = cursor.getInt(cursor.getColumnIndex(COLUMN_RUN_IDP));
        int runColumnIndex = cursor.getColumnIndex(COLUMN_RUN_IDP);
        int stopTimeColumnIndex = cursor.getColumnIndex(COLUMN_STOP_TIMEP);
        int runTypeColumnIndex = cursor.getColumnIndex(COLUMN_RUNTYPEP);
        int totalDistanceColumnIndex = cursor.getColumnIndex(COLUMN_TOTAL_DISTANCEP);
        int maxAltitudeColumnIndex = cursor.getColumnIndex(COLUMN_MAX_ALTP);
        int maxSpeedColumnIndex = cursor.getColumnIndex(COLUMN_MAX_SPEEDP);
        int avrSpeedColumnIndex = cursor.getColumnIndex(COLUMN_AVR_SPEEDP);
        int totalTimeColumnIndex = cursor.getColumnIndex(COLUMN_TIME_COUNTERP);
        int favouriteColumnIndex = cursor.getColumnIndex(COLUMN_FAVORITEP);

        String runID = cursor.getString(runColumnIndex);

        mRunType = cursor.getInt(runTypeColumnIndex);
        totalDistance = cursor.getDouble(totalDistanceColumnIndex);
        Double maxAltitude = cursor.getDouble(maxAltitudeColumnIndex);
        Double maxSpeed = cursor.getDouble(maxSpeedColumnIndex);
        avrSpeed = cursor.getDouble(avrSpeedColumnIndex);
        Long totalTime = cursor.getLong(totalTimeColumnIndex);
        String mHoursStop = new SimpleDateFormat("HH:mm:ss").format(new Date(totalTime));
        Long stopTime = cursor.getLong(stopTimeColumnIndex);
        String mStopTime = new SimpleDateFormat("HH:mm:ss").format(new Date(stopTime));
        mFavourite = cursor.getInt(favouriteColumnIndex);

        // Read the item attributes from the Cursor for the current item

        RunTypesAdapterNoUI mAdapter = new RunTypesAdapterNoUI(context, mCategories);
        int mRType = mAdapter.getItem(mRunType).getPicture();
        Bitmap icon = BitmapFactory.decodeResource(context.getResources(), mRType);

        mmElapsedTime = String.format("%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(totalTime),
                TimeUnit.MILLISECONDS.toMinutes(totalTime) % TimeUnit.HOURS.toMinutes(1),
                TimeUnit.MILLISECONDS.toSeconds(totalTime) % TimeUnit.MINUTES.toSeconds(1));

        viewHolder.runTypeIcon.setImageBitmap(icon);
        viewHolder.runIdTextView.setText(runID);
        String mDate = formatDate(stopTime);
        viewHolder.dateTextView.setText(mDate);
        viewHolder.stopTextView.setText(mStopTime);
        viewHolder.speedTextView.setText(String.format(Locale.ENGLISH, "%s: %.1f",
                "Avg Speed km/h", avrSpeed));
        viewHolder.timeCounter.setText(String.format(Locale.ENGLISH, "%s: %s",
                "Total Time", mmElapsedTime));
        viewHolder.totalDistance.setText(String.format(Locale.ENGLISH, "%s: %.3f",
                "Total Dist. km", totalDistance));

        viewHolder.itemView.setTag(mRunId);
        Log.e("itemView.setTag", String.valueOf(mRunId));
        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fragment.onItemClick(mRunId);
                Log.e("onclick in holder", String.valueOf(mRunId));
            }
        });

        DetailActivity.favPrefs = context.getSharedPreferences("favourites", Context.MODE_PRIVATE);
        Boolean a = DetailActivity.favPrefs.getBoolean("On" + context, true);


        if (a) {
            viewHolder.trashToggle.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.trashcan01));
            viewHolder.trashToggle.setChecked(true);
        } else {
//            !!!IMPORTANT!!! THESE LINES ARE NOT NECESSARY IN THE FOLLOWING CODE SINCE FAVOURITES ARE ALWAYS trashcan02
//            AND TRUE UNLESS DELETED. IF DELETED THEY DO NOT APPEAR IN THE FAVOURITES TABLE ANYWAY
//            viewHolder.favToggle.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.trashcan02));
//            viewHolder.favToggle.setChecked(false);
        }
        viewHolder.trashToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked) {
                    viewHolder.trashToggle.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.trashcan01));
                    SharedPreferences.Editor editor = DetailActivity.favPrefs.edit();
                    editor.putBoolean("On" + context, true);
                    editor.apply();
                } else {
                    viewHolder.trashToggle.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.trashcan02));

                    fragment.showDeleteConfirmationDialogOneItem(viewHolder);
                }
            }
        });

        viewHolder.exportCSV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fileName = createBackupFileName(mRunId);
                fragment.shareViaEmail(mRunId);
            }
        });
        viewHolder.shareImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fragment.onShareClick(mRunId);
                Log.e("viewHolder.shareImage", String.valueOf(mRunId));
            }
        });

        if (mFavourite == 1) {
            viewHolder.favToggle.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.if_star_black));
            viewHolder.favToggle.setChecked(true);

        } else {
            viewHolder.favToggle.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.if_star_white));
            viewHolder.favToggle.setChecked(false);
        }


        viewHolder.favToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (((ToggleButton) view).isChecked()) {
                    viewHolder.favToggle.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.if_star_black));
                    updateFavouritePost(1, mRunId, context);

                } else {
                    viewHolder.favToggle.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.if_star_white));
                    updateFavouritePost(0, mRunId, context);
                }
            }
        });
    }

    String formatDate(long dateInMillis) {
        Date date = new Date(dateInMillis);
        return DateFormat.getDateInstance(DateFormat.MEDIUM).format(date);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public ImageView runTypeIcon;
        public TextView runIdTextView;
        public TextView dateTextView;
        public TextView stopTextView;
        public TextView speedTextView;
        public TextView minAltitude;
        public TextView maxAltitude;
        public TextView timeCounter;
        public TextView totalDistance;
        public ToggleButton trashToggle, favToggle;
        public ImageView exportCSV;
        public ImageView shareImage;


        public ViewHolder(View view) {
            super(view);
            runTypeIcon = view.findViewById(R.id.run_type_icon);
            runIdTextView = view.findViewById(R.id.run_id);
            dateTextView = view.findViewById(R.id.day);
            stopTextView = view.findViewById(R.id.stop_time);
            speedTextView = view.findViewById(R.id.avg_speed);
            minAltitude = view.findViewById(R.id.min_alt);
            maxAltitude = view.findViewById(R.id.max_alt);
            timeCounter = view.findViewById(R.id.current_total_time);
            totalDistance = view.findViewById(R.id.current_total_distance);
            trashToggle = view.findViewById(R.id.trashToggleButton);
            exportCSV = view.findViewById(R.id.csv);
            shareImage = view.findViewById(R.id.share);
            favToggle = view.findViewById(R.id.favListToggleButton);
        }
    }
}