package app.dontwastetimeapp.services;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.widget.Toast;

import java.util.List;

import app.dontwastetimeapp.classes.AppInfo;
import app.dontwastetimeapp.database.AppPreferences;
import app.dontwastetimeapp.database.TimerPreferences;

public class AppBlockAccessibilityService extends AccessibilityService {
    private static final String TAG = "AppBlockService";
    private TimerPreferences timerPrefs;
    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        Log.d(TAG, "Event received: type=" + event.getEventType());
        if (event.getEventType() != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            return;
        }

        CharSequence packageNameCharSeq = event.getPackageName();
        if (packageNameCharSeq == null) {
            return;
        }
        String foregroundPackage = packageNameCharSeq.toString();

        if (foregroundPackage.equals(getPackageName())) {
            return; // never redirect away from our own app
        }

        try (AppPreferences db = new AppPreferences(this)) {
            List<AppInfo> apps = db.getAllApps();
            for (AppInfo app : apps) {
                boolean isRestricted = app.isBlocked() || app.isTimeOut();
                if (app.getPackageName().equals(foregroundPackage)
                        && (isRestricted
                        || timerPrefs.isFocusActive())) {
                    Log.d(TAG, "MATCH - redirecting home");
                    goHomeAndWarn();
                    return;
                }
            }
        }
    }

    private void goHomeAndWarn() {
        Log.d(TAG, "goHomeAndWarn() called");
        try {
            Intent homeIntent = new Intent(Intent.ACTION_MAIN);
            homeIntent.addCategory(Intent.CATEGORY_HOME);
            homeIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(homeIntent);
            Log.d(TAG, "startActivity(homeIntent) completed without throwing");
        } catch (Exception e) {
            Log.e(TAG, "Failed to start home intent", e);
        }

        new Handler(Looper.getMainLooper()).post(() -> {
            Log.d(TAG, "Toast handler running");
            Toast.makeText(this, "YOU HAVE TO FOCUS!", Toast.LENGTH_LONG).show();
        });
    }
    @Override
    public void onServiceConnected() {
        super.onServiceConnected();
        Log.d(TAG, "Accessibility service connected");
        timerPrefs = new TimerPreferences(this);
        AccessibilityServiceInfo info = new AccessibilityServiceInfo();
        info.eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED;
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC;
        info.notificationTimeout = 100;
        info.flags = AccessibilityServiceInfo.DEFAULT;

        setServiceInfo(info);

        Log.d(TAG, "Service info set programmatically: eventTypes=" + info.eventTypes);
    }

    @Override
    public void onInterrupt() {
        // required override, called if the system needs to stop this service abruptly - nothing to clean up here
    }
}