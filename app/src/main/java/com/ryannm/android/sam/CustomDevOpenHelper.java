package com.ryannm.android.sam;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import org.greenrobot.greendao.database.Database;

import static com.ryannm.android.sam.DaoMaster.dropAllTables;

public class CustomDevOpenHelper extends DaoMaster.DevOpenHelper {

    public CustomDevOpenHelper(Context context, String name) {
        super(context, name);
    }

    @Override
    public void onUpgrade(Database db, int oldVersion, int newVersion) {
        //Going from older schema to new schema
        if(oldVersion == 1 && newVersion == 2) // change these acc. to the version
        {
            Log.i("greenDAO", "Upgrading schema from version " + oldVersion + " to " + newVersion);
            /*boolean ifNotExists = false;
            //Leave old tables alone and only create ones that didn't exist
            //in the previous schema
            NewTable1Dao.createTable(db, ifNotExists); */ // In case you need to addTask tables

            db.execSQL("ALTER TABLE "+TaskDao.TABLENAME +" ADD "+TaskDao.Properties.ReminderBeforeMS.columnName+" BIGINT");
            db.execSQL("ALTER TABLE "+TaskDao.TABLENAME +" ADD "+TaskDao.Properties.ReminderJobId.columnName+" INTEGER");
        } else if (oldVersion == 2 && newVersion == 3) {
            Log.i("greenDAO", "Upgrading schema from version " + oldVersion + " to " + newVersion);
           // db.execSQL("ALTER TABLE "+TaskDao.TABLENAME +" ADD "+TaskDao.Properties.Difficulty.columnName+" SHORT"); Commented out cos difficulty doesn't exist anymore
        }

        else
        {
            super.onUpgrade(db, oldVersion, newVersion);
        }
    }
}
