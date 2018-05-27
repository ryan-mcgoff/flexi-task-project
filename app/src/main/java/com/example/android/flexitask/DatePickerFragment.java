package com.example.android.flexitask;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;

import java.util.Calendar;

/**
 * Created by Ryan Mcgoff (4086944), Jerry Kumar (3821971), Jaydin Mcmullan (9702973)
 *
 * Creates a Date picker dialog fragment using the dates that are passed to it.
 * The dialog prompts the user to select a date
 */


public class DatePickerFragment extends DialogFragment {


    /**
     * Creates a datePicker dialog with the default date that is passed to it
     *
     * @param savedInstanceState a bundle of default date values for the DatePicker to set itself to
     *                           (if creating a new task this is todays date, otherwise the values stored in the database)
     * @return a new instance of the datePickerDialog
     */
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        //gets arguments from savedInstanceState
        int year = getArguments().getInt("year");
        int month = getArguments().getInt("month");
        int day = getArguments().getInt("day");

        return new DatePickerDialog(getActivity(), (DatePickerDialog.OnDateSetListener) getActivity(), year, month, day);
    }
}
