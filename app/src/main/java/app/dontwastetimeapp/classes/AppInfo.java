package app.dontwastetimeapp.classes;

public class AppInfo {
    private int id;
    private String packageName;
    private String appName;
    private int dailyLimitMinutes;
    private int minutesUsedToday;
    private boolean blocked;

    public AppInfo(int id, String packageName, String appName, int dailyLimitMinutes) {
        this.id=id;
        this.packageName = packageName;
        this.appName = appName;
        this.dailyLimitMinutes = dailyLimitMinutes;
        this.minutesUsedToday = 0;
        this.blocked = false;
    }
    public int getId(){return id;}
    public String getPackageName() {
        return packageName;
    }

    public String getAppName() {
        return appName;
    }

    public int getDailyLimitMinutes() {
        return dailyLimitMinutes;
    }

    public int getMinutesUsedToday() {
        return minutesUsedToday;
    }

    public boolean isBlocked() {
        return blocked;
    }

    public void setDailyLimitMinutes(int dailyLimitMinutes) {
        this.dailyLimitMinutes = dailyLimitMinutes;
    }

    public void setMinutesUsedToday(int minutesUsedToday) {
        this.minutesUsedToday = minutesUsedToday;
    }

    public void setBlocked(boolean blocked) {
        this.blocked = blocked;
    }

    public boolean isOverLimit() {
        return minutesUsedToday >= dailyLimitMinutes;
    }

    public boolean isCurrentlyBlocked() {
        return blocked || isOverLimit();
    }
}