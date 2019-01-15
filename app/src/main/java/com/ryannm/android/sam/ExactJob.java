package com.ryannm.android.sam;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.evernote.android.job.Job;
import com.evernote.android.job.JobManager;
import com.evernote.android.job.JobRequest;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Set;

class ExactJob extends Job {

    static final String TAG_BLOCKING_JOB = "blockingJobSam";
    static final String TAG_UNBLOCKING_JOB = "unblockingJobSam";
    private static final String TAG_REMINDER_JOB = "reminderScSam";
    static final String TAG_END_OF_DAY_JOB = "endDaySamJob";
    private static final int BLOCK_NOTIFICATION_ID = 711;
    private static final long THIRTY_SECONDS = 30000;
    private static final int REMIND_NOTIFICATION_ID = 7328;


    @NonNull
    @Override
    protected Result onRunJob(Params params) {
        Log.d(params.getTag(), "Running job: "+params.getTag());
        switch (params.getTag()) {
            case TAG_BLOCKING_JOB:
                return onRunBlockingJob(params.getId(), getContext());
            case TAG_UNBLOCKING_JOB:
                return onRunUnblockingJob(params.getId(), getContext());
            case TAG_REMINDER_JOB:
                return onRemind(params.getId(), getContext());
            case TAG_END_OF_DAY_JOB:
                return onDayEnd(params.getId(), getContext());
        }

        return Result.FAILURE;
    }

    // Why have an end of day job? Well, we would have to run a job at midnight to detect all the completed tasks anyway.
    // We can't do the detection at blocking time since the user might have ticked after end of day.
    @NonNull
    Result onDayEnd(int jobId, Context context) {
        Punishment punishment = getPunishmentWithJobId(jobId);
        if (punishment==null) return Result.FAILURE;
        String time = punishment.getTimings();
        String[] times = time.split("-");

        Calendar blockCal = App.getCalendarFromString(times[0]);
        Calendar unBlockCal = App.getCalendarFromString(times[1]);

        long blockTimeMS = blockCal.getTimeInMillis() - System.currentTimeMillis();
        punishment.setJobId(ExactJob.scheduleBlocking(blockTimeMS));
        // todo: Below this isn't running?
        ExactJob.scheduleUnblocking((long) (blockTimeMS + (unBlockCal.getTimeInMillis()-blockCal.getTimeInMillis())*Task.getPendingTasksPercent(true)/100));
        App.getDaoSession().getTaskDao().deleteAll(); // todo: Add to statistics here

        return Result.SUCCESS;
    }

    @NonNull
    Result onRunBlockingJob(int jobId, Context context) {
        Punishment punishment = getPunishmentWithJobId(jobId);
        if (punishment==null) return Result.FAILURE;

        Set<String> packages = QueryPreferences.getOldPackagesToBlock(context);
        if (packages==null) packages = QueryPreferences.getPackagesToBlock(context);
        showBlockingNotification(packages,context);
        for (String pkgName: packages) {
            TrackAppServiceVersion9.addBlockedPackage(pkgName);
        }

        App.getDaoSession().getPunishmentDao().delete(punishment);
        // todo: Eventually delete punishment in unblocking job and schedule it here. And make punishment store blocked packages, so that we don't have to worry about multiple changes to it
        return Result.SUCCESS;
    }

    @Nullable
    private Punishment getPunishmentWithJobId(int jobId) {
        List<Punishment> punishments = App.getDaoSession().getPunishmentDao().loadAll();
        Punishment punishment = null;
        for (Punishment p: punishments) {
            if (p.getJobId().equals(jobId)) {
                punishment = p;
                break;
            }
        }
        return punishment;
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    private void showBlockingNotification(Set<String> packages, Context context) {
        List<String> appNames = new ArrayList<>(packages.size());

        Calendar cal = new GregorianCalendar();
        cal.setTimeInMillis((JobManager.instance().getAllJobRequestsForTag(TAG_UNBLOCKING_JOB).toArray(new JobRequest[0]))[0].getEndMs() + cal.getTimeInMillis());// task.getBlockDuration());

        for (String pkgName: packages) {
            try {
                appNames.add(context.getPackageManager().getApplicationInfo(pkgName, 0).loadLabel(context.getPackageManager()).toString());
            } catch (PackageManager.NameNotFoundException e) {
                appNames.add(context.getString(R.string.unknown_app));
            }
        }

        Notification notif = new Notification.Builder(context)
                .setContentTitle(context.getString(R.string.blocking_till, packages.size() + " apps", App.getHumanReadableDateTime(cal ,context)))
               // .setContentText(context.getString(R.string.blocking_till, TextUtils.join(", ", appNames),App.getHumanReadableDateTime(cal ,context)))
                .setSmallIcon(R.mipmap.ic_launcher)
                .setShowWhen(true)
                .setStyle(new Notification.BigTextStyle().bigText(TextUtils.join(", ", appNames)))
                .setOngoing(true)
                .setAutoCancel(true)
                .build();

        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify("", BLOCK_NOTIFICATION_ID, notif);
    }

    @NonNull
    Result onRunUnblockingJob(int jobId, Context context) {
        TrackAppServiceVersion9.removeAllBlockedPackages(); // todo: This & below line assume that there's only 1 ongoing punishment
        if (TrackAppServiceVersion9.isRunning) context.stopService(new Intent(context, TrackAppServiceVersion9.class));
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.cancel(""/*task.getBlockedPackage()*/, BLOCK_NOTIFICATION_ID);
        return Result.SUCCESS;
    }

    @NonNull
    private Result onRemind(int jobId, Context context) {
        for (Task task: App.getDaoSession().getTaskDao().loadAll()) {
            Log.d(TAG_REMINDER_JOB, "Task has reminder job id: "+task.getReminderJobId()+" and params.getId()="+jobId);
            if (task.getReminderJobId()==jobId) {
                String deadlineUpIn = null;
                String[] reminderValues = context.getResources().getStringArray(R.array.reminder_values);
                for (int i=0; i < reminderValues.length; i++) {
                    if (Long.parseLong(reminderValues[i])==task.getReminderBeforeMS()) deadlineUpIn = context.getResources().getStringArray(R.array.reminder_values)[i];
                }

                Notification notif = new Notification.Builder(context)
                        .setContentTitle(task.getName())
                        .setContentText(context.getString(R.string.deadline_approaching, deadlineUpIn))
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setOngoing(true)
                        .setAutoCancel(true)
                        .build();

                NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                manager.notify(REMIND_NOTIFICATION_ID, notif);

                return Result.SUCCESS;
            }
        }
        return Result.FAILURE; // cos if it reached this point, no task had the jobId of the running job
    }

    // Schedule a job that blocks the package stored in the task that contains the returned jobId
    private static int scheduleBlocking(long exactInMs) {
        return new JobRequest.Builder(TAG_BLOCKING_JOB)
                .setExact(exactInMs)//ONE_MINUTE)
                .setPersisted(true)
                .setBackoffCriteria(100, JobRequest.BackoffPolicy.LINEAR)
                .build()
                .schedule();
    }

    // Schedule a job that blocks the package stored in the task that contains the returned jobId
    private static int scheduleUnblocking(long exactInMs) {
        return new JobRequest.Builder(TAG_UNBLOCKING_JOB)
                .setExact(exactInMs)//+ONE_MINUTE)
                .setPersisted(true)
                .setBackoffCriteria(100, JobRequest.BackoffPolicy.LINEAR)
                .build()
                .schedule();
    }

    // Schedule a job that shows a reminder notification at a certain time
    static int scheduleReminder(long InMs) {
        if (InMs==0 || InMs==1) return -1;
        return new JobRequest.Builder(TAG_REMINDER_JOB)
                .setExecutionWindow(InMs - THIRTY_SECONDS, InMs + THIRTY_SECONDS)
               // .setExact(InMs)//+ONE_MINUTE)
                .setPersisted(true)
                .setBackoffCriteria(100, JobRequest.BackoffPolicy.LINEAR)
                .build()
                .schedule();
    }

    static boolean cancelJob(int jobId) {
        return JobManager.instance().cancel(jobId);
    }

    static int scheduleEndOfDayJob() {
        Calendar midnight = new GregorianCalendar();
        midnight.set(Calendar.HOUR_OF_DAY, midnight.getMinimum(Calendar.HOUR_OF_DAY));
        midnight.set(Calendar.MINUTE, midnight.getMinimum(Calendar.HOUR_OF_DAY));
        midnight.setTimeInMillis(midnight.getTimeInMillis() + 86400000);
        long startMs = midnight.getTimeInMillis()-System.currentTimeMillis();

        return new JobRequest.Builder(TAG_END_OF_DAY_JOB)
                .setExact(startMs)//, startMs + 10800000) // Execute b/w 12 - 2 am
                .setPersisted(true)
                .setBackoffCriteria(100, JobRequest.BackoffPolicy.LINEAR)
                .build()
                .schedule();
    }
}