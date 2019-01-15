package com.ryannm.android.sam;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.widget.TimePicker;

import java.lang.reflect.Field;
import java.util.Calendar;
import java.util.GregorianCalendar;

public class TimePickerDialogFragment extends DialogFragment {
    public static final String TAG = "TimePicker";
    private static final String ARGS_HOUR = "args_hour";
    private static final String ARGS_MINUTES = "args_minute";
    private TimePickerDialog.OnTimeSetListener mCallback;

    public static TimePickerDialogFragment getFragment(int hour, int minute) {
        TimePickerDialogFragment f = new TimePickerDialogFragment();
        Bundle args = new Bundle();
        args.putInt(ARGS_HOUR,hour);
        args.putInt(ARGS_MINUTES,minute);
        f.setArguments(args);
        return f;
    }

    @Override @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Calendar calendar = new GregorianCalendar();
        int hour = getArguments().getInt(ARGS_HOUR, Integer.MAX_VALUE);
        if (hour==Integer.MAX_VALUE) hour = calendar.get(Calendar.HOUR);

        int minute =getArguments().getInt(ARGS_MINUTES, Integer.MAX_VALUE);
        if (minute==Integer.MAX_VALUE) minute = calendar.get(Calendar.HOUR);

        return new CustomTimePickerDialog(getActivity(), mCallback, hour, minute, false);
    }

    public TimePickerDialogFragment onTimeSet (TimePickerDialog.OnTimeSetListener callback) {
        mCallback = callback;
        return this;
    }

    private class CustomTimePickerDialog extends TimePickerDialog {
        CustomTimePickerDialog(Context context, TimePickerDialog.OnTimeSetListener mCallback, int hour, int minute, boolean b) {
            super(context,mCallback, hour,minute,b);
        }

        @Override
        public void onStart() {
            super.onStart();
            TimePicker timePicker = null;
            try {
                Class<?> superclass = getClass().getSuperclass();
                Field mTimePickerField = superclass.getDeclaredField("mTimePicker");
                mTimePickerField.setAccessible(true); // Todo : Understand this try block
                timePicker = (TimePicker) mTimePickerField.get(this);
                timePicker.setOnTimeChangedListener(this);
            } catch (NoSuchFieldException | IllegalArgumentException | IllegalAccessException e) {
                e.printStackTrace();
            }
            final TimePicker finalTimePicker = timePicker;
            getButton(Dialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() { // So that it doesn't dismiss automatically
                @Override
                public void onClick(View v) {
                    if (finalTimePicker!=null) mCallback.onTimeSet(finalTimePicker,finalTimePicker.getCurrentHour(),finalTimePicker.getCurrentMinute());
                }
            });                     // todo: Unknown bug 2: Sometimes mCallback isn't set, resulting in NPE in above line
        }

    }
}
