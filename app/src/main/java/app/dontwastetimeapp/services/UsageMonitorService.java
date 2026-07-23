package app.dontwastetimeapp.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.app.usage.UsageEvents;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import java.util.Calendar;
import java.util.List;

import app.dontwastetimeapp.R;
import app.dontwastetimeapp.classes.AppInfo;
import app.dontwastetimeapp.database.AppPreferences;

public class UsageMonitorService extends Service {

    private static final String CHANNEL_ID = "usage_monitor_channel";
    private static final int NOTIFICATION_ID = 1;
    private static final long CHECK_INTERVAL_MS = 60000;

    private Handler handler;
    private Runnable checkRunnable;

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
        handler = new Handler(Looper.getMainLooper());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startForeground(NOTIFICATION_ID, buildNotification());

        checkRunnable = new Runnable() {
            @Override
            public void run() {
                checkUsageAndEnforceLimits();
                handler.postDelayed(this, CHECK_INTERVAL_MS);
            }
        };
        handler.post(checkRunnable);

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (handler != null && checkRunnable != null) {
            handler.removeCallbacks(checkRunnable);
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void checkUsageAndEnforceLimits() {
        try (AppPreferences db = new AppPreferences(this)) {
            List<AppInfo> apps = db.getAllApps();
            for (AppInfo app : apps) {
                int minutesUsed = getMinutesUsedToday(app.getPackageName());
                Log.d("UsageMonitor", app.getPackageName() + " minutesUsed=" + minutesUsed
                        + " limit=" + app.getDailyLimitMinutes());
                app.setMinutesUsedToday(minutesUsed);

                app.setTimeOut(minutesUsed >= app.getDailyLimitMinutes() && app.getDailyLimitMinutes() > 0);

                db.editApp(app);
            }
        }
    }

    private int getMinutesUsedToday(String packageName) {
        UsageStatsManager usm = (UsageStatsManager) getSystemService(Context.USAGE_STATS_SERVICE);
        long startTime = getLastResetBoundaryMillis();
        long endTime = System.currentTimeMillis();

        UsageEvents usageEvents = usm.queryEvents(startTime, endTime);
        long totalForegroundMillis = 0;
        long packageForegroundStart = -1;

            String lastForegroundPackage = null;

        UsageEvents.Event event = new UsageEvents.Event();
        while (usageEvents.hasNextEvent()) {
            usageEvents.getNextEvent(event);
            String pkg = event.getPackageName();
            int type = event.getEventType();

            if (type == UsageEvents.Event.MOVE_TO_FOREGROUND) {
                lastForegroundPackage = pkg;
                if (pkg.equals(packageName)) {
                    packageForegroundStart = event.getTimeStamp();
                }
            } else if (type == UsageEvents.Event.MOVE_TO_BACKGROUND) {
                if (pkg.equals(packageName) && packageForegroundStart != -1) {
                    totalForegroundMillis += event.getTimeStamp() - packageForegroundStart;
                    packageForegroundStart = -1;
                }
                if (pkg.equals(lastForegroundPackage)) {
                    lastForegroundPackage = null;
                }
            }
        }
        if (packageForegroundStart != -1 && packageName.equals(lastForegroundPackage)) {
            totalForegroundMillis += endTime - packageForegroundStart;
        }

        return (int) (totalForegroundMillis / 1000 / 60);
    }

    private long getLastResetBoundaryMillis() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 5);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        if (calendar.getTimeInMillis() > System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, -1);
        }
        return calendar.getTimeInMillis();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Usage Monitor",
                    NotificationManager.IMPORTANCE_LOW
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }

    private Notification buildNotification() {
        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Don't Waste Your Time")
                .setContentText("Monitoring your app usage")
                .setSmallIcon(R.drawable.app_icon)
                .setOngoing(true)
                .build();
    }
}