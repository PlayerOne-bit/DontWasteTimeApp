package app.dontwastetimeapp.activities;

import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
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

import app.dontwastetimeapp.R;
import app.dontwastetimeapp.classes.AppInfo;
import app.dontwastetimeapp.database.AppPreferences;

public class EditAppActivity extends AppCompatActivity {

    private AppInfo currentApp;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.edit_layout);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.edit_layout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        loadApp();
        setUpButtons();
        setUpTimeLimitSeekBar();
        setUpBlockSwitch();
    }

    private void loadApp() {
        int appId = getIntent().getIntExtra("app_id", -1);

        if (appId == -1) {
            Toast.makeText(this, "No app selected", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        try (AppPreferences db = new AppPreferences(this)) {
            currentApp = db.getApp(appId);
        }

        if (currentApp == null) {
            Toast.makeText(this, "App not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        populateFields();
    }

    private void populateFields() {
        TextView editAppName = findViewById(R.id.editAppName);
        TextView editAppUsage = findViewById(R.id.editAppUsage);
        ImageView editAppIcon = findViewById(R.id.editAppIcon);
        SeekBar editTimeLimitSeekBar = findViewById(R.id.editTimeLimitSeekBar);
        TextView editTimeLimitValue = findViewById(R.id.editTimeLimitValue);
        SwitchCompat blockOverrideSwitch = findViewById(R.id.blockSwitch);

        editAppName.setText(currentApp.getAppName());

        int totalMinutes = currentApp.getMinutesUsedToday();
        int hours = totalMinutes / 60, minutes = totalMinutes % 60;
        String text = (hours > 0) ? hours + "h " + ((minutes > 0) ? minutes + "m used today" : "used today")
                : minutes + "m used today";
        editAppUsage.setText(text);

        editTimeLimitSeekBar.setProgress(currentApp.getDailyLimitMinutes());
        updateTimeLimitLabel(editTimeLimitValue, currentApp.getDailyLimitMinutes());

        blockOverrideSwitch.setChecked(currentApp.isBlocked());

        PackageManager pm = getPackageManager();
        try {
            Drawable icon = pm.getApplicationIcon(currentApp.getPackageName());
            editAppIcon.setImageDrawable(icon);
        } catch (PackageManager.NameNotFoundException e) {
            editAppIcon.setImageResource(R.drawable.app_icon);
        }

        applyRestrictionLock();
    }

    private void applyRestrictionLock() {
        boolean isRestricted = currentApp.isTimeOut();

        SeekBar editTimeLimitSeekBar = findViewById(R.id.editTimeLimitSeekBar);
        CardView blockLayout = findViewById(R.id.cardViewStatus);
        CardView saveEditButton = findViewById(R.id.saveEditButton);
        CardView deleteAppButton = findViewById(R.id.deleteAppButton);

        editTimeLimitSeekBar.setVisibility(isRestricted ? View.GONE : View.VISIBLE);
        blockLayout.setVisibility(isRestricted ? View.GONE : View.VISIBLE);

        saveEditButton.setVisibility(isRestricted ? View.GONE : View.VISIBLE);
        deleteAppButton.setVisibility(isRestricted ? View.GONE : View.VISIBLE);

        if (isRestricted) {
            Toast.makeText(this,
                    currentApp.isTimeOut() ? "This app is timed out for today - no changes allowed" : "This app is blocked - no changes allowed",
                    Toast.LENGTH_LONG).show();
        }
    }

    private void setUpButtons(){
        CardView saveEditButton = findViewById(R.id.saveEditButton);
        ImageView editBackButton = findViewById(R.id.editBackButton);
        CardView deleteAppButton = findViewById(R.id.deleteAppButton);

        saveEditButton.setOnClickListener(v -> saveButton());
        editBackButton.setOnClickListener(v -> finish());
        deleteAppButton.setOnClickListener(v -> deleteButton());
    }

    private void setUpTimeLimitSeekBar() {
        SeekBar editTimeLimitSeekBar = findViewById(R.id.editTimeLimitSeekBar);
        TextView editTimeLimitValue = findViewById(R.id.editTimeLimitValue);

        editTimeLimitSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int snapped = Math.round(progress / 5f) * 5;
                updateTimeLimitLabel(editTimeLimitValue, snapped);
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

    private void setUpBlockSwitch() {
        SwitchCompat blockOverrideSwitch = findViewById(R.id.blockSwitch);
        blockOverrideSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            TextView editStatusLabel = findViewById(R.id.editStatusLabel);
            editStatusLabel.setText(isChecked ? "Blocked" : "Block the App");
        });
    }

    private void saveButton(){
        SeekBar editTimeLimitSeekBar = findViewById(R.id.editTimeLimitSeekBar);
        SwitchCompat blockOverrideSwitch = findViewById(R.id.blockSwitch);

        int dailyLimitMinutes = Math.round(editTimeLimitSeekBar.getProgress() / 5f) * 5;

        currentApp.setDailyLimitMinutes(dailyLimitMinutes);
        currentApp.setBlocked(blockOverrideSwitch.isChecked());

        try (AppPreferences db = new AppPreferences(this)) {
            boolean success = db.editApp(currentApp);
            if (success) {
                finish();
            } else {
                Toast.makeText(this, "Failed to save changes", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void deleteButton(){
        try (AppPreferences db = new AppPreferences(this)) {
            boolean success = db.removeApp(currentApp);
            if (success) {
                finish();
            } else {
                Toast.makeText(this, "Failed to remove app", Toast.LENGTH_SHORT).show();
            }
        }
    }
}