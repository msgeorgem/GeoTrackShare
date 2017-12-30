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
import android.widget.TextView;
import android.widget.ToggleButton;

import com.example.android.geotrackshare.Data.TrackContract;
import com.example.android.geotrackshare.DetailActivity;
import com.example.android.geotrackshare.R;


/**
 * Created by Marcin on 2017-10-28.
 */

public class TracksCursorAdapter extends CursorRecyclerAdapter<TracksCursorAdapter.ViewHolder> {

    private RunListFragment fragment = new RunListFragment();

    public TracksCursorAdapter(RunListFragment context, Cursor c) {
        super(context, c);
        this.fragment = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_tracks, parent, false);
        ViewHolder vh = new ViewHolder(itemView);
        return vh;

    }

    @Override
    public void onBindViewHolder(final ViewHolder viewHolder, Cursor cursor) {

        final long id;
        final int mQuantity;

        // Find the columns of item attributes that we're interested in
        id = cursor.getLong(cursor.getColumnIndex(TrackContract.TrackingEntry._ID));
        int runColumnIndex = cursor.getColumnIndex(TrackContract.TrackingEntry.COLUMN_RUN_ID);
        int timeColumnIndex = cursor.getColumnIndex(TrackContract.TrackingEntry.COLUMN_TIME);
        int speedColumnIndex = cursor.getColumnIndex(TrackContract.TrackingEntry.COLUMN_SPEED);
        int altColumnIndex = cursor.getColumnIndex(TrackContract.TrackingEntry.COLUMN_ALTITUDE);
        int timeCountColumnIndex = cursor.getColumnIndex(TrackContract.TrackingEntry.COLUMN_TIME_COUNTER);
        int distColumnIndex = cursor.getColumnIndex(TrackContract.TrackingEntry.COLUMN_TOTAL_DISTANCE);

        int maxAltColumnIndex = cursor.getColumnIndex(TrackContract.TrackingEntry.COLUMN_MAX_ALT);
        int minAltColumnIndex = cursor.getColumnIndex(TrackContract.TrackingEntry.COLUMN_MIN_ALT);
        int maxSpeedColumnIndex = cursor.getColumnIndex(TrackContract.TrackingEntry.COLUMN_MAX_SPEED);

        // Read the item attributes from the Cursor for the current item
        final String itemTitle = cursor.getString(runColumnIndex);
        String itemTime = cursor.getString(timeColumnIndex);
        String itemSpeed = cursor.getString(speedColumnIndex);
        String itemAltitude = cursor.getString(altColumnIndex);
        String itemTimeCount = cursor.getString(timeCountColumnIndex);
        String itemDistance = String.valueOf(cursor.getDouble(distColumnIndex));

//        String itemMaxAlt = cursor.getString(maxAltColumnIndex);
//        String itemMinAlt = cursor.getString(minAltColumnIndex);
//        String itemMaxSpeed = cursor.getString(maxSpeedColumnIndex);

        viewHolder.runIdTextView.setText(itemTitle);
        viewHolder.timeTextView.setText(itemTime);
        viewHolder.speedTextView.setText(itemSpeed);
        viewHolder.altitude.setText(itemAltitude);
        viewHolder.timeCounter.setText(itemTimeCount);
        viewHolder.totalDistance.setText(itemDistance);

//        viewHolder.maxAltitude.setText(itemMaxAlt);
//        viewHolder.minAltitude.setText(itemMinAlt);
//        viewHolder.maxSpeedTextView.setText(itemMaxSpeed);


        viewHolder.itemView.setTag(id);

        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fragment.onItemClick(id);
            }
        });
        final Context context = viewHolder.itemView.getContext();

        DetailActivity.favPrefs = context.getSharedPreferences("favourites", Context.MODE_PRIVATE);
        Boolean a = DetailActivity.favPrefs.getBoolean("On" + context, true);
        if (a) {
            viewHolder.favToggle.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.star_red));
            viewHolder.favToggle.setChecked(true);
        } else {
//            !!!IMPORTANT!!! THESE LINES ARE NOT NECESSARY IN THE FOLLOWING CODE SINCE FAVOURITES ARE ALWAYS STAR YELLOW
//            AND TRUE UNLESS DELETED. IF DELETED THEY DO NOT APPEAR IN THE FAVOURITES TABLE ANYWAY
//            viewHolder.favToggle.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.star_grey));
//            viewHolder.favToggle.setChecked(false);
        }
        viewHolder.favToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked) {
                    viewHolder.favToggle.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.star_red));
                    SharedPreferences.Editor editor = DetailActivity.favPrefs.edit();
                    editor.putBoolean("On" + context, true);
                    editor.apply();
                } else {
                    viewHolder.favToggle.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.star_star));
//                    STAR GREY IS ONLY TEMPORARY STATE BETWEEN CLICK AND SHOWDELETECONFIRMATIONDIALOG AND CANCEL BUTTON
//                    SO WE DO NOT SAVE GREY STAR STATE IN THIS CASE
//                    SharedPreferences.Editor editor = DetailActivity.favPrefs.edit();
//                    editor.putBoolean("On"+ context, false);
//                    editor.apply();
                    fragment.showDeleteConfirmationDialogOneItem(viewHolder);
                }
            }
        });
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public TextView runIdTextView;
        public TextView timeTextView;
        public TextView speedTextView;
        public TextView maxSpeedTextView;
        public TextView altitude;
        public TextView minAltitude;
        public TextView maxAltitude;
        public TextView timeCounter;
        public TextView totalDistance;



        public ToggleButton favToggle;

        public ViewHolder(View view) {
            super(view);
            runIdTextView = view.findViewById(R.id.run_id);
            timeTextView = view.findViewById(R.id.start_time);
            speedTextView = view.findViewById(R.id.speed);
            maxSpeedTextView = view.findViewById(R.id.max_speed);
            altitude = view.findViewById(R.id.altitude);
            minAltitude = view.findViewById(R.id.min_alt);
            maxAltitude = view.findViewById(R.id.max_alt);
            timeCounter = view.findViewById(R.id.current_total_time);
            totalDistance = view.findViewById(R.id.current_total_distance);
            favToggle = view.findViewById(R.id.favListToggleButton);
        }
    }
}