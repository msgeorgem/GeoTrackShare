package com.example.android.geotrackshare;


import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.Loader;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.example.android.geotrackshare.Data.TrackLoader;

import static com.example.android.geotrackshare.AdvancedSettingsActivity.preferenceBooleanTheme;
import static com.example.android.geotrackshare.TrackList.RunListFragment.EXTRA_RUN_ID;


/**
 * Created by Marcin on 2017-11-29.
 */

public class DetailActivity extends FragmentActivity
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

    private ViewPager2 viewPager;
    private FragmentStateAdapter pagerAdapter;

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
        //Temporary solution to prove it works
        //Temporary solution to prove it works
//        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
//        transaction.setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out, android.R.animator.fade_in, android.R.animator.fade_out);
//        transaction.replace(R.id.container,
//                DetailFragment.newInstance((int) mStartId));
//        transaction.commit();

        LoaderManager.getInstance(this).initLoader(0, null, this);
        viewPager = findViewById(R.id.pager);
        pagerAdapter = new ScreenSlidePagerAdapter(this);
        viewPager.setAdapter(pagerAdapter);

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                Log.e("Selected_Page", String.valueOf(position));
                if (mCursor != null) {
                    mCursor.moveToPosition(position);
                }
                mSelectedItemId = mCursor.getLong(TrackLoader.Query.COLUMN_RUN_IDP);
                Log.e(LOG_TAG + "_onPageSelected", String.valueOf(mSelectedItemId));

//                updateUpButtonPosition();
            }

        });

//        mUpButtonContainer = findViewById(R.id.up_container);
//
//        mUpButton = findViewById(R.id.action_up);
//        mUpButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
////                onSupportNavigateUp();
//            }
//        });

//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//
//            mUpButtonContainer.setOnApplyWindowInsetsListener(new View.OnApplyWindowInsetsListener() {
//                @Override
//                public WindowInsets onApplyWindowInsets(View view, WindowInsets windowInsets) {
//                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
//                        view.onApplyWindowInsets(windowInsets);
//                    }
//                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
//                        mTopInset = windowInsets.getSystemWindowInsetTop();
//                    }
//                    mUpButtonContainer.setTranslationY(mTopInset);
//                    updateUpButtonPosition();
//                    return windowInsets;
//
//                }
//            });
//        }

        if (savedInstanceState == null) {
            Log.e("savedInstanceState", "null");
            mSelectedItemId = mStartId;
        }
    }
//
//    private void updateUpButtonPosition() {
//        int upButtonNormalBottom = mTopInset + mUpButton.getHeight();
//        mUpButton.setTranslationY(Math.min(mSelectedItemUpButtonFloor - upButtonNormalBottom, 0));
//    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        Log.e(LOG_TAG, "onCreateLoader");
        return TrackLoader.newAllArticlesInstance(this);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        mCursor = cursor;
        Log.e(LOG_TAG, "onLoadFinished");
        Log.e(LOG_TAG, String.valueOf(mStartId));
        // Select the start ID
        if (mStartId > 0) {
            mCursor.moveToFirst();
            while (!mCursor.isAfterLast()) {
                if (mCursor.getInt(TrackLoader.Query.COLUMN_RUN_IDP) == mStartId) {
                    Log.e("Query.COLUMN_RUN_IDP", String.valueOf(TrackLoader.Query.COLUMN_RUN_IDP));
                    final int position = mCursor.getPosition();
                    pagerAdapter.notifyDataSetChanged();
                    viewPager.setCurrentItem(position, false);
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
        pagerAdapter.notifyDataSetChanged();
    }
//    @Override
//    public void onBackPressed() {
//        if (viewPager.getCurrentItem() == 0) {
//            // If the user is currently looking at the first step, allow the system to handle the
//            // Back button. This calls finish() on this activity and pops the back stack.
//            super.onBackPressed();
//        } else {
//            // Otherwise, select the previous step.
//            viewPager.setCurrentItem(viewPager.getCurrentItem() - 1);
//        }
//    }

    private class ScreenSlidePagerAdapter extends FragmentStateAdapter {

        public ScreenSlidePagerAdapter(@NonNull FragmentActivity fragmentActivity) {
            super(fragmentActivity);
            Log.e(LOG_TAG, "Looking for me3");
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            Log.e(LOG_TAG + "_getItem_whatever", String.valueOf(position));
            mCursor.moveToPosition(position);
            int whatever = mCursor.getInt(TrackLoader.Query.COLUMN_RUN_IDP);
            return DetailFragment.newInstance(mCursor.getInt(TrackLoader.Query.COLUMN_RUN_IDP));
        }

        @Override
        public int getItemCount() {
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

    private class ZoomOutPageTransformer implements ViewPager2.PageTransformer {
        private static final float MIN_SCALE = 0.85f;
        private static final float MIN_ALPHA = 0.5f;

        public void transformPage(View view, float position) {
            int pageWidth = view.getWidth();
            int pageHeight = view.getHeight();

            if (position < -1) { // [-Infinity,-1)
                // This page is way off-screen to the left.
                view.setAlpha(0f);

            } else if (position <= 1) { // [-1,1]
                // Modify the default slide transition to shrink the page as well
                float scaleFactor = Math.max(MIN_SCALE, 1 - Math.abs(position));
                float vertMargin = pageHeight * (1 - scaleFactor) / 2;
                float horzMargin = pageWidth * (1 - scaleFactor) / 2;
                if (position < 0) {
                    view.setTranslationX(horzMargin - vertMargin / 2);
                } else {
                    view.setTranslationX(-horzMargin + vertMargin / 2);
                }

                // Scale the page down (between MIN_SCALE and 1)
                view.setScaleX(scaleFactor);
                view.setScaleY(scaleFactor);

                // Fade the page relative to its size.
                view.setAlpha(MIN_ALPHA +
                        (scaleFactor - MIN_SCALE) /
                                (1 - MIN_SCALE) * (1 - MIN_ALPHA));

            } else { // (1,+Infinity]
                // This page is way off-screen to the right.
                view.setAlpha(0f);
            }
        }
    }

}