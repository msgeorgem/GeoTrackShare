package com.example.android.geotrackshare.Utils;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.example.android.geotrackshare.R;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.Metadata;
import com.google.android.gms.drive.MetadataChangeSet;
import com.google.android.gms.drive.query.Filters;
import com.google.android.gms.drive.query.Query;
import com.google.android.gms.drive.query.SearchableField;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLConnection;
import java.nio.channels.FileChannel;
import java.util.ArrayList;

import static com.example.android.geotrackshare.Data.TrackContract.CONTENT_AUTHORITY;
import static com.example.android.geotrackshare.Data.TrackContract.TrackingEntry.COLUMN_ALTITUDE;
import static com.example.android.geotrackshare.Data.TrackContract.TrackingEntry.COLUMN_AVR_SPEED;
import static com.example.android.geotrackshare.Data.TrackContract.TrackingEntry.COLUMN_AVR_SPEEDP;
import static com.example.android.geotrackshare.Data.TrackContract.TrackingEntry.COLUMN_DISTANCE;
import static com.example.android.geotrackshare.Data.TrackContract.TrackingEntry.COLUMN_FAVORITEP;
import static com.example.android.geotrackshare.Data.TrackContract.TrackingEntry.COLUMN_LATITUDE;
import static com.example.android.geotrackshare.Data.TrackContract.TrackingEntry.COLUMN_LONGITUDE;
import static com.example.android.geotrackshare.Data.TrackContract.TrackingEntry.COLUMN_MAX_ALT;
import static com.example.android.geotrackshare.Data.TrackContract.TrackingEntry.COLUMN_MAX_ALTP;
import static com.example.android.geotrackshare.Data.TrackContract.TrackingEntry.COLUMN_MAX_SPEED;
import static com.example.android.geotrackshare.Data.TrackContract.TrackingEntry.COLUMN_MAX_SPEEDP;
import static com.example.android.geotrackshare.Data.TrackContract.TrackingEntry.COLUMN_MIN_ALT;
import static com.example.android.geotrackshare.Data.TrackContract.TrackingEntry.COLUMN_MOVE_CLOSE;
import static com.example.android.geotrackshare.Data.TrackContract.TrackingEntry.COLUMN_MOVE_DISTANCE;
import static com.example.android.geotrackshare.Data.TrackContract.TrackingEntry.COLUMN_RUNTYPE;
import static com.example.android.geotrackshare.Data.TrackContract.TrackingEntry.COLUMN_RUNTYPEP;
import static com.example.android.geotrackshare.Data.TrackContract.TrackingEntry.COLUMN_RUN_ID;
import static com.example.android.geotrackshare.Data.TrackContract.TrackingEntry.COLUMN_RUN_IDP;
import static com.example.android.geotrackshare.Data.TrackContract.TrackingEntry.COLUMN_SPEED;
import static com.example.android.geotrackshare.Data.TrackContract.TrackingEntry.COLUMN_START_TIME;
import static com.example.android.geotrackshare.Data.TrackContract.TrackingEntry.COLUMN_START_TIMEP;
import static com.example.android.geotrackshare.Data.TrackContract.TrackingEntry.COLUMN_STOP_TIMEP;
import static com.example.android.geotrackshare.Data.TrackContract.TrackingEntry.COLUMN_TIME;
import static com.example.android.geotrackshare.Data.TrackContract.TrackingEntry.COLUMN_TIME_COUNTER;
import static com.example.android.geotrackshare.Data.TrackContract.TrackingEntry.COLUMN_TIME_COUNTERP;
import static com.example.android.geotrackshare.Data.TrackContract.TrackingEntry.COLUMN_TOTAL_DISTANCE;
import static com.example.android.geotrackshare.Data.TrackContract.TrackingEntry.COLUMN_TOTAL_DISTANCEP;
import static com.example.android.geotrackshare.Data.TrackContract.TrackingEntry.CONTENT_URI;
import static com.example.android.geotrackshare.Data.TrackContract.TrackingEntry.CONTENT_URI_POST;
import static com.example.android.geotrackshare.Data.TrackContract.TrackingEntry.TABLE_NAME_POST_TRACKING;
import static com.example.android.geotrackshare.Data.TrackContract.TrackingEntry.TABLE_NAME_TRACKING;
import static com.example.android.geotrackshare.Data.TrackDbHelper.DATABASE_NAME;
import static com.example.android.geotrackshare.Utils.StopWatch.formatDateToFileName;

//import com.google.auth.oauth2.GoogleCredentials;

public class ExportImportDB extends Activity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    public static final String TAG = ExportImportDB.class.getName();
    public static final String PACKAGE_NAME = CONTENT_AUTHORITY;

    public static final String currentDBPath = "//data//" + PACKAGE_NAME + "//databases//" + DATABASE_NAME;
    public static final String backupDBPath = "/BackupFolder/Geotrackshare/";

    private static final File data = Environment.getDataDirectory();
    private static final File sd = Environment.getExternalStorageDirectory();

    public static final File appDB = new File(data, currentDBPath);
    public static final File backupPath = new File(sd, backupDBPath);

    /**
     * Contains: /data/data/com.example.android.geotrackshare/databases/geotrackshare.db
     **/

    public static final File backupDBonlyPath = new File(sd, backupDBPath);
    protected static final String totalDBFilePath = backupDBPath + "geotrackshare.db";
    public static final File backupDBwithFIle = new File(sd, totalDBFilePath);
    public static File appDir = new File(Environment.getExternalStorageDirectory() + "/BackupFolder/GeoTrackShare");


    protected static final int REQUEST_CODE_RESOLUTION = 1337;
    private static final String TAG_DRIVE = "<< DRIVE >>";
    static File backupDB;
    public GoogleApiClient mGoogleApiClient;
    private String FOLDER_NAME = "GTS";

    public ExportImportDB() {

    }

    /**
     * Imports the file at IMPORT_FILE
     **/
    public static boolean importIntoDb(Context ctx, File choseFile) {


        if (!SdIsPresent()) return false;

        if (!checkDbIsValid(choseFile, ctx)) return false;

        try {
            SQLiteDatabase sqlDbBackup = SQLiteDatabase.openDatabase
                    (choseFile.getPath(), null, SQLiteDatabase.OPEN_READONLY);

            ContentResolver trackContentResolver = ctx.getContentResolver();
            /* Delete old track data because we don't need to keep multiple tracks' data */
            trackContentResolver.delete(
                    CONTENT_URI_POST,
                    null,
                    null);

            /* Insert backedup data into GeotrackShare's ContentProvider */
            trackContentResolver.bulkInsert(
                    CONTENT_URI_POST,
                    readTableToArrayPost(sqlDbBackup, TABLE_NAME_POST_TRACKING));

            trackContentResolver.delete(
                    CONTENT_URI,
                    null,
                    null);

            /* Insert backedup data into GeotrackShare's ContentProvider */
            trackContentResolver.bulkInsert(
                    CONTENT_URI,
                    readTableToArray(sqlDbBackup, TABLE_NAME_TRACKING));

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * Imports the file at IMPORT_FILE
     **/
    public static boolean importIntoDb1(Context ctx) {


        if (!SdIsPresent()) return false;

        if (!checkDbIsValid(backupDBwithFIle, ctx)) return false;

        try {
            SQLiteDatabase sqlDbBackup = SQLiteDatabase.openDatabase
                    (backupDBwithFIle.getPath(), null, SQLiteDatabase.OPEN_READONLY);

            ContentResolver trackContentResolver = ctx.getContentResolver();
            /* Delete old track data because we don't need to keep multiple tracks' data */
            trackContentResolver.delete(
                    CONTENT_URI_POST,
                    null,
                    null);

            /* Insert backedup data into GeotrackShare's ContentProvider */
            trackContentResolver.bulkInsert(
                    CONTENT_URI_POST,
                    readTableToArrayPost(sqlDbBackup, TABLE_NAME_POST_TRACKING));

            trackContentResolver.delete(
                    CONTENT_URI,
                    null,
                    null);

            /* Insert backedup data into GeotrackShare's ContentProvider */
            trackContentResolver.bulkInsert(
                    CONTENT_URI,
                    readTableToArray(sqlDbBackup, TABLE_NAME_TRACKING));

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    //exporting database
    public static void exportDB() {
        String dateTimeString = formatDateToFileName();
        String dbFileName = "geotrackshare_" + dateTimeString + ".db";
        File backupDB = new File(backupDBonlyPath, dbFileName);

        if (!appDir.exists() && !appDir.isDirectory()) {
            // create empty directory
            if (appDir.mkdirs()) {
                Log.i("CreateDir", "App dir created");
            } else {
                Log.w("CreateDir", "Unable to create app dir!");
            }
        } else {
            Log.i("CreateDir", "App dir already exists");
        }

        try {

            if (sd.canWrite()) {

                copyFile(appDB, backupDB);
            }

        } catch (Exception e) {
            Log.e("ExportDB", e.toString());

        }
    }

    //exporting database
    public static void autoExportDB() {
        String dateTimeString = formatDateToFileName();
        String dbFileName = "geotrackshare_auto_backup" + ".db";
        backupDB = new File(backupDBonlyPath, dbFileName);

        if (!appDir.exists() && !appDir.isDirectory()) {
            // create empty directory
            if (appDir.mkdirs()) {
                Log.i("CreateDir", "App dir created");
            } else {
                Log.w("CreateDir", "Unable to create app dir!");
            }
        } else {
            Log.i("CreateDir", "App dir already exists");
        }

        try {

            if (sd.canWrite()) {

                copyFile(appDB, backupDB);
            }

        } catch (Exception e) {
            Log.e("ExportDB", e.toString());

        }
    }


    private static void copyFile(File src, File dst) throws IOException {
        FileChannel inChannel = new FileInputStream(src).getChannel();
        FileChannel outChannel = new FileOutputStream(dst).getChannel();

        String mime = URLConnection.guessContentTypeFromStream(new FileInputStream(dst));
        if (mime == null) mime = URLConnection.guessContentTypeFromName(dst.getName());

        try {
            inChannel.transferTo(0, inChannel.size(), outChannel);
        } finally {
            if (inChannel != null)
                inChannel.close();
            if (outChannel != null)
                outChannel.close();
        }
    }

    /**
     * Given an SQLite database file, this checks if the file
     * is a valid SQLite database and that it contains all the
     * columns represented by DbAdapter.ALL_COLUMN_KEYS
     **/
    protected static boolean checkDbIsValid(File db, Context ctx) {

        try {
            SQLiteDatabase sqlDb = SQLiteDatabase.openDatabase
                    (db.getPath(), null, SQLiteDatabase.OPEN_READONLY);

            Cursor cursorBackupPost = sqlDb.query(true, TABLE_NAME_POST_TRACKING,
                    null, null, null, null, null, null, null
            );
            Cursor cursorBackup = sqlDb.query(true, TABLE_NAME_TRACKING,
                    null, null, null, null, null, null, null
            );

            /* Delete old track data because we don't need to keep multiple tracks' data */
            Cursor cursorLocal = ctx.getContentResolver().query(
                    CONTENT_URI, null, null, null, null);

            String[] columnNamesLocal = cursorLocal.getColumnNames();
            // ALL_COLUMN_KEYS should be an array of keys of essential columns.
            // Throws exception if any column is missing
            for (String s : columnNamesLocal) {
                cursorBackup.getColumnIndexOrThrow(s);
            }
            sqlDb.close();
            cursorBackup.close();
            cursorBackupPost.close();
            cursorLocal.close();
        } catch (IllegalArgumentException e) {
            Log.d(TAG, "Database valid but not the right type");
            e.printStackTrace();
            return false;
        } catch (SQLiteException e) {
            Log.d(TAG, "Database file is invalid.");
            e.printStackTrace();
            return false;
        } catch (Exception e) {
            Log.d(TAG, "checkDbIsValid encountered an exception");
            e.printStackTrace();
            return false;
        }

        return true;
    }

    /**
     * Returns whether an SD card is present and writable
     **/
    public static boolean SdIsPresent() {
        return Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED);
    }

    /**
     * Reads backup Database and records in local/app database
     *
     * @param sqlDbBackup              Reads backup Database
     * @param TABLE_NAME_POST_TRACKING string to backup Table
     **/

    public static ContentValues[] readTableToArrayPost(SQLiteDatabase sqlDbBackup, String TABLE_NAME_POST_TRACKING) {

        ArrayList<ContentValues> tracksArrayList = null;
//    ContentValues[] trackValuesPost = new ContentValues[getTableLinesCount(sqlDbBackup, TABLE_NAME_POST_TRACKING)];

        try {

            Cursor curBackUp = sqlDbBackup.query(true, TABLE_NAME_POST_TRACKING,
                    null, null, null, null, null, null, null
            );

            tracksArrayList = new ArrayList<>();

            if (curBackUp != null && curBackUp.moveToFirst()) {
                while (curBackUp.moveToNext()) {

                    int runId = curBackUp.getInt(curBackUp.getColumnIndex(COLUMN_RUN_IDP));
                    long startTime = curBackUp.getLong(curBackUp.getColumnIndex(COLUMN_START_TIMEP));
                    long stopTime = curBackUp.getLong(curBackUp.getColumnIndex(COLUMN_STOP_TIMEP));
                    int runType = curBackUp.getInt(curBackUp.getColumnIndex(COLUMN_RUNTYPEP));
                    double totalDistance = curBackUp.getDouble(curBackUp.getColumnIndex(COLUMN_TOTAL_DISTANCEP));
                    double maxAlt = curBackUp.getDouble(curBackUp.getColumnIndex(COLUMN_MAX_ALTP));
                    double maxSpeed = curBackUp.getDouble(curBackUp.getColumnIndex(COLUMN_MAX_SPEEDP));
                    double avgSpeed = curBackUp.getDouble(curBackUp.getColumnIndex(COLUMN_AVR_SPEEDP));
                    long timeCount = curBackUp.getLong(curBackUp.getColumnIndex(COLUMN_TIME_COUNTERP));
                    long favourite = curBackUp.getLong(curBackUp.getColumnIndex(COLUMN_FAVORITEP));

                    ContentValues values = new ContentValues();
                    values.put(COLUMN_RUN_IDP, runId);
                    values.put(COLUMN_START_TIMEP, startTime);
                    values.put(COLUMN_STOP_TIMEP, stopTime);
                    values.put(COLUMN_RUNTYPEP, runType);
                    values.put(COLUMN_TOTAL_DISTANCEP, totalDistance);
                    values.put(COLUMN_MAX_ALTP, maxAlt);
                    values.put(COLUMN_MAX_SPEEDP, maxSpeed);
                    values.put(COLUMN_AVR_SPEEDP, avgSpeed);
                    values.put(COLUMN_TIME_COUNTERP, timeCount);
                    values.put(COLUMN_FAVORITEP, favourite);

                    Log.i("Bulkinsert", String.valueOf(values));
                    tracksArrayList.add(values);
                }
            }
            if (curBackUp != null) {
                curBackUp.close();
            }

        } catch (Exception e) {
            Log.e("Path Error", e.toString());
        }

        ContentValues[] tracksArrayPost = new ContentValues[tracksArrayList.size()];
        tracksArrayPost = tracksArrayList.toArray(tracksArrayPost);

        return tracksArrayPost;

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        //creating a new folder for the database to be backuped to
    }

    /**
     * Reads backup Database and records in local/app database
     *
     * @param sqlDbBackup         Reads backup Database
     * @param TABLE_NAME_TRACKING string to backup Table
     **/

    public static ContentValues[] readTableToArray(SQLiteDatabase sqlDbBackup, String TABLE_NAME_TRACKING) {

        ArrayList<ContentValues> tracksArrayList = null;
//    ContentValues[] trackValuesPost = new ContentValues[getTableLinesCount(sqlDbBackup, TABLE_NAME_POST_TRACKING)];

        try {

            Cursor curBackUp = sqlDbBackup.query(true, TABLE_NAME_TRACKING,
                    null, null, null, null, null, null, null
            );

            tracksArrayList = new ArrayList<>();

            if (curBackUp != null && curBackUp.moveToFirst()) {
                while (curBackUp.moveToNext()) {

                    int runId = curBackUp.getInt(curBackUp.getColumnIndex(COLUMN_RUN_ID));
                    int runType = curBackUp.getInt(curBackUp.getColumnIndex(COLUMN_RUNTYPE));
                    long time = curBackUp.getLong(curBackUp.getColumnIndex(COLUMN_TIME));
                    double latitude = curBackUp.getDouble(curBackUp.getColumnIndex(COLUMN_LATITUDE));
                    double longitude = curBackUp.getDouble(curBackUp.getColumnIndex(COLUMN_LONGITUDE));
                    double altitude = curBackUp.getDouble(curBackUp.getColumnIndex(COLUMN_ALTITUDE));
                    double maxAlt = curBackUp.getDouble(curBackUp.getColumnIndex(COLUMN_MAX_ALT));
                    double minAlt = curBackUp.getDouble(curBackUp.getColumnIndex(COLUMN_MIN_ALT));
                    double speed = curBackUp.getDouble(curBackUp.getColumnIndex(COLUMN_SPEED));
                    double maxSpeed = curBackUp.getDouble(curBackUp.getColumnIndex(COLUMN_MAX_SPEED));
                    double avgSpeed = curBackUp.getDouble(curBackUp.getColumnIndex(COLUMN_AVR_SPEED));
                    long timeCounter = curBackUp.getLong(curBackUp.getColumnIndex(COLUMN_TIME_COUNTER));
                    double distance = curBackUp.getDouble(curBackUp.getColumnIndex(COLUMN_DISTANCE));
                    double totalDistance = curBackUp.getDouble(curBackUp.getColumnIndex(COLUMN_TOTAL_DISTANCE));
                    double moveDistance = curBackUp.getDouble(curBackUp.getColumnIndex(COLUMN_MOVE_DISTANCE));
                    double moveClose = curBackUp.getDouble(curBackUp.getColumnIndex(COLUMN_MOVE_CLOSE));
                    long startTime = curBackUp.getLong(curBackUp.getColumnIndex(COLUMN_START_TIME));

                    ContentValues values = new ContentValues();
                    values.put(COLUMN_RUN_ID, runId);
                    values.put(COLUMN_RUNTYPE, runType);
                    values.put(COLUMN_TIME, time);
                    values.put(COLUMN_LATITUDE, latitude);
                    values.put(COLUMN_LONGITUDE, longitude);
                    values.put(COLUMN_ALTITUDE, altitude);
                    values.put(COLUMN_MAX_ALT, maxAlt);
                    values.put(COLUMN_MIN_ALT, minAlt);
                    values.put(COLUMN_SPEED, speed);
                    values.put(COLUMN_MAX_SPEED, maxSpeed);
                    values.put(COLUMN_AVR_SPEED, avgSpeed);
                    values.put(COLUMN_TIME_COUNTER, timeCounter);
                    values.put(COLUMN_DISTANCE, distance);
                    values.put(COLUMN_TOTAL_DISTANCE, totalDistance);
                    values.put(COLUMN_MOVE_DISTANCE, moveDistance);
                    values.put(COLUMN_MOVE_CLOSE, moveClose);
                    values.put(COLUMN_START_TIME, startTime);

                    Log.i("Bulkinsert", String.valueOf(values));
                    tracksArrayList.add(values);
                }
            }
            if (curBackUp != null) {
                curBackUp.close();
            }

        } catch (Exception e) {
            Log.e("Path Error", e.toString());
        }

        ContentValues[] tracksArray = new ContentValues[tracksArrayList.size()];
        tracksArray = tracksArrayList.toArray(tracksArray);

        return tracksArray;

    }

    /**
     * Replaces current database with the backupDB if
     * import database is valid and of the correct type
     **/
    protected boolean restoreDb(Context ctx) {

        if (!SdIsPresent()) return false;

        if (!checkDbIsValid(backupDBwithFIle, ctx)) return false;

        if (!backupDBwithFIle.exists()) {
            Log.d(TAG, "File does not exist");
            return false;
        }

        try {
            appDB.createNewFile();
            copyFile(backupDBwithFIle, appDB);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public int getTableLinesCount(SQLiteDatabase database, String tableName) {
        String countQuery = "SELECT  * FROM " + tableName;
        Cursor cursor = database.rawQuery(countQuery, null);
        int count = cursor.getCount();
        cursor.close();
        return count;
    }


    public static void uploadToFirebaseStorage() {


//        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
//        if(user == null){
//            //if user is not authenticated show authentication screen
//            Intent i = new Intent();
//            i.setClass(context, EmailPasswordAuthActivity.class);
//            startActivity(i);
//        }


        FirebaseStorage storage = FirebaseStorage.getInstance();
        Uri file = Uri.fromFile(appDB);
        // Create a storage reference from our app
        StorageReference storageRef = storage.getReference();
        StorageReference uploadRef = storageRef.child("Database1779/" + file.getLastPathSegment());

        UploadTask uploadTask = uploadRef.putFile(file);

        // Register observers to listen for when the download is done or if it fails
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle unsuccessful uploads
                Log.i(TAG, "uploadToFirebaseStorage:" + " unsuccessful upload");
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, etc.
                Log.i(TAG, "uploadToFirebaseStroage: " + "Successful upload");
            }
        });
        // [END upload_file]
    }

    public void googleSignIn() {
        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
    }

    public void upload_to_drive() {

        //async check if folder exists... if not, create it. continue after with create_file_in_folder(driveId);
        check_folder_exists();
    }

    private void check_folder_exists() {
        Query query =
                new Query.Builder().addFilter(Filters.and(Filters.eq(SearchableField.TITLE, FOLDER_NAME), Filters.eq(SearchableField.TRASHED, false)))
                        .build();
        Drive.DriveApi.query(mGoogleApiClient, query).setResultCallback(new ResultCallback<DriveApi.MetadataBufferResult>() {
            @Override
            public void onResult(DriveApi.MetadataBufferResult result) {
                if (!result.getStatus().isSuccess()) {
                    Log.e(TAG, "Cannot create folder in the root.");
                } else {
                    boolean isFound = false;
                    for (Metadata m : result.getMetadataBuffer()) {
                        if (m.getTitle().equals(FOLDER_NAME)) {
                            Log.e(TAG, "Folder exists");
                            isFound = true;
                            DriveId driveId = m.getDriveId();
                            create_file_in_folder(driveId);
                            break;
                        }
                    }
                    if (!isFound) {
                        Log.i(TAG, "Folder not found; creating it.");
                        MetadataChangeSet changeSet = new MetadataChangeSet.Builder().setTitle(FOLDER_NAME).build();
                        Drive.DriveApi.getRootFolder(mGoogleApiClient)
                                .createFolder(mGoogleApiClient, changeSet)
                                .setResultCallback(new ResultCallback<DriveFolder.DriveFolderResult>() {
                                    @Override
                                    public void onResult(DriveFolder.DriveFolderResult result) {
                                        if (!result.getStatus().isSuccess()) {
                                            Log.e(TAG, "U AR A MORON! Error while trying to create the folder");
                                        } else {
                                            Log.i(TAG, "Created a folder");
                                            DriveId driveId = result.getDriveFolder().getDriveId();
                                            create_file_in_folder(driveId);
                                        }
                                    }
                                });
                    }
                }
            }
        });
    }

    private void create_file_in_folder(final DriveId driveId) {

        Drive.DriveApi.newDriveContents(mGoogleApiClient).setResultCallback(new ResultCallback<DriveApi.DriveContentsResult>() {
            @Override
            public void onResult(@NonNull DriveApi.DriveContentsResult driveContentsResult) {
                if (!driveContentsResult.getStatus().isSuccess()) {
                    Log.e(TAG, "U AR A MORON! Error while trying to create new file contents");
                    return;
                }

                OutputStream outputStream = driveContentsResult.getDriveContents().getOutputStream();

                //------ THIS IS AN EXAMPLE FOR PICTURE ------
                //ByteArrayOutputStream bitmapStream = new ByteArrayOutputStream();
                //image.compress(Bitmap.CompressFormat.PNG, 100, bitmapStream);
                //try {
                //  outputStream.write(bitmapStream.toByteArray());
                //} catch (IOException e1) {
                //  Log.i(TAG, "Unable to write file contents.");
                //}
                //// Create the initial metadata - MIME type and title.
                //// Note that the user will be able to change the title later.
                //MetadataChangeSet metadataChangeSet = new MetadataChangeSet.Builder()
                //    .setMimeType("image/jpeg").setTitle("Android Photo.png").build();

                //------ THIS IS AN EXAMPLE FOR FILE --------
                Toast.makeText(getApplicationContext(), "Uploading to drive. If you didn't fucked up something like usual you should see it there", Toast.LENGTH_LONG).show();
//                final File theFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/xtests/tehfile.txt"); //>>>>>> WHAT FILE ?
                try {
                    FileInputStream fileInputStream = new FileInputStream(appDB);
                    byte[] buffer = new byte[1024];
                    int bytesRead;
                    while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, bytesRead);
                    }
                } catch (IOException e1) {
                    Log.i(TAG, "U AR A MORON! Unable to write file contents.");
                }

                MetadataChangeSet changeSet = new MetadataChangeSet.Builder().setTitle(backupDB.getName()).setMimeType("text/plain").setStarred(false).build();
                DriveFolder folder = driveId.asDriveFolder();
                folder.createFile(mGoogleApiClient, changeSet, driveContentsResult.getDriveContents())
                        .setResultCallback(new ResultCallback<DriveFolder.DriveFileResult>() {
                            @Override
                            public void onResult(@NonNull DriveFolder.DriveFileResult driveFileResult) {
                                if (!driveFileResult.getStatus().isSuccess()) {
                                    Log.e(TAG, "U AR A MORON!  Error while trying to create the file");
                                    return;
                                }
                                Log.v(TAG, "Created a file: " + driveFileResult.getDriveFile().getDriveId());
                            }
                        });
            }
        });
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.v(TAG, "+++++++++++++++++++ onConnected +++++++++++++++++++");
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.e(TAG, "onConnectionSuspended [" + String.valueOf(i) + "]");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.i(TAG, "GoogleApiClient connection failed: " + connectionResult.toString());
        if (!connectionResult.hasResolution()) {
            // show the localized error dialog.
            GoogleApiAvailability.getInstance().getErrorDialog(this, connectionResult.getErrorCode(), 0).show();
            return;
        }
        try {
            connectionResult.startResolutionForResult(this, REQUEST_CODE_RESOLUTION);
        } catch (IntentSender.SendIntentException e) {
            Log.e(TAG, "U AR A MORON! Exception while starting resolution activity", e);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_RESOLUTION && resultCode == RESULT_OK) {
            mGoogleApiClient.connect();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this).addApi(Drive.API)
                    .addScope(Drive.SCOPE_FILE)
                    .addScope(Drive.SCOPE_APPFOLDER) // required for App Folder sample
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();
        }
        mGoogleApiClient.connect();
    }

    // [START upload_file]

    @Override
    protected void onPause() {
        if (mGoogleApiClient != null) {
            mGoogleApiClient.disconnect();
        }
        super.onPause();
    }

}