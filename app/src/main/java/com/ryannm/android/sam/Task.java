package com.ryannm.android.sam;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Keep;
import org.greenrobot.greendao.annotation.Property;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.DaoException;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

@Entity(
        // Flag to make an entity "active": Active entities have update,
        // mDelete, and refresh methods.
        active = true,

        // Whether getters and setters for properties should be generated if missing.
        generateGettersSetters = true
)
class Task {
    @Id
    private Long id;

    @Property
    private String name;

    @Property
    private Date deadline;

    @Property
    private long reminderBeforeMS;

    @Property
    private Integer reminderJobId;

    @Property
    private boolean completed;


    /** Used to resolve relations */
@Generated(hash = 2040040024)
private transient DaoSession daoSession;

/** Used for active entity operations. */
@Generated(hash = 1469429066)
private transient TaskDao myDao;

@Generated(hash = 1182562313)
public Task(Long id, String name, Date deadline, long reminderBeforeMS,
        Integer reminderJobId, boolean completed) {
    this.id = id;
    this.name = name;
    this.deadline = deadline;
    this.reminderBeforeMS = reminderBeforeMS;
    this.reminderJobId = reminderJobId;
    this.completed = completed;
}

@Generated(hash = 733837707)
public Task() {
}

public Long getId() {
    return this.id;
}

public void setId(Long id) {
    this.id = id;
}

public String getName() {
    return this.name;
}

public void setName(String name) {
    this.name = name;
}

public Date getDeadline() {
    return this.deadline;
}

public void setDeadline(Date deadline) {
    this.deadline = deadline;
}

/**
 * Convenient call for {@link org.greenrobot.greendao.AbstractDao#delete(Object)}.
 * Entity must attached to an entity context.
 */
@Generated(hash = 128553479)
public void delete() {
    if (myDao == null) {
        throw new DaoException("Entity is detached from DAO context");
    }
    myDao.delete(this);
}

/**
 * Convenient call for {@link org.greenrobot.greendao.AbstractDao#refresh(Object)}.
 * Entity must attached to an entity context.
 */
@Generated(hash = 1942392019)
public void refresh() {
    if (myDao == null) {
        throw new DaoException("Entity is detached from DAO context");
    }
    myDao.refresh(this);
}

/**
 * Convenient call for {@link org.greenrobot.greendao.AbstractDao#update(Object)}.
 * Entity must attached to an entity context.
 */
@Generated(hash = 713229351)
public void update() {
    if (myDao == null) {
        throw new DaoException("Entity is detached from DAO context");
    }
    myDao.update(this);
}

public long getReminderBeforeMS() {
    return this.reminderBeforeMS;
}

public void setReminderBeforeMS(long reminderBeforeMS) {
    this.reminderBeforeMS = reminderBeforeMS;
}

public Integer getReminderJobId() {
    return this.reminderJobId;
}

public void setReminderJobId(Integer reminderJobId) {
    this.reminderJobId = reminderJobId;
}

public boolean getCompleted() {
    return this.completed;
}

public void setCompleted(boolean completed) {
    this.completed = completed;
}

@Keep
// Returns either today's or yesterday's incomplete tasks depending on the boolean parameter
static float getPendingTasksPercent(boolean today) {
    List<Task> tasks = App.getDaoSession().getTaskDao().loadAll();
    float result=0f;

    if (today) {
        Calendar cal = new GregorianCalendar();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.setTimeInMillis(cal.getTimeInMillis() + 86400000); // Added one day to today's early morning
        Date midnight = cal.getTime();

        int todayTasks = 0;

        for (Task task: tasks) {
            if (!task.getDeadline().after(midnight)) {
                todayTasks++;
                if (task.getCompleted()) result++; // No. of completed tasks today
            }
        }
        if (todayTasks!=0) result/=(float) todayTasks; // Fraction of today's tasks that have been completed
        result = 1 - result; // Fraction of today's tasks that have not been completed

    }
    // todo: Determine if yesterday's tasks reqd. If so,addTask the functionality.

    return result*100;
}

/** called by internal mechanisms, do not call yourself. */
@Generated(hash = 1442741304)
public void __setDaoSession(DaoSession daoSession) {
    this.daoSession = daoSession;
    myDao = daoSession != null ? daoSession.getTaskDao() : null;
}
}
