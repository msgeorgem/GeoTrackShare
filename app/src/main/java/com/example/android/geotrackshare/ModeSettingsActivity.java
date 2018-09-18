package com.example.android.geotrackshare;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.geotrackshare.RunTypes.RunTypesAdapterNoUI;
import com.example.android.geotrackshare.Utils.ExportImportDB;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URLConnection;

import static com.example.android.geotrackshare.LocationService.LocationServiceConstants.isExportDone;
import static com.example.android.geotrackshare.LocationService.LocationServiceConstants.lastDBExportTime;
import static com.example.android.geotrackshare.LocationService.LocationServiceConstants.setExportDone;
import static com.example.android.geotrackshare.LocationService.LocationServiceConstants.setLastExportTime;
import static com.example.android.geotrackshare.MainActivity.BICYCLE;
import static com.example.android.geotrackshare.MainActivity.CAR;
import static com.example.android.geotrackshare.MainActivity.EXPORTIMPORT;
import static com.example.android.geotrackshare.MainActivity.WALK;
import static com.example.android.geotrackshare.MainActivity.mCategories;
import static com.example.android.geotrackshare.Utils.ExportImportDB.appDB;
import static com.example.android.geotrackshare.Utils.ExportImportDB.backupDB;
import static com.example.android.geotrackshare.Utils.StopWatch.formatDate;

/**
 * Created by Marcin on 2017-09-12.
 */

public class ModeSettingsActivity extends AppCompatActivity {

    public static boolean preferenceBooleanTheme;
    RunTypesAdapterNoUI mAdapter;
    String description, description1;
    int interval;

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
        Button exportButton = findViewById(R.id.exportButton);
        Button importButton = findViewById(R.id.importButton);

        Intent intent = getIntent();
        if ((WALK).equals(intent.getAction())) {
            description = String.valueOf(mAdapter.getItem(0).getDescription());
            interval = (int) mAdapter.getItem(0).getIntervalPreset();
            exportButton.setVisibility(View.INVISIBLE);
            importButton.setVisibility(View.INVISIBLE);
        } else if ((BICYCLE).equals(intent.getAction())) {
            description = String.valueOf(mAdapter.getItem(1).getDescription());
            interval = (int) mAdapter.getItem(1).getIntervalPreset();
            exportButton.setVisibility(View.INVISIBLE);
            importButton.setVisibility(View.INVISIBLE);
        } else if ((CAR).equals(intent.getAction())) {
            description = String.valueOf(mAdapter.getItem(2).getDescription());
            interval = (int) mAdapter.getItem(2).getIntervalPreset();
            exportButton.setVisibility(View.INVISIBLE);
            importButton.setVisibility(View.INVISIBLE);
        } else if ((EXPORTIMPORT).equals(intent.getAction())) {
            description = getResources().getString(R.string.export_database_summary);
            description1 = getResources().getString(R.string.import_database_summary);
            exportButton.setVisibility(View.VISIBLE);
            if (!isExportDone(getApplicationContext())) {
                lastBackup.setText(R.string.no_backup);
            } else {
                String lastBackupString = getResources().getString(R.string.last_backup);
                lastBackupDesc.setText(lastBackupString);
                lastBackup.setText(lastDBExportTime(getApplicationContext()));
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
                    showFileChooser();
//                    ExportImportDB.importIntoDb(getApplication());

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


    public void showFileChooser() {


//        Uri selectedUri = Uri.parse(String.valueOf(backupPath));
        Uri selectedUri = Uri.parse(Environment.getExternalStorageDirectory().getPath() + "/BackupFolder/Geotrackshare/");
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
//                Toast.makeText(getBaseContext(), "DataBase Imported",
//                        Toast.LENGTH_LONG).show();


                Log.i("OnActivityResult", String.valueOf(finalSelectedPath));
                Log.i("OnActivityResult", String.valueOf(backupDB));
            }
        }
    }

}