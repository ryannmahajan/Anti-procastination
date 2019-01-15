package com.ryannm.android.sam;

import android.app.TimePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.concurrent.TimeUnit;

public class TaskDetailFragment extends Fragment {

    private static final String TAG = "TaskDetailFragment";
    private static final String EXTRA_TASK_ID = "taskIdForDetailFrag";
    private CharSequence taskName;
    private Calendar deadline = new GregorianCalendar();
    private Callback mCallback;
    private boolean taskAlreadyExists;
    private long reminderMS;

    public static Fragment getFragment(long taskId) {
        Fragment f = new TaskDetailFragment();
        Bundle args = new Bundle();
        args.putLong(EXTRA_TASK_ID, taskId);
        f.setArguments(args);
        return f;
    }

    @CallSuper
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mCallback = (Callback) context;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.details_task_new, container, false);

        // Below will run to give proper values from the task (if any passed)
        final long taskId = getArguments().getLong(EXTRA_TASK_ID, Long.MAX_VALUE);
        Task task = taskId != Long.MAX_VALUE ? App.getDaoSession().getTaskDao().load(taskId) : null;
        if (task !=null) {
            taskName = task.getName();
            deadline.setTimeInMillis(task.getDeadline().getTime());
            taskAlreadyExists = true;
            reminderMS = task.getReminderBeforeMS();
        }

        ((AppCompatActivity) getActivity()).setSupportActionBar((Toolbar) v.findViewById(R.id.toolbar));
        final CollapsingToolbarLayout collapsingToolbarLayout = (CollapsingToolbarLayout) v.findViewById(R.id.collapsing_toolbar);

        AppBarLayout appBarLayout = (AppBarLayout) v.findViewById(R.id.appbar);
        appBarLayout.addOnOffsetChangedListener(new AppBarStateChangeListener() {
            @Override
            void onStateChanged(AppBarLayout appBarLayout, State state) {
                if (state.equals(State.COLLAPSED)) collapsingToolbarLayout.setTitle((taskName!=null&&taskName!="")? taskName:"Task");
                if (state.equals(State.EXPANDED)) collapsingToolbarLayout.setTitle("");
            }
        });

        TextInputLayout textInputLayout = (TextInputLayout) v.findViewById(R.id.text_input_layout);
        textInputLayout.setHint("Task (e.g waking up)");
        if (task !=null) textInputLayout.getEditText().setText(taskName);
        textInputLayout.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Empty
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                taskName = s;
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Empty
            }
        });

        final RecyclerView recyclerView = (RecyclerView) v.findViewById(R.id.details_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(new TaskDetailAdapter());

        FloatingActionButton confirmFAB = (FloatingActionButton) v.findViewById(R.id.tickFAB);
        confirmFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean accessibilityEnabled = TrackAppServiceVersion9.isAccessibilityEnabled();
                if (accessibilityEnabled && !areImportantFieldsEmpty()) {
                    Task task = new Task();
                    if (taskAlreadyExists) task.setId(taskId);
                    // todo: Final confirmation
                    task.setName(taskName.toString());
                    task.setDeadline(deadline.getTime());

                    long blockStartMs = deadline.getTimeInMillis() - System.currentTimeMillis();
                    if (taskAlreadyExists) {
                      /*  ExactJob.cancelJob(task.getBlockingJobId());
                        ExactJob.cancelJob(task.getUnBlockingJobId()); */
                    }

                    if (reminderMS!=0 && reminderMS!=-1) { // So a reminder is selected
                        long remindInMS = blockStartMs-reminderMS;
                        if (remindInMS>0) task.setReminderJobId(ExactJob.scheduleReminder(remindInMS));

                    }

                    App.getDaoSession().getTaskDao().insertOrReplace(task);
                    mCallback.onConfirmTask(task);
                } else {
                    if (!accessibilityEnabled) TrackAppServiceVersion9.showAccessibilityDialog(TaskDetailFragment.this);
                }

            }
        });

        return v;
    }

    private boolean areImportantFieldsEmpty() {
        if (taskName==null) {
            Toast.makeText(getActivity(), R.string.name_required, Toast.LENGTH_SHORT).show();
            return true;
        }

        if (!deadline.after(new GregorianCalendar())) {
            Toast.makeText(getActivity(), R.string.please_set_deadline, Toast.LENGTH_SHORT).show();
            return true;
        }

        return false;
    }

    @CallSuper
    @Override
    public void onDetach() {
        super.onDetach();
        mCallback = null;
    }

    private class TaskDetailAdapter extends RecyclerView.Adapter<TaskDetailHolder> {
        private static final int TEXT_ONLY = 0;
        private static final int TEXT_SPINNER = 1;

        @Override
        public TaskDetailHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            if (viewType==TEXT_ONLY)
                return new TaskDetailHolder(getActivity().getLayoutInflater().inflate(R.layout.task_detail_individual_text_only, parent, false), viewType);
            else
                return new TaskDetailHolder(getActivity().getLayoutInflater().inflate(R.layout.task_detail_individual_spinner_only, parent, false), viewType);
        }

        @Override
        public void onBindViewHolder(TaskDetailHolder holder, int position) {
            holder.bindHolder(position);
        }

        @Override
        public int getItemCount() {
            return 2;
        }

        @Override
        public int getItemViewType(int position) {
            switch (position) {
                case 0: return TEXT_ONLY;
                case 1: return TEXT_SPINNER;
            }
            return TEXT_ONLY;
        }
    }

    private class TaskDetailHolder extends RecyclerView.ViewHolder {
        private ImageView mIcon;
        private TextView mDetailTitle;
        private TextView mDetailDescription;
        private Spinner mSpinner;

        TaskDetailHolder(View itemView, int viewType) {
            super(itemView);

            mIcon = (ImageView) itemView.findViewById(R.id.task_detail_icon);
            mDetailTitle = (TextView) itemView.findViewById(R.id.task_detail_title);
            mDetailDescription = (TextView) itemView.findViewById(R.id.task_detail);
            mSpinner = (Spinner) itemView.findViewById(R.id.spinner);
        }

        void bindHolder(int position) {
            mDetailTitle.setText(getResources().getStringArray(R.array.task_detail_title)[position]);
            if (getItemViewType()==TaskDetailAdapter.TEXT_ONLY) mDetailDescription.setText(getResources().getStringArray(R.array.task_detail)[position]);
            switch (position) {

                case 0: // Deadline
                    mIcon.setImageDrawable(getResources().getDrawable(R.drawable.ic_deadline_black));
                    if (taskAlreadyExists) mDetailDescription.setText(App.getHumanReadableDateTime(deadline,getActivity()));
                    itemView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(final View v) {
                            final TimePickerDialogFragment timePickerDialogFragment = TimePickerDialogFragment.getFragment(deadline.get(Calendar.HOUR_OF_DAY), deadline.get(Calendar.MINUTE));
                            timePickerDialogFragment.onTimeSet(new TimePickerDialog.OnTimeSetListener() {
                                @Override
                                public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                                    deadline.set(Calendar.HOUR_OF_DAY, hourOfDay);
                                    deadline.set(Calendar.MINUTE, minute);
                                    mDetailDescription.setText(App.getHumanReadableDateTime(deadline, getActivity()));

                                    long diffMs = deadline.getTimeInMillis() - System.currentTimeMillis();
                                    if (TimeUnit.MILLISECONDS.toMinutes(diffMs) <= 2) {
                                        Toast.makeText(getActivity(), R.string.set_deadline_3_minutes_future, Toast.LENGTH_SHORT).show();
                                    } else {
                                        timePickerDialogFragment.getDialog().dismiss();
                                    }
                                }
                            });
                            timePickerDialogFragment.show(getFragmentManager(), TimePickerDialogFragment.TAG);
                        }
                    });
                    break;

                case 1: // Reminder
                    mIcon.setImageDrawable(getResources().getDrawable(R.drawable.ic_alert_black));

                    mSpinner.setAdapter(new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_dropdown_item, getResources().getStringArray(R.array.reminder)));
                    int selection = 0;
                    if (taskAlreadyExists) {
                        String[] reminderValues = getResources().getStringArray(R.array.reminder_values);
                        for (int i=0; i < reminderValues.length; i++) {
                            if (Long.parseLong(reminderValues[i])==reminderMS) selection = i;
                        }
                    }
                    mSpinner.setSelection(selection);
                    mSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            String reminderBefore  = getResources().getStringArray(R.array.reminder_values)[position];
                            if (reminderBefore!=null) reminderMS = Long.parseLong(reminderBefore);
                            else reminderMS = -1;
                            parent.setSelection(position);
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {
                            parent.setSelection(0);
                        }
                    });
                    
                    itemView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            mSpinner.performClick();
                        }
                    });

                    break;
            }
        }
    }

    abstract static class AppBarStateChangeListener implements AppBarLayout.OnOffsetChangedListener {

        enum State {
            EXPANDED,
            COLLAPSED,
            IDLE
        }

        private State mCurrentState = State.IDLE;

        @Override
        public final void onOffsetChanged(AppBarLayout appBarLayout, int i) {
            if (i == 0) {
                if (mCurrentState != State.EXPANDED) {
                    onStateChanged(appBarLayout, State.EXPANDED);
                }
                mCurrentState = State.EXPANDED;
            } else if (Math.abs(i) >= appBarLayout.getTotalScrollRange()) {
                if (mCurrentState != State.COLLAPSED) {
                    onStateChanged(appBarLayout, State.COLLAPSED);
                }
                mCurrentState = State.COLLAPSED;
            } else {
                if (mCurrentState != State.IDLE) {
                    onStateChanged(appBarLayout, State.IDLE);
                }
                mCurrentState = State.IDLE;
            }
        }

        abstract void onStateChanged(AppBarLayout appBarLayout, State state);
    }


    interface Callback {
        void onConfirmTask(Task task);
    }
}
