package com.example.android.flexitask;

import android.app.DatePickerDialog;
import android.app.LoaderManager;
import android.app.TimePickerDialog;
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
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.text.DateFormat;
import java.util.Calendar;

import com.example.android.flexitask.data.taskContract;

/**
 * Created by Ryan Mcgoff (4086944), Jerry Kumar (3821971), Jaydin Mcmullan (9702973)
 *
 * The FixedTask Editor acts as an editor for both new tasks and an editor updating existing tasks.
 * The editor checks if the intent that started the activity had a URI (.getData()) to determine
 * if the user is trying to create a new task or update an existing task.
 * If it's updating, it uses a Cursorloader to retrieve the data for that task and uses the contentProviders (TaskProvider's)
 * update method instead of insert.
 *
 */
public class FixedTaskEditor extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>, DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener {

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
    private long mDate;

    /**
     * time
     */
    private String mTime;

    /**
     * Boolean to see if Custom Spinner is selected
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

    /*Spinner View for recurring days selection */
    private Spinner spinner;

    private int currentDay;
    private int currentMonth;
    private int currentYear;
    private int currentHour;
    private int currentMin;


    /**
     * Boolean flag that keeps track of whether the Task has been edited (true) or not (false)
     */
    private boolean mTaskHasChanged = false;

    private final TextWatcher mTextEditorWatcher = new TextWatcher() {
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            //do nothing
        }

        public void onTextChanged(CharSequence s, int start, int before, int count) {
            /**This sets a textCountView to the current length of the input from the editTextView*/
            textCountTaskTitle.setText(String.valueOf(s.length()) + "/30");
        }

        public void afterTextChanged(Editable s) {
            //do nothing
        }
    };

    /**
     * OnTouchListener checks to see when the user modifies a task
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
        setContentView(R.layout.activity_fixed_task_editor);

        // Find all relevant views that we will need to read user input from
        textCountTaskTitle = (TextView) findViewById(R.id.textCountTaskTitle);
        mtitleEditText = (EditText) findViewById(R.id.taskTitle);
        mRecurringSpinner = (Spinner) findViewById(R.id.recurringSpinner);
        mDescriptionEditText = (EditText) findViewById(R.id.descriptionEditText);
        mCustomRecurring = (EditText) findViewById(R.id.customRecurringText);
        spinner = (Spinner) findViewById(R.id.recurringSpinner);

        //adds textlistener to Title field, to listen to input
        mtitleEditText.addTextChangedListener(mTextEditorWatcher);

        //gets uri from the intent that was used to launch this activity, if it was launched from
        //the floating action button (big plus symbol) then there won't be a URI, meaning the user is creating
        //a task
        uriCurrentTask = getIntent().getData();

        /*SPINNER*/

        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.recurring_options_array, android.R.layout.simple_spinner_item);

        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Apply the adapter to the spinner
        spinner.setAdapter(adapter);


        /**if URI is "null" then we know we're creating a new task. If it isn't null
         * then we know we're editing an existing task
         *
         * */
        if (uriCurrentTask == null) {
            setTitle("Create a task");

            //get Todays date and set up private variables for calander to use

            Calendar c = Calendar.getInstance();
            currentYear = c.get(Calendar.YEAR);
            currentMonth = c.get(Calendar.MONTH);
            currentDay = c.get(Calendar.DAY_OF_MONTH);

            /** set {@link com.example.android.flexitask.R.id.dateDiplayLabel} to todays date */

            String chosenDateAsString = DateFormat.getDateInstance().format(c.getTime());
            TextView dateLabel = (TextView) findViewById(R.id.dateDiplayLabel);
            dateLabel.setText(chosenDateAsString);
            mDate = c.getTimeInMillis();

            /**set {@link com.example.android.flexitask.R.id.timeDisplayLabel} to todays date*/

            currentHour = c.get(Calendar.HOUR_OF_DAY);
            currentMin = c.get(Calendar.MINUTE);
            TextView timeLabel = (TextView) findViewById(R.id.timeDisplayLabel);
            mTime = convertTimeToString(currentHour, currentMin);
            timeLabel.setText(mTime);
        } else {

            setTitle("Edit a task");

        }


        RelativeLayout dateButton = (RelativeLayout) findViewById(R.id.dateButton);
        dateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                /*if creating task for the first time*/
                if (uriCurrentTask == null) {
                    //bundle for argument to pass to date picker
                    Bundle args = new Bundle();
                    /**Add date (current date set in OnCreate) to bundle*/
                    args.putInt("year", currentYear);
                    args.putInt("month", currentMonth);
                    args.putInt("day", currentDay);

                    //create new fragment
                    android.support.v4.app.DialogFragment datePicker = new DatePickerFragment();
                    //pass it the bundle of argument
                    datePicker.setArguments(args);
                    //Show fragment as a dialog
                    datePicker.show(getSupportFragmentManager(), "DatePicker");
                }
                //ELSE if updating existing task
                else {
                    Bundle args = new Bundle();
                    /**get Year month and day from the Date (in milliseconds)*/

                    Calendar c = Calendar.getInstance();
                    c.setTimeInMillis(mDate);

                    int year = c.get(Calendar.YEAR);
                    int month = c.get(Calendar.MONTH);
                    int day = c.get(Calendar.DAY_OF_MONTH);
                    args.putInt("year", year);
                    args.putInt("month", month);
                    args.putInt("day", day);

                    //create new fragment
                    android.support.v4.app.DialogFragment datePicker = new DatePickerFragment();
                    //pass it the bundle of arguments
                    datePicker.setArguments(args);
                    //Show fragment as a dialog
                    datePicker.show(getSupportFragmentManager(), "DatePicker");
                }


            }
        });

        RelativeLayout timeButton = (RelativeLayout) findViewById(R.id.timeButton);
        timeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                /*if creating task for the first time*/
                if (uriCurrentTask == null) {

                    /*Get current time and add to bundle*/

                    Bundle args = new Bundle();
                    Calendar c = Calendar.getInstance();
                    int hour = c.get(Calendar.HOUR_OF_DAY);
                    int min = c.get(Calendar.MINUTE);
                    args.putInt("hour", currentHour);
                    args.putInt("min", currentMin);

                    //create new fragment
                    android.support.v4.app.DialogFragment timePicker = new TimePickerFragment();
                    //pass it the bundle of arguments
                    timePicker.setArguments(args);
                    //Show fragment as a dialog
                    timePicker.show(getSupportFragmentManager(), "TimePicker");

                }
                //ELSE you're updating existing task
                else {

                     /*Get time value for the task (already retrived by the loader from the time column)*/

                    Bundle args = new Bundle();
                    String[] stringParts = mTime.split(":");
                    int hour = Integer.valueOf(stringParts[0]);
                    int min = Integer.valueOf(stringParts[1]);
                    args.putInt("hour", hour);
                    args.putInt("min", min);

                    //create new fragment
                    android.support.v4.app.DialogFragment timePicker = new TimePickerFragment();
                    //pass it the bundle of arguments
                    timePicker.setArguments(args);
                    //Show fragment as a dialog
                    timePicker.show(getSupportFragmentManager(), "TimePicker");

                }

            }
        });


        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                /*When "Custom" is selected, the method exposes the customView, where the user can input there own
                 *chosen dates, this view is hidden if anything else is selected*/
                LinearLayout customView = (LinearLayout) findViewById(R.id.customReccuringSelected);

                String selectedItemText = parent.getItemAtPosition(position).toString();

                switch (selectedItemText) {
                    case ("Never"):
                        customView.setVisibility(View.GONE);
                        mNumberOfRecurringDays = 0;
                        isCustomSpinnerSelected = false;
                        break;
                    case ("Every Day"):
                        customView.setVisibility(View.GONE);
                        mNumberOfRecurringDays = taskContract.TaskEntry.RECURRING_DAILY;
                        isCustomSpinnerSelected = false;
                        break;

                    case ("Every Week"):
                        //Toast.makeText(parent.getContext(),"Every Week",Toast.LENGTH_LONG).show();
                        customView.setVisibility(View.GONE);
                        mNumberOfRecurringDays = taskContract.TaskEntry.RECURRING_WEEKLY;
                        isCustomSpinnerSelected = false;
                        break;
                    case ("Every Fortnight"):
                        customView.setVisibility(View.GONE);
                        mNumberOfRecurringDays = taskContract.TaskEntry.RECURRING_FORTNIGHTLY;
                        isCustomSpinnerSelected = false;
                        break;
                    case ("Every Year"):
                        customView.setVisibility(View.GONE);
                        mNumberOfRecurringDays = taskContract.TaskEntry.RECURRING_YEARLY;
                        isCustomSpinnerSelected = false;
                        break;

                    case ("Custom"):

                        customView.setVisibility(View.VISIBLE);
                        isCustomSpinnerSelected = true;
                        break;

                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }

        });


        //set touchlisteners on all input fields to listen for any chnages to data
        mtitleEditText.setOnTouchListener(mTouchListener);
        //mDescriptionEditText.setOnTouchListener(mTouchListener);
        mRecurringSpinner.setOnTouchListener(mTouchListener);
        //dateButton.setOnTouchListener(mTouchListener);
        //timeButton.setOnTouchListener(mTouchListener);

        getLoaderManager().initLoader(0, null, this);
    }

    //TODO: JAVA DOCS FOR THIS

    /**
     * Helper method to insertTask into the database
     */
    public void insertTask() {

        // Read from input fields
        // Use trim to eliminate leading or trailing white space
        String taskTitle = mtitleEditText.getText().toString().trim();
        String description = mDescriptionEditText.getText().toString().trim();

        if (uriCurrentTask == null &&
                TextUtils.isEmpty(taskTitle)) {
            return;
        }

        /**if custom spinner was selected, retrieve the number of days from its editText
         * view to get {@link #mNumberOfRecurringDays}, if nothing was inputed default to zero*/
        if (isCustomSpinnerSelected == true) {
            if (!mCustomRecurring.getText().toString().equals("")) {
                mNumberOfRecurringDays = Integer.parseInt(mCustomRecurring.getText().toString().trim());
            } else {
                mNumberOfRecurringDays = 0;
            }
        }

        // ContentValues object with column names on the left and values from the editor on the right,
        ContentValues values = new ContentValues();
        values.put(taskContract.TaskEntry.COLUMN_TASK_TITLE, taskTitle);
        values.put(taskContract.TaskEntry.COLUMN_DATE, mDate);
        values.put(taskContract.TaskEntry.COLUMN_DESCRIPTION, description);
        values.put(taskContract.TaskEntry.COLUMN_HISTORY, "c");
        values.put(taskContract.TaskEntry.COLUMN_STATUS, 0);
        values.put(taskContract.TaskEntry.COLUMN_RECCURING_PERIOD, mNumberOfRecurringDays);
        values.put(taskContract.TaskEntry.COLUMN_TYPE_TASK, 0);
        values.put(taskContract.TaskEntry.COLUMN_TIME, mTime);


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
            //if there is a URI, that means the user is requesting an update to an existing task
            getContentResolver().update(uriCurrentTask, values, null, null);
        }

    }


    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_editor.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.editor_menu, menu);
        return true;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.action_insert:
                // Insert Task into database
                insertTask();
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {

        /**when date has been selected by the user using the {@link DatePickerDialog}, set the {@link R.id.timeDisplayLabel}
         * to reflect these changes.Update {@link #mDate} so that when  {@link #onOptionsItemSelected(R.id.action_save)} is
         * selected and calls {@link insertTask()} the new date value is updated.
         */

        Calendar c = Calendar.getInstance();
        c.set(Calendar.YEAR, year);
        c.set(Calendar.MONTH, month);
        c.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        //Date given in local language, can also format the date here
        String chosenDateAsString = DateFormat.getDateInstance().format(c.getTime());
        mDate = c.getTimeInMillis();

        TextView dateLabel = (TextView) findViewById(R.id.dateDiplayLabel);
        dateLabel.setText(chosenDateAsString);

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {

        /**when time has been selected by the user using the {@link TimePickerDialog}, set the {@link R.id.dateDiplayLabel}
         *  to reflect these changes.Update {@link #mTime} so that when {@link #onOptionsItemSelected(R.id.action_save)} is
         * selected and calls {@link insertTask()} the new time value is updated.
         */
        TextView timeLabel = (TextView) findViewById(R.id.timeDisplayLabel);
        mTime = convertTimeToString(hourOfDay, minute);
        timeLabel.setText(mTime);

    }


    /**
     * The cursor query is used on a background thread
     * so that it does not interfere the app's UI. When the user edits a task
     * (uriCurrentTask!=null), the loader creates a cursor loader which goes and retrieves
     * the desired column values to populate the editor screen
     *
     * @param id   of the loader
     * @param args set to null in this case
     * @return cursor loader
     */
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        if (uriCurrentTask != null) {
            //the columns wanted for the particular task
            String[] projection = {
                    taskContract.TaskEntry._ID,
                    taskContract.TaskEntry.COLUMN_TASK_TITLE,
                    taskContract.TaskEntry.COLUMN_DESCRIPTION,
                    taskContract.TaskEntry.COLUMN_LAST_COMPLETED,
                    taskContract.TaskEntry.COLUMN_TYPE_TASK,
                    taskContract.TaskEntry.COLUMN_DATE,
                    taskContract.TaskEntry.COLUMN_TIME,
                    taskContract.TaskEntry.COLUMN_HISTORY,
                    taskContract.TaskEntry.COLUMN_STATUS,
                    taskContract.TaskEntry.COLUMN_RECCURING_PERIOD};

            return new android.content.CursorLoader(this,
                    uriCurrentTask,         // The URI for the desired task
                    projection,             // Columns for the cursor to retrieve
                    null,
                    null,
                    null);
        }
        return null;

    }


    /**
     * After the cursor comes back with the given data from the Task database,
     * the relevant data is populated onto the screen and the required varaibles for the rest
     * of the class are given
     *
     * @param loader the cursor loader for this class
     * @param data   the cursor containing the data for the queried task
     */
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (uriCurrentTask != null) {
            if (data.moveToFirst()) {
                /*Find the columns of Task attributes needed in this class {@link FixedTaskEditor}*/
                int titleColumnIndex = data.getColumnIndex(taskContract.TaskEntry.COLUMN_TASK_TITLE);
                int descriptionColumnIndex = data.getColumnIndex(taskContract.TaskEntry.COLUMN_DESCRIPTION);
                int dateColumnIndex = data.getColumnIndex(taskContract.TaskEntry.COLUMN_DATE);
                int timeColumnIndex = data.getColumnIndex(taskContract.TaskEntry.COLUMN_TIME);
                int RecurringColumnIndex = data.getColumnIndex(taskContract.TaskEntry.COLUMN_RECCURING_PERIOD);

                /*Retrieve the values from the {@link #loader} cursor for the given column index*/
                String titleString = data.getString(titleColumnIndex);
                String descriptionString = data.getString(descriptionColumnIndex);
                long dateLong = data.getLong(dateColumnIndex);
                String timeString = data.getString(timeColumnIndex);
                int recurringDaysInt = data.getInt(RecurringColumnIndex);

                /**set values for{@link #mDate},{@link #mTime}*/
                mDate = dateLong;
                mTime = timeString;
                mRecurringDaysEditMode = recurringDaysInt;

                /**select the appropriate {@link R.id.recurringSpinner} option*/
                switch (mRecurringDaysEditMode) {
                    case (taskContract.TaskEntry.RECURRING_NEVER):
                        Log.w("Date in milliseconds", String.valueOf(mRecurringDaysEditMode));
                        mRecurringSpinner.setSelection(0);
                        break;
                    case (taskContract.TaskEntry.RECURRING_DAILY):
                        mRecurringSpinner.setSelection(1);
                        break;
                    case (taskContract.TaskEntry.RECURRING_WEEKLY):
                        mRecurringSpinner.setSelection(2);
                        break;
                    case (taskContract.TaskEntry.RECURRING_FORTNIGHTLY):
                        mRecurringSpinner.setSelection(3);
                        break;
                    case (taskContract.TaskEntry.RECURRING_YEARLY):
                        mRecurringSpinner.setSelection(4);
                        break;
                    default:
                        mRecurringSpinner.setSelection(5);
                        mCustomRecurring.setText(String.valueOf(mRecurringDaysEditMode));
                }

                /**set textfield labels for {@link R.id.title}, {@link R.id.descriptionEditText},
                 * {@link R.id.dateDiplayLabel} & {@link R.id.timeDisplayLabel}*/

                //title & description
                mtitleEditText.setText(titleString);
                mDescriptionEditText.setText(descriptionString);

                //time
                String[] stringParts = mTime.split(":");
                int hour = Integer.valueOf(stringParts[0]);
                int min = Integer.valueOf(stringParts[1]);
                String tempStringForTimeLabel = convertTimeToString(hour, min);
                TextView timeLabel = (TextView) findViewById(R.id.timeDisplayLabel);
                timeLabel.setText(tempStringForTimeLabel);

                //date
                Calendar c = Calendar.getInstance();
                c.setTimeInMillis(mDate);
                String chosenDateAsString = DateFormat.getDateInstance().format(c.getTime());
                TextView dateLabel = (TextView) findViewById(R.id.dateDiplayLabel);
                dateLabel.setText(chosenDateAsString);

            }
        }

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    /**
     * Because {@link TimePickerDialog} returns the time in single digits (ie: 7 instead of 07)
     * this helper method converts the parameters into a 24hour formatted string to display
     * to the user (ie: so any single digit number because a double digit)
     */
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





