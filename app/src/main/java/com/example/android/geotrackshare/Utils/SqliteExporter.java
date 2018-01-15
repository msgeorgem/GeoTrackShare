package com.example.android.geotrackshare.Utils;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.android.geotrackshare.Data.TrackContract;
import com.example.android.geotrackshare.TrackList.RunListFragment;
import com.example.android.geotrackshare.TrackList.TracksCursorAdapter;
import com.opencsv.CSVWriter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static com.example.android.geotrackshare.Data.TrackContract.TrackingEntry.CONTENT_URI;

/**
 * Can export an sqlite databse into a csv file.
 * <p>
 * The file has on the top dbVersion and on top of each table data the name of the table
 * <p>
 * Inspired by
 * https://stackoverflow.com/questions/31367270/exporting-sqlite-database-to-csv-file-in-android
 * and some other SO threads as well.
 */
public class SqliteExporter {
    public static final String DB_BACKUP_DB_VERSION_KEY = "dbVersion";
    public static final String DB_BACKUP_TABLE_NAME = "table";
    private static final String TAG = SqliteExporter.class.getSimpleName();


    public static String export(SQLiteDatabase db, int runId) throws IOException {
        if (!RunListFragment.isExternalStorageWritable()) {
            throw new IOException("Cannot write to external storage");
        }


        String fileName = TracksCursorAdapter.fileName;
        File backupDirEXT = new File(RunListFragment.mContext.getExternalCacheDir(), fileName);
//       file saved on internal drive will not be read by gmail without permissions
        File backupFileINT = new File(RunListFragment.mContext.getFilesDir(), fileName);

        boolean success = backupDirEXT.createNewFile();
        if (!success) {
            throw new IOException("Failed to create the backup file");
        }
        Log.d(TAG, "Started to fill the backup file in " + backupDirEXT.getAbsolutePath());
        long starTime = System.currentTimeMillis();
        writeCsv(backupDirEXT, db, runId);
        long endTime = System.currentTimeMillis();
        Log.d(TAG, "Creating backup took " + (endTime - starTime) + "ms.");

        return backupDirEXT.getAbsolutePath();
    }

    public static String createBackupFileName(int runID) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HHmm");
        return "Run_No:" + runID + "_backup_" + sdf.format(new Date()) + ".csv";
    }

    public static String createBackupFileName(Long runID) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HHmm");
        return "backup_run_" + runID + "_" + sdf.format(new Date()) + ".csv";
    }


    private static void writeCsv(File backupFile, SQLiteDatabase db, int runId) {
        CSVWriter csvWrite = null;
        Cursor curCSV = null;
        String tableName = TrackContract.TrackingEntry.TABLE_NAME;
        String mSelectionClause = TrackContract.TrackingEntry.COLUMN_RUN_ID;
        String SELECTION = mSelectionClause + " = '" + runId + "'";
        try {
            csvWrite = new CSVWriter(new FileWriter(backupFile));
            writeSingleValue(csvWrite, DB_BACKUP_DB_VERSION_KEY + "=" + db.getVersion());
            writeSingleValue(csvWrite, DB_BACKUP_TABLE_NAME + "=" + tableName);
            curCSV = RunListFragment.mContext.getContentResolver()
                    .query(CONTENT_URI, null, SELECTION, null, null);

            assert curCSV != null;
            csvWrite.writeNext(curCSV.getColumnNames());
            while (curCSV.moveToNext()) {
                int columns = curCSV.getColumnCount();
                String[] columnArr = new String[columns];
                for (int i = 0; i < columns; i++) {
                    columnArr[i] = curCSV.getString(i);
                }
                csvWrite.writeNext(columnArr);
            }

        } catch (Exception sqlEx) {
            Log.e(TAG, sqlEx.getMessage(), sqlEx);
        } finally {
            if (csvWrite != null) {
                try {
                    csvWrite.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (curCSV != null) {
                curCSV.close();
            }
        }
    }

    private static void writeSingleValue(CSVWriter writer, String value) {
        writer.writeNext(new String[]{value});
    }
}