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
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import app.dontwastetimeapp.R;
import app.dontwastetimeapp.classes.AppInfo;
import app.dontwastetimeapp.database.AppPreferences;

public class EditAppActivity extends AppCompatActivity {

    private AppInfo currentApp;
    private boolean blockedState;

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
        setUpBlockToggle();
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

        blockedState = currentApp.isBlocked();
        populateFields();
    }

    private void populateFields() {
        TextView editAppName = findViewById(R.id.editAppName);
        TextView editAppUsage = findViewById(R.id.editAppUsage);
        ImageView editAppIcon = findViewById(R.id.editAppIcon);
        SeekBar editTimeLimitSeekBar = findViewById(R.id.editTimeLimitSeekBar);
        TextView editTimeLimitValue = findViewById(R.id.editTimeLimitValue);

        editAppName.setText(currentApp.getAppName());
        editAppUsage.setText(currentApp.getMinutesUsedToday() + "m used today");
        editTimeLimitSeekBar.setProgress(currentApp.getDailyLimitMinutes());
        updateTimeLimitLabel(editTimeLimitValue, currentApp.getDailyLimitMinutes());
        updateBlockLabel();

        PackageManager pm = getPackageManager();
        try {
            Drawable icon = pm.getApplicationIcon(currentApp.getPackageName());
            editAppIcon.setImageDrawable(icon);
        } catch (PackageManager.NameNotFoundException e) {
            editAppIcon.setImageResource(R.drawable.app_icon);
        }
        updateDeleteButtonState();
    }
    private void updateDeleteButtonState() {
        CardView deleteAppButton = findViewById(R.id.deleteAppButton);
        boolean isRestricted = currentApp.isBlocked() || currentApp.isTimeOut();
        deleteAppButton.setVisibility(isRestricted ? View.GONE : View.VISIBLE);
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

    private void setUpBlockToggle() {
        CardView cardViewStatus = findViewById(R.id.cardViewStatus);
        cardViewStatus.setOnClickListener(v -> {
            blockedState = !blockedState;
            updateBlockLabel();
        });
    }

    private void updateBlockLabel() {
        TextView editStatusLabel = findViewById(R.id.editStatusLabel);
        editStatusLabel.setText(blockedState ? "Blocked" : "Block the App");
        updateDeleteButtonState();
    }

    private void saveButton(){
        SeekBar editTimeLimitSeekBar = findViewById(R.id.editTimeLimitSeekBar);
        int dailyLimitMinutes = Math.round(editTimeLimitSeekBar.getProgress() / 5f) * 5;

        currentApp.setDailyLimitMinutes(dailyLimitMinutes);
        currentApp.setBlocked(blockedState);

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