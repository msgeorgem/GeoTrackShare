package com.example.android.geotrackshare;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.NavUtils;
import android.support.v4.app.ShareCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.CompoundButton;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.example.android.geotrackshare.Data.TrackContract;
import com.example.android.geotrackshare.TrackList.RunListFragment;
import com.example.android.geotrackshare.databinding.ActivityDetailBinding;

import java.io.IOException;
import java.util.ArrayList;

import static com.example.android.geotrackshare.Data.TrackContract.TrackingEntry.COLUMN_RUN_ID;
import static com.example.android.geotrackshare.Data.TrackContract.TrackingEntry.COLUMN_TIME;
import static com.example.android.geotrackshare.Data.TrackContract.TrackingEntry.CONTENT_URI;

/**
 * Created by Marcin on 2017-11-29.
 */

public class DetailActivity extends AppCompatActivity {


    public static final String TEST_MDB_MOVIE_PATH = "https://api.themoviedb.org/3/movie/321612/videos?api_key=1157007d8e3f7d5e0af6d7e4165e2730";
    public static final String LOG_TAG = DetailActivity.class.getSimpleName();
    private static final String BUNDLE_RECYCLER_LAYOUT = "DetailActivity.clipsRecyclerView.activity_detail";
    private static final String[] PROJECTION = {
            TrackContract.TrackingEntry._ID,
            TrackContract.TrackingEntry.COLUMN_TIME,
    };
    public static String CURRENT_RUN_ID;
    public static SharedPreferences favPrefs;
    private final String MDB_SHARE_HASHTAG = "IMDB Source";
    private String mMovieSummary;

    private Context context;
    private ToggleButton FAVtoggleButton;
    private String currentRun, currentTimeStart;
    private long currentRunId;
    private String runId;
    private Uri mCurrentItemUri;
    private ActivityDetailBinding mDetailBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDetailBinding = DataBindingUtil.setContentView(this, R.layout.activity_detail);

        // Find the toolbar view inside the activity layout
        Toolbar toolbar = findViewById(R.id.toolbar);
        // Sets the Toolbar to act as the ActionBar for this Activity window.
        // Make sure the toolbar exists in the activity and is not null
        setSupportActionBar(toolbar);

        // Examine the intent that was used to launch this activity,
        // in order to figure out if we're creating a new item or editing an existing one.
        Intent intent = getIntent();
        mCurrentItemUri = intent.getData();

        CURRENT_RUN_ID = intent.getStringExtra(RunListFragment.EXTRA_RUN_ID);
        runId = CURRENT_RUN_ID;
        currentRun = intent.getStringExtra(RunListFragment.EXTRA_RUN_ID);
        currentTimeStart = intent.getStringExtra(RunListFragment.EXTRA_TIME);

        mDetailBinding.part2.runId.setText(currentRun);
        mDetailBinding.part2.startTime.setText(currentTimeStart);

        currentRunId = Long.parseLong(runId);

        context = mDetailBinding.part2.favDetToggleButton.getContext();
        FAVtoggleButton = mDetailBinding.part2.favDetToggleButton;
        FAVtoggleButton.setChecked(true);

        Boolean a = checkIfInFavorites();

        if (a) {
            FAVtoggleButton.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.star_star));
            FAVtoggleButton.setChecked(true);
        } else {
            FAVtoggleButton.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.star_red));
            FAVtoggleButton.setChecked(false);
        }
        FAVtoggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {

                if (isChecked) {
//                    try {
//                        saveItem();
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }

                    FAVtoggleButton.setBackgroundDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.star_star));
                    SharedPreferences.Editor editor = favPrefs.edit();
                    editor.putBoolean("On", true);
                    editor.apply();

                } else {
                    FAVtoggleButton.setBackgroundDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.star_red));
                    SharedPreferences.Editor editor = favPrefs.edit();
                    editor.putBoolean("On", false);
                    editor.apply();
//                    delete(currentRunId);
                }
            }
        });


    }

    private boolean checkIfInFavorites() {
        Cursor cur = getContentResolver().query(CONTENT_URI, PROJECTION, null, null, null);

        ArrayList<String> favsTempList = new ArrayList<>();
        boolean favourite;
        if (cur != null) {
            while (cur.moveToNext()) {
                String i = cur.getString(cur.getColumnIndex(COLUMN_RUN_ID));
                favsTempList.add(i);
            }
        }
        favourite = favsTempList.contains(runId);

        if (cur != null) {
            cur.close();
        }
        return favourite;
    }

    public void delete(long id) {
        int rowDeleted = getContentResolver().delete(CONTENT_URI, TrackContract.TrackingEntry.COLUMN_RUN_ID + "=" + id, null);
        Toast.makeText(this, rowDeleted + " " + getString(R.string.delete_one_item), Toast.LENGTH_SHORT).show();
    }

//    private long checkIfDeleted(Long runId){
//        long tempId = 0;
//        if(!(runId == null) || !(runId == 0)){
//            Log.i(LOG_TAG, "not null or zero");
//            tempId = runId;
//        } else tempId = justDeletedMovieId;
//        return tempId;
//    };

    // Get user input from editor and save item into database.
    private void saveItem() throws IOException {

        ContentValues values = new ContentValues();
        values.put(COLUMN_RUN_ID, runId);
        values.put(COLUMN_TIME, currentTimeStart);
//        values.put(COLUMN_LATITUDE, currentReleaseDate);
//        values.put(COLUMN_LONGITUDE, currentVote);
//        values.put(COLUMN_ALTITUDE, currentOverview);
//        values.put(COLUMN_SPEED currentPoster);

        // This is a NEW item, so insert a new item into the provider,
        // returning the content URI for the item item.
        Uri newUri = getContentResolver().insert(CONTENT_URI, values);

        // Show a toast message depending on whether or not the insertion was successful.
        if (newUri == null) {
            // If the new content URI is null, then there was an error with insertion.
            Toast.makeText(this, getString(R.string.editor_insert_item_failed), Toast.LENGTH_SHORT).show();
        } else {
            // Otherwise, the insertion was successful and we can display a toast.
            Toast.makeText(this, getString(R.string.editor_insert_item_success), Toast.LENGTH_SHORT).show();
        }
        currentRunId = Long.parseLong(runId);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState != null) {
            Parcelable savedRecyclerLayoutState = savedInstanceState.getParcelable(BUNDLE_RECYCLER_LAYOUT);
//            ClipsFragment.clipsRecyclerView.getLayoutManager().onRestoreInstanceState(savedRecyclerLayoutState);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
//        outState.putParcelable(BUNDLE_RECYCLER_LAYOUT, ClipsFragment.clipsRecyclerView.getLayoutManager().onSaveInstanceState());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        /* Use AppCompatActivity's method getMenuInflater to get a handle on the menu inflater */
        MenuInflater inflater = getMenuInflater();
        /* Use the inflater's inflate method to inflate our menu layout to this menu */
        inflater.inflate(R.menu.detail, menu);
        /* Return true so that the menu is displayed in the Toolbar */
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();


        if (id == android.R.id.home) {
            NavUtils.navigateUpFromSameTask(DetailActivity.this);
            return true;
        }
        /* Share menu item clicked */
        if (id == R.id.action_share_d) {
            Intent shareIntent = createShareMovieIntent();
            startActivity(shareIntent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private Intent createShareMovieIntent() {
        Intent shareIntent = ShareCompat.IntentBuilder.from(this)
                .setType("text/plain")
                .setText(mMovieSummary + MDB_SHARE_HASHTAG)
                .getIntent();
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
        return shareIntent;
    }


}