package com.ryannm.android.sam;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.DialogInterface;
import android.content.Intent;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;

import java.util.ArrayList;
import java.util.List;

public class TrackAppServiceVersion9 extends AccessibilityService {
    public static boolean isRunning;
    private static List<String> blockedPackages = new ArrayList<>();
   // private static String ID = "com.ryannm.android.sam/.TrackAppServiceVersion9";

    @Override
    public void onServiceConnected() {
        isRunning = true;
        // Set the type of events that this service wants to listen to.  Others
        // won't be passed to this service.
        AccessibilityServiceInfo info = getServiceInfo();

        info.eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED;// | AccessibilityEvent.TYPE_WINDOWS_CHANGED;

        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC;

        info.flags = AccessibilityServiceInfo.DEFAULT;

        info.notificationTimeout = 10;

        info.packageNames = null;

        setServiceInfo(info);
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event.getPackageName()==null) {
            Crashlytics.log("Unknown bug 1 event: "+event);
            return;
        }
        if (event.getEventType()==AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED && blockedPackages.contains(event.getPackageName().toString())) {//.equals("com.ryannm.android.sam")) {
            Intent startMain = new Intent(Intent.ACTION_MAIN);              // Todo: Unknown bug 1: NPE while calling .toString() above for the package name
            startMain.addCategory(Intent.CATEGORY_HOME);
            startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            getApplicationContext().startActivity(startMain);
        }
    }

    @Override
    public void onInterrupt() {
        isRunning = false;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        isRunning = false;
    }

    public static void addBlockedPackage(String blockedPackage) {
        TrackAppServiceVersion9.blockedPackages.add(blockedPackage);
        Log.d("TrackAppServiceVersion9", "Added to blocked packages "+blockedPackage);
    }

    // Returns true if supplied package name is the only blockedPackage or blockedPackages.size()==0. In any other case, returns false
    public static boolean ifLonePackage(String packageName) {
        return blockedPackages.isEmpty() || (blockedPackages.size()==1 && blockedPackages.contains(packageName));
    }

    public static void removeBlockedPackage(String packageName) {
        TrackAppServiceVersion9.blockedPackages.remove(packageName);
        Log.d("TrackAppServiceVersion9", "Removed from blocked packages"+packageName);
    }

    public static boolean isAccessibilityEnabled() {
        /*AccessibilityManager am = (AccessibilityManager) context.getSystemService(Context.ACCESSIBILITY_SERVICE);
        List<AccessibilityServiceInfo> runningServices = am.getEnabledAccessibilityServiceList(AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED);
        for (AccessibilityServiceInfo service : runningServices) {
            if (ID.equals(service.getId())) {
                return true;
            }
        }
        return false; */ // Due to a bug in android, one can't use above code and has to update service name on every update
        return isRunning;
    }

    static void showAccessibilityDialog(final Fragment fragment) {
        new AlertDialog.Builder(fragment.getActivity())
                .setTitle(R.string.accessibility_access_required)
                .setMessage(R.string.sam_accessibility_reasson)
                .setPositiveButton(R.string.enable_now, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
                        fragment.startActivity(intent);
                        Toast.makeText(fragment.getActivity(), R.string.enable_sam_from_the_list, Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    }
                })
                .create()
                .show();
    }

    static void removeAllBlockedPackages() {
        Log.d("TrackAppServiceVersion9", "Removed from blocked packages: "+ TextUtils.join(",", blockedPackages));
        blockedPackages.clear();
    }
}
