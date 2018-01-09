package com.example.android.geotrackshare.TrackList;

import android.content.ContentUris;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.geotrackshare.Data.TrackContract;
import com.example.android.geotrackshare.DetailActivity;
import com.example.android.geotrackshare.R;

import static com.example.android.geotrackshare.Data.TrackContract.TrackingEntry.COLUMN_ALTITUDE;
import static com.example.android.geotrackshare.Data.TrackContract.TrackingEntry.COLUMN_AVR_SPEED;
import static com.example.android.geotrackshare.Data.TrackContract.TrackingEntry.COLUMN_LATITUDE;
import static com.example.android.geotrackshare.Data.TrackContract.TrackingEntry.COLUMN_LONGITUDE;
import static com.example.android.geotrackshare.Data.TrackContract.TrackingEntry.COLUMN_RUN_ID;
import static com.example.android.geotrackshare.Data.TrackContract.TrackingEntry.COLUMN_SPEED;
import static com.example.android.geotrackshare.Data.TrackContract.TrackingEntry.COLUMN_TIME;
import static com.example.android.geotrackshare.Data.TrackContract.TrackingEntry.COLUMN_TIME_COUNTER;
import static com.example.android.geotrackshare.Data.TrackContract.TrackingEntry.COLUMN_TOTAL_DISTANCE;


/**
 * Created by Marcin on 2017-10-25.
 */

public class RunListFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final String LOG_TAG = RunListFragment.class.getName();
    public static final String EXTRA_RUN_ID = "EXTRA_RUN_ID";
    public static final String EXTRA_TIME = "EXTRA_TIME";
    public static final String EXTRA_LATITUDE = "EXTRA_LATITUDE";
    public static final String EXTRA_LONGITUDE = "EXTRA_LONGITUDE";
    public static final String EXTRA_ALTITUDE = "EXTRA_ALTITUDE";
    public static final String EXTRA_SPEED = "EXTRA_SPEED";

    private static final int FAV_LOADER = 0;

    private static final String[] PROJECTION = {
            TrackContract.TrackingEntry._ID,
            COLUMN_RUN_ID,
            COLUMN_TIME,
            COLUMN_LATITUDE,
            COLUMN_LONGITUDE,
            COLUMN_ALTITUDE,
            COLUMN_SPEED,
            COLUMN_AVR_SPEED,
            COLUMN_TIME_COUNTER,
            COLUMN_TOTAL_DISTANCE
    };


    //   Just a rough idea how to sort in query
    private static final String SORT_ORDER_ID = TrackContract.TrackingEntry._ID + " DESC";
    private static final String BUNDLE_RECYCLER_LAYOUT = "TrackListFragment.tracksRecyclerView";
    public TracksCursorAdapter mTracksAdapter;
    Parcelable state;
    private View mloadingIndicator;
    private TextView mEmptyStateTextView;
    private View view;
    //    private static final String SELECTION = TrackContract.TrackingEntry.getGreaterThanZero();
    private RecyclerView tracksRecyclerView;

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_run_list, container, false);
        Log.i(LOG_TAG, "initLoader");

        // Find a reference to the {@link ListView} in the layout
        tracksRecyclerView = view.findViewById(R.id.list_runs);

        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            tracksRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 1));
        } else {
            tracksRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 2));
        }

        mTracksAdapter = new TracksCursorAdapter(this, null);

        // Set the adapter on the {@link ListView}
        // so the list can be populated in the user interface
        tracksRecyclerView.setAdapter(mTracksAdapter);
//        tracksRecyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL_LIST));
//        tracksRecyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.HORIZONTAL_LIST));
        tracksRecyclerView.setItemAnimator(new DefaultItemAnimator());

        mEmptyStateTextView = view.findViewById(R.id.empty_view_runs);
        mEmptyStateTextView.setText(R.string.no_runs);

        mloadingIndicator = view.findViewById(R.id.loading_indicator_runs);
        //kick off the loader
        getLoaderManager().initLoader(FAV_LOADER, null, this);
        return view;
    }

    public Cursor querY() {
        return getActivity().getContentResolver().query(TrackContract.TrackingEntry.CONTENT_URI, null, null, null, SORT_ORDER_ID);
    }

    void deleteOneItem(int runId) {
        int rowDeleted = getActivity().getContentResolver().delete(TrackContract.TrackingEntry.CONTENT_URI, TrackContract.TrackingEntry.COLUMN_RUN_ID + "=" + runId, null);
        Toast.makeText(getActivity(), rowDeleted + " " + getString(R.string.delete_one_item), Toast.LENGTH_SHORT).show();
//        mFavsAdapter.swapCursor(querY());
    }

    public void showDeleteConfirmationDialogOneItem(final RecyclerView.ViewHolder viewHolder) {
        //Inside, get the viewHolder's itemView's tag and store in a long variable id
        //get the iD of the item being swiped
        final int runId = (int) viewHolder.itemView.getTag();

        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(R.string.delete_oneitem_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the item.
                //remove from DB
                deleteOneItem(runId);
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                if (dialog != null) {
                    dialog.dismiss();
                }
                //call swapCursor on mAdapter passing in null as the argument
                //update the list
                mTracksAdapter.swapCursor(querY());
            }
        });
        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
        alertDialog.setCanceledOnTouchOutside(false);
    }

    @Override
    public void onPause() {
        super.onPause();
        // save RecyclerView state
        state = tracksRecyclerView.getLayoutManager().onSaveInstanceState();
    }

    @Override
    public void onResume() {
        super.onResume();
        // restore RecyclerView state
        if (state != null) {
            tracksRecyclerView.getLayoutManager().onRestoreInstanceState(state);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());

        String COLUMN = sharedPrefs.getString(
                getString(R.string.run_settings_order_by_key),
                getString(R.string.run_settings_order_by_label)

        );

        String SELECTION = COLUMN_RUN_ID + " GROUP BY " + COLUMN_RUN_ID;
        String SORT_ORDER = COLUMN + " DESC ";


        // Perform a query using CursorLoader
        return new CursorLoader(getActivity(),    // Parent activity context
                TrackContract.TrackingEntry.CONTENT_URI, // Provider content URI to query
                PROJECTION,            // The columns to include in the resulting Cursor
                SELECTION,         // The values for the WHERE clause
                null,   // No SELECTION arguments
                SORT_ORDER);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // Callback called when the data needs to be deleted
        mTracksAdapter.swapCursor(null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        // Update {@link ItemCursor Adapter with this new cursor containing updated item data
        if (!data.moveToFirst()) {
            mEmptyStateTextView.setVisibility(View.VISIBLE);
            mloadingIndicator.setVisibility(View.GONE);
        } else {
            mEmptyStateTextView.setVisibility(View.GONE);
            mloadingIndicator.setVisibility(View.GONE);
        }
        mTracksAdapter.swapCursor(data);
    }

    @Override
    public void onViewStateRestored(Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (savedInstanceState != null) {
            Parcelable savedRecyclerLayoutState = savedInstanceState.getParcelable(BUNDLE_RECYCLER_LAYOUT);
            tracksRecyclerView.getLayoutManager().onRestoreInstanceState(savedRecyclerLayoutState);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(BUNDLE_RECYCLER_LAYOUT, tracksRecyclerView.getLayoutManager().onSaveInstanceState());
    }

    public void onItemClick(long id) {
        Intent intent = new Intent(getActivity(), DetailActivity.class);

        String specificID = String.valueOf(id);
        String mSelectionClause = TrackContract.TrackingEntry._ID;
        try {
            Cursor cursor = getActivity().getContentResolver().query(TrackContract.TrackingEntry.CONTENT_URI, PROJECTION, mSelectionClause + " = '" + specificID + "'", null, null);
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    int runColumnIndex = cursor.getColumnIndex(COLUMN_RUN_ID);
                    int timeColumnIndex = cursor.getColumnIndex(COLUMN_TIME);
                    int latitudeColumnIndex = cursor.getColumnIndex(COLUMN_LATITUDE);
                    int londitudeColumnIndex = cursor.getColumnIndex(COLUMN_LONGITUDE);
                    int altitudeColumnIndex = cursor.getColumnIndex(COLUMN_ALTITUDE);
                    int speedColumnIndex = cursor.getColumnIndex(COLUMN_SPEED);

                    String runID = cursor.getString(runColumnIndex);
                    String timeTitle = cursor.getString(timeColumnIndex);
                    String latitude = cursor.getString(latitudeColumnIndex);
                    String longitude = cursor.getString(londitudeColumnIndex);
                    String altitude = cursor.getString(altitudeColumnIndex);
                    String speed = cursor.getString(speedColumnIndex);

                    intent.putExtra(EXTRA_RUN_ID, runID);
                    intent.putExtra(EXTRA_TIME, timeTitle);
                    intent.putExtra(EXTRA_LATITUDE, latitude);
                    intent.putExtra(EXTRA_LONGITUDE, longitude);
                    intent.putExtra(EXTRA_ALTITUDE, altitude);
                    intent.putExtra(EXTRA_SPEED, speed);

                } while (cursor.moveToNext());
            }
            if (cursor != null) {
                cursor.close();
            }

        } catch (Exception e) {
            Log.e("Path Error", e.toString());
        }

        Uri currentProductUri = ContentUris.withAppendedId(TrackContract.TrackingEntry.CONTENT_URI, id);
        intent.setData(currentProductUri);
        startActivity(intent);
    }
}
