package com.example.android.flexitask.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

/**
 * Created by Ryan Mcgoff (4086944), Jerry Kumar (3821971), Jaydin Mcmullan (9702973)
 *
 * Provides the an interface with the Task database
 */

public class TaskProvider extends ContentProvider {

    /* Tag for lgging purposes */
    private static final String LOG_TAG = TaskProvider.class.getSimpleName();

    /*Database*/
    private taskDBHelper mdbHelper;

    /** URI matcher code for the content URI for retrieving everything in the table */
    private static final int TASKS = 100;

    /** URI matcher code for the content URI for an individual task from the table */
    private static final int TASK_ID = 101;


    /** UriMatcher object to match a content URI to a corresponding code */
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);


    /*
     * Static initializer is run the first time anything is called from this class.
     * This prevents the program from setting up the URI matcher multiple times
     * The initializer adds paths to the sUriMatcher that have a corresponding code to return (ie: TASKS)
     * upon finding a match
     *
     */
    static {

        sUriMatcher.addURI(taskContract.CONTENT_AUTHORITY,taskContract.PATH_Tasks,TASKS);

        sUriMatcher.addURI(taskContract.CONTENT_AUTHORITY,taskContract.PATH_Tasks +"/#",TASK_ID);

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onCreate() {
        mdbHelper = new taskDBHelper(getContext());
        return true;
    }


    /**
     * {@inheritDoc}
     */
    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {

        //readable database
        SQLiteDatabase database = mdbHelper.getReadableDatabase();

        // Cusor to hold the result of the query
        Cursor cursor = null;

        /*Match the given URI code with the URI matcher to see if we're dealing with the whole table or a single row*/
        int uriCode = sUriMatcher.match(uri);

        switch (uriCode) {


            case TASKS:
                //returns a cursor holding information for the entire table
                cursor = database.query(taskContract.TaskEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);

                break;


            case TASK_ID:

                //for every "?" in the selection string there needs to be an argument in the
                //selection array
                selection = taskContract.TaskEntry._ID + "=?";

                //Extracts the row number ID from the end of the URI (1 argument)
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };

                //returns a cursor holding information for the given rowID
                cursor = database.query(taskContract.TaskEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);

                break;

            default:

                throw new IllegalArgumentException("Query failed, URI not valid " + uri);

        }
        /*registers observer for the cursor and corrosponding URI that the app can notify when changes are made to the database*/
        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;
    }



    /**
     * {@inheritDoc}
     */
    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        final int uriCode = sUriMatcher.match(uri);
        switch (uriCode) {
            case TASKS:
                return taskContract.TaskEntry.CONTENT_LIST_TYPE;
            case TASK_ID:
                return taskContract.TaskEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Invalid URI");
        }
    }



    /**
     * {@inheritDoc}
     */
    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {

        /* DATA VALIDATION
        * Checks to see if there is a title
        */
        String title = values.getAsString(taskContract.TaskEntry.COLUMN_TASK_TITLE);
        if (TextUtils.isEmpty(title)) {

            Log.e(LOG_TAG, "Enter Name for row " + uri);
            throw new IllegalArgumentException("Task requires a title");

        }

        int uriCode = sUriMatcher.match(uri);

        switch (uriCode){
            case TASKS:
                return insertTask(uri,values);
            default:
                throw new IllegalArgumentException("Query failed, URI wasn't for the whole table");

        }
    }


    /**
    * Helper insert Method for {@link TaskProvider#insert(Uri, ContentValues)}
    * creates a writeable version of the databae and inserts the contentValues into the table
    *
    * @param uri so the app can notify the corresponding cursor observer of the recent changes
    * @param values containe both data for the task and the column names to insert those values into
    * @return URI for the newly inserted row
    *
    */
    private Uri insertTask(Uri uri, ContentValues values){
        // Get writeable database
        SQLiteDatabase database = mdbHelper.getWritableDatabase();
        // Insert the new Task with the provided content values
        long id = database.insert(taskContract.TaskEntry.TABLE_NAME, null, values);

        // If the ID is -1, then the insertion didn't work. Log an error and return null to exit.
        if (id == -1) {
            Log.e(LOG_TAG, "row " + uri +" failed to insert");
            return null;
        }
        // Notify all listeners that the data has changed
        getContext().getContentResolver().notifyChange(uri, null);


        //returns the new URI
        return ContentUris.withAppendedId(uri,id);

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        // Get database
        SQLiteDatabase database = mdbHelper.getWritableDatabase();
        //A count of the rows which are deleted
        int numberOfdeletedRows;

        final int uriCode = sUriMatcher.match(uri);
        switch (uriCode) {
            case TASKS:
                // Delete all rows that match the selection and selection args
                numberOfdeletedRows= database.delete(taskContract.TaskEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case TASK_ID:
                // Delete a single row given by the ID in the URI
                selection = taskContract.TaskEntry._ID + "=?";
                //gets the ID of the URI
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                numberOfdeletedRows = database.delete(taskContract.TaskEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("URI invalid for deletion " + uri);
        }
        //if 1 or more rows are then notify content observer
        if(numberOfdeletedRows!=0){
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return numberOfdeletedRows;

    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        /* DATA VALIDATION
        *Check that the task title is not null */
        String title = values.getAsString(taskContract.TaskEntry.COLUMN_TASK_TITLE);
        if (TextUtils.isEmpty(title)) {
            return 0;
        }
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case TASKS:
                return updateTask(uri, values, selection, selectionArgs);
            case TASK_ID:
                selection = taskContract.TaskEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                return updateTask(uri, values, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update not available for " + uri);
        }

    }

    /**
    * Helper update Method for {@link TaskProvider#update(Uri, ContentValues, String, String[])}
    * creates a writeable version of the databae and updates the given contentValues for the row
    * provides in the selectionARGS array
    *
    * @param uri so the app can notify the corresponding cursor observer of the recent changes
    * @param selection the column you are selecting (ID)
    * @param selectionArgs the value you are selecting by (ie: ID = 3)
    * @param values set of column_name/value pairs to update in the database
    * @return int the number of rows updated
    *
    */
    private int updateTask(Uri uri, ContentValues values, String selection, String[] selectionArgs){

        //Database to write to
        SQLiteDatabase database = mdbHelper.getWritableDatabase();

        int numberUpdatedRows = database.update(taskContract.TaskEntry.TABLE_NAME, values, selection, selectionArgs);
        //If a row is updated, notify the content observor
        if(numberUpdatedRows!=0){
            getContext().getContentResolver().notifyChange(uri, null);
        }
        //return number of rows updated
        return numberUpdatedRows;

    }
}
