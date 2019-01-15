package com.ryannm.android.sam;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import com.github.paolorotolo.appintro.AppIntro2;
import com.github.paolorotolo.appintro.AppIntro2Fragment;
import com.github.paolorotolo.appintro.ISlidePolicy;
import com.simplicityapks.reminderdatepicker.lib.TimeSpinner;

import java.util.HashSet;
import java.util.Set;

public class AppIntroActivity extends AppIntro2 implements ISlidePolicy{
    private Set<String> appsSelected = new HashSet<>();
    private String punishmentTime; // In format "hh.mm-HH.MM"

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addSlide(AppIntro2Fragment.newInstance(getString(R.string.welcome_to_sam), getString(R.string.use_gamification_get_things_done), R.mipmap.ic_launcher, getResources().getColor(R.color.colorPrimary)));
        //addSlide(AppIntro2Fragment.newInstance(getString(R.string.easily_send_feedback), getString(R.string.simply_shake_the_device), R.drawable.ic_shake_white, getResources().getColor(R.color.colorPrimary))); // Shake to send feedback
        addSlide(new PackageSelectDialogFragment().onPackageSelected(new PackageSelectDialogFragment.Callback() {
            @Override
            public void onPackageClick(String packageName, DialogFragment dialogFragment, boolean selected) {
                if (selected) appsSelected.add(packageName);
                else appsSelected.remove(packageName);
            }
        }));

        showSkipButton(false);
        setProgressButtonEnabled(true);
    }

    @Override
    public void onSkipPressed(Fragment currentFragment) {
        super.onSkipPressed(currentFragment);
      //  if (TrackAppService.isAccessibilityEnabled(currentFragment.getActivity())) TrackAppService.showAccessibilityDialog(currentFragment);
        //  todo: Add a tutorial when app gets complicated enough
    }

    @Override
    public void onDonePressed(Fragment currentFragment) {
        super.onDonePressed(currentFragment);
        if (isPolicyRespected()) showPunishTimeDialog(currentFragment);

        //  if (TrackAppService.isAccessibilityEnabled(currentFragment.getActivity())) TrackAppService.showAccessibilityDialog(currentFragment);
    }

    private void showPunishTimeDialog(Fragment currentFragment) {
        if (currentFragment instanceof PackageSelectDialogFragment) {
            View v = LayoutInflater.from(this).inflate(R.layout.block_timings_choose, null);
            TimeSpinner fromSpinner = (TimeSpinner) v.findViewById(R.id.from_spinner);
            final TimeSpinner toSpinner = (TimeSpinner) v.findViewById(R.id.to_spinner);
            fromSpinner.setOnTimeSelectedListener(new TimeSpinner.OnTimeSelectedListener() {
                @Override
                public void onTimeSelected(int hour, int minute) {
                    toSpinner.setSelectedTime(hour + 3, minute);
                    punishmentTime = getPunishmentTimingString(hour, minute);
                }
            });
            toSpinner.setOnTimeSelectedListener(new TimeSpinner.OnTimeSelectedListener() {
                @Override
                public void onTimeSelected(int hour, int minute) {
                    punishmentTime = punishmentTime.concat("-" + getPunishmentTimingString(hour, minute));
                    // todo: User might select the toSpinner first
                }
            });

            new AlertDialog.Builder(this)
                    .setView(v)
                    .setTitle("When to block?")
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            startActivity(MainActivity.firstTimeIntent(AppIntroActivity.this));
                            QueryPreferences.setPunishmentTime(AppIntroActivity.this, punishmentTime);
                            QueryPreferences.setIsFirstRun(AppIntroActivity.this, false);
                            QueryPreferences.setPackagesToBlock(AppIntroActivity.this, appsSelected);
                        }
                    })
                    .show();
        }
    }

    // returns as "hh.mm"
    static String getPunishmentTimingString(int hour, int minute) {
        return "" + hour + "." + minute;
    }

    @Override
    public boolean isPolicyRespected() {
        if(AppIntroActivity.this.getPager().getCurrentItem()==1 && appsSelected.size()<4) {
            Toast.makeText(this, "Please select at least 4 apps", Toast.LENGTH_SHORT).show(); // Shouldn't be here. But, temp. work-around
            return false;
        }
        return true;
    }

    @Override
    public void onUserIllegallyRequestedNextPage() {
        if(AppIntroActivity.this.getPager().getCurrentItem()==1) {
            Toast.makeText(this, "Please select at least 4 apps", Toast.LENGTH_SHORT).show();
        }
    }
}