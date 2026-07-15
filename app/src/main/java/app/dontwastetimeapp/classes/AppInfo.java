package app.dontwastetimeapp.classes;

public class AppInfo {
    private final int id;
    private final String packageName;
    private final String appName;
    private int dailyLimitMinutes;
    private int minutesUsedToday;
    private boolean blocked;
    private boolean timeOut;
    public AppInfo(String packageName, String appName, int dailyLimitMinutes) {
        this.id=0;
        this.packageName = packageName;
        this.appName = appName;
        this.dailyLimitMinutes = dailyLimitMinutes;
        this.minutesUsedToday = 0;
        this.blocked = false;
        this.timeOut = false;
    }
    public AppInfo(int id, String packageName, String appName, int dailyLimitMinutes) {
        this.id=id;
        this.packageName = packageName;
        this.appName = appName;
        this.dailyLimitMinutes = dailyLimitMinutes;
        this.minutesUsedToday = 0;
        this.blocked = false;
        this.timeOut = false;
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
    public boolean isTimeOut(){return timeOut; }
    public void setDailyLimitMinutes(int dailyLimitMinutes) {
        this.dailyLimitMinutes = dailyLimitMinutes;
    }

    public void setMinutesUsedToday(int minutesUsedToday) {
        this.minutesUsedToday = minutesUsedToday;
    }

    public void setBlocked(boolean blocked) {
        this.blocked = blocked;
    }
    public void setTimeOut(boolean timeOut){this.timeOut = timeOut;}

    public boolean isOverLimit() {
        return minutesUsedToday >= dailyLimitMinutes;
    }

    public boolean isCurrentlyBlocked() {
        return blocked || isOverLimit();
    }
}