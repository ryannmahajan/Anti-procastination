package com.ryannm.android.sam;

import android.app.Application;
import android.content.Context;
import android.text.format.DateFormat;
import com.crashlytics.android.Crashlytics;
import com.evernote.android.job.Job;
import com.evernote.android.job.JobCreator;
import com.evernote.android.job.JobManager;
import com.instabug.library.Instabug;
import com.instabug.library.invocation.InstabugInvocationEvent;

import org.greenrobot.greendao.database.Database;

import java.util.Calendar;
import java.util.GregorianCalendar;

import io.fabric.sdk.android.Fabric;

public class App extends Application {

    private static DaoSession daoSession;

    @Override
    public void onCreate() {
        super.onCreate();

        // GreenDao set-up
        DaoMaster.DevOpenHelper helper = new CustomDevOpenHelper(this, "notes-db");
        Database db = helper.getWritableDb();
        daoSession = new DaoMaster(db).newSession();

        // Evernote's job api set-up
        JobManager.create(this).addJobCreator(new JobCreator() {
            @Override
            public Job create(String tag) {
                return new ExactJob();
            }
        });

        Fabric.with(this, new Crashlytics()); // Crashlytics set-up

        new Instabug.Builder(this, "cee1b0884f9310d1c98b61006f58ffa5") // InstaBug set-up
                .setInvocationEvent(InstabugInvocationEvent.NONE)
                .build();
    }

    public static DaoSession getDaoSession() {
        return daoSession;
    }

    // Typically used to format deadlines nicely
    public static String getHumanReadableDateTime(Calendar deadline, Context context) {
        int daysBetweenNowAndDeadline = deadline.get(Calendar.DAY_OF_YEAR) - new GregorianCalendar().get(Calendar.DAY_OF_YEAR);
        if (daysBetweenNowAndDeadline==0) return (String) DateFormat.format("h:mm a", deadline); // 07:30 am
        else if (daysBetweenNowAndDeadline==1) return context.getString(R.string.tomorrow_short)+" "+DateFormat.format("h:mm a", deadline); //Tom 07:30 am
        else if (daysBetweenNowAndDeadline<=7) return (String) DateFormat.format("EEE, h:mm a", deadline); // Sat, 07:30 am
        else return (String) DateFormat.format("MMM d, h:mm a", deadline); // Jul 14, 07:30 am
        // Todo : Handle deadline set to more than a year away
    }

    // time must be in format "hh.mm". Returns Calendar object linked to the NEXT occurrence of the given time
    static Calendar getCalendarFromString(String time) {
        GregorianCalendar cal = new GregorianCalendar();
        String[] splitTime = time.split("\\.");

        cal.set(Calendar.HOUR_OF_DAY, Integer.parseInt(splitTime[0]));
        cal.set(Calendar.MINUTE, Integer.parseInt(splitTime[1]));
        if (cal.before(new GregorianCalendar())) cal.setTimeInMillis(cal.getTimeInMillis() + 86400000); // So that makes sure its the next occurrence, not the previous
        return cal;
    }
}
