package com.ryannm.android.sam;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Keep;
import org.greenrobot.greendao.annotation.Property;
import org.greenrobot.greendao.annotation.Generated;

@Entity
class Punishment {

    @Id
    private Long id;

    @Property
    private String timings;

    @Property
    private Integer jobId; // This holds first the  midnight job id and then, the block job id, and then the unblock job id

    @Property
    private int initialPunishmentMinutes; // This is to reliably increase or decrease punishment

    @Generated(hash = 1197464324)
    public Punishment(Long id, String timings, Integer jobId, int initialPunishmentMinutes) {
        this.id = id;
        this.timings = timings;
        this.jobId = jobId;
        this.initialPunishmentMinutes = initialPunishmentMinutes;
    }

    @Keep
    private Punishment() {
    }

    Long getId() {
        return this.id;
    }

    void setId(Long id) {
        this.id = id;
    }

    Integer getJobId() {
        return this.jobId;
    }

    void setJobId(Integer jobId) {
        this.jobId = jobId;
    }

    void initEndOfDayJob() {
        setJobId(ExactJob.scheduleEndOfDayJob());
    }

    void deleteEndOfDayJob() {
        ExactJob.cancelJob(getJobId());
        setJobId(null);
        App.getDaoSession().getPunishmentDao().insertOrReplace(this);
    }

    public String getTimings() {
        return this.timings;
    }

    public void setTimings(String timings) {
        this.timings = timings;
    }

    public void changeTimingsBy(double fractionOfTime) {
        String[] times = timings.split("-");

        int diffMins = convertToMins(times[1]) - convertToMins(times[0]);
        if (diffMins<0) diffMins+=convertToMins(24.00);
        diffMins += Math.round(getInitialPunishmentMinutes()*fractionOfTime);

        timings = returnTimingsString(times[0], diffMins);
        App.getDaoSession().getPunishmentDao().insertOrReplace(this);
    }

    // startTime must be as hh.mm todo: Make sure all strings are
    private String returnTimingsString(String startTime, int diffMins) {
        double diffHours = ((int) diffMins/60)+ (double)(diffMins%60)/100;
        diffHours = Math.round(diffHours*100); // So that only 2 digits after the point
        diffHours/=100;
        if (diffHours - (int)diffHours>=0.6) {
            diffHours+=1;
            diffHours-=0.6;
        }
        return (startTime+"-"+(Double.parseDouble(startTime)+diffHours));
    }

    // Reqd. hh.mm
    static int convertToMins(double hours) {
        int hoursInt = (int) Math.round(hours);
        return (int) (hoursInt*60 + (hours-hoursInt)*100);

    } // todo: Probably won't work for 11 pm to 1 am. Update: should now due to line 76

    static int convertToMins(String hours) {
        return convertToMins(Double.parseDouble(hours));
    }

    public int getInitialPunishmentMinutes() {
        return this.initialPunishmentMinutes;
    }

    public void setInitialPunishmentMinutes(int initialPunishmentMinutes) {
        this.initialPunishmentMinutes = initialPunishmentMinutes;
    }
}
