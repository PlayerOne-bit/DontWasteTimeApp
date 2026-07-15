package app.dontwastetimeapp.activities;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;
import java.util.List;

import app.dontwastetimeapp.R;
import app.dontwastetimeapp.classes.AppInfo;
import app.dontwastetimeapp.database.AppPreferences;

public class HomeActivity extends AppCompatActivity {
    private List<AppInfo> savedTop5AppList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.home_layout);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        setUpNavigation();
    }
    @Override
    public void onResume(){
        super.onResume();
        loadSavedApps();
    }
    private void loadSavedApps(){
        List<AppInfo> apps = new ArrayList<>();
        try(AppPreferences db = new AppPreferences(this)){
            apps = db.getAllApps();
            sortTop5(apps);
            displayAllApps(savedTop5AppList);
        }
    }
    private void sortTop5(List<AppInfo> apps){
        apps.sort((a,b)->Integer.compare(b.getMinutesUsedToday(),a.getMinutesUsedToday()));
        savedTop5AppList=apps.subList(0,Math.min(3,apps.size()));
    }
    private void displayAllApps(List<AppInfo> apps){
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
        String text="";
        int[] colors={
                Color.parseColor("#D9181B"),
                Color.parseColor("#FF8200"),
                Color.parseColor("#B8FF2F")

        };
        if(app.isBlocked()){
            text="BLOCKED";
            bar.setProgress(bar.getMax());
            bar.setProgressTintList(ColorStateList.valueOf(colors[0]));
        }else if (app.isTimeOut()){
            text="TIME OUT";
            bar.setProgress(bar.getMax());
            bar.setProgressTintList(ColorStateList.valueOf(colors[1]));
        }else{
            int totalMinutes = app.getMinutesUsedToday(),
                maxTotalMinutes = app.getDailyLimitMinutes();
            int hours = totalMinutes / 60;
            int maxHours = maxTotalMinutes / 60;
            String minutes = ((hours>0)?hours+"h ":"")+(totalMinutes % 60)+"m";
            String maxMinutes = ((maxHours>60)?maxHours+"h ":"")+(totalMinutes % 60)+"m";
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
        navApps.setOnClickListener(v-> toNavigate(AppActivity.class));
        navFocus.setOnClickListener(v-> toNavigate(FocusActivity.class));
    }
    private void toNavigate(Class<?> destination){
        Intent intent = new Intent(HomeActivity.this,destination);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
        overridePendingTransition(0,0);
    }
}