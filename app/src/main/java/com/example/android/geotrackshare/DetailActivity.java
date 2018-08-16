package com.example.android.geotrackshare;


import android.app.Fragment;
import android.app.FragmentManager;
import android.app.LoaderManager;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v13.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;

import com.example.android.geotrackshare.Data.TrackLoader;

import static com.example.android.geotrackshare.AdvancedSettingsActivity.preferenceBooleanTheme;
import static com.example.android.geotrackshare.TrackList.RunListFragment.EXTRA_RUN_ID;


/**
 * Created by Marcin on 2017-11-29.
 */

public class DetailActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<Cursor> {


    public static final String LOG_TAG = DetailActivity.class.getSimpleName();
    public static String ACTION_FROM_RUNLISTFRAGMENT = "ACTION_FROM_RUNLISTFRAGMENT";

    public static SharedPreferences favPrefs;

    private Cursor mCursor;
    private long mStartId;
    private Intent intent;

    private long mSelectedItemId;
    private int mSelectedItemUpButtonFloor = Integer.MAX_VALUE;
    private int mTopInset;

    private ViewPager mPager;
    private MyPagerAdapter mPagerAdapter;
    private View mUpButtonContainer;
    private View mUpButton;

    public DetailActivity() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        switchThemeD();
        setContentView(R.layout.activity_detail);

        intent = getIntent();
        if (ACTION_FROM_RUNLISTFRAGMENT.equals(intent.getAction())) {
            mStartId = intent.getLongExtra(EXTRA_RUN_ID, 0);
        }

        getLoaderManager().initLoader(0, null, this);

        mPagerAdapter = new MyPagerAdapter(getFragmentManager());
        mPager = findViewById(R.id.pager);
        mPager.setAdapter(mPagerAdapter);
        mPager.setPageMargin((int) TypedValue
                .applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1, getResources().getDisplayMetrics()));
        mPager.setPageMarginDrawable(new ColorDrawable(0x22000000));

        mPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageScrollStateChanged(int state) {
                super.onPageScrollStateChanged(state);
                mUpButton.animate()
                        .alpha((state == ViewPager.SCROLL_STATE_IDLE) ? 1f : 0f)
                        .setDuration(300);
            }

            @Override
            public void onPageSelected(int position) {
                if (mCursor != null) {
                    mCursor.moveToPosition(position);
                }
                mSelectedItemId = mCursor.getLong(TrackLoader.Query.COLUMN_RUN_IDP);
                Log.e(LOG_TAG, String.valueOf(mSelectedItemId));
                updateUpButtonPosition();
            }
        });

        mUpButtonContainer = findViewById(R.id.up_container);

        mUpButton = findViewById(R.id.action_up);
        mUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onSupportNavigateUp();
            }
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

            mUpButtonContainer.setOnApplyWindowInsetsListener(new View.OnApplyWindowInsetsListener() {
                @Override
                public WindowInsets onApplyWindowInsets(View view, WindowInsets windowInsets) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
                        view.onApplyWindowInsets(windowInsets);
                    }
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
                        mTopInset = windowInsets.getSystemWindowInsetTop();
                    }
                    mUpButtonContainer.setTranslationY(mTopInset);
                    updateUpButtonPosition();
                    return windowInsets;

                }
            });
        }

        if (savedInstanceState == null) {
            Log.e("savedInstanceState", String.valueOf("null"));
            mSelectedItemId = mStartId;
        }
    }

    private void updateUpButtonPosition() {
        int upButtonNormalBottom = mTopInset + mUpButton.getHeight();
        mUpButton.setTranslationY(Math.min(mSelectedItemUpButtonFloor - upButtonNormalBottom, 0));
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        Log.e(LOG_TAG, String.valueOf("onCreateLoader"));
        return TrackLoader.newAllArticlesInstance(this);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        mCursor = cursor;
        Log.e(LOG_TAG, String.valueOf("onLoadFinished"));
        Log.e(LOG_TAG, String.valueOf(mStartId));
        // Select the start ID
        if (mStartId > 0) {
            mCursor.moveToFirst();
            while (!mCursor.isAfterLast()) {
                if (mCursor.getInt(TrackLoader.Query.COLUMN_RUN_IDP) == mStartId) {
                    Log.e("Query.COLUMN_RUN_IDP", String.valueOf(TrackLoader.Query.COLUMN_RUN_IDP));
                    final int position = mCursor.getPosition();
                    mPagerAdapter.notifyDataSetChanged();
                    mPager.setCurrentItem(position, false);
                    break;
                }
                mCursor.moveToNext();
            }
            mStartId = 0;
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mCursor = null;
        mPagerAdapter.notifyDataSetChanged();
    }


    private class MyPagerAdapter extends FragmentStatePagerAdapter {
        public MyPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public void setPrimaryItem(ViewGroup container, int position, Object object) {
            super.setPrimaryItem(container, position, object);
            DetailFragment fragment = (DetailFragment) object;
            if (fragment != null) {
                updateUpButtonPosition();
            }
        }

        @Override
        public Fragment getItem(int position) {
            mCursor.moveToPosition(position);
            int whatever = mCursor.getInt(TrackLoader.Query.COLUMN_RUN_IDP);
            Log.e(LOG_TAG, String.valueOf(whatever));
            return DetailFragment.newInstance(mCursor.getInt(TrackLoader.Query.COLUMN_RUN_IDP));

        }

        @Override
        public int getCount() {
            return (mCursor != null) ? mCursor.getCount() : 0;
        }
    }

    private void switchThemeD() {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        boolean themeBoolean = sharedPrefs.getBoolean("theme_switch", preferenceBooleanTheme);
        if (!themeBoolean) {
            setTheme(R.style.AppTheme);
        } else {
            setTheme(R.style.AppThemeDarkTheme);
        }
    }
}