package app.dontwastetimeapp.database;

import android.content.Context;
import android.content.SharedPreferences;

public class TimerPreferences {
    private static final String PREFS_NAME = "timer_prefs";
    private static final String KEY_PHASE = "phase";
    private static final String KEY_IS_RUNNING = "is_running";
    private static final String KEY_END_TIME = "end_time_millis";
    private static final String KEY_REMAINING = "remaining_millis";
    private static final String MAX_TIME = "max_time";
    private final SharedPreferences prefs;

    public TimerPreferences(Context context) {
        prefs = context.getApplicationContext()
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public void start(String phase, long durationMillis, int maxTime) {
        long endTime = System.currentTimeMillis() + durationMillis;
        prefs.edit()
                .putString(KEY_PHASE, phase)
                .putInt(MAX_TIME, maxTime)
                .putLong(KEY_END_TIME, endTime)
                .putBoolean(KEY_IS_RUNNING, true)
                .apply();
    }



    public void stop() {
        prefs.edit()
                .putString(KEY_PHASE, "NONE")
                .putBoolean(KEY_IS_RUNNING, false)
                .remove(MAX_TIME)
                .remove(KEY_END_TIME)
                .remove(KEY_REMAINING)
                .apply();
    }

    public boolean isRunning() {
        return prefs.getBoolean(KEY_IS_RUNNING, false);
    }

    public String getPhase() {
        return prefs.getString(KEY_PHASE, "NONE");
    }

    public long getMillisLeft() {
        if (!isRunning()) return prefs.getLong(KEY_REMAINING, 0);
        long endTime = prefs.getLong(KEY_END_TIME, 0);
        return Math.max(0, endTime - System.currentTimeMillis());
    }
    public int getMax(){
        return prefs.getInt(MAX_TIME,0);
    }

    public boolean isFocusActive() {
        return isRunning() && "FOCUS".equals(getPhase()) && getMillisLeft() > 0;
    }
    public boolean isRestActive(){
        return isRunning() && "REST".equalsIgnoreCase(getPhase()) && getMillisLeft()>0;
    }
}