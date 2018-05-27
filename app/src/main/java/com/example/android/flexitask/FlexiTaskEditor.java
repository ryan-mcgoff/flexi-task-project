package com.example.android.flexitask;

import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.flexitask.data.taskContract;
import com.example.android.flexitask.data.taskDBHelper;

import java.text.DateFormat;
import java.util.Calendar;


/**
 * Created by Ryan Mcgoff (4086944), Jerry Kumar (3821971), Jaydin Mcmullan (9702973)
 *
 * The flexitask Editor acts as an editor for both new tasks and an editor updating existing tasks.
 * The editor checks if the intent that started the activity had a URI (.getData()) to determine
 * if the user is trying to create a new task or update an existing task.
 * If it's updating, it uses a Cursorloader to retrieve the data for that task and uses the contentProviders (TaskProvider's)
 * update method instead of insert.
 *
 */
public class FlexiTaskEditor extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    /**
     * EditText field to enter Task's title
     */
    private EditText mtitleEditText;

    /**
     * EditText field to Task's description
     */
    private EditText mDescriptionEditText;

    /**
     * EditText field to enter the custom number of recurring days
     */
    private EditText mCustomRecurring;

    /**
     * EditText field to select how often the task
     */
    private Spinner mRecurringSpinner;

    /**
     * Date
     */
    private Long mDate;

    /**
     * time
     */
    private String mTime;
    /**
     * .....
     */
    private boolean isCustomSpinnerSelected;

    /**
     * Number of Recurring days
     */
    private int mNumberOfRecurringDays = taskContract.TaskEntry.RECURRING_NEVER;

    /**
     * Text count for Task Title
     */
    private TextView textCountTaskTitle;

    /**
     * current URI for editing an existing task
     */
    private Uri uriCurrentTask;

    /**
     * mRecurringDaysEditMode
     */
    private int mRecurringDaysEditMode;

    private long mDateLastCompleted;

    private long mDueDate;


    /**
     * Boolean flag that keeps track of whether the Task has been edited (true) or not (false)
     */
    private boolean mTaskHasChanged = false;

    private final TextWatcher mTextEditorWatcher = new TextWatcher() {
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        public void onTextChanged(CharSequence s, int start, int before, int count) {
            /**This sets a textview to the current length*/
            textCountTaskTitle.setText(String.valueOf(s.length()) + "/30");
        }

        public void afterTextChanged(Editable s) {

        }
    };
    /**
     * Updates the due date as the user inputs how often the task recurs
     */
    private final TextWatcher mCustomDayTextWatcher = new TextWatcher() {
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        public void onTextChanged(CharSequence s, int start, int before, int count) {
            /**This sets a textview to the current length*/
            if (mCustomRecurring.getText().toString().isEmpty()) {
                mNumberOfRecurringDays = 0;
            } else {
                mNumberOfRecurringDays = Integer.parseInt(mCustomRecurring.getText().toString());
            }
            mDueDate = mDateLastCompleted + (86400000L * mNumberOfRecurringDays);
            displayDueDate();
        }

        public void afterTextChanged(Editable s) {

        }
    };

    /**
     * OnTouchListener that listens for any user touches on a View, implying that they are modifying
     * the view, and we change the Task boolean to true.
     */
    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {

        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mTaskHasChanged = true;
            return false;
        }

    };


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flexitask_editor);


        // Find all relevant views that we will need to read user input from
        textCountTaskTitle = (TextView) findViewById(R.id.textCountTaskTitle);
        mtitleEditText = (EditText) findViewById(R.id.taskTitle);
        mRecurringSpinner = (Spinner) findViewById(R.id.recurringSpinner);
        mDescriptionEditText = (EditText) findViewById(R.id.descriptionEditText);
        mCustomRecurring = (EditText) findViewById(R.id.customRecurringText);

        mtitleEditText.addTextChangedListener(mTextEditorWatcher);
        mCustomRecurring.addTextChangedListener(mCustomDayTextWatcher);

        //gets uri the intent that was used to launch this activity, if it's null that
        //means there wasn't one passed to it and the user isn't editing an exisiting
        //task, but creating a new one
        uriCurrentTask = getIntent().getData();


        Spinner spinner = (Spinner) findViewById(R.id.recurringSpinner);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.recurring_options_array_flexiTask, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinner.setAdapter(adapter);

        //if URI is "null" then we know we're creating a new task (FAB). If it isn't null
        //then we know we're editing an existing task
        if (uriCurrentTask == null) {
            setTitle("Create a task");
            //set date last completed to todays date
            Calendar c = Calendar.getInstance();
            c.set(Calendar.HOUR_OF_DAY, 0);
            c.set(Calendar.MINUTE, 0);
            c.set(Calendar.SECOND, 0);
            c.set(Calendar.MILLISECOND, 0);
            mDateLastCompleted = c.getTimeInMillis();
        } else {

            setTitle("Edit a task");

        }


        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                LinearLayout custom = (LinearLayout) findViewById(R.id.customReccuringSelected);

                String selectedItemText = parent.getItemAtPosition(position).toString();

                switch (selectedItemText) {
                    case ("Every Day"):
                        custom.setVisibility(View.GONE);
                        mNumberOfRecurringDays = taskContract.TaskEntry.RECURRING_DAILY;
                        mDueDate = mDateLastCompleted + (mNumberOfRecurringDays * 86400000);
                        displayDueDate();
                        isCustomSpinnerSelected = false;
                        break;

                    case ("Every Week"):
                        custom.setVisibility(View.GONE);
                        mNumberOfRecurringDays = taskContract.TaskEntry.RECURRING_WEEKLY;
                        mDueDate = mDateLastCompleted + (mNumberOfRecurringDays * 86400000);
                        isCustomSpinnerSelected = false;
                        displayDueDate();
                        break;
                    case ("Every Fortnight"):
                        custom.setVisibility(View.GONE);
                        mNumberOfRecurringDays = taskContract.TaskEntry.RECURRING_FORTNIGHTLY;
                        mDueDate = mDateLastCompleted + (mNumberOfRecurringDays * 86400000);
                        displayDueDate();
                        isCustomSpinnerSelected = false;
                        break;
                    case ("Every Year"):
                        custom.setVisibility(View.GONE);
                        mNumberOfRecurringDays = taskContract.TaskEntry.RECURRING_YEARLY;
                        mDueDate = mDateLastCompleted + (86400000L * mNumberOfRecurringDays);
                        displayDueDate();
                        isCustomSpinnerSelected = false;
                        break;

                    case ("Custom"):
                        custom.setVisibility(View.VISIBLE);
                        isCustomSpinnerSelected = true;
                        break;

                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }

        });


        //set touchlisteners on all input fields to listen for any changes to data
        mtitleEditText.setOnTouchListener(mTouchListener);
        mDescriptionEditText.setOnTouchListener(mTouchListener);
        mRecurringSpinner.setOnTouchListener(mTouchListener);

        getLoaderManager().initLoader(0, null, this);
    }

    /**
     * The insert task method is called when the user click the "tick" symbol in the editor
     * This determines whether we are inserting a task or updating an existing one by seeing if the URI is null or not.
     *
     */
    public void insertTask() {

        // Read from input fields
        // Use trim to eliminate leading or trailing white space
        String taskTitle = mtitleEditText.getText().toString().trim();
        String descrption = mDescriptionEditText.getText().toString().trim();

        //won't insert task without a title
        if (uriCurrentTask == null &&
                TextUtils.isEmpty(taskTitle)) {
            return;
        }

        if (isCustomSpinnerSelected == true) {
            if (!mCustomRecurring.getText().toString().equals("")) {
                mNumberOfRecurringDays = Integer.parseInt(mCustomRecurring.getText().toString().trim());
            } else {
                mNumberOfRecurringDays = 0;
            }
        }


        // Create database helper
        taskDBHelper mDBHelper = new taskDBHelper(this);


        // Create a ContentValues object where column names are the keys,
        // and values from the editor are the values.
        ContentValues values = new ContentValues();
        values.put(taskContract.TaskEntry.COLUMN_TASK_TITLE, taskTitle);
        values.put(taskContract.TaskEntry.COLUMN_DATE, 0);
        values.put(taskContract.TaskEntry.COLUMN_LAST_COMPLETED, mDateLastCompleted);
        values.put(taskContract.TaskEntry.COLUMN_DESCRIPTION, descrption);
        values.put(taskContract.TaskEntry.COLUMN_HISTORY, "c");
        values.put(taskContract.TaskEntry.COLUMN_STATUS, 0);
        values.put(taskContract.TaskEntry.COLUMN_RECCURING_PERIOD, mNumberOfRecurringDays);
        values.put(taskContract.TaskEntry.COLUMN_TYPE_TASK, 1);
        values.put(taskContract.TaskEntry.COLUMN_TIME, "000");

        // Insert a new task into the provider, returning the content URI for the new task.
        if (uriCurrentTask == null) {
            Uri insertUri = getContentResolver().insert(taskContract.TaskEntry.CONTENT_URI, values);

            // Show a toast message depending on whether or not the insertion was successful
            if (insertUri == null) {
                // If the row ID is -1, then there was an error with insertion.
                Toast.makeText(this, "Error with saving task", Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the insertion was successful and we can display a toast with the row ID.
                Toast.makeText(this, "Task saved successfully!", Toast.LENGTH_SHORT).show();
            }
        } else {
            getContentResolver().update(uriCurrentTask, values, null, null);
        }

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_editor.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.editor_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option

            case R.id.action_insert:
                // Save Task to database
                insertTask();
                // Exits the activity
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void displayDueDate() {
        TextView dueDateDisplayLabel = findViewById(R.id.dueDateDisplayLabel);
        String chosenDateAsString = DateFormat.getDateInstance().format(mDueDate);
        dueDateDisplayLabel.setText(chosenDateAsString);
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (uriCurrentTask != null) {
            String[] projection = {
                    taskContract.TaskEntry._ID,
                    taskContract.TaskEntry.COLUMN_TASK_TITLE,
                    taskContract.TaskEntry.COLUMN_DESCRIPTION,
                    taskContract.TaskEntry.COLUMN_TYPE_TASK,
                    taskContract.TaskEntry.COLUMN_DATE,
                    taskContract.TaskEntry.COLUMN_TIME,
                    taskContract.TaskEntry.COLUMN_HISTORY,
                    taskContract.TaskEntry.COLUMN_LAST_COMPLETED,
                    taskContract.TaskEntry.COLUMN_STATUS,
                    taskContract.TaskEntry.COLUMN_RECCURING_PERIOD};

            return new android.content.CursorLoader(this,   // Parent activity context
                    uriCurrentTask,         // Query the content URI for the current Task
                    projection,             // Columns to query
                    null,                   // No selection clause
                    null,                   // No selection arguments
                    null);
        }
        return null;// Default sort order

    }


    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (uriCurrentTask != null) {
            if (data.moveToFirst()) {

                // Find the columns of Task values we need
                int titleColumnIndex = data.getColumnIndex(taskContract.TaskEntry.COLUMN_TASK_TITLE);
                int descriptionColumnIndex = data.getColumnIndex(taskContract.TaskEntry.COLUMN_DESCRIPTION);
                int dateColumnIndex = data.getColumnIndex(taskContract.TaskEntry.COLUMN_DATE);
                int lastCompletedColumnIndex = data.getColumnIndex(taskContract.TaskEntry.COLUMN_LAST_COMPLETED);
                int timeColumnIndex = data.getColumnIndex(taskContract.TaskEntry.COLUMN_TIME);
                int RecurringColumnIndex = data.getColumnIndex(taskContract.TaskEntry.COLUMN_RECCURING_PERIOD);

                // Gte the values from the Cursor for the given column index
                String titleString = data.getString(titleColumnIndex);
                String descriptionString = data.getString(descriptionColumnIndex);
                Long dateLong = data.getLong(dateColumnIndex);
                String timeString = data.getString(timeColumnIndex);
                int reccuringDaysInt = data.getInt(RecurringColumnIndex);
                mDateLastCompleted = data.getLong(lastCompletedColumnIndex);

                //set textfields
                mtitleEditText.setText(titleString);
                mDescriptionEditText.setText(descriptionString);
                mDate = dateLong;
                mTime = timeString;
                mRecurringDaysEditMode = reccuringDaysInt;
                displayDueDate();
                switch (mRecurringDaysEditMode) {
                    case (1):
                        mRecurringSpinner.setSelection(0);
                        break;
                    case (7):
                        mRecurringSpinner.setSelection(1);
                        break;
                    case (14):
                        mRecurringSpinner.setSelection(2);
                        break;
                    case (365):
                        mRecurringSpinner.setSelection(3);
                        break;
                    default:
                        mRecurringSpinner.setSelection(4);
                        mCustomRecurring.setText(String.valueOf(mRecurringDaysEditMode));
                }

            }
        }

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    public String convertTimeToString(int hour, int min) {
        String timeString = "";

        if (hour < 10) {
            timeString += "0" + hour + ":";
        } else {
            timeString += hour + ":";
        }
        if (min < 10) {
            timeString += "0" + min;
        } else {
            timeString += min;
        }
        return timeString;
    }
}
