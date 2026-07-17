package app.dontwastetimeapp.services;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import java.util.Calendar;

import app.dontwastetimeapp.database.AppPreferences;

public class DailyResetReceiver extends BroadcastReceiver {
    private static final String TAG = "DailyResetReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "Daily reset triggered");
        try (AppPreferences db = new AppPreferences(context)) {
            boolean success = db.resetAllTimeOuts();
            Log.d(TAG, "Reset all timeouts, success=" + success);
        }
        scheduleNext(context);
    }

    private void scheduleNext(Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, DailyResetReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context, 0, intent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 5);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
                    return;
            }
            alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
        } catch (SecurityException e) {
            alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
        }
    }
}