package com.hermosaprogramacion.blog.saludmock.ui;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.app.DatePickerDialog;

import java.util.Calendar;

/**
 * Dialogo para seleccionar la fecha de la cita médica
 */

public class DatePickerFragment extends DialogFragment {

    private DatePickerDialog.OnDateSetListener mListener;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog pickerDialog
                = new DatePickerDialog(getActivity(), mListener, year, month, day);
        pickerDialog.getDatePicker().setMinDate(c.getTimeInMillis());
        return pickerDialog;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            mListener = (DatePickerDialog.OnDateSetListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " debe implementar OnDateSetListener");

        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }
}
