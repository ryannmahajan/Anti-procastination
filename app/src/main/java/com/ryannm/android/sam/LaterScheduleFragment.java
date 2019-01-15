package com.ryannm.android.sam;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.evernote.android.job.JobManager;
import com.evernote.android.job.JobRequest;
import com.simplicityapks.reminderdatepicker.lib.TimeSpinner;

import java.util.Calendar;
import java.util.List;
import java.util.Set;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class LaterScheduleFragment extends Fragment {
    private Unbinder unbinder;
    @BindView(R.id.button1) Button EndOfDayButton;
    @BindView(R.id.button2) Button BlockingButton;
    @BindView(R.id.button3) Button UnblockingButton;
    @BindView(R.id.button4) Button TimingsChangeButton;
    String punishmentTime;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.job_fragment, container, false);
        unbinder = ButterKnife.bind(this, v);

        refreshEndOfDay();
        refreshBlocking();
        refreshUnblocking();

        TimingsChangeButton.setText("Current block-unblock timings: "+QueryPreferences.getPunishmentTime(getActivity()));
        TimingsChangeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPunishTimeDialog(LaterScheduleFragment.this);
            }
        });

        return v;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    private void refreshEndOfDay() {
        Integer reqdJobId = null;
        Set<JobRequest> jobRequests = JobManager.instance().getAllJobRequestsForTag(ExactJob.TAG_END_OF_DAY_JOB);
        JobRequest jobRequest = null;
        for (JobRequest j: jobRequests) {
            if (j.getStartMs() < 86400000) {
                reqdJobId = j.getJobId();
                jobRequest = j;
                break; // So get the first (chronological?) job within 24 hours
            }
        }
        List<Punishment> punishments = App.getDaoSession().getPunishmentDao().loadAll();
        Punishment punishment = null; // Punishment object that holds the key to today's blocking
        for (Punishment p: punishments) {
            if (p.getJobId()!=null && p.getJobId().equals(reqdJobId)) {
                punishment = p;
                break;
            }
        }
        if (punishment!=null && punishment.getJobId()!=null) { // So, there's a blocking for today
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(jobRequest.getScheduledAt() + jobRequest.getStartMs());
            EndOfDayButton.setText(App.getHumanReadableDateTime(cal, getActivity()));
            final JobRequest finalJobRequest = jobRequest;
            EndOfDayButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new ExactJob().onDayEnd(finalJobRequest.getJobId(), getActivity());
                    finalJobRequest.cancelAndEdit();
                    EndOfDayButton.setText("No end of day job");
                    refreshBlocking();
                    refreshUnblocking();
                }
            });
        }
    }

    private void refreshBlocking() {
        Integer reqdJobId = null;
        Set<JobRequest> jobRequests = JobManager.instance().getAllJobRequestsForTag(ExactJob.TAG_BLOCKING_JOB);
        JobRequest jobRequest = null;
        for (JobRequest j: jobRequests) {
            if (j.getStartMs() < 86400000) {
                reqdJobId = j.getJobId();
                jobRequest = j;
                break; // So get the first (chronological?) job within 24 hours
            }
        }
        List<Punishment> punishments = App.getDaoSession().getPunishmentDao().loadAll();
        Punishment punishment = null; // Punishment object that holds the key to today's blocking
        for (Punishment p: punishments) {
            if (p.getJobId()!=null && p.getJobId().equals(reqdJobId)) {
                punishment = p;
                break;
            }
        }
        if (punishment!=null && punishment.getJobId()!=null) { // So, there's a blocking for today
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(jobRequest.getScheduledAt() + jobRequest.getStartMs());
            BlockingButton.setText(App.getHumanReadableDateTime(cal, getActivity()));
            final JobRequest finalJobRequest = jobRequest;
            BlockingButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new ExactJob().onRunBlockingJob(finalJobRequest.getJobId(), getActivity());
                    finalJobRequest.cancelAndEdit();
                    BlockingButton.setText("No blocking job");
                }
            });
        }
    }

    private void refreshUnblocking() {
        Integer reqdJobId = null;
        Set<JobRequest> jobRequests = JobManager.instance().getAllJobRequestsForTag(ExactJob.TAG_UNBLOCKING_JOB);
        JobRequest jobRequest = null;
        for (JobRequest j: jobRequests) {
            if (j.getStartMs() < 86400000) {
                reqdJobId = j.getJobId();
                jobRequest = j;
                break; // So get the first (chronological?) job within 24 hours
            }
        }
        List<Punishment> punishments = App.getDaoSession().getPunishmentDao().loadAll();
        Punishment punishment = null; // Punishment object that holds the key to today's blocking
        for (Punishment p: punishments) {
            if (p.getJobId()!=null && p.getJobId().equals(reqdJobId)) {
                punishment = p;
                break;
            }
        }
        if (punishment!=null && punishment.getJobId()!=null) { // So, there's a blocking for today
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(jobRequest.getScheduledAt() + jobRequest.getStartMs());
            UnblockingButton.setText(App.getHumanReadableDateTime(cal, getActivity()));
            final JobRequest finalJobRequest = jobRequest;
            UnblockingButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new ExactJob().onRunUnblockingJob(finalJobRequest.getJobId(), getActivity());
                    finalJobRequest.cancelAndEdit();
                    UnblockingButton.setText("No unblocking job");
                }
            });
        }
    }

    private void showPunishTimeDialog(final Fragment currentFragment) {
        View v = LayoutInflater.from(currentFragment.getActivity()).inflate(R.layout.block_timings_choose, null);
        TimeSpinner fromSpinner = (TimeSpinner) v.findViewById(R.id.from_spinner);
        final TimeSpinner toSpinner = (TimeSpinner) v.findViewById(R.id.to_spinner);
        fromSpinner.setOnTimeSelectedListener(new TimeSpinner.OnTimeSelectedListener() {
            @Override
            public void onTimeSelected(int hour, int minute) {
                toSpinner.setSelectedTime(hour + 3, minute);
                punishmentTime = AppIntroActivity.getPunishmentTimingString(hour, minute);
            }
        });
        toSpinner.setOnTimeSelectedListener(new TimeSpinner.OnTimeSelectedListener() {
            @Override
            public void onTimeSelected(int hour, int minute) {
                punishmentTime = punishmentTime.concat("-" + AppIntroActivity.getPunishmentTimingString(hour, minute));
            }
        });

        new AlertDialog.Builder(currentFragment.getActivity())
                .setView(v)
                .setTitle("When to block?")
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        QueryPreferences.setPunishmentTime(currentFragment.getActivity(), punishmentTime);
                        TimingsChangeButton.setText("Current block-unblock timings: "+punishmentTime);
                    }
                })
                .show();
    }


}
