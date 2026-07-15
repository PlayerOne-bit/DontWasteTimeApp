package app.dontwastetimeapp.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import app.dontwastetimeapp.database.AppPreferences;

public class DailyResetReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        try (AppPreferences db = new AppPreferences(context)) {
            db.resetAllTimeOuts();
        }
    }
}