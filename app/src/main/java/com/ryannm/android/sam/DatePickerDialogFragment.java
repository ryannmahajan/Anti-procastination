package com.ryannm.android.sam;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.widget.DatePicker;

import java.lang.reflect.Field;
import java.util.Calendar;
import java.util.GregorianCalendar;

public class DatePickerDialogFragment extends DialogFragment {
    public static final String TAG = "datePicker";
    private static final String ARGS_YEAR = "args_YEar";
    private static final String ARGS_MONTH = "arfMonth";
    private static final String ARGS_DATE = "args_dater";
    private DatePickerDialog.OnDateSetListener mCallback;

    public static DatePickerDialogFragment getFragment(int year, int month, int date) {
        DatePickerDialogFragment f = new DatePickerDialogFragment();
        Bundle args = new Bundle();
        args.putInt(ARGS_YEAR,year);
        args.putInt(ARGS_MONTH, month);
        args.putInt(ARGS_DATE, date);
        f.setArguments(args);
        return f;
    }

    @Override @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Calendar calendar = new GregorianCalendar();

        int year = getArguments().getInt(ARGS_YEAR, Integer.MAX_VALUE);
        if (year==Integer.MAX_VALUE) year = calendar.get(Calendar.YEAR);

        int month = getArguments().getInt(ARGS_MONTH, Integer.MAX_VALUE);
        if (month==Integer.MAX_VALUE) month = calendar.get(Calendar.MONTH);

        int day = getArguments().getInt(ARGS_DATE, Integer.MAX_VALUE);
        if (day==Integer.MAX_VALUE) day = calendar.get(Calendar.DATE);

        return new CustomDatePickerDialog(getActivity(), mCallback,year,month,day);
    }

    public DatePickerDialogFragment onDateSet (DatePickerDialog.OnDateSetListener callback) {
        mCallback = callback;
        return this;
    }

    private class CustomDatePickerDialog extends DatePickerDialog {
        CustomDatePickerDialog(Context context, DatePickerDialog.OnDateSetListener mCallback, int year, int month, int day) {
            super(context, mCallback, year, month, day);
        }

        @Override
        public void onStart() {
            super.onStart();
            DatePicker datePicker = null;
            try {
                Class<?> superclass = getClass().getSuperclass();
                Field mDatePickerField = superclass.getDeclaredField("mDatePicker");
                mDatePickerField.setAccessible(true);
                datePicker = (DatePicker) mDatePickerField.get(this);
            } catch (NoSuchFieldException | IllegalArgumentException | IllegalAccessException e) {
                e.printStackTrace();
            }

            final DatePicker finalDatePicker = datePicker;
            getButton(Dialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() { // So that it doesn't dismiss automatically
                @Override
                public void onClick(View v) {
                    if (finalDatePicker!=null) mCallback.onDateSet(finalDatePicker,finalDatePicker.getYear(), finalDatePicker.getMonth(),finalDatePicker.getDayOfMonth());
                }
            });
        }

    }
}
