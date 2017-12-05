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

        // Read the item attributes from the Cursor for the current item
        final String itemTitle = cursor.getString(runColumnIndex);
        String itemOverview = cursor.getString(timeColumnIndex);

        viewHolder.runIdTextView.setText(itemTitle);
        viewHolder.timeStartTextView.setText(itemOverview);
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
        public TextView timeStartTextView;
        public ToggleButton favToggle;

        public ViewHolder(View view) {
            super(view);
            runIdTextView = view.findViewById(R.id.run_id);
            timeStartTextView = view.findViewById(R.id.start_time);
            favToggle = view.findViewById(R.id.favListToggleButton);
        }
    }
}