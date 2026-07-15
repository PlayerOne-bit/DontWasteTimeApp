package app.dontwastetimeapp.activities;

import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP;
import static android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;
import java.util.List;

import app.dontwastetimeapp.R;
import app.dontwastetimeapp.classes.AppInfo;
import app.dontwastetimeapp.database.AppPreferences;

public class AppActivity extends AppCompatActivity {
    private List<AppInfo> allSavedApps = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.app_layout);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.app_layout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        setUpNavigation();
        setUpSearch();
    }
    @Override
    public void onResume(){
        super.onResume();
        loadSavedApps();
    }
    private void loadSavedApps(){
        try(AppPreferences db = new AppPreferences(this)){
            allSavedApps = db.getAllApps();
            sortAppsAlphabetically(allSavedApps);
            displayAllApps(allSavedApps);
        }
    }
    private void sortAppsAlphabetically(List<AppInfo> apps){
        apps.sort((a,b)->a.getAppName().compareToIgnoreCase(b.getAppName()));
    }

    private void displayAllApps(List<AppInfo> apps){
        LinearLayout savedAppsContainer = findViewById(R.id.savedAppsContainer);
        savedAppsContainer.removeAllViews();
        for(AppInfo app: apps) {
            View row = getLayoutInflater().inflate(R.layout.app_card, savedAppsContainer, false);
            ImageView appIcon = row.findViewById(R.id.appIcon);
            TextView appName = row.findViewById(R.id.appName);
            TextView appDailyMinutesUsedToday = row.findViewById(R.id.appDailyMinutesUsedToday);
            ProgressBar appProgressBar = row.findViewById(R.id.appProgressBar);

            appName.setText(app.getAppName());
            updateTimeLimitLabel(appDailyMinutesUsedToday,appProgressBar, app);
            bindAppIcon(appIcon, app.getPackageName());

            row.setOnClickListener(v->openEditScreen(app));
            savedAppsContainer.addView(row);
        }
        toggleHint(apps.isEmpty());
    }
    private void bindAppIcon(ImageView icon, String packageName) {
        PackageManager pm = getPackageManager();
        try {
            Drawable appIcon = pm.getApplicationIcon(packageName);
            icon.setImageDrawable(appIcon);
        } catch (PackageManager.NameNotFoundException e) {
            icon.setImageResource(R.drawable.app_icon);
        }
    }
    private void toggleHint(boolean isEmpty) {
        TextView hintApps = findViewById(R.id.hintApps);
        hintApps.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
    }

    private void openEditScreen(AppInfo app) {
        Intent intent = new Intent(AppActivity.this, EditAppActivity.class);
        intent.putExtra("app_id", app.getId());
        startActivity(intent);
    }
    private void setUpSearch() {
        EditText searchInput = findViewById(R.id.searchInstalledApps);

        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s.toString().toLowerCase();
                List<AppInfo> filtered = new ArrayList<>();

                for (AppInfo app : allSavedApps) {
                    if (app.getAppName().toLowerCase().contains(query)) {
                        filtered.add(app);
                    }
                }

                displayAllApps(filtered);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }
    private void updateTimeLimitLabel(TextView label,ProgressBar bar,AppInfo app) {
        String text="";

        int[] colors={
                Color.parseColor("#D9181B"),
                Color.parseColor("#FF8200"),
                Color.parseColor("#B8FF2F")
        };
        if(app.isBlocked()) {
            text="BLOCKED";
            bar.setProgress(bar.getMax());
            bar.setProgressTintList(ColorStateList.valueOf(colors[0]));
        }else if (app.isTimeOut()){
            text="TIME OUT";
            bar.setProgress(bar.getMax());
            bar.setProgressTintList(ColorStateList.valueOf(colors[1]));
        }else{
            int totalMinutes = app.getMinutesUsedToday();
            int hours = totalMinutes / 60;
            String minutesUsedToday = ((hours>0)?(hours+"h "):"") + (totalMinutes % 60+"m");
            int maxTotalMinutes = app.getDailyLimitMinutes();
            int maxHours = maxTotalMinutes / 60;
            String dailyLimitMinutes =((maxHours>0)?(maxHours+"h "):"") + (maxTotalMinutes % 60 +"m");
            text = String.format("%s / %s",minutesUsedToday,dailyLimitMinutes);

            int percentUsed = (int) ((totalMinutes/ (float) maxTotalMinutes) * 100);
            bar.setProgress(Math.min(percentUsed, 100));
            bar.setProgressTintList(ColorStateList.valueOf(colors[2]));
        }
        label.setText(text);
    }
    private void setUpNavigation(){
        LinearLayout navHome = findViewById(R.id.navHome),
                    navFocus = findViewById(R.id.navFocus);
        CardView addAppButton = findViewById(R.id.addAppButton);
        navHome.setOnClickListener(v->toNavigate(HomeActivity.class));
        navFocus.setOnClickListener(v->toNavigate(FocusActivity.class));
        addAppButton.setOnClickListener(v->toAddAppActivity());
    }

    private void toAddAppActivity(){
        Intent intent = new Intent(AppActivity.this, AddAppActivity.class);
        startActivity(intent);
    }
    private void toNavigate(Class<?> destination){
        Intent intent = new Intent(AppActivity.this, destination);
        intent.addFlags(FLAG_ACTIVITY_CLEAR_TOP|FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
        overridePendingTransition(0,0);
    }
}
