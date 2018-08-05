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
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import static com.example.android.geotrackshare.Data.TrackContract.TrackingEntry.COLUMN_ALTITUDE;
import static com.example.android.geotrackshare.Data.TrackContract.TrackingEntry.COLUMN_AVR_SPEED;
import static com.example.android.geotrackshare.Data.TrackContract.TrackingEntry.COLUMN_DISTANCE;
import static com.example.android.geotrackshare.Data.TrackContract.TrackingEntry.COLUMN_MAX_ALT;
import static com.example.android.geotrackshare.Data.TrackContract.TrackingEntry.COLUMN_MAX_SPEED;
import static com.example.android.geotrackshare.Data.TrackContract.TrackingEntry.COLUMN_MIN_ALT;
import static com.example.android.geotrackshare.Data.TrackContract.TrackingEntry.COLUMN_RUNTYPE;
import static com.example.android.geotrackshare.Data.TrackContract.TrackingEntry.COLUMN_RUN_ID;
import static com.example.android.geotrackshare.Data.TrackContract.TrackingEntry.COLUMN_TIME;
import static com.example.android.geotrackshare.Data.TrackContract.TrackingEntry.COLUMN_TIME_COUNTER;
import static com.example.android.geotrackshare.Data.TrackContract.TrackingEntry.COLUMN_TOTAL_DISTANCE;
import static com.example.android.geotrackshare.Data.TrackContract.TrackingEntry.CONTENT_URI;
import static com.example.android.geotrackshare.Data.TrackContract.TrackingEntry._ID;
import static com.example.android.geotrackshare.RealTimeFragment.RUN_TYPE_PICTURE;
import static com.example.android.geotrackshare.RealTimeFragment.mCategories;
import static com.example.android.geotrackshare.Utils.SqliteExporter.createBackupFileName;


/**
 * Created by Marcin on 2017-10-28.
 */

public class TracksCursorAdapter extends CursorRecyclerAdapter<TracksCursorAdapter.ViewHolder> {

    public static String fileName;
    private RunListFragment fragment = new RunListFragment();
    private long id;
    private String mmElapsedTime, mDate, mHours;
    private int id1;
    private int mQuantity;
    private String ORDER = " DESC LIMIT 1";

    @Override
    public Cursor getCursor() {
        return super.getCursor();
    }

    public TracksCursorAdapter(RunListFragment context, Cursor cursor) {
//        super( c, );
        this.fragment = context;
//        setHasStableIds(true);

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
    public void onBindViewHolderCursor(final ViewHolder viewHolder, Cursor cursor) {

        final Context context = viewHolder.itemView.getContext();

//        cursor = context.getContentResolver()
//                .query(CONTENT_URI, null, null, null, null);
        int numberofcolumns = cursor.getColumnCount();

        cursor.getColumnNames();
        Log.e("numberofcolumns", Arrays.toString(cursor.getColumnNames()));
//        viewHolder.setIsRecyclable(false);

//        if (cursor != null && cursor.moveToFirst()) {
//            while (cursor.moveToNext()) {

                // Find the columns of item attributes that we're interested in
                id = cursor.getLong(cursor.getColumnIndex(_ID));
                id1 = cursor.getInt(cursor.getColumnIndex(COLUMN_RUN_ID));
                int runColumnIndex = cursor.getColumnIndex(COLUMN_RUN_ID);
                int runTypeColumnIndex = cursor.getColumnIndex(COLUMN_RUNTYPE);
                int timeColumnIndex = cursor.getColumnIndex(COLUMN_TIME);
                int avgSpeedColumnIndex = cursor.getColumnIndex(COLUMN_AVR_SPEED);
                int timeCountColumnIndex = cursor.getColumnIndex(COLUMN_TIME_COUNTER);
                int distColumnIndex = cursor.getColumnIndex(COLUMN_TOTAL_DISTANCE);


                int maxAltColumnIndex = cursor.getColumnIndex(COLUMN_MAX_ALT);
                int minAltColumnIndex = cursor.getColumnIndex(COLUMN_MIN_ALT);
                int maxSpeedColumnIndex = cursor.getColumnIndex(COLUMN_MAX_SPEED);
                int altColumnIndex = cursor.getColumnIndex(COLUMN_ALTITUDE);

                // Read the item attributes from the Cursor for the current item

                final String itemTitle = cursor.getString(runColumnIndex);
                long itemTime = cursor.getLong(timeColumnIndex);
                double itemAvgSpeed = cursor.getDouble(avgSpeedColumnIndex);
//        String itemAltitude = cursor.getString(altColumnIndex);
                long itemTimeCount = cursor.getLong(timeCountColumnIndex);
                double itemDistance = cursor.getDouble(distColumnIndex);
//        int runType = cursor.getInt(runTypeColumnIndex);
//        Log.e("Database", String.valueOf(runTypeColumnIndex));
                int runTypeInt = 1;
                Log.e("runTypeInt", String.valueOf(runTypeInt));
                Log.e("runTypeColumnIndex", String.valueOf(runTypeColumnIndex));

//        String itemMaxAlt = cursor.getString(maxAltColumnIndex);
//        String itemMinAlt = cursor.getString(minAltColumnIndex);
//        String itemMaxSpeed = cursor.getString(maxSpeedColumnIndex);
//        mDate = new Date(itemTime).toString();
                RunTypesAdapterNoUI mAdapter = new RunTypesAdapterNoUI(context, mCategories);
                RUN_TYPE_PICTURE = mAdapter.getItem(runTypeInt).getPicture();
                Bitmap icon = BitmapFactory.decodeResource(context.getResources(), RUN_TYPE_PICTURE);


                mDate = DateFormat.getDateInstance(DateFormat.LONG).format(itemTime);
                mHours = new SimpleDateFormat("HH:mm:ss").format(new Date(itemTime));


                mmElapsedTime = String.format("%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(itemTimeCount),
                        TimeUnit.MILLISECONDS.toMinutes(itemTimeCount) % TimeUnit.HOURS.toMinutes(1),
                        TimeUnit.MILLISECONDS.toSeconds(itemTimeCount) % TimeUnit.MINUTES.toSeconds(1));

                viewHolder.runTypeIcon.setImageBitmap(icon);
                viewHolder.runIdTextView.setText(itemTitle);
                viewHolder.dateTextView.setText(mDate);
                viewHolder.hoursTextView.setText(mHours);
                viewHolder.speedTextView.setText(String.format(Locale.ENGLISH, "%s: %.1f",
                        "Avg Speed km/h", itemAvgSpeed));
//        viewHolder.altitude.setText(itemAltitude);
                viewHolder.timeCounter.setText(String.format(Locale.ENGLISH, "%s: %s",
                        "Total Time", mmElapsedTime));
                viewHolder.totalDistance.setText(String.format(Locale.ENGLISH, "%s: %.3f",
                        "Total Dist. km", itemDistance));
//        viewHolder.maxAltitude.setText(itemMaxAlt);
//        viewHolder.minAltitude.setText(itemMinAlt);
//        viewHolder.maxSpeedTextView.setText(itemMaxSpeed);


                viewHolder.itemView.setTag(id1);

                viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        fragment.onItemClick(id);
                    }
                });
//            }
//        }

//        if (cursor != null) {
//            cursor.close();
//        }


        DetailActivity.favPrefs = context.getSharedPreferences("favourites", Context.MODE_PRIVATE);
        Boolean a = DetailActivity.favPrefs.getBoolean("On" + context, true);
        if (a) {
            viewHolder.favToggle.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.trashcan01));
            viewHolder.favToggle.setChecked(true);
        } else {
//            !!!IMPORTANT!!! THESE LINES ARE NOT NECESSARY IN THE FOLLOWING CODE SINCE FAVOURITES ARE ALWAYS trashcan02
//            AND TRUE UNLESS DELETED. IF DELETED THEY DO NOT APPEAR IN THE FAVOURITES TABLE ANYWAY
//            viewHolder.favToggle.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.trashcan02));
//            viewHolder.favToggle.setChecked(false);
        }
        viewHolder.favToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked) {
                    viewHolder.favToggle.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.trashcan01));
                    SharedPreferences.Editor editor = DetailActivity.favPrefs.edit();
                    editor.putBoolean("On" + context, true);
                    editor.apply();
                } else {
                    viewHolder.favToggle.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.trashcan02));
//                    STAR GREY IS ONLY TEMPORARY STATE BETWEEN CLICK AND SHOWDELETECONFIRMATIONDIALOG AND CANCEL BUTTON
//                    SO WE DO NOT SAVE GREY STAR STATE IN THIS CASE
//                    SharedPreferences.Editor editor = DetailActivity.favPrefs.edit();
//                    editor.putBoolean("On"+ context, false);
//                    editor.apply();
                    fragment.showDeleteConfirmationDialogOneItem(viewHolder);
                }
            }
        });
        viewHolder.exportCSV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                long id2 = viewHolder.getItemId();
                fileName = createBackupFileName(id1);
                fragment.shareViaEmail(id1);
            }
        });
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public ImageView runTypeIcon;
        public TextView runIdTextView;
        public TextView dateTextView;
        public TextView hoursTextView;
        public TextView speedTextView;
        public TextView maxSpeedTextView;
        public TextView altitude;
        public TextView minAltitude;
        public TextView maxAltitude;
        public TextView timeCounter;
        public TextView totalDistance;
        public ToggleButton favToggle;
        public ImageView exportCSV;


        public ViewHolder(View view) {
            super(view);
            runTypeIcon = view.findViewById(R.id.run_type_icon);
            runIdTextView = view.findViewById(R.id.run_id);
            dateTextView = view.findViewById(R.id.day);
            hoursTextView = view.findViewById(R.id.hours);
            speedTextView = view.findViewById(R.id.avg_speed);
            minAltitude = view.findViewById(R.id.min_alt);
            maxAltitude = view.findViewById(R.id.max_alt);
            timeCounter = view.findViewById(R.id.current_total_time);
            totalDistance = view.findViewById(R.id.current_total_distance);
            favToggle = view.findViewById(R.id.favListToggleButton);
            exportCSV = view.findViewById(R.id.csv);
        }
    }
}