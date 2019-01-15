package com.ryannm.android.sam;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.v4.app.Fragment;

public class MainActivity extends SinglePaneActivity {
    private static final String SHOW_INFO_DIALOG = "showInfoBro";
    private boolean firstTime; // First time viewing the mainActivity i.e after going through the app intro

    // todo: Save instance state on destruction
    @Override
    Fragment getFragment() {
        return new PagerFragment().setScreen(PagerFragment.MAIN);
    }

    @CallSuper
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        firstTime = getIntent().getBooleanExtra(SHOW_INFO_DIALOG, false);
        if (firstTime) {
            // todo : Show info dialog. Tell them these settings will be saved. ooh, and an exit dialog!
        }
        if (QueryPreferences.isFirstRun(this)) {
            Intent i = new Intent(this, AppIntroActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NO_HISTORY);
            startActivity(i);
            finish();
        }
    }

    // First time viewing the mainActivity i.e after going through the app intro
    public static Intent firstTimeIntent(Context context) {
        Intent i = new Intent(context, MainActivity.class);
        i.putExtra(SHOW_INFO_DIALOG,true);
        return i;
    }
}
