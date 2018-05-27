package com.example.android.flexitask;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.support.v4.widget.CursorAdapter;

import com.example.android.flexitask.data.taskDBHelper;
import com.example.android.flexitask.data.taskContract;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.concurrent.TimeUnit;

/**
 * Created by Ryan Mcgoff (4086944), Jerry Kumar (3821971), Jaydin Mcmullan (9702973)
 *
 * A Cursor Adaptor  lets Android manage resources more efficiently
 * by retrieving and releasing row and column values when the user scrolls, rather than loading everything into memory.
 * This is a custom extension of CursorAdaptor for tasks
 *
 */
public class TaskCursorAdaptor extends CursorAdapter {

    /**
     * Is an adapter for a listview
     * that uses a {@link Cursor} to retrieve data from the tasks table{@link taskDBHelper}.
     *
     * @param context app context
     * @param c       The cursor that provides the data.
     */
    public TaskCursorAdaptor(Context context, Cursor c) {

        super(context, c, 0 /* flags */);

    }


    /**
     * Inflates a new view for the bind view method to add data to.
     *
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already moved to the correct position.
     * @param parent  The parent to which the new view is attached to
     * @return the new list item view.
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {

        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }


    /**
     * This method binds the Task data (in the current row pointed to by cursor) to the given
     * list item layout (view). For example, the name for the current Task can be set on the name TextView
     * in the list item layout. This allows us to recycle previous item layouts.
     *
     * @param view    A previous view, returned by the newView() method
     * @param context the context of the app
     * @param cursor  The cursor from which to get the data. The cursor is already moved to the
     *                correct row.
     */
    @Override

    public void bindView(View view, Context context, Cursor cursor) {
        //find the views for the list item
        TextView titleTextView = view.findViewById(R.id.titleListView);
        TextView descriptionTextView = view.findViewById(R.id.descriptionListView);
        TextView dueDateView = view.findViewById(R.id.testView);
        LinearLayout priorityLine = view.findViewById(R.id.priorityMargin);

        //find the column values
        int titleColumnIndex = cursor.getColumnIndex(taskContract.TaskEntry.COLUMN_TASK_TITLE);
        int descriptionColumnIndex = cursor.getColumnIndex(taskContract.TaskEntry.COLUMN_DESCRIPTION);
        int dateColumnIndex = cursor.getColumnIndex(taskContract.TaskEntry.COLUMN_DATE);
        int taskTypeColumnIndex = cursor.getColumnIndex(taskContract.TaskEntry.COLUMN_TYPE_TASK);
        int lastCompletedIndex = cursor.getColumnIndex(taskContract.TaskEntry.COLUMN_LAST_COMPLETED);
        int recurringColumnIndex = cursor.getColumnIndex(taskContract.TaskEntry.COLUMN_RECCURING_PERIOD);

        //Read the values for current Tasks
        String titleString = cursor.getString(titleColumnIndex);
        String descriptionString = cursor.getString(descriptionColumnIndex);
        String dateString = cursor.getString(dateColumnIndex);
        int taskType = cursor.getInt(taskTypeColumnIndex);
        long lastCompletedLong = cursor.getLong(lastCompletedIndex);
        int recurringPeriod = cursor.getInt(recurringColumnIndex);

        //set date values for priority checker & dueDateChecker
        long dateLong = 0;
        Calendar cTodayDate = Calendar.getInstance();
        cTodayDate.set(Calendar.HOUR_OF_DAY, 0);
        cTodayDate.set(Calendar.MINUTE, 0);
        cTodayDate.set(Calendar.SECOND, 0);
        cTodayDate.set(Calendar.MILLISECOND, 0);
        long todayDate = cTodayDate.getTimeInMillis();


        //checks if the item is a flexi or fixed task, if a flexi task it uses the {@link priorityChecker}
        if (taskType == taskContract.TaskEntry.TYPE_FLEXI) {

            priorityChecker(priorityLine, titleString, todayDate, lastCompletedLong, recurringPeriod);
            dateLong = lastCompletedLong + (recurringPeriod * 86400000L);

        } else {

            dateLong = Long.parseLong(dateString);

        }

        //checks how many days until a given task is due
        String daysUntilDue = daysUntilDue(dateLong);

        /**set views for {@link com.example.android.flexitask#listItem} with retrieved text*/
        dueDateView.setText(String.valueOf(daysUntilDue));
        titleTextView.setText(titleString);
        descriptionTextView.setText(descriptionString);

    }


    /**
     * This method determines a flexi tasks priority and assigns an appropriate color to its margin.
     * The priority is calculated using a custom urgency algorithm that takes todays date (in milliseconds) and subtracts
     * the date the task was created/ last done (in milliseconds) from it.
     * The result of this calculation represents the milliseconds since the task was last completed
     * the algorithm then convert this to number of days by dividing by 86,400,000 (milliseconds in a day)
     * and adding 1 (for the due day). The algorithm then divides that number by how ofen the task is set to recur
     * (ie: weekly task, daily task). The end result is a number that represents a percentage of how
     * complete/overdue/underdue a task is. For tasks that have a lower recurring frequency (ie daily),
     * each day that task is overdue will increase the result relatively more than a task with a higher frequency (ie: yearly)
     *
     * @param priorityLine      the colored margin line on the list_item that represents how overdue a task is
     * @param todayDate         today's date (in milliseconds)
     * @param lastCompletedLong the date the flexitask was last completed or created (in milliseconds)
     * @param recurringPeriod   the tasks recurring period (ie: daily, yearly)
     */
    public void priorityChecker(View priorityLine, String taskTitle, long todayDate, long lastCompletedLong, int recurringPeriod) {


        long daysSinceTaskLastCompleted = ((todayDate - lastCompletedLong) / 86400000L) + 1;

        double priorityRating = (daysSinceTaskLastCompleted / (double) recurringPeriod);


        //log for testing purposes
         Log.v(" ",taskTitle + " \nDays: " + String.valueOf(daysSinceTaskLastCompleted)
                    + "\nrecurring: " +String.valueOf(recurringPeriod) + "\npriority: "
                    + String.valueOf(priorityRating) +"\n-------------------\n" );


        //if 75% until due, set green priority
        if (priorityRating < 0.75) {

            priorityLine.setBackgroundResource(R.color.greenPriority);

        }
        //if between 75% and 150%, set yellow priority
        else if (priorityRating > 0.75 && priorityRating < 1.5) {

            priorityLine.setBackgroundResource(R.color.yellowPriority);

        }
        //if between over 150%, set red (URGENT) priority
        else {

            priorityLine.setBackgroundResource(R.color.redPriority);

        }
    }

    /**
     * This method creates a string to is used to show the user how long until that task is due.
     * ie: Overdue by X, Due in X or Due Today
     * It takes the current date (in milliseconds) and finds the difference between that date
     * and the due date (in milliseconds). Then it converts this number into days and then selects
     * the appropriate response to concatenate the message with.
     *
     * @param dateLong the date the task is due
     * @return dateMessage an English representation of how long until the task is due / how overdue it is
     */

    public String daysUntilDue(long dateLong) {
        long todayDate = Calendar.getInstance().getTimeInMillis();
        long dueDate = dateLong;


        String datemessage;


        if (dueDate >= todayDate) {
            long differenceMillisecond = dueDate - todayDate + 1;
            long differenceDays = TimeUnit.MILLISECONDS.toDays(differenceMillisecond) + 1;
            datemessage = "Due in " + String.valueOf(differenceDays);

            if (differenceDays == 0) {
                datemessage = "DUE TODAY!";
            } else if (differenceDays != 1) {
                datemessage += " days";
            } else {
                datemessage += " day";
            }
        } else {

            long differenceMillisecond = todayDate - dueDate;
            long differenceDays = TimeUnit.MILLISECONDS.toDays(differenceMillisecond);
            datemessage = "Overdue by " + String.valueOf(differenceDays);
            if (differenceDays == 0) {
                datemessage = "DUE TODAY!";
            } else if (differenceDays != 1) {
                datemessage += " days";
            } else {
                datemessage += " day";
            }
        }

        return String.valueOf(datemessage);

    }

}
