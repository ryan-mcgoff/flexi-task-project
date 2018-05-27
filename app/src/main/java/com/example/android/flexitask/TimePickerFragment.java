package com.example.android.flexitask;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;

import java.text.DateFormat;
import java.util.Calendar;

/**
 * Created by Ryan Mcgoff (4086944), Jerry Kumar (3821971), Jaydin Mcmullan (9702973)
 * Creates a Time picker dialog fragment using the times that are passed to it.
 * The dialog prompts the user to select a time of day
 */

public class TimePickerFragment extends DialogFragment {

    /**
     * Creates a timepicker with the default times that are passed to it
     *
     * @param savedInstanceState a bundle of default time values for the TimePicker
     *                           to set itself to (if creating a new task this todays
     *                           time, otherwise the values stored in the database)
     * @return a new instance of the TimePickerDialog
     */
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        //gets arguments from savedInstanceState
        int hour = getArguments().getInt("hour");
        int min = getArguments().getInt("min");

        return new TimePickerDialog(getActivity(), (TimePickerDialog.OnTimeSetListener) getActivity(), hour, min, true);
    }
}
