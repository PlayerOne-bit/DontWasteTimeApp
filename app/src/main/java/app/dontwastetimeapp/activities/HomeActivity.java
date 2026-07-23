package app.dontwastetimeapp.activities;

import android.app.AlarmManager;
import android.app.AppOpsManager;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.splashscreen.SplashScreen;
import androidx.core.splashscreen.SplashScreenViewProvider;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.List;
import app.dontwastetimeapp.R;
import app.dontwastetimeapp.classes.AppInfo;
import app.dontwastetimeapp.database.AppPreferences;
import app.dontwastetimeapp.services.DailyResetReceiver;
import app.dontwastetimeapp.services.UsageMonitorService;
import app.dontwastetimeapp.services.AppBlockAccessibilityService;
public class HomeActivity extends AppCompatActivity {
    private Handler refreshHandler;
    private Runnable refreshRunnable;
    private List<AppInfo> savedTop5AppList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SplashScreen splashScreen = SplashScreen.installSplashScreen(this);
        splashScreen.setOnExitAnimationListener(SplashScreenViewProvider::remove);
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.home_layout);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        if (!hasUsageAccessPermission()) {
            requestUsageAccessPermission();
        }
        startUsageMonitorService();
        requestAccessibilityPermission();
        setUpNavigation();
        scheduleDailyReset();
    }
    @Override
    protected void onResume() {
        super.onResume();
        loadSavedApps();
        startPeriodicRefresh();
    }
    @Override
    protected void onPause() {
        super.onPause();
        stopPeriodicRefresh();
    }
    private void startPeriodicRefresh() {
        refreshHandler = new Handler(Looper.getMainLooper());
        refreshRunnable = new Runnable() {
            @Override
            public void run() {
                loadSavedApps();
                refreshHandler.postDelayed(this, 5000);
            }
        };
        refreshHandler.post(refreshRunnable);
    }
    private void stopPeriodicRefresh() {
        if (refreshHandler != null && refreshRunnable != null) {
            refreshHandler.removeCallbacks(refreshRunnable);
        }
    }
    private void loadSavedApps(){
        List<AppInfo> apps;
        try(AppPreferences db = new AppPreferences(this)){
            apps = db.getAllApps();
            sortTop5(apps);
            displayRecordTexts(apps);
            displayTop5Apps(savedTop5AppList);
        }
    }
    private void displayRecordTexts(List<AppInfo> apps){
        LocalDate currentDate = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            currentDate = LocalDate.now();
        }
        DateTimeFormatter formatter = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            formatter = DateTimeFormatter.ofPattern("E - dd/MM/yyyy");
        }
        TextView dateText = findViewById(R.id.dateText);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            dateText.setText(currentDate.format(formatter));
        }
        TextView totalMinutesUsedText = findViewById(R.id.totalUsedMinutes);
        TextView activeText = findViewById(R.id.activeText);
        TextView blockText = findViewById(R.id.blockText);
        TextView timeOutText = findViewById(R.id.timeOutText);
        int totalActive=0,totalBlock=0,totalTimeOut=0, totalUsedMinutes=0;
        for (AppInfo app: apps){
            if(app.isBlocked()){
                totalBlock++;
            }else if(app.isTimeOut()){
                totalTimeOut++;
            }else{
                totalActive++;
            }
            totalUsedMinutes+= app.getMinutesUsedToday();
        }
        int hours = totalUsedMinutes/60, minutes = totalUsedMinutes %60;
        String totalTime = ((hours>0)?hours+"h "+((minutes>0)?minutes+"m":""):minutes+"m");
        totalMinutesUsedText.setText(totalTime);
        activeText.setText(String.valueOf(totalActive));
        blockText.setText(String.valueOf(totalBlock));
        timeOutText.setText(String.valueOf(totalTimeOut));
    }
    private void sortTop5(List<AppInfo> apps){
        apps.sort((a,b)->Integer.compare(b.getMinutesUsedToday(),a.getMinutesUsedToday()));
        savedTop5AppList=apps.subList(0,Math.min(5,apps.size()));
    }
    private void displayTop5Apps(List<AppInfo> apps){
        LinearLayout topSavedAppsContainer = findViewById(R.id.topSavedAppsContainer);
        topSavedAppsContainer.removeAllViews();
        for(AppInfo app : apps){
            View row =  getLayoutInflater().inflate(R.layout.app_card,topSavedAppsContainer,false);
            ImageView appIcon = row.findViewById(R.id.appIcon);
            TextView appName = row.findViewById(R.id.appName);
            TextView appDailyMinutesUsedToday = row.findViewById(R.id.appDailyMinutesUsedToday);
            ProgressBar appProgressBar = row.findViewById(R.id.appProgressBar);

            appName.setText(app.getAppName());
            bindAppIcon(appIcon, app.getPackageName());
            updateTimeLimitLabel(appDailyMinutesUsedToday, appProgressBar, app);

            topSavedAppsContainer.addView(row);
        }
        toggleHintTopApps(apps.isEmpty());
    }
    private void updateTimeLimitLabel(TextView label, ProgressBar bar, AppInfo app){
        String text;
        int[] colors={
                Color.parseColor("#D9181B"),
                Color.parseColor("#FF8200"),
                Color.parseColor("#B8FF2F")
        };
        if(app.isBlocked()){
            text="BLOCKED";
            bar.setProgress(bar.getMax());
            bar.setProgressTintList(ColorStateList.valueOf(colors[0]));
        }else if (app.isTimeOut() || app.isOverLimit()){
            text="TIME OUT";
            bar.setProgress(bar.getMax());
            bar.setProgressTintList(ColorStateList.valueOf(colors[1]));
        }else{
            int totalMinutes = app.getMinutesUsedToday(),
                maxTotalMinutes = app.getDailyLimitMinutes();
            int hours = totalMinutes / 60;
            int maxHours = maxTotalMinutes / 60;
            String minutes = ((hours>0)?hours+"h ":"")+(totalMinutes % 60)+"m";
            String maxMinutes = ((maxHours>0)?maxHours+"h ":"")+(maxTotalMinutes % 60)+"m";
            text = String.format("%s / %s",minutes,maxMinutes);

            int percentUsed = (int) ((totalMinutes / (float) maxTotalMinutes) * 100);
            bar.setProgress(Math.min(percentUsed,100));
            bar.setProgressTintList(ColorStateList.valueOf(colors[2]));
        }
        label.setText(text);
    }
    private void toggleHintTopApps(boolean isEmpty){
        TextView hintTopApps = findViewById(R.id.hintTopApps);
        hintTopApps.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
    }
    private void bindAppIcon(ImageView icon, String packageName){
        PackageManager pm = getPackageManager();
        try{
            Drawable appIcon = pm.getApplicationIcon(packageName);
            icon.setImageDrawable(appIcon);
        }catch(PackageManager.NameNotFoundException e){
            icon.setImageResource(R.drawable.app_icon);
        }
    }
    private void setUpNavigation(){
        LinearLayout navApps = findViewById(R.id.navApps),
                    navFocus = findViewById(R.id.navFocus);
        ImageView info_button = findViewById(R.id.info_button);
        info_button.setOnClickListener(v->openInfoCard());
        navApps.setOnClickListener(v-> toNavigate(AppActivity.class));
        navFocus.setOnClickListener(v-> toNavigate(FocusActivity.class));
    }
    private void openInfoCard(){
        final Dialog dialog = new Dialog(HomeActivity.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.info_card);
        if (dialog.getWindow() != null)
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        TextView closeButton = dialog.findViewById(R.id.info_close_button);
        closeButton.setOnClickListener(v-> dialog.dismiss());
        dialog.show();
    }
    private void toNavigate(Class<?> destination){
        Intent intent = new Intent(HomeActivity.this,destination);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
        overridePendingTransition(0,0);
    }
    private void startUsageMonitorService() {
        Intent serviceIntent = new Intent(this, UsageMonitorService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent);
        } else {
            startService(serviceIntent);
        }
    }
    private void requestAccessibilityPermission() {
        if (!isAccessibilityServiceEnabled()) {
            Toast.makeText(this, "Please enable Odysseus Focus in Accessibility settings", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
            startActivity(intent);
        }
    }
    private void requestUsageAccessPermission() {
        Toast.makeText(this, "Please enable Usage Access for this app", Toast.LENGTH_LONG).show();
        Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
        startActivity(intent);
    }
    private boolean hasUsageAccessPermission() {
        AppOpsManager appOps = (AppOpsManager) getSystemService(Context.APP_OPS_SERVICE);
        int mode = appOps.checkOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                android.os.Process.myUid(),
                getPackageName()
        );
        return mode == AppOpsManager.MODE_ALLOWED;
    }
    private boolean isAccessibilityServiceEnabled() {
        String serviceName = getPackageName() + "/" + AppBlockAccessibilityService.class.getCanonicalName();

        int accessibilityEnabled;
        try {
            accessibilityEnabled = Settings.Secure.getInt(
                    getContentResolver(),
                    Settings.Secure.ACCESSIBILITY_ENABLED
            );
        } catch (Settings.SettingNotFoundException e) {
            return false;
        }

        if (accessibilityEnabled != 1) {
            return false;
        }

        String enabledServices = Settings.Secure.getString(
                getContentResolver(),
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        );

        if (enabledServices == null) {
            return false;
        }

        for (String service : enabledServices.split(":")) {
            if (service.equalsIgnoreCase(serviceName)) {
                return true;
            }
        }
        return false;
    }
    private void scheduleDailyReset() {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, DailyResetReceiver.class);

        PendingIntent existing = PendingIntent.getBroadcast(
                this, 0, intent,
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_NO_CREATE);
        if (existing != null) {
            return;
        }

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this, 0, intent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 5);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        if (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 1);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
            return;
        }
        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
    }
}