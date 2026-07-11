package app.dontwastetimeapp.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

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
    public void addApp(AppInfo app){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_PACKAGE_NAME,app.getPackageName());
        values.put(COLUMN_APP_NAME,app.getAppName());
        values.put(COLUMN_DAILY_LIMIT_MINUTES,app.getDailyLimitMinutes());
        values.put(COLUMN_MINUTES_USED_TODAY,app.getMinutesUsedToday());
        values.put(COLUMN_BLOCKED,app.isBlocked());
    }

}
