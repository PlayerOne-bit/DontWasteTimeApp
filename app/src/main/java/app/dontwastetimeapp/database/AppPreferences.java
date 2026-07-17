package app.dontwastetimeapp.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import app.dontwastetimeapp.classes.AppInfo;

public class AppPreferences extends SQLiteOpenHelper {
    private static final String NAME="appdata.db";
    private static final int VERSION=1;
    private static final String TABLE_APP_INFO="app_info";
    private static final String COLUMN_ID="id";
    private static final String COLUMN_PACKAGE_NAME="package_name";
    private static final String COLUMN_APP_NAME="app_name";
    private static final String COLUMN_DAILY_LIMIT_MINUTES="daily_limit_minutes";
    private static final String COLUMN_MINUTES_USED_TODAY="minutes_used_today";
    private static final String COLUMN_BLOCKED="blocked";
    private static final String COLUMN_TIME_OUT="time_out";
    public AppPreferences(@Nullable Context context) {
        super(context, NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE "+TABLE_APP_INFO+"("+
                COLUMN_ID+" INTEGER PRIMARY KEY AUTOINCREMENT UNIQUE,"+
                COLUMN_PACKAGE_NAME+" TEXT," +
                COLUMN_APP_NAME+" TEXT,"+
                COLUMN_DAILY_LIMIT_MINUTES+" INTEGER,"+
                COLUMN_MINUTES_USED_TODAY+" INTEGER,"+
                COLUMN_BLOCKED+" BOOL,"+
                COLUMN_TIME_OUT+" BOOL"+
                ");");
    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS "+TABLE_APP_INFO);
        onCreate(db);
    }
    public AppInfo getApp(int id){
        AppInfo app=null;
        try(SQLiteDatabase db = this.getReadableDatabase()) {
            try (Cursor cursor = db.query(TABLE_APP_INFO, null, COLUMN_ID + "=?", new String[]{String.valueOf(id)}, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    String packageName = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PACKAGE_NAME));
                    String appName = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_APP_NAME));
                    int dailyLimitMinutes = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_DAILY_LIMIT_MINUTES));
                    int minutesUsedToday = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_MINUTES_USED_TODAY));
                    int block = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_BLOCKED));
                    int timeOut = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_TIME_OUT));
                    boolean isBlocked = (block == 1);
                    boolean isTimeOut = (timeOut == 1);
                    app = new AppInfo(id, packageName, appName, dailyLimitMinutes);
                    app.setMinutesUsedToday(minutesUsedToday);
                    app.setBlocked(isBlocked);
                    app.setTimeOut(isTimeOut);
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return app;
    }
    public List<AppInfo> searchApps(String search){
        List<AppInfo> appList = new ArrayList<>();
        try (SQLiteDatabase db = this.getReadableDatabase()) {
            try (Cursor cursor = db.query(TABLE_APP_INFO, null, COLUMN_APP_NAME+" LIKE ?", new String[]{"%"+search+"%"}, null, null, COLUMN_APP_NAME+" ASC")) {
                if (cursor != null && cursor.moveToFirst()) {
                    do {
                        int id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID));
                        String packageName = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PACKAGE_NAME));
                        String appName = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_APP_NAME));
                        int dailyLimitMinutes = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_DAILY_LIMIT_MINUTES));
                        int minutesUsedToday = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_MINUTES_USED_TODAY));
                        int block = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_BLOCKED));
                        int timeOut = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_TIME_OUT));
                        AppInfo app = new AppInfo(id, packageName, appName, dailyLimitMinutes);
                        app.setMinutesUsedToday(minutesUsedToday);
                        app.setBlocked(block == 1);
                        app.setTimeOut(timeOut == 1);
                        appList.add(app);
                    } while (cursor.moveToNext());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return appList;
    }
    public List<AppInfo> getAllApps() {
        List<AppInfo> appList = new ArrayList<>();
        try (SQLiteDatabase db = this.getReadableDatabase()) {
            try (Cursor cursor = db.query(TABLE_APP_INFO, null, null, null, null, null, COLUMN_APP_NAME+" ASC")) {
                if (cursor != null && cursor.moveToFirst()) {
                    do {
                        int id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID));
                        String packageName = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PACKAGE_NAME));
                        String appName = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_APP_NAME));
                        int dailyLimitMinutes = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_DAILY_LIMIT_MINUTES));
                        int minutesUsedToday = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_MINUTES_USED_TODAY));
                        int block = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_BLOCKED));
                        int timeOut = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_TIME_OUT));
                        AppInfo app = new AppInfo(id, packageName, appName, dailyLimitMinutes);
                        app.setMinutesUsedToday(minutesUsedToday);
                        app.setBlocked(block == 1);
                        app.setTimeOut(timeOut == 1);
                        appList.add(app);
                    } while (cursor.moveToNext());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return appList;
    }
    public boolean addApp(AppInfo app){
        long result=0;
        try(SQLiteDatabase db = this.getWritableDatabase()) {
            ContentValues values = getValues(app);
            result = db.insert(TABLE_APP_INFO, null, values);
        }
        return result != -1;
    }
    public boolean editApp(AppInfo app){
        int result=0;
        try(SQLiteDatabase db = this.getWritableDatabase()) {
            ContentValues values = getValues(app);
            result = db.update(TABLE_APP_INFO, values, COLUMN_ID + "=?", new String[]{String.valueOf(app.getId())});
        }
        return result >0;
    }
    public boolean removeApp(AppInfo app){
        if (app != null && (app.isBlocked() || app.isTimeOut())) {
            return false;
        }

        int result=0;
        try(SQLiteDatabase db=this.getWritableDatabase()) {
            result = db.delete(TABLE_APP_INFO, COLUMN_ID + "=?", new String[]{String.valueOf(app.getId())});
        }
        return result>0;
    }
    public boolean resetAllTimeOuts() {
        ContentValues values = new ContentValues();
        values.put(COLUMN_TIME_OUT, false);
        values.put(COLUMN_MINUTES_USED_TODAY, 0);

        int result;
        try (SQLiteDatabase db = this.getWritableDatabase()) {
            result = db.update(TABLE_APP_INFO, values, null, null);
        }
        return result >= 0;
    }
    public ContentValues getValues(AppInfo app){
        ContentValues values = new ContentValues();
        values.put(COLUMN_PACKAGE_NAME,app.getPackageName());
        values.put(COLUMN_APP_NAME,app.getAppName());
        values.put(COLUMN_DAILY_LIMIT_MINUTES,app.getDailyLimitMinutes());
        values.put(COLUMN_MINUTES_USED_TODAY,app.getMinutesUsedToday());
        values.put(COLUMN_BLOCKED,app.isBlocked());
        values.put(COLUMN_TIME_OUT,app.isTimeOut());
        return values;
    }
}
