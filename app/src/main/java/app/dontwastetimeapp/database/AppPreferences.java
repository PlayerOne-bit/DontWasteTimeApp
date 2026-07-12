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
    public AppPreferences(@Nullable Context context) {
        super(context, NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE "+TABLE_APP_INFO+"("+
                COLUMN_ID+" INT PRIMARY KEY AUTOINCREMENT UNIQUE,"+
                COLUMN_PACKAGE_NAME+" TEXT," +
                COLUMN_APP_NAME+" TEXT,"+
                COLUMN_DAILY_LIMIT_MINUTES+" INT,"+
                COLUMN_MINUTES_USED_TODAY+" INT,"+
                COLUMN_BLOCKED+" BOOL"+
                ");");
    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS "+TABLE_APP_INFO);
        onCreate(db);
    }
    public AppInfo getApp(int id){
        AppInfo app=null;
        try(
            SQLiteDatabase db = this.getReadableDatabase()){
            try(Cursor cursor = db.query(TABLE_APP_INFO,null,"id=?",new String[]{String.valueOf(id)},null,null,null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    String packageName=cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PACKAGE_NAME));
                    String appName=cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_APP_NAME));
                    int dailyLimitMinutes = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_DAILY_LIMIT_MINUTES));
                    int minutesUsedToday = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_MINUTES_USED_TODAY));
                    int block = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_BLOCKED));
                    boolean isBlocked = (block==1);
                    app=new AppInfo(id,packageName,appName, dailyLimitMinutes);
                    app.setMinutesUsedToday(minutesUsedToday);
                    app.setBlocked(isBlocked);
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return app;
    }
    public List<AppInfo> getAllApps() {
        List<AppInfo> appList = new ArrayList<>();
        try (SQLiteDatabase db = this.getReadableDatabase()) {
            try (Cursor cursor = db.query(TABLE_APP_INFO, null, null, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    do {
                        int id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
                        String packageName = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PACKAGE_NAME));
                        String appName = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_APP_NAME));
                        int dailyLimitMinutes = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_DAILY_LIMIT_MINUTES));
                        int minutesUsedToday = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_MINUTES_USED_TODAY));
                        int block = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_BLOCKED));

                        AppInfo app = new AppInfo(id, packageName, appName, dailyLimitMinutes);
                        app.setMinutesUsedToday(minutesUsedToday);
                        app.setBlocked(block == 1);

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

    public boolean removeApp(int id){
        int result=0;
        try(SQLiteDatabase db=this.getWritableDatabase()) {
            result = db.delete(TABLE_APP_INFO, "id=?", new String[]{String.valueOf(id)});
        }
        return result>0;
    }
    public ContentValues getValues(AppInfo app){
        ContentValues values = new ContentValues();
        values.put(COLUMN_PACKAGE_NAME,app.getPackageName());
        values.put(COLUMN_APP_NAME,app.getAppName());
        values.put(COLUMN_DAILY_LIMIT_MINUTES,app.getDailyLimitMinutes());
        values.put(COLUMN_MINUTES_USED_TODAY,app.getMinutesUsedToday());
        values.put(COLUMN_BLOCKED,app.isBlocked());
        return values;
    }
}
