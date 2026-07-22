package app.dontwastetimeapp.database;

import android.content.Context;
import android.content.SharedPreferences;

public class TimerPreferences {
    private static final String PREFS_NAME = "timer_prefs";
    private static final String KEY_PHASE = "phase";               // "NONE", "FOCUS", "REST"
    private static final String KEY_IS_RUNNING = "is_running";
    private static final String KEY_END_TIME = "end_time_millis";   // valid only while running
    private static final String KEY_REMAINING = "remaining_millis"; // valid only while paused

    private final SharedPreferences prefs;

    public TimerPreferences(Context context) {
        prefs = context.getApplicationContext()
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public void start(String phase, long durationMillis) {
        long endTime = System.currentTimeMillis() + durationMillis;
        prefs.edit()
                .putString(KEY_PHASE, phase)
                .putLong(KEY_END_TIME, endTime)
                .putBoolean(KEY_IS_RUNNING, true)
                .apply();
    }

    public void pause() {
        prefs.edit()
                .putLong(KEY_REMAINING, getMillisLeft())
                .putBoolean(KEY_IS_RUNNING, false)
                .apply();
    }

    public void resume() {
        long remaining = prefs.getLong(KEY_REMAINING, 0);
        prefs.edit()
                .putLong(KEY_END_TIME, System.currentTimeMillis() + remaining)
                .putBoolean(KEY_IS_RUNNING, true)
                .apply();
    }

    public void stop() {
        prefs.edit()
                .putString(KEY_PHASE, "NONE")
                .putBoolean(KEY_IS_RUNNING, false)
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

    /** True only if the FOCUS phase is currently active and hasn't expired. */
    public boolean isFocusActive() {
        return isRunning() && "FOCUS".equals(getPhase()) && getMillisLeft() > 0;
    }
    public boolean isRestActive(){
        return isRunning() && "REST".equalsIgnoreCase(getPhase()) && getMillisLeft()>0;
    }
}