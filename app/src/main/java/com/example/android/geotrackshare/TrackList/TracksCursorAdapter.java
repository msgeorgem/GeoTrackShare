package com.example.android.geotrackshare.TrackList;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.example.android.geotrackshare.Data.TrackContract;
import com.example.android.geotrackshare.DetailActivity;
import com.example.android.geotrackshare.R;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import static com.example.android.geotrackshare.Utils.SqliteExporter.createBackupFileName;


/**
 * Created by Marcin on 2017-10-28.
 */

public class TracksCursorAdapter extends CursorRecyclerAdapter<TracksCursorAdapter.ViewHolder> {

    public static String fileName;
    private RunListFragment fragment = new RunListFragment();

    public TracksCursorAdapter(RunListFragment context, Cursor c) {
        super(context, c);
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
    public void onBindViewHolder(final ViewHolder viewHolder, Cursor cursor) {

        final Context context = viewHolder.itemView.getContext();
//        viewHolder.setIsRecyclable(false);
        final long id;
        final String mmElapsedTime, mDate, mHours;
        final int id1;
        final int mQuantity;

        // Find the columns of item attributes that we're interested in
        id = cursor.getLong(cursor.getColumnIndex(TrackContract.TrackingEntry._ID));
        id1 = cursor.getInt(cursor.getColumnIndex(TrackContract.TrackingEntry.COLUMN_RUN_ID));
        int runColumnIndex = cursor.getColumnIndex(TrackContract.TrackingEntry.COLUMN_RUN_ID);
        int timeColumnIndex = cursor.getColumnIndex(TrackContract.TrackingEntry.COLUMN_TIME);
        int avgSpeedColumnIndex = cursor.getColumnIndex(TrackContract.TrackingEntry.COLUMN_AVR_SPEED);
        int altColumnIndex = cursor.getColumnIndex(TrackContract.TrackingEntry.COLUMN_ALTITUDE);
        int timeCountColumnIndex = cursor.getColumnIndex(TrackContract.TrackingEntry.COLUMN_TIME_COUNTER);
        int distColumnIndex = cursor.getColumnIndex(TrackContract.TrackingEntry.COLUMN_TOTAL_DISTANCE);

        int maxAltColumnIndex = cursor.getColumnIndex(TrackContract.TrackingEntry.COLUMN_MAX_ALT);
        int minAltColumnIndex = cursor.getColumnIndex(TrackContract.TrackingEntry.COLUMN_MIN_ALT);
        int maxSpeedColumnIndex = cursor.getColumnIndex(TrackContract.TrackingEntry.COLUMN_MAX_SPEED);

        // Read the item attributes from the Cursor for the current item
        final String itemTitle = cursor.getString(runColumnIndex);
        long itemTime = cursor.getLong(timeColumnIndex);
        double itemAvgSpeed = cursor.getDouble(avgSpeedColumnIndex);
//        String itemAltitude = cursor.getString(altColumnIndex);
        long itemTimeCount = cursor.getLong(timeCountColumnIndex);
        double itemDistance = cursor.getDouble(distColumnIndex);

//        String itemMaxAlt = cursor.getString(maxAltColumnIndex);
//        String itemMinAlt = cursor.getString(minAltColumnIndex);
//        String itemMaxSpeed = cursor.getString(maxSpeedColumnIndex);
//        mDate = new Date(itemTime).toString();
        mDate = DateFormat.getDateInstance(DateFormat.LONG).format(itemTime);
        mHours = new SimpleDateFormat("HH:mm:ss").format(new Date(itemTime));


        mmElapsedTime = String.format("%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(itemTimeCount),
                TimeUnit.MILLISECONDS.toMinutes(itemTimeCount) % TimeUnit.HOURS.toMinutes(1),
                TimeUnit.MILLISECONDS.toSeconds(itemTimeCount) % TimeUnit.MINUTES.toSeconds(1));


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