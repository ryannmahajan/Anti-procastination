package com.ryannm.android.sam;

import android.content.Context;
import android.preference.PreferenceManager;
import java.util.Set;

class QueryPreferences {
    private static final String PREF_FIRST_RUN = "firstRung";
    private static final String PREF_PACKAGE_TO_BLOCK = "blockThisPack";
    private static final String PREF_PUNISHMENT_TIME = "punishTim";
    private static final String PREF_ADD_NEW_TO_BOTTOM = "addNewTOBot";
    private static final String PREF_END_OF_DAY = "endOfDayPref";
    private static final String PREF_OLD_PUNISHMENT_TIME = "prefPunishPre";
    private static final String PREF_OLD_PACKAGE_TO_BLOCK = "prefOldPackBlockPlease";

    static boolean isFirstRun(Context c) {
        return PreferenceManager.getDefaultSharedPreferences(c).getBoolean(PREF_FIRST_RUN, true);
    }

    static void setIsFirstRun(Context c, boolean b) {
        PreferenceManager.getDefaultSharedPreferences(c)
                .edit()
                .putBoolean(PREF_FIRST_RUN, b)
                .apply();
    }

    static Boolean addNewToBottom(Context c) {
        int result = PreferenceManager.getDefaultSharedPreferences(c).getInt(PREF_ADD_NEW_TO_BOTTOM, -1);
        if (result==1) return true;
        else if (result==0) return false;
        else return null;
    }

    static void addNewToBottom(Context c, boolean yes) {
        PreferenceManager.getDefaultSharedPreferences(c)
                .edit()
                .putInt(PREF_ADD_NEW_TO_BOTTOM, yes? 1:0)
                .apply();
    }

    static Set<String> getPackagesToBlock(Context c) {
        return PreferenceManager.getDefaultSharedPreferences(c).getStringSet(PREF_PACKAGE_TO_BLOCK, null);
    }

    static void setPackagesToBlock(Context c, Set<String> packageNames) {
        PreferenceManager.getDefaultSharedPreferences(c)
                .edit()
                .putStringSet(PREF_PACKAGE_TO_BLOCK, packageNames)
                .apply();
    }

    // Use this to block. The old value is stored here, if packages are changed during the day
    static Set<String> getOldPackagesToBlock(Context c) {
        return PreferenceManager.getDefaultSharedPreferences(c).getStringSet(PREF_OLD_PACKAGE_TO_BLOCK, null);
    }

    static void setOldPackagesToBlock(Context c, Set<String> packageNames) {
        PreferenceManager.getDefaultSharedPreferences(c)
                .edit()
                .putStringSet(PREF_OLD_PACKAGE_TO_BLOCK, packageNames)
                .apply();
    }

    static String getPunishmentTime(Context c) {
        return PreferenceManager.getDefaultSharedPreferences(c).getString(PREF_PUNISHMENT_TIME, null);
    }

    // string must be in the format "hh.mm-HH.MM". 24-hour format. Midnight is 0.00
    static void setPunishmentTime(Context c, String punishmentTime) {
        PreferenceManager.getDefaultSharedPreferences(c)
                .edit()
                .putString(PREF_PUNISHMENT_TIME, punishmentTime)
                .apply();
    }
}
