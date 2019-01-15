package com.ryannm.android.sam;

import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.evernote.android.job.JobManager;
import com.evernote.android.job.JobRequest;

import java.util.List;
import java.util.Set;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class PunishmentFragment extends Fragment {
    @BindView(R.id.pie_chart_tom) TaskPieChart mPunishTom;
    @BindView(R.id.punishment_today) TextView mPunishTextToday;
    private Unbinder unbinder;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_punishment, container, false);
        unbinder = ButterKnife.bind(this,v);
        mPunishTom.refresh();
        refreshTodayPunishment();
        return v;
    }

    private void refreshTodayPunishment() {
        Integer reqdJobId = null;
        String timings = getString(R.string.none);
        Set<JobRequest> jobRequests = JobManager.instance().getAllJobRequestsForTag(ExactJob.TAG_BLOCKING_JOB);
        for (JobRequest jobRequest: jobRequests) {
            if (jobRequest.getStartMs() < 86400000) {
                reqdJobId = jobRequest.getJobId();
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
            timings = punishment.getTimings();
        }
        mPunishTextToday.setText(getString(R.string.todays_punishment, timings));
    }

    @CallSuper
    @Override
    public void onResume() {
        super.onResume();
      //  refresh();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }



}
