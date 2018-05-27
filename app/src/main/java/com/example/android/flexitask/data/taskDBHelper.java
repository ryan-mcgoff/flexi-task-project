package com.example.android.flexitask.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Ryan Mcgoff (4086944), Jerry Kumar (3821971), Jaydin Mcmullan (9702973)
 */

public class taskDBHelper extends SQLiteOpenHelper {

    //To help with identifying log messages
    public static final String LOG_TAG = taskDBHelper.class.getSimpleName();

    /** Name of the database file */
    private static final String DATABASE_NAME = "taskDataBase.db";
    /**
     * Database version. If you change the database schema, you must increment the database version.
     */
    private static final int DATABASE_VERSION = 1;


    /**
     * Constructs a new instance of {@link taskDBHelper}
     * calls the SQLiteOpenHelper database superclass.
     *
     * @param context that the app is in
     */
    public taskDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**This is called when the database is created for the very first time
     * @param db
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        //This string is used to create the table
        String SQL_CREATE_TASK_DATABASE = "CREATE TABLE " + taskContract.TaskEntry.TABLE_NAME + " ("
                + taskContract.TaskEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + taskContract.TaskEntry.COLUMN_TASK_TITLE + " TEXT NOT NULL, "
                + taskContract.TaskEntry.COLUMN_DESCRIPTION + " TEXT, "
                + taskContract.TaskEntry.COLUMN_DATE + " INTEGER DEFAULT 0, "
                + taskContract.TaskEntry.COLUMN_LAST_COMPLETED + " INTEGER DEFAULT 0, "
                + taskContract.TaskEntry.COLUMN_TIME + " TEXT NOT NULL DEFAULT 0, "
                + taskContract.TaskEntry.COLUMN_RECCURING_PERIOD + " INTEGER NOT NULL DEFAULT 0, "
                + taskContract.TaskEntry.COLUMN_HISTORY + " TEXT, "
                + taskContract.TaskEntry.COLUMN_TYPE_TASK + " INTEGER NOT NULL, "
                + taskContract.TaskEntry.COLUMN_STATUS + " INTEGER NOT NULL);";
        db.execSQL(SQL_CREATE_TASK_DATABASE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //not used
    }
}
