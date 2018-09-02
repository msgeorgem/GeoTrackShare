package com.example.android.geotrackshare.TrackList;

import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
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
import com.example.android.geotrackshare.Data.TrackDbHelper;
import com.example.android.geotrackshare.DetailActivity;
import com.example.android.geotrackshare.R;
import com.example.android.geotrackshare.ScreenShotActivity;
import com.example.android.geotrackshare.Utils.SqliteExporter;

import java.util.Arrays;

import static android.support.constraint.Constraints.TAG;
import static com.example.android.geotrackshare.Data.TrackContract.TrackingEntry.COLUMN_AVR_SPEEDP;
import static com.example.android.geotrackshare.Data.TrackContract.TrackingEntry.COLUMN_FAVORITEP;
import static com.example.android.geotrackshare.Data.TrackContract.TrackingEntry.COLUMN_MAX_ALTP;
import static com.example.android.geotrackshare.Data.TrackContract.TrackingEntry.COLUMN_MAX_SPEEDP;
import static com.example.android.geotrackshare.Data.TrackContract.TrackingEntry.COLUMN_RUNTYPEP;
import static com.example.android.geotrackshare.Data.TrackContract.TrackingEntry.COLUMN_RUN_IDP;
import static com.example.android.geotrackshare.Data.TrackContract.TrackingEntry.COLUMN_START_TIMEP;
import static com.example.android.geotrackshare.Data.TrackContract.TrackingEntry.COLUMN_STOP_TIMEP;
import static com.example.android.geotrackshare.Data.TrackContract.TrackingEntry.COLUMN_TIME_COUNTERP;
import static com.example.android.geotrackshare.Data.TrackContract.TrackingEntry.COLUMN_TOTAL_DISTANCEP;
import static com.example.android.geotrackshare.Data.TrackContract.TrackingEntry.CONTENT_URI_POST;
import static com.example.android.geotrackshare.DetailActivity.ACTION_FROM_RUNLISTFRAGMENT;
import static com.example.android.geotrackshare.LocationService.LocationServiceConstants.lastTrackID;


/**
 * Created by Marcin on 2017-10-25.
 */

public class RunListFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final String LOG_TAG = RunListFragment.class.getName();
    public static final String EXTRA_RUN_ID = "EXTRA_RUN_ID";
    public static final String EXTRA_RUNTYPE = "EXTRA_RUNTYPE";
    public static final String EXTRA_TOTAL_DISTANCE = "EXTRA_TOTAL_DISTANCE";
    public static final String EXTRA_AVG_SPEED = "EXTRA_AVG_SPEED";
    public static final String EXTRA_TOTAL_TIME = "EXTRA_TOTAL_TIME";

    private static final int FAV_LOADER = 0;

    public static final String[] PROJECTION_POST = {
            TrackContract.TrackingEntry._ID,
            COLUMN_RUN_IDP,
            COLUMN_START_TIMEP,
            COLUMN_STOP_TIMEP,
            COLUMN_RUNTYPEP,
            COLUMN_TOTAL_DISTANCEP,
            COLUMN_MAX_ALTP,
            COLUMN_MAX_SPEEDP,
            COLUMN_AVR_SPEEDP,
            COLUMN_TIME_COUNTERP,
            COLUMN_FAVORITEP
    };
    private static final String BUNDLE_RECYCLER_LAYOUT = "TrackListFragment.tracksRecyclerView";
    public static Context mContext;
    public TracksCursorAdapter mTracksAdapter;
    Parcelable state;
    private View mloadingIndicator;
    private TextView mEmptyStateTextView;
    private View view;
    //    private static final String SELECTION = TrackContract.TrackingEntry.getGreaterThanZero();
    private RecyclerView tracksRecyclerView;
    private TrackDbHelper mDbHelper;

    /* Checks if external storage is available for read and write */
    public static boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }

    public RunListFragment() {
        // Required empty public constructor
    }

    public static RunListFragment newInstance() {
        return new RunListFragment();
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_run_list, container, false);
        Log.i(LOG_TAG, "initLoader");
        mContext = getActivity();

        // Find a reference to the {@link ListView} in the layout
        tracksRecyclerView = view.findViewById(R.id.list_runs);

        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            tracksRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 1));
        } else {
            tracksRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 1));
        }

        mTracksAdapter = new TracksCursorAdapter(this, null);

        // Set the adapter on the {@link ListView}
        // so the list can be populated in the user interface
        tracksRecyclerView.setAdapter(mTracksAdapter);
        tracksRecyclerView.setItemAnimator(new DefaultItemAnimator());

        mEmptyStateTextView = view.findViewById(R.id.empty_view_runs);
        if (lastTrackID(mContext) == 0) {
            mEmptyStateTextView.setText(R.string.no_runs);
        }

        mloadingIndicator = view.findViewById(R.id.loading_indicator_runs);
        //kick off the loader
        getLoaderManager().initLoader(FAV_LOADER, null, this);
        mDbHelper = new TrackDbHelper(getActivity());

        return view;
    }

    public static void updateFavouritePost(final int favourite, final int id, final Context context) {
        Log.e(TAG, "saving " + favourite);
        String specificID = String.valueOf(id);
        final String mSelectionClause = TrackContract.TrackingEntry.COLUMN_RUN_IDP;
        final String mSelection = mSelectionClause + " = '" + specificID + "'";

        // Database operations should not be done on the main thread
        AsyncTask<Void, Void, Void> insertItem = new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... voids) {
                ContentValues values = new ContentValues();
                values.put(COLUMN_FAVORITEP, favourite);
                // This is a NEW item, so insert a new item into the provider,
                // returning the content URI for the item item.
                context.getContentResolver().update(CONTENT_URI_POST, values, mSelection, null);
                return null;
            }
        };
        insertItem.execute();
    }

    void deleteOneItem(int runId) {
        int rowDeleted = getActivity().getContentResolver().delete(TrackContract.TrackingEntry.CONTENT_URI_POST, TrackContract.TrackingEntry.COLUMN_RUN_IDP + "=" + runId, null);
        Toast.makeText(getActivity(), rowDeleted + " " + getString(R.string.delete_one_item), Toast.LENGTH_SHORT).show();

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

                //update the list (no changes to the list!) very improtant line
                mTracksAdapter.notifyDataSetChanged();

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
        Log.e(LOG_TAG, Arrays.toString(PROJECTION_POST));
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());

        String COLUMN = sharedPrefs.getString(
                getString(R.string.run_settings_order_by_key),
                getString(R.string.run_settings_order_by_key)
        );

        String SORT_ORDER = COLUMN + " DESC ";

        // Perform a query using CursorLoader
        return new CursorLoader(getActivity(),    // Parent activity context
                TrackContract.TrackingEntry.CONTENT_URI_POST, // Provider content URI to query
                PROJECTION_POST,            // The columns to include in the resulting Cursor
                null,         // The values for the WHERE clause
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
            Log.e(LOG_TAG, "NO DATA");
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


    public void onItemClick(int id) {
        Long idLong = (long) id;
        Intent intent = new Intent(getActivity(), DetailActivity.class);
        intent.setAction(ACTION_FROM_RUNLISTFRAGMENT);
        intent.putExtra(EXTRA_RUN_ID, idLong);

        startActivity(intent);
    }



    public void shareViaEmail(int runId) {
        try {
            SQLiteDatabase database = mDbHelper.getReadableDatabase();
            String filelocation1 = SqliteExporter.export(database, runId);
            String file_name = SqliteExporter.createBackupFileName(runId);

            Intent intent = new Intent(Intent.ACTION_SENDTO);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            intent.setType("text/plain");
            String sarharingMessage = getString(R.string.sharing_message);
            String message = sarharingMessage + " " + file_name + ".";
            intent.putExtra(Intent.EXTRA_SUBJECT, "Subject");
            intent.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://" + filelocation1));
            intent.putExtra(Intent.EXTRA_TEXT, message);
            intent.setData(Uri.parse("mailto:xyz@gmail.com"));

            startActivityForResult(intent, 1);
        } catch (Exception e) {
            System.out.println("is exception raises during sending mail" + e);
        }
    }

    public void onShareClick(int id) {
        Intent intent = new Intent(getActivity(), ScreenShotActivity.class);
        intent.setFlags(intent.getFlags() | Intent.FLAG_ACTIVITY_NO_HISTORY);
        intent.setAction(ACTION_FROM_RUNLISTFRAGMENT);
        intent.putExtra(EXTRA_RUN_ID, id);

        startActivity(intent);
    }

}
