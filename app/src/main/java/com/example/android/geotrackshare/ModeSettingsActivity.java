package com.example.android.geotrackshare;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.geotrackshare.RunTypes.RunTypesAdapterNoUI;
import com.example.android.geotrackshare.Sync.GeoTrackShareFirebaseJobService;
import com.example.android.geotrackshare.Utils.ExportImportDB;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URLConnection;

import static com.example.android.geotrackshare.LocationService.LocationServiceConstants.isAutoExportDone;
import static com.example.android.geotrackshare.LocationService.LocationServiceConstants.isExportDone;
import static com.example.android.geotrackshare.LocationService.LocationServiceConstants.lastDBAutoExportTime;
import static com.example.android.geotrackshare.LocationService.LocationServiceConstants.lastDBExportTime;
import static com.example.android.geotrackshare.LocationService.LocationServiceConstants.setExportDone;
import static com.example.android.geotrackshare.LocationService.LocationServiceConstants.setLastExportTime;
import static com.example.android.geotrackshare.MainActivity.BICYCLE;
import static com.example.android.geotrackshare.MainActivity.CAR;
import static com.example.android.geotrackshare.MainActivity.EXPORTIMPORT;
import static com.example.android.geotrackshare.MainActivity.WALK;
import static com.example.android.geotrackshare.MainActivity.mCategories;
import static com.example.android.geotrackshare.Utils.ExportImportDB.appDB;
import static com.example.android.geotrackshare.Utils.ExportImportDB.backupPath;
import static com.example.android.geotrackshare.Utils.StopWatch.formatDate;

/**
 * Created by Marcin on 2017-09-12.
 */

public class ModeSettingsActivity extends AppCompatActivity {

    private static final String TAG = ModeSettingsActivity.class.getSimpleName();
    public static boolean preferenceBooleanTheme;
    RunTypesAdapterNoUI mAdapter;
    String description, description1;
    int interval;
    private TextView lastAutoBackup;
    // The BroadcastReceiver used to listen from broadcasts from the service.
    private MyReceiver myReceiver;
    /**
     * Code used in requesting runtime permissions.
     */
    public static final int REQUEST_PERMISSIONS_REQUEST_CODE = 34;
    private Context mContext;


    public static boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state) && Environment.getExternalStorageDirectory()
                .canWrite();
    }

    private void switchThemeS() {

        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        boolean themeBoolean = sharedPrefs.getBoolean("theme_switch", preferenceBooleanTheme);
        if (!themeBoolean) {
            this.setTheme(R.style.AppThemeSettings);
        } else {
            this.setTheme(R.style.AppThemeSettingsDarkTheme);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        switchThemeS();
        setContentView(R.layout.mode_settings);
        myReceiver = new MyReceiver();
        mContext = getApplication();

        ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
            actionBar.setDisplayShowTitleEnabled(true);
        }
        mAdapter = new RunTypesAdapterNoUI(this, mCategories);
        TextView modeDesctription = findViewById(R.id.mode_description);
        TextView modeDesctription1 = findViewById(R.id.mode_description1);
        TextView modeIntervaltext = findViewById(R.id.interval_text);
        TextView modeIntervalvalue = findViewById(R.id.interval_value);
        final TextView lastBackup = findViewById(R.id.last_backup);
        TextView lastBackupDesc = findViewById(R.id.last_backup_desc);
        lastAutoBackup = findViewById(R.id.last_auto_backup);
        TextView lastAutoBackupDesc = findViewById(R.id.last_auto_backup_desc);
        Button exportButton = findViewById(R.id.exportButton);
        Button importButton = findViewById(R.id.importButton);
        Button uploadToFirebase = findViewById(R.id.importAutoButton);
        final Button downloadFromFirebase = findViewById(R.id.downloadFromFireBase);
        Button signOUT = findViewById(R.id.signOut);

        Intent intent = getIntent();
        if ((WALK).equals(intent.getAction())) {
            description = String.valueOf(mAdapter.getItem(0).getDescription());
            interval = (int) mAdapter.getItem(0).getIntervalPreset();
            exportButton.setVisibility(View.INVISIBLE);
            importButton.setVisibility(View.INVISIBLE);
            uploadToFirebase.setVisibility(View.INVISIBLE);
            downloadFromFirebase.setVisibility(View.INVISIBLE);
            signOUT.setVisibility(View.INVISIBLE);
//            importAutoButton.setVisibility(View.INVISIBLE);
        } else if ((BICYCLE).equals(intent.getAction())) {
            description = String.valueOf(mAdapter.getItem(1).getDescription());
            interval = (int) mAdapter.getItem(1).getIntervalPreset();
            exportButton.setVisibility(View.INVISIBLE);
            importButton.setVisibility(View.INVISIBLE);
            uploadToFirebase.setVisibility(View.INVISIBLE);
            downloadFromFirebase.setVisibility(View.INVISIBLE);
            signOUT.setVisibility(View.INVISIBLE);
//            importAutoButton.setVisibility(View.INVISIBLE);
        } else if ((CAR).equals(intent.getAction())) {
            description = String.valueOf(mAdapter.getItem(2).getDescription());
            interval = (int) mAdapter.getItem(2).getIntervalPreset();
            exportButton.setVisibility(View.INVISIBLE);
            importButton.setVisibility(View.INVISIBLE);
            uploadToFirebase.setVisibility(View.VISIBLE);
            downloadFromFirebase.setVisibility(View.VISIBLE);
            signOUT.setVisibility(View.VISIBLE);
//            importAutoButton.setVisibility(View.INVISIBLE);
        } else if ((EXPORTIMPORT).equals(intent.getAction())) {
            description = getResources().getString(R.string.export_database_summary);
            description1 = getResources().getString(R.string.import_database_summary);
            exportButton.setVisibility(View.VISIBLE);
            uploadToFirebase.setVisibility(View.VISIBLE);
            downloadFromFirebase.setVisibility(View.VISIBLE);
            signOUT.setVisibility(View.INVISIBLE);
            if (!isExportDone(getApplicationContext())) {
                lastBackup.setText(R.string.no_backup);
            } else {
                String lastBackupString = getResources().getString(R.string.last_backup);
                lastBackupDesc.setText(lastBackupString);
                lastBackup.setText(lastDBExportTime(getApplicationContext()));
            }
            if (!isAutoExportDone(getApplicationContext())) {
                lastAutoBackup.setText(R.string.no_auto_backup);
            } else {
                String lastAutoBackupString = getResources().getString(R.string.last_auto_backup);
                lastAutoBackupDesc.setText(lastAutoBackupString);
                lastAutoBackup.setText(lastDBAutoExportTime(getApplicationContext()));
            }

            exportButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (isExternalStorageWritable()) {
                        ExportImportDB.exportDB();

                        String currentDateTimeString = formatDate();

                        lastBackup.setText(currentDateTimeString);
                        setLastExportTime(getApplicationContext(), currentDateTimeString);
                        setExportDone(getApplicationContext(), true);
                        Toast.makeText(getBaseContext(), "DataBase Exported",
                                Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(getBaseContext(), "External Storage in not writable",
                                Toast.LENGTH_LONG).show();
                    }
                }
            });

            importButton.setVisibility(View.VISIBLE);

            importButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
//                    showFileChooser();
                    showAlertDialogWithListOFFIles();

                }
            });


            uploadToFirebase.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    ExportImportDB.uploadToFirebaseStorage();
                }
            });

            downloadFromFirebase.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!checkPermissionsStorage()) {
                        requestPermissions();
                    }
                    ExportImportDB.downloadFromFirebaseStorage();

                }
            });

            signOUT.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {


                }
            });


            modeIntervaltext.setVisibility(View.INVISIBLE);
            modeIntervalvalue.setVisibility(View.INVISIBLE);
            modeDesctription1.setText(description1);
        }
        modeDesctription.setText(description);

        String intervalValue = String.valueOf(interval / 1000);
        modeIntervalvalue.setText(intervalValue + " s");
    }


    public ArrayAdapter<String> fileChooserList() {
        ArrayAdapter<String> results = new ArrayAdapter<String>(ModeSettingsActivity.this, android.R.layout.select_dialog_singlechoice);

        FilenameFilter fileFilter = new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(".db");
            }
        };

        File[] listOfFiles = new File(String.valueOf(backupPath)).listFiles(fileFilter);

        try {
            for (File file : listOfFiles) {
                if (file.isFile()) {
                    results.add(file.getName());
                }
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
            Toast.makeText(this, "No backups in Folder",
                    Toast.LENGTH_SHORT).show();
        }

        return results;
    }


    private void showAlertDialogWithListOFFIles() {
        AlertDialog.Builder builderSingle = new AlertDialog.Builder(ModeSettingsActivity.this);
//        builderSingle.setIcon(R.drawable.ic_launcher);
        builderSingle.setTitle(getResources().getString(R.string.select_database));

        final ArrayAdapter<String> arrayAdapter = fileChooserList();

        builderSingle.setNegativeButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builderSingle.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                final String strName = arrayAdapter.getItem(which);
                AlertDialog.Builder builderInner = new AlertDialog.Builder(ModeSettingsActivity.this);
                builderInner.setMessage(strName);
                builderInner.setTitle(getResources().getString(R.string.selected_database));
                builderInner.setPositiveButton(getResources().getString(R.string.importt), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        String finalSelectedPath = String.valueOf(backupPath) + "/" + strName;
                        File file1 = new File(String.valueOf(finalSelectedPath));
                        ExportImportDB.importIntoDb(getApplication(), file1);
                        Toast.makeText(getBaseContext(), getResources().getString(R.string.database_imported), Toast.LENGTH_LONG).show();

                        dialog.dismiss();
                    }
                });
                builderInner.setNegativeButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                builderInner.show();
            }
        });
        builderSingle.show();
    }

    /**
     * showFileChooser() works but not used in this case.
     * In this case it is better to restrict choosing options to one catalog
     * AlertDialog is more userfriendly solution in this app
     * Therefore OnActivityResult is also unused
     **/

    public void showFileChooser() {

        Uri selectedUri = Uri.parse(String.valueOf(backupPath));
        String s = String.valueOf(selectedUri);

        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);

        //        intent.setType("text/db");
//        intent.setData(selectedUri);
//        intent.addCategory(Intent.CATEGORY_DEFAULT);
//        intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(selectedImagePath));
        String mime = null;
        try {
            mime = URLConnection.guessContentTypeFromStream(new FileInputStream(appDB));
            String mimes = mime;
            Log.i("mime", mime);
        } catch (IOException e) {
            e.printStackTrace();
        }
//        // New Approach
//        Uri apkURI = FileProvider.getUriForFile(
//                this,
//                getApplicationContext()
//                        .getPackageName() + ".provider", appDB);
//        intent.setDataAndType(apkURI, mime);
//        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);


        Toast.makeText(this, s,
                Toast.LENGTH_SHORT).show();

        try {
            startActivityForResult(Intent.createChooser(intent, "Select a File to Upload"), 0);
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(this, "Please install a File Manager.",
                    Toast.LENGTH_SHORT).show();
        }

//        I can download contents from Dropbox and for Google Drive, by getContentResolver().openInputStream(data.getData()).

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        switch (requestCode) {
            case 0: {
                //what you want to do
                String FileName;
                String selectedFile = null;
                String selectedFile0 = null;

                try {
                    selectedFile = data.getData().getPath();
                    selectedFile0 = selectedFile.substring(selectedFile.lastIndexOf(":") + 1);
                    Log.i("OnActivityResult", String.valueOf(selectedFile));
                } catch (NullPointerException e) {
                    System.out.print("Caught the NullPointerException");
                }


//                /** Gets the file name from the path, only option code not implemented below**/
//                Path path = null;
//                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
//                    path = Paths.get(selectedFile);
//                    FileName = path.getFileName().toString();
//                }

                System.out.println();

                String finalSelectedPath = Environment.getExternalStorageDirectory().getPath() + "/" + selectedFile0;
                File file1 = new File(String.valueOf(finalSelectedPath));

                ExportImportDB.importIntoDb(getApplication(), file1);
                Toast.makeText(getBaseContext(), "DataBase Imported",
                        Toast.LENGTH_LONG).show();

                Log.i("OnActivityResult", String.valueOf(finalSelectedPath));

            }
        }
    }


    // [END on_start_check_user]
    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(myReceiver,
                new IntentFilter(GeoTrackShareFirebaseJobService.ACTION_BROADCAST_TIME));

    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "onPause()");
        LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(myReceiver);


    }

    /**
     * Receiver for broadcasts sent by {@link GeoTrackShareFirebaseJobService}.
     */
    private class MyReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String timeDate = intent.getStringExtra(GeoTrackShareFirebaseJobService.EXTRA_TIME_DATE);
            lastAutoBackup.setText(timeDate);
        }
    }

    /**
     * Returns the current state of the permissions needed.
     */
    private boolean checkPermissionsStorage() {
        return PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(this,
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
                findViewById(android.R.id.content),
                getString(mainTextStringId),
                Snackbar.LENGTH_INDEFINITE)
                .setAction(getString(actionStringId), listener).show();
    }


    public void requestPermissions() {
        boolean shouldProvideRationale1 =
                ActivityCompat.shouldShowRequestPermissionRationale(ModeSettingsActivity.this,
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
                            ActivityCompat.requestPermissions(ModeSettingsActivity.this,
                                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                    REQUEST_PERMISSIONS_REQUEST_CODE);

                        }
                    });
        } else {
            Log.i(TAG, "Requesting permission");
            // Request permission. It's possible this can be auto answered if device policy
            // sets the permission in a given state or the user denied the permission
            // previously and checked "Never ask again".
            ActivityCompat.requestPermissions(ModeSettingsActivity.this,
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
    }
}