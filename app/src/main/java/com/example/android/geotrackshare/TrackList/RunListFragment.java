package com.example.android.geotrackshare.TrackList;

import android.Manifest;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
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
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.geotrackshare.BuildConfig;
import com.example.android.geotrackshare.Data.TrackContract;
import com.example.android.geotrackshare.Data.TrackDbHelper;
import com.example.android.geotrackshare.DetailActivity;
import com.example.android.geotrackshare.R;
import com.example.android.geotrackshare.ScreenShotActivity;
import com.example.android.geotrackshare.Utils.ExportImportDB;
import com.example.android.geotrackshare.Utils.SqliteExporter;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;

import java.io.File;
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
import static com.example.android.geotrackshare.LocationService.LocationServiceConstants.isDBBackupOnFirebase;
import static com.example.android.geotrackshare.ModeSettingsActivity.REQUEST_PERMISSIONS_REQUEST_CODE;
import static com.example.android.geotrackshare.Utils.ExportImportDB.appDir;
import static com.example.android.geotrackshare.Utils.ExportImportDB.backupDBwithFIle;


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
    private Button importButtonLocal, importButtonFirebase;
    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;

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
        appDir = new File(Environment.getExternalStorageDirectory() + "/BackupFolder/GeoTrackShare/geotrackshare.db");
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

        mloadingIndicator = view.findViewById(R.id.loading_indicator_runs);
        //kick off the loader
        getLoaderManager().initLoader(FAV_LOADER, null, this);
        mDbHelper = new TrackDbHelper(getActivity());
        importButtonLocal = view.findViewById(R.id.importButtonLocal);
        importButtonLocal.setVisibility(View.INVISIBLE);
        importButtonFirebase = view.findViewById(R.id.importButtonFirebase);
        importButtonFirebase.setVisibility(View.INVISIBLE);

        // [START config_signin]
        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        // [END config_signin]

        mGoogleSignInClient = GoogleSignIn.getClient(getActivity(), gso);

        // [START initialize_auth]
        mAuth = FirebaseAuth.getInstance();
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
            if (backupDBwithFIle.exists()) {
                importButtonLocal.setVisibility(View.VISIBLE);
                importButtonLocal.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (isExternalStorageWritable()) {
                            ExportImportDB.importIntoEmptyDbFromLocalBackup(mContext);
                            Toast.makeText(mContext, "DataBase Imported",
                                    Toast.LENGTH_LONG).show();
                            importButtonLocal.setVisibility(View.INVISIBLE);
                        } else {
                            Toast.makeText(mContext, "Storage in not writable",
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                });
            } else if (isDBBackupOnFirebase(mContext)) {
                importButtonFirebase.setVisibility(View.VISIBLE);
                importButtonFirebase.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (isExternalStorageWritable()) {
                            if (!checkPermissionsStorage()) {
                                requestPermissions();
                            }
                            ExportImportDB.downloadFromFirebaseStorage();
                            Toast.makeText(mContext, "DataBase Imported from Firebase",
                                    Toast.LENGTH_LONG).show();
                            importButtonFirebase.setVisibility(View.INVISIBLE);
                        } else {
                            Toast.makeText(mContext, "Storage in not writable",
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                });
            } else {
                mEmptyStateTextView.setVisibility(View.VISIBLE);
                mEmptyStateTextView.setText(R.string.no_runs);
            }
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

    /**
     * Returns the current state of the permissions needed.
     */
    private boolean checkPermissionsStorage() {
        return PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(mContext,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);
    }

    /**
     * Shows a {@link Snackbar}.
     *
     * @param mainTextStringId The id for the string resource for the Snackbar text.
     * @param actionStringId   The text of the action item.
     * @param listener         The listener associated with the Snackbar action.
     */
    private void showSnackbar(final int mainTextStringId, final int actionStringId,
                              View.OnClickListener listener) {
        Snackbar.make(
                getActivity().findViewById(android.R.id.content),
                getString(mainTextStringId),
                Snackbar.LENGTH_INDEFINITE)
                .setAction(getString(actionStringId), listener).show();
    }


    public void requestPermissions() {
        boolean shouldProvideRationale1 =
                ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                        Manifest.permission.WRITE_EXTERNAL_STORAGE);

        // Provide an additional rationale to the user. This would happen if the user denied the
        // request previously, but didn't check the "Don't ask again" checkbox.
        if (shouldProvideRationale1) {
            Log.i(TAG, "Displaying permission rationale to provide additional context.");
            showSnackbar(R.string.permission_rationale,
                    android.R.string.ok, new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            // Request permission
                            ActivityCompat.requestPermissions(getActivity(),
                                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                    REQUEST_PERMISSIONS_REQUEST_CODE);


                        }
                    });
        } else {
            Log.i(TAG, "Requesting permission");
            // Request permission. It's possible this can be auto answered if device policy
            // sets the permission in a given state or the user denied the permission
            // previously and checked "Never ask again".
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_PERMISSIONS_REQUEST_CODE);

        }
    }

    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        Log.i(TAG, "onRequestPermissionResult");
        if (requestCode == REQUEST_PERMISSIONS_REQUEST_CODE) {
            if (grantResults.length <= 0) {
                // If user interaction was interrupted, the permission request is cancelled and you
                // receive empty arrays.
                Log.i(TAG, "User interaction was cancelled.");
            } else if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                Log.i(TAG, "Permission granted, download requested, starting location updates");

                ExportImportDB.downloadFromFirebaseStorage();

            } else {
                // Permission denied.
//                setButtonsEnabledState(false);
                // Notify the user via a SnackBar that they have rejected a core permission for the
                // app, which makes the Activity useless. In a real app, core permissions would
                // typically be best requested during a welcome-screen flow.

                // Additionally, it is important to remember that a permission might have been
                // rejected without asking the user for permission (device policy or "Never ask
                // again" prompts). Therefore, a user interface affordance is typically implemented
                // when permissions are denied. Otherwise, your app could appear unresponsive to
                // touches or interactions which have required permissions.
                showSnackbar(R.string.permission_denied_explanation,
                        R.string.settings, new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                // Build intent that displays the App settings screen.
                                Intent intent = new Intent();
                                intent.setAction(
                                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                Uri uri = Uri.fromParts("package",
                                        BuildConfig.APPLICATION_ID, null);
                                intent.setData(uri);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                            }
                        });
            }
        }
    }  // [END auth_with_google]
    // [START signin]


}
