package app.dontwastetimeapp.activities;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;
import java.util.List;

import app.dontwastetimeapp.R;
import app.dontwastetimeapp.classes.AppInfo;
import app.dontwastetimeapp.database.AppPreferences;

public class AddAppActivity extends AppCompatActivity {
    private List<ApplicationInfo> allInstalledApps = new ArrayList<>();
    private ApplicationInfo selectedApp;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.add_layout);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.add_layout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        setUpSearch();
        setUpNavigation();
        setUpAddAppButton();
        setUpTimeLimitSeekBar();
    }

    private void setUpNavigation(){
        ImageView backButton = findViewById(R.id.backButton);
        CardView saveAppButton = findViewById(R.id.saveAppButton);
        backButton.setOnClickListener(v -> finish());
        saveAppButton.setOnClickListener(v -> saveAppButton());
    }

    private void setUpAddAppButton(){
        LinearLayout selectedAppRow = findViewById(R.id.selectedAppRow);
        selectedAppRow.setOnClickListener(v -> {
            findViewById(R.id.searchInstalledApps).setVisibility(View.VISIBLE);
            findViewById(R.id.appPickerScroll).setVisibility(View.VISIBLE);
        });

        loadInstalledApps();
    }

    private void loadInstalledApps() {
        PackageManager pm = getPackageManager();
        List<ApplicationInfo> rawList = pm.getInstalledApplications(0);
        String myPackageName = getPackageName();

        allInstalledApps = new ArrayList<>();
        for (ApplicationInfo appInfo : rawList) {
            boolean isSystemApp = (appInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0;
            boolean isSelf = appInfo.packageName.equals(myPackageName);
            if (!isSystemApp && !isSelf) {
                allInstalledApps.add(appInfo);
            }
        }

        sortAppsAlphabetically(allInstalledApps);
        displayApps(allInstalledApps);
    }
    private void sortAppsAlphabetically(List<ApplicationInfo> apps) {
        PackageManager pm = getPackageManager();
        apps.sort((a, b) -> {
            String nameA = pm.getApplicationLabel(a).toString();
            String nameB = pm.getApplicationLabel(b).toString();
            return nameA.compareToIgnoreCase(nameB);
        });
    }

    private void displayApps(List<ApplicationInfo> apps) {
        LinearLayout container = findViewById(R.id.installedAppsContainer);
        PackageManager pm = getPackageManager();

        container.removeAllViews(); // clear whatever was there before re-drawing

        for (ApplicationInfo appInfo : apps) {
            View row = getLayoutInflater().inflate(R.layout.installed_app_card, container, false);

            ImageView icon = row.findViewById(R.id.installedAppIcon);
            TextView name = row.findViewById(R.id.installedAppName);

            icon.setImageDrawable(pm.getApplicationIcon(appInfo));
            name.setText(pm.getApplicationLabel(appInfo));

            row.setOnClickListener(v -> selectApp(appInfo));

            container.addView(row);
        }
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
                List<ApplicationInfo> filtered = new ArrayList<>();
                PackageManager pm = getPackageManager();

                for (ApplicationInfo appInfo : allInstalledApps) {
                    String label = pm.getApplicationLabel(appInfo).toString().toLowerCase();
                    if (label.contains(query)) {
                        filtered.add(appInfo);
                    }
                }

                displayApps(filtered);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }
    private void selectApp(ApplicationInfo appInfo) {
        selectedApp = appInfo;

        PackageManager pm = getPackageManager();
        ImageView selectedIcon = findViewById(R.id.selectedAppIcon);
        TextView selectedName = findViewById(R.id.selectedAppName);

        selectedIcon.setImageDrawable(pm.getApplicationIcon(appInfo));
        selectedName.setText(pm.getApplicationLabel(appInfo));

        findViewById(R.id.searchInstalledApps).setVisibility(View.GONE);
        findViewById(R.id.appPickerScroll).setVisibility(View.GONE);
    }

    private void setUpTimeLimitSeekBar() {
        SeekBar timeLimitSeekBar = findViewById(R.id.timeLimitSeekBar);
        TextView timeLimitValue = findViewById(R.id.timeLimitValue);

        updateTimeLimitLabel(timeLimitValue, timeLimitSeekBar.getProgress());

        timeLimitSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int snapped = Math.round(progress / 5f) * 5;
                updateTimeLimitLabel(timeLimitValue, snapped);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int snapped = Math.round(seekBar.getProgress() / 5f) * 5;
                seekBar.setProgress(snapped);
            }
        });
    }

    private void updateTimeLimitLabel(TextView label, int totalMinutes) {
        int hours = totalMinutes / 60;
        int minutes = totalMinutes % 60;

        if (hours == 0) {
            label.setText(String.format("%dm", minutes));
        } else {
            label.setText(String.format("%dh %02dm", hours, minutes));
        }
    }

    private void saveAppButton(){
        if (selectedApp == null) {
            Toast.makeText(this, "Please select an app first", Toast.LENGTH_SHORT).show();
            return;
        }

        PackageManager pm = getPackageManager();
        String packageName = selectedApp.packageName;
        String appName = pm.getApplicationLabel(selectedApp).toString();

        SeekBar timeLimitSeekBar = findViewById(R.id.timeLimitSeekBar);
        int dailyLimitMinutes = Math.round(timeLimitSeekBar.getProgress() / 5f) * 5;


        AppInfo newApp = new AppInfo(packageName, appName, dailyLimitMinutes);

        AppPreferences db = new AppPreferences(this);
        boolean success = db.addApp(newApp);

        if (success) {
            finish();
        } else {
            Toast.makeText(this, "Failed to save app", Toast.LENGTH_SHORT).show();
        }
    }
}