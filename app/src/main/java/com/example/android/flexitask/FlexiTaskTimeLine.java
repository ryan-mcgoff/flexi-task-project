package com.example.android.flexitask;


import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;

import com.example.android.flexitask.data.taskContract;
import com.example.android.flexitask.data.taskDBHelper;
import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;

import java.util.Calendar;


/**
 * Created by Ryan Mcgoff (4086944), Jerry Kumar (3821971), Jaydin Mcmullan (9702973)
 *
 * A {@link Fragment} subclass for the flexi-task fragment of the app
 * that implements the {@link LoaderManager] interface to pass flexi task data to the a cursor
 * adaptor for the fragment's listview..
 *
 * Code for floating ActionButton, including animations and design from GitHub
 * https://github.com/Clans/FloatingActionButton
 *
 */
public class FlexiTaskTimeLine extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int TASKLOADER = 0;
    TaskCursorAdaptor mTaskCursorAdaptor;

    /**
     * Floating Action Buttons from
     * https://github.com/Clans/FloatingActionButton
     */
    private FloatingActionButton mFabFixedTask;
    private FloatingActionButton mFabFlexi;
    private FloatingActionMenu mFabMenu;

    /**
     * Database helper that provides access to the database
     */
    private taskDBHelper mDbHelper;

    /*ID of list item clicked*/
    private long item_iD;

    private Toolbar bottomBar;

    private boolean toolBarShown;
    private int lastClickedPostion;
    private long lastClickedID;


    public FlexiTaskTimeLine() {
        //empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //inflates the XML layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_flexi_task_timeline, container, false);

        // Find the ListView which will be populated with the tasks data
        final ListView timeLineListView = (ListView) rootView.findViewById(R.id.timelineListView);

        mFabMenu = (FloatingActionMenu) rootView.findViewById(R.id.menu);

        mDbHelper = new taskDBHelper(getActivity());

        // Find and set empty view on the ListView, so that it shows a message when the list has 0 items.
        View emptyView = rootView.findViewById(R.id.empty_view);

        timeLineListView.setEmptyView(emptyView);

        //sets up an Cursoradaptor for the listview
        //The adaptor creates a list item for each row in the returned cursor
        // that it is given in the (OnLoaderFinished)method
        mTaskCursorAdaptor = new TaskCursorAdaptor(getActivity(), null);
        timeLineListView.setAdapter(mTaskCursorAdaptor);

        bottomBar = rootView.findViewById(R.id.toolbar);


        mFabFixedTask = (FloatingActionButton) rootView.findViewById(R.id.fixedTaskButton);
        mFabFixedTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), FixedTaskEditor.class);
                mFabMenu.close(false);
                startActivity(intent);
            }
        });
        mFabFlexi = (FloatingActionButton) rootView.findViewById(R.id.flexiTaskButton);
        mFabFlexi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), FlexiTaskEditor.class);
                mFabMenu.close(false);
                startActivity(intent);
            }
        });


        //gets postion and row ID details for seleted list_item
        timeLineListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                item_iD = id;
                if (toolBarShown == false) {
                    lastClickedID = id;
                    lastClickedPostion = position;

                    timeLineListView.setItemChecked(position, true);


                    getActivity().setTitle("task selected");
                    toolBarShown = true;
                    mFabMenu.setVisibility(View.GONE);
                    mFabMenu.close(false);

                    bottomBar.setVisibility(View.VISIBLE);
                } else {
                    lastClickedID = id;
                    lastClickedPostion = position;
                    timeLineListView.setItemChecked(position, false);

                    getActivity().setTitle("FlexiTask");

                    resetUI();

                }

            }
        });


            /*START OF TOOLBAR BUTTONS*/


        /**EDITING BUTTON -
         * gets the URI for the selected list item, and sends it to the editor activity for processing.
         */
        ImageView editButtonToolBar = bottomBar.findViewById(R.id.edit);
        editButtonToolBar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), FlexiTaskEditor.class);

                timeLineListView.setItemChecked(lastClickedPostion, false);
                resetUI();
                Uri currentTaskUri = ContentUris.withAppendedId(taskContract.TaskEntry.CONTENT_URI, item_iD);

                //sets the URI for that intent
                intent.setData(currentTaskUri);

                startActivity(intent);
            }
        });


        /** DONE BUTTON -
         * When the done button (tick) on the toolbar has been selected, the app updates
         * the last_completed_date field for that task/listitem to todays date
         */
        ImageView doneButtonToolBar = bottomBar.findViewById(R.id.done);
        doneButtonToolBar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SQLiteDatabase db = mDbHelper.getWritableDatabase();

                //db.query()
                Cursor cursorc = db.rawQuery("SELECT * FROM " + taskContract.TaskEntry.TABLE_NAME +
                        " WHERE " + taskContract.TaskEntry._ID + " = " + lastClickedID, null);
                if (cursorc.moveToFirst()) {
                    Calendar c = Calendar.getInstance();
                    c.set(Calendar.HOUR_OF_DAY, 0);
                    c.set(Calendar.MINUTE, 0);
                    c.set(Calendar.SECOND, 0);

                    long todayDate = c.getTimeInMillis();

                    ContentValues cv = new ContentValues();
                    cv.put(taskContract.TaskEntry.COLUMN_LAST_COMPLETED, String.valueOf(todayDate));
                    db.update(taskContract.TaskEntry.TABLE_NAME, cv, taskContract.TaskEntry._ID
                            + " = " + lastClickedID, null);
                }

                mTaskCursorAdaptor.notifyDataSetChanged();

                timeLineListView.setItemChecked(lastClickedPostion, false);
                resetUI();
                getLoaderManager().restartLoader(TASKLOADER, null, FlexiTaskTimeLine.this);


            }
        });

            /* DELETE BUTTON -
             * gets the URI for the selected listitem, and deletes it by calling cthe content resolver
             */
        ImageView deleteButtonToolBar = bottomBar.findViewById(R.id.delete);
        deleteButtonToolBar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //creates a URI for the specific task that was clicked on
                //ie: task on row three
                //would be "content.example.android.flexitask/task" + "3" (the ID)
                timeLineListView.setItemChecked(lastClickedPostion, false);
                resetUI();
                Uri currentTaskUri = ContentUris.withAppendedId(taskContract.TaskEntry.CONTENT_URI, item_iD);
                getActivity().getContentResolver().delete(currentTaskUri, null, null);


            }
        });

            /*END OF TOOLBAR BUTTONS*/

        //when this fragment is first created, it will initalise the loader, which calls the OnCreate
        // loader method which in turn retrieves data from the database
        getLoaderManager().initLoader(TASKLOADER, null, this);

        setHasOptionsMenu(true);

        return rootView;

    }

    /**
     * Method for deleting all the tasks UI. Calls the content resolver, which
     * matches the URI with the contentProvider interface (TaskProvider) that preforms the delete method
     * on the database
     */
    public void deleteAllTasks() {
        int rowsDeleted = getActivity().getContentResolver().delete(taskContract.TaskEntry.CONTENT_URI, null, null);
        Log.v("CatalogActivity", rowsDeleted + " rows deleted from the database");
    }

    /**
     * Method for reseting UI. Sets title back to the name of the app, and hides
     * the tool bar and shows the floatingActionButton
     */
    public void resetUI() {
        getActivity().setTitle("FlexiTask");
        bottomBar.setVisibility(View.GONE);
        mFabMenu.setVisibility(View.VISIBLE);
        toolBarShown = false;
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        inflater.inflate(R.menu.timeline_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.deleteAll:
                //FOR DEBUGGING PURPOSES
                bottomBar.setVisibility(View.GONE);
                mFabMenu.setVisibility(View.VISIBLE);
                deleteAllTasks();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    /**
     * This onCreateLoader method creates a new Cursor that contains the URI for the database and the columns wanted. It's passed
     * to the content resolver which uses the URI to determine which contentProvider we want to interface with
     * The contentProvider is then responsible for interfacing with the database and returning a cursor
     * (with the requested data from our projection) back to the contentResolver, and finally back to the
     * loadermanager. The loader manager then passes
     * this cursor object to the {@link #onLoadFinished(Loader, Cursor)} .
     *
     * @param id   the loader's ID
     * @param args any arguments you want to pass to the loader when creating it (NOT USED)
     * @return new cursorLoader with SQL projection and URI for the database we want to query. (this is passed to the cotent
     * resolver by the loaderManager)
     */
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        //new
        mTaskCursorAdaptor.notifyDataSetChanged();
        //Define a projection that specifies which columns from the database
        // you will actually use after this query.

        String[] projection = {
                taskContract.TaskEntry._ID,
                taskContract.TaskEntry.COLUMN_TASK_TITLE,
                taskContract.TaskEntry.COLUMN_DESCRIPTION,
                taskContract.TaskEntry.COLUMN_TYPE_TASK,
                taskContract.TaskEntry.COLUMN_DATE,
                taskContract.TaskEntry.COLUMN_LAST_COMPLETED,
                taskContract.TaskEntry.COLUMN_TIME,
                taskContract.TaskEntry.COLUMN_HISTORY,
                taskContract.TaskEntry.COLUMN_STATUS,
                taskContract.TaskEntry.COLUMN_RECCURING_PERIOD};


        String WHERE = "task_type='1'";
        Calendar cTodayDate = Calendar.getInstance();
        cTodayDate.set(Calendar.HOUR_OF_DAY, 0);
        cTodayDate.set(Calendar.MINUTE, 0);
        cTodayDate.set(Calendar.SECOND, 0);
        cTodayDate.set(Calendar.MILLISECOND, 0);
        long todayDate = cTodayDate.getTimeInMillis();
        String todayDateString = String.valueOf(todayDate);

        // Perform a query on the tasks table
        //Executes the TaskProvider query method on a background thread
        //uses our prority algorithm as the sort method
        return new CursorLoader(getActivity(),
                taskContract.TaskEntry.CONTENT_URI,
                projection,
                WHERE,
                null,
                "(((" + "(CAST(" + todayDateString + " AS DOUBLE))" + " - " + taskContract.TaskEntry.COLUMN_LAST_COMPLETED +
                        ") / CAST(86400000 AS DOUBLE) ) + 1)" + " / " +
                        taskContract.TaskEntry.COLUMN_RECCURING_PERIOD + " DESC");
    }


    /**
     * Once the cursor loader set up in {@link #onCreateLoader(int, Bundle)} has been given the
     * returned data cursor, the {@link LoaderManager} calls this method with the data.
     * This method then gives the cursor adaptor the data to process
     *
     * @param loader the cursor loader
     * @param data   the returned cursor with the requested data
     */
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        //updates the Listview's cursorAdaptor with new data from database
        mTaskCursorAdaptor.swapCursor(data);

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        //when the data needs to be deleted
        mTaskCursorAdaptor.swapCursor(null);
    }
}

